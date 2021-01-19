package com.eric0210.nomorecheats.checks.movement;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.Violation;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.Protections;
import com.eric0210.nomorecheats.api.util.TimeUtils;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.Lag;
import com.eric0210.nomorecheats.api.util.MathUtils;
import com.eric0210.nomorecheats.api.util.MovementUtils;
import com.eric0210.nomorecheats.api.util.PlayerUtils;
import com.eric0210.nomorecheats.api.util.Reverter;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.GroundChecks;
import com.eric0210.nomorecheats.api.util.YMap;
import com.eric0210.nomorecheats.api.util.Protections.ProtectionType;

// Insane Scale Flight-likely movement check code //
public class Fly extends Check implements EventListener
{
	private static final double GRAVITY_MAX_DIFFERENCE = .74;
	private HashMap<UUID, Location> lastPlayerLocations = new HashMap<>();
	private HashMap<UUID, Location> lastGroundPos = new HashMap<>();

	public Fly()
	{
		super("Fly");
		EventManager.onPlayerMove.add(new EventInfo(this, 0));
		EventManager.onPlayerTeleport.add(new EventInfo(this, 1));
		EventManager.onPlayerChangeWorld.add(new EventInfo(this, 2));
		EventManager.onPlayerJoin.add(new EventInfo(this, 3));
		EventManager.onBlockPlace.add(new EventInfo(this, 4));
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				PlayerMoveEvent e = (PlayerMoveEvent) ev;
				Player p = e.getPlayer();
				UUID uid = p.getUniqueId();
				Location from = e.getFrom();
				Location to = e.getTo();
				if (PlayerUtils.wasFlightAllowed(p) || PlayerUtils.wasFlying(p) || e.isCancelled() || p.isInsideVehicle() || p.isDead() || BlockUtils.isMaterialSurround(p.getLocation(), 0.3, false, Material.WEB) || BlockUtils.isPartiallyStuck(p) || BlockUtils.isFullyStuck(p) || MovementUtils.teleported(p))
					return;

				ArrayList<String> violations = new ArrayList<>();
				int violation_level = 0;

				double yDiff = calculateYDelta(p, from);
				double lastYDiff = Cache.get(uid, "lastYDiff", 0D);
				double yDiffDelta = lastYDiff - yDiff;
				double yDiffDelta_floor = MathUtils.round(yDiffDelta, 5, RoundingMode.FLOOR);
				double yDiffAbs = Math.abs(yDiff);
				double yDiffDeltaAbs = Math.abs(yDiffDelta);

				double y_vel = p.getVelocity().getY();

				int ticksUp = 0;
				int ticksDown = 0;
				float fallDistance = Cache.get(uid, "fallDistance", 0F);

				int oldTicksUp = Cache.get(uid, "oldTicksUp", 0);
				int airticks = Counter.getCount(uid, "airTicks");

				boolean jumping = Math.abs(YMap.get(p).getY(0) - yDiff) <= .02;
				boolean jumping_velocity = Math.abs(YMap.get(p).getY(0) - y_vel) <= .02;
				boolean blockabove = PlayerUtils.hadBlockAbove(p, 10);
				boolean buggy = BlockUtils.hasBuggiesNearby(from) || BlockUtils.hasBuggiesNearby(to);
				boolean lily = BlockUtils.isMaterialSurround(to, .3, false, Material.WATER_LILY, Material.CARPET, Material.SOUL_SAND);
				boolean liquid = (BlockUtils.isLiquidNearby(from) || BlockUtils.isLiquidNearby(from.clone().add(0, 1, 0)) || BlockUtils.isOnLiquid(from.clone().add(0, -1, 0))) && !lily;
				boolean liquid_protection = Protections.hasProtection(p, ProtectionType.LIQUID);
				
				if (GroundChecks.isOnGround(p.getLocation()))
					this.lastGroundPos.put(uid, p.getLocation());
				Location lastGround = this.lastGroundPos.get(uid);

				boolean hadGroundBuggy = BlockUtils.hasBuggiesNearby(lastGround) || BlockUtils.hasSteppableNearby(lastGround);

				double lastGroundYDistance = MathUtils.getVerticalDistance(lastGround, p.getLocation());

				if (GroundChecks.isOnGround(to) || BlockUtils.isClimbableNearby(p.getLocation()))
					TimeUtils.putCurrentTime(uid, "lastOnGround");

				if (Cache.contains(uid, "lastYDiff") && yDiff > 0)
				{
					Counter.remove(uid, "ticksDown");
					Cache.set(uid, this.name + ".wasGoingUp", true);
					oldTicksUp = Cache.get(uid, "oldTicksUp", 0);
					Cache.set(uid, "oldTicksUp", (ticksUp = Counter.increment1AndGetCount(uid, "ticksUp", -1)));

					int jumpModifier = PlayerUtils.getPotionEffectLevel(p, PotionEffectType.JUMP);
					if (jumpModifier > Cache.get(uid, "maxJumpEffectAmp", 0))
					{
						Cache.set(uid, "maxJumpEffectAmp", jumpModifier);
					}
					jumpModifier = Cache.get(uid, "maxJumpEffectAmp", 0);

					YMap ymap = YMap.get(jumpModifier);

					// TODO: Checks while jumping
					if (ymap != null && !liquid_protection && !BlockUtils.isGlideableNearby(p.getLocation()) && !Protections.hasProtection(p, ProtectionType.UNNORMAL_Y))
					{
						// HighJump //
						if (!ymap.containsTicksUp(ticksUp) && !BlockUtils.hadSteppableNearby(p) && !Protections.hasProtection(p, ProtectionType.ASCENSION))
						{
							suspect(p, (ticksUp * 1.5), "t: highjump", "y: " + yDiff, "up: " + ticksUp);
							Reverter.getInstance(this).teleport(p);
						}

						// Repeated-Value //
						if (!blockabove)
						{
							if (yDiff >= .05 && lastYDiff >= .05 && yDiffDelta < .08)
							{
								suspect(p, 50, "t: repeated-value", "y: " + yDiff, "y_c: " + lastYDiff);
								Reverter.getInstance(this).teleport(p);
							}
							else if (yDiff >= .08 && !TimeUtils.elapsed(uid, this.name + ".ytime." + yDiffDelta_floor, true, 200L) && !ymap.containsY(yDiff) && !(BlockUtils.hadSteppableNearby(p) && yDiff == .5))
							{
								suspect(p, 10, "t: repeated-value", "y: " + yDiffDelta_floor, "t: " + TimeUtils.getTimeDiff(uid, this.name + ".ytime." + yDiffDelta_floor, -1L));
								Reverter.getInstance(this).teleport(p);
							}
							TimeUtils.putCurrentTime(uid, this.name + ".ytime." + yDiffDelta_floor);
						}

						if ((yDiff > .341 || (yDiff > .012 && p.getFallDistance() > .01)) && liquid)
						{
							// WaterY //
							suspect(p, 5, "t: water", "y: " + yDiff);
							Reverter.getInstance(this).teleport(p);
						}
						else if (lastGroundYDistance >= 0.0015 && lastGroundYDistance < 0.166 && ticksUp > 2 && ticksUp != 5)
						{
							// LowJump //
							suspect(p, 10, "t: lowjump", "d: " + lastGroundYDistance, "t: " + ticksUp);
							Reverter.getInstance(this).teleport(p);
						}
						else if (y_vel > 0.1625 && !jumping_velocity && p.getFallDistance() == 0 && ticksUp < 2 && !blockabove && !GroundChecks.isOnGround(p.getLocation()))
						{
							suspect(p, 5, "t: unusual", "r: velocity", "t: " + ticksUp, "y_v: " + y_vel);
							Reverter.getInstance(this).teleport(p);
						}

						double ySpeed = MathUtils.getHorizontalDistance(from, to);
						double maxDefaultYSpeed = .601D;
						double maxYSpeed = maxDefaultYSpeed;
						boolean nearbyLiquids = BlockUtils.isLiquidNearby(p.getLocation());
						boolean normal_y = ymap.containsY(yDiff);
						boolean hasJumpPE = p.hasPotionEffect(PotionEffectType.JUMP);
						boolean hasLowjumpPE = MovementUtils.hasLowJumpEffect(p);
						boolean nearbyClimbables = BlockUtils.isClimbableNearby(p.getLocation());
						boolean nearbyPistons = BlockUtils.isPistonNearby(p.getLocation());
						boolean nearbySteppables = BlockUtils.hadSteppableNearby(p);
						if (!Cooldowns.isCooldownEnded(uid, "protection.explosion"))
							maxYSpeed = 5.0D;
						else if (!Cooldowns.isCooldownEnded(uid, "protection.fishinghook.caught"))
							maxYSpeed = 3.1D;
						else if (nearbyLiquids)
							maxYSpeed = 2.0D;
						else if (nearbyPistons)
							maxYSpeed = 1.6D;
						else if (Protections.hasProtection(p, ProtectionType.DAMAGE))
							maxYSpeed = 1.3D;
						else if (nearbySteppables)
							maxYSpeed = 1.1D;

						double jumpYModifier;

						if (hasJumpPE)
						{
							jumpYModifier = MovementUtils.getJumpPE_YModifier(p, .41999);
							maxYSpeed = jumpYModifier > maxYSpeed ? jumpYModifier : maxYSpeed;
						}
						if (yDiff >= maxYSpeed)
						{
							// Step(high) //
							suspect(p, (yDiff * 10), "t: clip", "y: " + yDiff, "y_l: " + maxYSpeed);
							Reverter.getInstance(this).teleport(p);
						}
						else if (yDiff == .25D && maxDefaultYSpeed == maxYSpeed && !nearbyLiquids && !nearbyClimbables && Cooldowns.isCooldownEnded(uid, "protection.enderpearl") && !BlockUtils.isMaterialSurround(to, .3, false, Material.SNOW))
						{
							// Step(low)
							suspect(p, 10, "t: lowstep", "y: " + yDiff);
							Reverter.getInstance(this).teleport(p);
						}

						if (!nearbyPistons && !nearbyLiquids && !nearbyClimbables)
						{
							double bdelta = from.getY() - from.getBlockY();
							double ground_sensitivity;
							boolean stepping;
							if (yDiff > 0 && (ySpeed == 0 || ySpeed > .01) && !hasLowjumpPE && GroundChecks.isOnGroundCache(p, 0, 0, 0))
							{
								int step_advanced_violation_type = 0;
								double lastNormalY = 0;
								ground_sensitivity = (!PlayerUtils.isInClosedChamber(p.getLocation()) ? .02 : .1);
								stepping = (bdelta == .5 || ySpeed == .5D) ? true : false;
								boolean b12 = !stepping || step_checkbuggyblock_3d(p, to, .3, 1.0, .3) || step_checkbuggyblock_3d(p, to, .3, 2.0, .3) ? true : false;
								boolean norm_cached_y_abnorm_current_y = ymap.containsY(yDiff) && !ymap.containsY(Math.abs(to.getY() - from.getY()));
								Location step_lowerest_pos = step_get_lowerest_position(to, from, .7);
								if (normal_y && Cache.contains(uid, this.name + ".step.lastNormalY"))
								{
									lastNormalY = Cache.get(uid, this.name + ".step.lastNormalY", 0.0D);
									if (ymap.containsY(lastNormalY) && yDiff < lastNormalY)
									{
										Counter.increment1AndGetCount(uid, this.name + ".advanced.jumping", 4);
										int resetTime = Counter.getResetCooldown(uid, this.name + ".advanced.jumping");
										int data = Counter.getCount(uid, this.name + ".advanced.jumping");
										int diff = Math.abs(resetTime - data);
										if ((resetTime != 4 && data != 4) || (diff != 3 || (resetTime != 4 && data != 4 && diff != 1)))
											step_advanced_violation_type = 2;
									}
								}
								if (normal_y)
									Cache.set(uid, this.name + ".step.lastNormalY", yDiff);
								else
								{
									Cache.remove(uid, this.name + ".step.lastNormalY");
									step_advanced_violation_type = 1;
								}
								if (yDiff >= ground_sensitivity && !norm_cached_y_abnorm_current_y && b12 && step_advanced_violation_type != 0 && !(yDiff == .5 || (yDiff != ySpeed && yDiff + bdelta != 1.0)) && (yDiff >= .24 || BlockUtils.isPassable2D(p.getLocation(), .3, 2.0, true)) && !BlockUtils.isSolid(p.getLocation().getBlock()) && step_internalutils_isbuggyblock(p, step_lowerest_pos) && step_internalutils_isbuggyblock(p, p.getLocation().clone().add(0, -1, 0)) && step_internalutils_isbuggyblock(p, from.clone().add(0, -1, 0)))
								{
									suspect(p, 10, "t: step", "r: advanced_" + step_advanced_violation_type, "y: " + yDiff, "c_n_y: " + lastNormalY, "s: " + ySpeed, "l: " + ground_sensitivity);
									Reverter.getInstance(this).teleport(p);
								}
							}
							if (yDiff >= .08 && yDiff % .5 != 0 && yDiffDeltaAbs >= .248 && !p.hasPotionEffect(PotionEffectType.SPEED) && !hasJumpPE && !nearbySteppables && !blockabove && !Protections.hasProtection(p, ProtectionType.UNNORMAL_Y))
							{
								Counter.increment1AndGetCount(uid, this.name + ".step.normal", 3);
								if (!MovementUtils.check_y_values(p, yDiff, yDiffDeltaAbs) && yDiffDeltaAbs >= 1.25)
								{
									if (Counter.increment1AndGetCount(uid, this.name + ".step.high", 3) >= Lag.correction(p, 2))
									{
										suspect(p, (yDiffDeltaAbs * 20), "t: step", "r: packet-hard", "y: " + yDiff, "y_d: " + yDiffDeltaAbs);
										Reverter.getInstance(this).teleport(p);
									}
								}
								else if (!normal_y)
								{
									if (Counter.getCount(uid, this.name + ".step.normal") >= 2)
									{
										suspect(p, (yDiffDeltaAbs * 10), "t: step", "r: packet", "y: " + yDiff, "y_c: " + lastYDiff);
										Reverter.getInstance(this).teleport(p);
									}
								}
							}
						}
						
						if (!Protections.hasProtection(p, ProtectionType.DAMAGE) && !BlockUtils.isClimbableNearby(p.getLocation()) && Cooldowns.isCooldownEnded(uid, "protection.fishinghook.caught") && lastGroundYDistance != .5 && yDiff != .5 && !nearbyPistons && Cooldowns.isCooldownEnded(uid, "protection.enderpearl") && !BlockUtils.isLiquidNearby(to))
						{
							boolean hadSelfhitProtection = !Cooldowns.isCooldownEnded(uid, "protection.selfhit");
							int fireTicks = p.getFireTicks();
							boolean abnormalY = !blockabove && !ymap.containsY(yDiff) && yDiff < .3 && airticks <= 20;
							int airTicksThreshold = fireTicks > 0 || (BlockUtils.getBlocksInRadius2D(p.getLocation(), .3, 2.0, true).isEmpty() && !BlockUtils.getBlocksInRadius2D(p.getLocation(), .3, 0, true).isEmpty())
									? 5
									: (p.hasPotionEffect(PotionEffectType.HARM) || p.hasPotionEffect(PotionEffectType.POISON)) ? 8 : 1;
							int airTicksThreshold2 = hadSelfhitProtection ? 8 : !Cooldowns.isCooldownEnded(uid, "protection.bow") ? 20 : airTicksThreshold;
							if (jumping)
							{
								if (lastGround.equals(Cache.get(uid, "lastPos", from)) || hadGroundBuggy)
									Cache.set(uid, this.name + ".jump.hasJumpedOnGround", true);
								else
									Cache.set(uid, this.name + ".jump.hasJumpedOnGround", false);
							}
							if (airticks >= airTicksThreshold && ((Cooldowns.get(uid, "protection.selfhit") <= 20 && MovementUtils.check_y_normal_jump_similar(p, yDiff)) || (ymap.containsY(yDiff) && (yDiff != lastGroundYDistance || yDiff < ymap.getY(0) || airticks > 1) && (Cooldowns.isCooldownEnded(uid, "protection.floor") || airticks > 0))))
							{
								String cause = null;
								boolean y_similar_normal = MovementUtils.check_y_normal_jump_similar(p, lastYDiff);
								double deltaY = Math.abs(MovementUtils.expectedYDifferenceDelta(p) - yDiffDeltaAbs);

								if (yDiff >= lastYDiff && y_similar_normal)
									cause = "new y must be smaller than before";
								else if (yDiff > 0 && ((!jumping && lastYDiff <= -0.03) || (lastYDiff <= -.3 && !p.hasPotionEffect(PotionEffectType.JUMP))))
									cause = "jump cannot start while falling";
								else if (yDiffDeltaAbs >= .09 && y_similar_normal && PlayerUtils.getPotionEffectLevel(p, PotionEffectType.JUMP) == 0 && !jumping)
									cause = "y difference is too big";
								else if (yDiff < lastYDiff && y_similar_normal && airticks > 5)
								{
									if (MovementUtils.check_y_values(p, yDiff, lastGroundYDistance) && !Cache.get(uid, this.name + ".jump.hasJumpedOnGround", false))
										cause = "jump must be start on ground";
								}
								if (cause != null)
								{
									int resetTicks = 1;
									int attemptsLimit = 1;
									if (Protections.hasProtection(p, ProtectionType.DAMAGE))
									{
										resetTicks = 4;
										attemptsLimit = 2;
									}
									if (Counter.increment1AndGetCount(uid, this.name + ".jump.illegal", resetTicks) >= attemptsLimit)
									{
										suspect(p, 2, "t: illegal", "r: (" + cause + ")", "y: " + yDiff, "y_c: " + lastYDiff, "t: " + airticks, "b: " + lastGroundYDistance, "e: " + deltaY, "f: " + fireTicks);
										Reverter.getInstance(this).teleport(p);
									}
								}
							}
							else if (yDiff >= .08 && lastGroundYDistance >= .1 && !GroundChecks.isOnGroundCache(p, 0, 0, 0) && !GroundChecks.isOnGroundCache(p, 0, -.6, 0) && !abnormalY && !ymap.containsY(yDiff) && airticks >= airTicksThreshold2 && !ymap.containsY(lastYDiff) && (airticks > 2 || yDiff != .5))
							{
								int resetTick = 10;
								abnormalY = true;
								int verbLimit = 1;
								if (Protections.hasProtection(p, ProtectionType.DAMAGE))
								{
									resetTick = 4;
									verbLimit = 2;
								}
								else if ((yDiff < .13 && airticks <= 30) || hadSelfhitProtection)
								{
									resetTick = 8;
									verbLimit = 2;
								}
								if (Counter.increment1AndGetCount(uid, this.name + ".jump.unusual", resetTick) >= verbLimit)
								{
									suspect(p, 3, "t: unusual", "y: " + yDiff);
									Reverter.getInstance(this).teleport(p);
								}
							}

						}
					}
					boolean hasProtections = liquid_protection || BlockUtils.hasSteppableNearby(p.getLocation()) || BlockUtils.isGlideableNearby(p.getLocation()) || PlayerUtils.hasBlockAbove(p) || BlockUtils.hasBuggiesNearby(p.getLocation()) || Protections.hasProtection(p, ProtectionType.DAMAGE);

					float jumpVelocity = .42F + (PlayerUtils.getPotionEffectLevel(p, PotionEffectType.JUMP) * 0.1f);
					Vector clientVelocity = new Vector(0.0, 0.0, 0.0);
					clientVelocity = new Vector(Math.abs(from.getX() - to.getX()), yDiff, Math.abs(from.getZ() - to.getZ()));

					float expectedVelocity = 0.0f;

					if (clientVelocity.getY() < jumpVelocity)
						expectedVelocity = MathUtils.convertToFloat(clientVelocity.getY());
					else if (GroundChecks.isOnGround(p))
						expectedVelocity = 0.0f;

					if (clientVelocity.getY() > 0.0)
						expectedVelocity = clientVelocity.getY() > jumpVelocity - 0.001 && clientVelocity.getY() < jumpVelocity + 0.001 ? jumpVelocity : Math.abs(expectedVelocity);
					else
					{
						expectedVelocity -= 0.07999999821186066;
						expectedVelocity *= 0.9800000190734863;
					}
					if (expectedVelocity >= 0.0 && expectedVelocity <= 0.01 && clientVelocity.getY() <= 0.0 && clientVelocity.getY() >= -0.106)
						expectedVelocity = MathUtils.convertToFloat(clientVelocity.getY());

					float serversideVelocity = MathUtils.round_f(Math.abs(y_vel), 5, RoundingMode.FLOOR);
					float expectedVelocityY = MathUtils.round_f(expectedVelocity, 5, RoundingMode.FLOOR);
					float expectedVelocityY_cache = MathUtils.round_f(Cache.get(uid, this.name + ".calculatedVelocityCache", 0.0f), 5, RoundingMode.FLOOR);
					Cache.set(uid, this.name + ".calculatedVelocityCache", expectedVelocity);
					if (!hasProtections && !GroundChecks.isOnGround(p) && (serversideVelocity != expectedVelocityY_cache && serversideVelocity != expectedVelocityY) && yDiff > 0.02 && !p.getLocation().getBlock().getRelative(BlockFace.DOWN).isEmpty() && !BlockUtils.hadSteppableNearby(p))
					{
						suspect(p, (Math.abs(expectedVelocityY_cache - serversideVelocity) * 10), "y_v: " + serversideVelocity, "e_y_v: " + expectedVelocityY, "e_y_v_c: " + expectedVelocityY_cache);
						Reverter.getInstance(this).teleport(p);
					}
				}
				else
				{
					// TODO: Checks while Falling
					Cache.set(uid, this.name + ".wasGoingUp", false);
					Cache.set(uid, "maxJumpEffectAmp", 0);
					if (yDiff < 0)
						ticksDown = Counter.increment1AndGetCount(uid, "ticksDown", -1);
					else
						Counter.remove(uid, "ticksDown");
					Counter.remove(uid, "ticksUp");
					if (GroundChecks.isOnGround(from))
						Reverter.getInstance(this).setPosition(uid, from);
				}
				// ClimbSpeed //
				if (BlockUtils.isClimbableNearby(from))
					Counter.increment1AndGetCount(uid, this.name + ".climbspeed.climbticks", -1);
				else
					Counter.remove(uid, this.name + ".climbspeed.climbticks");
				if (Counter.getCount(uid, this.name + ".climbspeed.climbticks") > 6 && yDiff > .13)
				{
					suspect(p, (yDiff > .21 ? 10 : 8), "climbspeed", "y: " + yDiff);
					Reverter.getInstance(this).teleport(p);
				}
				// FalseGround
				double ydown = from.getY() - to.getY();
				if (ticksUp > 0 && !BlockUtils.isLiquidNearby(p.getLocation()) && !BlockUtils.hasSteppableNearby(p.getLocation()) && !BlockUtils.isClimbableNearby(p.getLocation()) && airticks < 10 && !BlockUtils.isSolid(p.getLocation().getBlock()) && !p.hasPotionEffect(PotionEffectType.JUMP))
				{
					if (p.isOnGround() && ((!GroundChecks.isOnGround(p.getLocation()) && yDiff > 0.0) || y_vel > 0.16 || ticksUp > 4) && !Protections.hasProtection(p, ProtectionType.ASCENSION) && !Protections.hasProtection(p, ProtectionType.UNNORMAL_Y))
					{
						// Fake-Ground
						suspect(p, fallDistance, "t: fake-ground", "y: " + yDiff, "y_v: " + y_vel, "t: " + ticksUp);
						Reverter.getInstance(this).teleport(p);
					}
					else if (ticksUp == 1 && y_vel <= 0.0 && yDiff > 0.34 && !BlockUtils.isGlideableNearby(p.getLocation()) && !BlockUtils.hasSteppableNearby(p.getLocation()))
					{
						// Velocity
						suspect(p, fallDistance, "t: velocity", "y: " + yDiff, "y_v: " + y_vel);
						Reverter.getInstance(this).teleport(p);
					}
				}
				if (!Protections.hasProtection(p, ProtectionType.UNNORMAL_Y))
				{
					// Stable //
					if (!GroundChecks.isOnGround(from) && !GroundChecks.isOnGround(to) && !BlockUtils.isMaterialSurround(p.getLocation(), 0.3, false, Material.WEB) && !Protections.hasProtection(p, ProtectionType.UNNORMAL_Y) && !Protections.hasProtection(p, ProtectionType.ASCENSION))
					{
						int hover_violation = liquid_protection ? 0 : (yDiffAbs == 0.0 ? (airticks > 0 && airticks <= 7 ? 1 : 3) : ((yDiffAbs < 0.001 && airticks > 7) ? 2 : (yDiffAbs < 0.06 ? 1 : 0)));
						boolean isHoveringAtWater = (liquid && ((y_vel > .419 && !p.hasPotionEffect(PotionEffectType.JUMP) && ticksUp == 0) || (y_vel < -0.8 && ticksUp < 2) || (yDiff > p.getFallDistance() && ticksUp > 0 && p.getFallDistance() != 0.0f && yDiff > 0.024) || (p.getFallDistance() == 0.0f && y_vel < -0.1625 && yDiff > 0.1625))) && yDiffAbs <= .44 && BlockUtils.isOnLiquid(p.getLocation());
						if ((isHoveringAtWater || hover_violation > 0) && Counter.incrementAndGetCount(uid, this.name + ".stable", hover_violation, 99) > ((PlayerUtils.getPotionEffectLevel(p, PotionEffectType.JUMP) > 1) ? 4 : 3))
						{
							suspect(p, Math.abs(20 - Math.abs(yDiffAbs * 100)), "t: stable", "y: " + yDiffAbs, "airticks: " + airticks);
							Reverter.getInstance(this).teleport(p);
						}
					}
					else if (GroundChecks.isOnGround(from) || GroundChecks.isOnGround(to))
						Counter.remove(uid, this.name + ".stable");
					if (yDiffAbs > 0.09 && airticks > 12 && !Protections.hasProtection(p, ProtectionType.ASCENSION) && (y_vel < -1.0 && fallDistance < 2.0D) || (y_vel > fallDistance && ticksUp == 0 && fallDistance > 0 && y_vel > 0.07))
					{
						// FastFall
						suspect(p, fallDistance, "t: fastfall", "fd: " + fallDistance, "y_v: " + y_vel);
						Reverter.getInstance(this).teleport(p);
					}
					if (y_vel < -1.32 && yDiffAbs < .419 && fallDistance > 0.51 && !liquid_protection && ticksUp == 0)
					{
						// Slowfall
						suspect(p, fallDistance, "t: slowfall", "y: " + yDiffAbs, "y_v: " + y_vel);
						Reverter.getInstance(this).teleport(p);
					}
					if (ydown > 0 && ydown <= .16 && !liquid_protection && !BlockUtils.isClimbableNearby(p.getLocation()) && ticksDown >= 6 && Counter.increment1AndGetCount(uid, this.name + ".glide", 99) >= 3)
					{
						// Glide
						suspect(p, fallDistance, "t: glide", "y: " + ydown);
						Reverter.getInstance(this).teleport(p);
					}
				}
				// Gravity
				String gravity_type = null;
				boolean gravity_setback = true;
				double y_vel_floor_3 = yfloor(y_vel);
				if ((fallDistance < 7.0D && y_vel < -2.0D) || (yDiff > .35 && y_vel < -.2 && !p.hasPotionEffect(PotionEffectType.JUMP) && oldTicksUp <= 5) || (p.getLocation().getBlock().isEmpty() && yDiff > .17 && (y_vel_floor_3 == -.419 || (y_vel_floor_3 == .248 && oldTicksUp > 5) || (y_vel_floor_3 == .164 && fallDistance != 0.0) || y_vel < -0.7)) || (yDiff > 0.56 && y_vel_floor_3 != -0.0784 && y_vel <= 0.0 && p.getFallDistance() <= 0.0f))
					gravity_type = "irregular";
				else if ((yDiff > 0.3994 && ticksUp > 1 && y_vel < yDiff && y_vel > 0.0784D && !BlockUtils.isGlideableNearby(p.getLocation()) && !BlockUtils.hasSteppableNearby(p.getLocation()) && yDiff != 0.5 && !jumping && !p.hasPotionEffect(PotionEffectType.JUMP) && !BlockUtils.hasSteppableNearby(to)))
					gravity_type = "bounce";
				if (!GroundChecks.isOnGround(p))
				{
					Cache.set(uid, this.name + ".gravity.gravity_y_delta", yDiff);
					if (!BlockUtils.isLiquidNearby(p.getLocation()))
					{
						if (yDiff < 0)
						{
							double diff = Math.abs(getExpectedGravity(uid) - yDiff);
							if (diff > GRAVITY_MAX_DIFFERENCE)
							{
								if (Counter.increment1AndGetCount(uid, this.name + ".gravity.calculated", 99) >= 5)
								{
									gravity_type = "calculated";
									gravity_setback = false;
								}
							}
						}
					}
					else if (GroundChecks.isOnGround(from))
						Counter.incrementAndGetCount(uid, this.name + ".gravity.calculated", -2, 99);
				}
				if (!buggy && !Protections.hasProtection(p, ProtectionType.UNNORMAL_Y) && gravity_type != null && gravity_type != "")
				{
					suspect(p, (fallDistance + Math.abs(y_vel * 10) + Math.abs(yDiffAbs * 10)), "t: gravity", "r: (" + gravity_type + ")", "y: " + yDiff, "y_vel: " + y_vel);
					if (gravity_setback)
						Reverter.getInstance(this).teleport(p);
				}
				// Ascension
				double up = 0.0;
				if (!BlockUtils.isLiquidNearby(from) && !BlockUtils.isClimbableNearby(from) && !BlockUtils.hasSteppableNearby(from) && !p.isInsideVehicle() && !Protections.hasProtection(p, ProtectionType.ASCENSION))
				{
					int ascensionCountLimit = 9;
					if (!Cache.contains(uid, this.name + ".ascension.ground"))
						Cache.set(uid, this.name + ".ascension.ground", true);
					if (hadGroundBuggy)
						ascensionCountLimit += 3;
					if (p.hasPotionEffect(PotionEffectType.JUMP))
					{
						ascensionCountLimit += 12 * (PlayerUtils.getPotionEffectLevel(p, PotionEffectType.JUMP) + 1);
					}
					else
					{
						double yDescend = from.getY() - to.getY();
						if (p.getLocation().getBlock().getType() != Material.AIR || GroundChecks.isOnGround(to))
						{
							Cache.set(uid, this.name + ".ascension.ground", true);
						}
						else if (Cache.get(uid, this.name + ".ascension.ground", false))
						{
							if (yDescend > 0D)
							{
								// Falling
								Cache.set(uid, this.name + ".ascension.ground", false);
							}
						}
						else if (yDescend < 0D)
						{
							if (Counter.increment1AndGetCount(uid, this.name + ".y-violation", 200) > Lag.correction(p, ascensionCountLimit))
							{
								suspect(p, (Counter.getCount(uid, this.name + ".y-violation") * 2), "t: up", "c: " + Counter.getCount(uid, this.name + ".y-violation"));
								Reverter.getInstance(this).teleport(p);
							}
						}
					}
					// Ascension(cached)
					double ascension_limit = 1.62;
					if (p.hasPotionEffect(PotionEffectType.JUMP))
					{
						for (PotionEffect effect : p.getActivePotionEffects())
						{
							if (!effect.getType().equals(PotionEffectType.JUMP))
								continue;
							int level = effect.getAmplifier() + 1;
							ascension_limit += Math.pow(level + 4.2, 2.0) / 16.0;
							break;
						}
					}
					if (GroundChecks.isOnGround(from) || BlockUtils.isLiquidNearby(from) || BlockUtils.isClimbableNearby(from) || GroundChecks.isOnGround(to) || BlockUtils.isLiquidNearby(to) || BlockUtils.isClimbableNearby(to) || buggy)
					{
						Cache.set(uid, this.name + ".ascension.ycache", p.getLocation().getY());
					}
					else if (Cache.contains(uid, this.name + ".ascension.ycache"))
					{
						double saved_ypos = Cache.get(uid, this.name + ".ascension.ycache", 0.0D);
						double current_ypos = p.getLocation().getY();
						double delta = current_ypos - saved_ypos;
						if (delta > ascension_limit)
						{
							suspect(p, (up * 2), "t: up", "y: " + delta);
							Reverter.getInstance(this).teleport(p);
						}
					}
					else
						Cache.set(uid, this.name + ".ascension.ycache", p.getLocation().getY());
				}
				if (!violations.isEmpty())
				{
					suspect(p, violation_level, Violation.convertTagListtoTag(violations));
					Reverter.getInstance(this).teleport(p);
				}
				this.lastPlayerLocations.put(uid, p.getLocation());
				Cache.set(uid, "lastYDiff", yDiff);
				Cache.set(uid, "lastYPos", p.getLocation().getY());
				Cache.set(uid, "lastPos", from);
				break;
			case 1:
				PlayerTeleportEvent e3 = (PlayerTeleportEvent) ev;
				Cache.remove(e3.getPlayer().getUniqueId(), this.name + ".ascension.ycache");
				break;
			case 2:
				PlayerChangedWorldEvent e4 = (PlayerChangedWorldEvent) ev;
				Cache.remove(e4.getPlayer().getUniqueId(), this.name + ".ascension.ycache");
				break;
			case 3:
				PlayerJoinEvent e5 = (PlayerJoinEvent) ev;
				Cache.remove(e5.getPlayer().getUniqueId(), this.name + ".ascension.ycache");
				break;
			case 4:
				BlockPlaceEvent e6 = (BlockPlaceEvent) ev;
				if ((e6.getBlock().getFace(e6.getBlockAgainst()) == BlockFace.DOWN || e6.getBlock().getFace(e6.getBlockAgainst()) == BlockFace.UP))
				{
					Cooldowns.set(e6.getPlayer().getUniqueId(), "towering", 10);
				}
				break;
		}
	}

	private double getExpectedGravity(UUID uid)
	{
		if (Cache.contains(uid, this.name + ".gravity.gravity_y_delta"))
		{
			double calculated = (Cache.get(uid, this.name + ".gravity.gravity_y_delta", 0.0D) - .08) * .98;
			Cache.set(uid, this.name + ".gravity.gravity_y_delta", calculated);
			if (calculated > 3.92)
				calculated = 3.92;
			return calculated;
		}
		return 0.0;
	}

	private boolean step_internalutils_isbuggyblock(Player p, Location loc)
	{
		Material localMaterial = loc.getBlock().getType();
		return (PlayerUtils.isInClosedChamber(p.getLocation())) || (localMaterial == Material.PISTON_EXTENSION) || (localMaterial == Material.PISTON_MOVING_PIECE) || (localMaterial == Material.DRAGON_EGG) || (localMaterial == Material.LEAVES) || (localMaterial == Material.LEAVES_2) || (localMaterial == Material.SOIL) || (localMaterial == Material.SOUL_SAND) || (localMaterial == Material.CAULDRON) || (localMaterial == Material.ANVIL);
	}

	private boolean step_checkbuggyblock_3d(Player p, Location loc, double x, double y, double z)
	{
		return (step_internalutils_isbuggyblock(p, loc.clone().add(0.0D, y, 0.0D))) && (step_internalutils_isbuggyblock(p, loc.clone().add(x, y, 0.0D))) && (step_internalutils_isbuggyblock(p, loc.clone().add(-x, y, 0.0D))) && (step_internalutils_isbuggyblock(p, loc.clone().add(0.0D, y, z))) && (step_internalutils_isbuggyblock(p, loc.clone().add(0.0D, y, -z))) && (step_internalutils_isbuggyblock(p, loc.clone().add(x, y, z))) && (step_internalutils_isbuggyblock(p, loc.clone().add(-x, y, -z))) && (step_internalutils_isbuggyblock(p, loc.clone().add(x, y, -z))) && (step_internalutils_isbuggyblock(p, loc.clone().add(-x, y, z)));
	}

	private double calculateYDelta(Player p, Location modifier)
	{
		if (modifier.getWorld() != p.getWorld() || Protections.hasProtection(p, ProtectionType.TELEPORT))
			return 0D;
		Location lastPos = this.lastPlayerLocations.getOrDefault(p.getUniqueId(), null);
		Location currentPos = p.getLocation();
		if (lastPos != null && lastPos.getWorld() == currentPos.getWorld())
		{
			double currentY = currentPos.getY();
			return Math.abs(currentY - modifier.getY() - (currentY - lastPos.getY())) * ((currentY - lastPos.getY()) < 0 ? -1 : 1);
		}
		return 0D;
	}

	private Location step_get_lowerest_position(Location to, Location from, double d)
	{
		from.setPitch(0.0F);
		from.setYaw(0.0F);
		Location from_clone = from.clone().add(from.clone().getDirection().multiply(d));
		Location localLocation2 = from.clone().add(from.clone().getDirection().multiply(-d));
		from.setYaw(45.0F);
		Location localLocation3 = from.clone().add(from.clone().getDirection().multiply(d));
		Location localLocation4 = from.clone().add(from.clone().getDirection().multiply(-d));
		from.setYaw(90.0F);
		Location localLocation5 = from.clone().add(from.clone().getDirection().multiply(d));
		Location localLocation6 = from.clone().add(from.clone().getDirection().multiply(-d));
		from.setYaw(135.0F);
		Location localLocation7 = from.clone().add(from.clone().getDirection().multiply(d));
		Location localLocation8 = from.clone().add(from.clone().getDirection().multiply(-d));
		double d1 = MathUtils.getHorizontalDistance(to, from_clone);
		double d2 = MathUtils.getHorizontalDistance(to, localLocation2);
		double d3 = MathUtils.getHorizontalDistance(to, localLocation3);
		double d4 = MathUtils.getHorizontalDistance(to, localLocation4);
		double d5 = MathUtils.getHorizontalDistance(to, localLocation5);
		double d6 = MathUtils.getHorizontalDistance(to, localLocation6);
		double d7 = MathUtils.getHorizontalDistance(to, localLocation7);
		double d8 = MathUtils.getHorizontalDistance(to, localLocation8);
		double d9 = Math.min(d1, d2);
		d9 = Math.min(d9, d3);
		d9 = Math.min(d9, d4);
		d9 = Math.min(d9, d5);
		d9 = Math.min(d9, d6);
		d9 = Math.min(d9, d7);
		d9 = Math.min(d9, d8);
		return d9 == d7 ? localLocation7 : d9 == d6 ? localLocation6 : d9 == d5 ? localLocation5 : d9 == d4 ? localLocation4 : d9 == d3 ? localLocation3 : d9 == d2 ? localLocation2 : d9 == d1 ? from_clone : localLocation8;
	}

	public float yfloor(double yDiff)
	{
		return MathUtils.round_f(yDiff, 3, RoundingMode.FLOOR);
	}
}
