package com.eric0210.nomorecheats.checks.combat.killaura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.Violation;
import com.eric0210.nomorecheats.api.ViolationMap;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.GroundChecks;
import com.eric0210.nomorecheats.api.util.TimeUtils;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.AverageCollector;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.Checks;
import com.eric0210.nomorecheats.api.util.CombatUtils;
import com.eric0210.nomorecheats.api.util.KillauraUtils;
import com.eric0210.nomorecheats.api.util.MathUtils;
import com.eric0210.nomorecheats.api.util.MovementUtils;
import com.eric0210.nomorecheats.api.util.PlayerUtils;

/*
 * An KillauraHeuristics-aura checks with bukkit event handling API
 */
public class KillauraEvent extends Check implements EventListener
{
	public HashMap<UUID, KillauraData> datas = new HashMap<>();

	public KillauraEvent()
	{
		super("KillAura");
		EventManager.onPlayerQuit.add(new EventInfo(this, 0));
		EventManager.onEntityDamageByEntity.add(new EventInfo(this, 1));
		EventManager.onPlayerMove.add(new EventInfo(this, 2));
		EventManager.onPlayerInteract.add(new EventInfo(this, 3));
		EventManager.onPlayerTeleport.add(new EventInfo(this, 4));
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				PlayerQuitEvent quitEvent = (PlayerQuitEvent) ev;
				reset(quitEvent.getPlayer().getUniqueId());
				break;
			case 1:
				EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) ev;
				Entity entity = e.getEntity();
				if (e.getDamager() instanceof Player)
				{
					Player p = (Player) e.getDamager();
					KillauraData data = this.datas.getOrDefault(p.getUniqueId(), new KillauraData());
					ArrayList<String> violations = new ArrayList<>();
					int vl = 0;
					double xDiff = Math.abs(entity.getLocation().getX() - p.getLocation().getX());
					double yDiff = Math.abs(entity.getLocation().getY() - p.getLocation().getY());
					double zDiff = Math.abs(entity.getLocation().getZ() - p.getLocation().getZ());
					if (xDiff != 0.0 && zDiff != 0.0 && yDiff < 0.6)
					{
						String violationTags = getWallcheck_tags(p, entity.getLocation());
						if (violationTags != null)
							violations.add("wall");
					}
					KillauraUtils.handleAttack((Player) e.getDamager(), e.getEntity());
					UUID uid = p.getUniqueId();
					if (e.getEntity() instanceof Player)
					{
						// Hit Accuracy
						Player damagee = (Player) e.getEntity();
						long tbcP = KillauraUtils.timeBetweenClicks.getOrDefault(uid, 0L);
						long tbcDamagee = KillauraUtils.timeBetweenClicks.getOrDefault(damagee.getUniqueId(), 0L);
						double accPercentage = KillauraUtils.accuracyPercentage.getOrDefault(uid, 0D);
						if (accPercentage >= 90.0D && tbcP > 0 && tbcP <= 550L && tbcDamagee > 0 && tbcDamagee <= 550 && CombatUtils.hasCombat(p) && CombatUtils.hasCombat(damagee))
						{
							KillauraUtils.removeAccuracy(p);
							violations.add("hit-accuracy");
							vl += 0.05 * accPercentage;
						}
					}
					Entity damagee = e.getEntity();
					int nearbyAttackables = KillauraUtils.getNearbyAttackableEntitiesCountAdv(p, 6);
					double hDist = MathUtils.getHorizontalDistance(p.getLocation(), damagee.getLocation());
					if (damagee instanceof LivingEntity)
					{
						LivingEntity le = (LivingEntity) damagee;
						double dir = MathUtils.getDirectionDelta(p, le);
						double rot = MathUtils.getRotationDelta(p, le);
						double ang = MathUtils.getAngle(p, le);
						double pitch = Math.abs(p.getLocation().getPitch());
						
						if (nearbyAttackables <= 12 && hDist >= 1.0D)
						{
							// Wrong Direction
							double offset = 0.0D;
							Location entLoc = e.getEntity().getLocation();
							Location pLoc = e.getDamager().getLocation();
							Vector player_rot = new Vector(pLoc.getYaw(), pLoc.getPitch(), 0);
							Vector aimbot_rot = MathUtils.getRotation(pLoc, entLoc);
							double deltaYaw = MathUtils.clamp180D(player_rot.getX() - aimbot_rot.getX());
							double deltaPitch = MathUtils.clamp180D(player_rot.getY() - aimbot_rot.getY());
							double horizontalDist = MathUtils.getHorizontalDistance(pLoc, entLoc);
							double dist = MathUtils.getDistance3D(pLoc, entLoc);
							double offsetX = deltaYaw * horizontalDist * dist;
							double offsetY = deltaPitch * Math.abs(entLoc.getY() - pLoc.getY()) * dist;
							if ((offset +=  Math.abs(offsetX) + Math.abs(offsetY)) > 1150)
							{
								violations.add("wrong-direction");
								vl += Math.min((1150 - offset) * 10, 50);
							}
							// Aim Accuracy //
							if (!KillauraUtils.isEasyToAim(p, damagee))
							{
								long t_b_c = KillauraUtils.timeBetweenClicks.getOrDefault(uid, 0L);
								if ((KillauraUtils.isBigEntity(damagee) || (damagee instanceof Player)) && MathUtils.inRange(t_b_c, 1, 400))
								{
									double rot_delta = MathUtils.getRotationDelta(p, le);
									if (rot_delta <= 3.0)
										data.hits++;
									else
										data.misses++;
									double count = data.hits + data.misses;
									if (count >= 10 && data.hits / count >= 0.9)
									{
										violations.add("aim-accuracy");
										vl += 5 * data.hits / count;
									}
								}
								// Aim Consistency //
								long tbc = KillauraUtils.timeBetweenClicks.getOrDefault(p.getUniqueId(), 0L);
								if (data.direction != -1 && data.rotation != -1 && data.angle != -1 && data.pitch != -1)
								{
									double cached_dir = data.direction;
									double cached_rot = data.rotation;
									double cached_ang = data.angle;
									double cached_pitch = data.pitch;
									if ((cached_dir < .2) && (dir <= .5) && (cached_rot <= 4.0) && (rot <= 25.0) && (cached_ang < .01) && (ang >= .999) && (cached_pitch > 0) && (cached_pitch < 8.0) && MathUtils.inRange(tbc, 1, 550))
									{
										Location aimposDiff = MathUtils.getFacingPosition(p, le);
										double x = MathUtils.abs(aimposDiff.getX());
										double z = MathUtils.abs(aimposDiff.getZ());
										boolean isPlayer = (le instanceof Player);
										double d11 = !isPlayer ? 0 : KillauraUtils.hitRatioDiff(p, (Player) le);
										double threshold = ViolationMap.getInstance(p).getLevel(this) >= 2 || d11 >= 35 ? 6 : 7;
										if (x >= threshold || z >= threshold)
											if (!Cooldowns.isCooldownEnded(uid, "sprint-jump") || (CombatUtils.hasHitEntity(p) && ((!isPlayer) || CombatUtils.hasHitByEntity((Player) le))) || (isPlayer && CombatUtils.hasCombat((Player) le)) || (!Cooldowns.isCooldownEnded(uid, this.name + ".advanced.yaw.suspicious") || !Cooldowns.isCooldownEnded(uid, this.name + ".advanced.pitch.suspicious")) || (isPlayer && !GroundChecks.isOnGround((Player) le)))
											{
												violations.add("aim-consistency");
												vl += 5;
											}
									}
								}
								// Direction //
								if (PlayerUtils.getPing(p) <= 300 && Counter.getCount(uid, Checks.falsePackets.pingspoof_keepalive_detections) <= 0 && TimeUtils.elapsed(uid, this.name + ".direction", true, 1000L) && dir >= 2)
								{
									if (TimeUtils.contains(uid, this.name + ".direction.violation"))
									{
										int dvTicks = (int) (TimeUtils.get(uid, this.name + ".direction.violation", 0) / 50L);
										if (dvTicks <= 200 && Counter.increment1AndGetCount(uid, this.name + ".advanced.direction", 120) >= 2)
										{
											violations.add("direction");
											vl += 10;
										}
									}
									TimeUtils.putCurrentTime(uid, this.name + ".direction.violation");
								}
							}
						}
						// Angle
						if (nearbyAttackables <= 5 && hDist >= 1.5D && PlayerUtils.getPing(p) <= 500 && Counter.getCount(uid, Checks.falsePackets.pingspoof_keepalive_detections) <= 0 && !p.hasPotionEffect(PotionEffectType.SPEED))
							if (ang != 0)
							{
								double ang2 = KillauraUtils.getAngle2(p, damagee);
								if ((!((ang + .4 / hDist > 1.0D) && (ang - .4 / hDist < 1.0D)) || !((ang + .2 > 1.0D) && (ang - .2 < 1.0D)) || (ang2 > .6)) && Counter.increment1AndGetCount(uid, this.name + ".advanced.angle", 100) >= 5)
								{
									violations.add("angle");
									vl += 2;
								}
							}
						// Modulo
						if (Cache.get(uid, this.name + ".advanced.modulo.teleport", 0) == 0 && !killaura_modulo_nocheck(p, damagee))
						{
							Location loc = p.getLocation();
							double yaw = Math.abs(loc.getYaw());
							double modYaw = yaw % .5D;
							if (modYaw == 0)
							{
								violations.add("modulo-yaw");
								vl += 10;
							}
							if (Cache.contains(uid, this.name + ".modulo.pitch-cache"))
							{
								double cached = Math.abs(Cache.get(uid, this.name + ".modulo.pitch-cache", 0.0D));
								double modPitch = cached % .5;
								if (cached >= 15.0D && modPitch == 0)
								{
									violations.add("modulo-pitch");
									vl += 10;
								}
							}
							Cache.set(uid, this.name + ".modulo.pitch-cache", pitch);
						}
						// HitConsistency (Tick-aura)
						if (TimeUtils.contains(uid, this.name + ".hitconsistency.time"))
						{
							long delay = TimeUtils.getTimeDiff(uid, this.name + ".hitconsistency.time", System.currentTimeMillis());
							if (MathUtils.inRange(delay, 450, 500))
							{
								long tbc = KillauraUtils.timeBetweenClicks.getOrDefault(uid, 0L);
								long diff = Math.abs(delay - tbc);
								if (diff <= 3L)
								{
									violations.add("hit-consistency");
									vl += 2;
								}
							}
						}
						TimeUtils.putCurrentTime(uid, this.name + ".hitconsistency.time");
						if (Cooldowns.isCooldownEnded(uid, this.name + ".advanced.rotations.dir"))
						{
							if (dir >= 1.25)
							{
								Cooldowns.set(uid, this.name + ".advanced.rotations.dir", 14);
							}
						}
						Cooldowns.set(uid, this.name + ".hitCooldown", 1);
						if (hDist > 1)
						{
							// Combined
							long tbc = KillauraUtils.timeBetweenClicks.getOrDefault(uid, 0L);
							if (Cache.contains(uid, this.name + ".combined.direction-cache") && tbc != 0)
							{
								Location loc = MathUtils.getFacingPosition(p, damagee);
								double absX = MathUtils.abs(loc.getX());
								double absZ = MathUtils.abs(loc.getZ());
								boolean isPlayer = (damagee instanceof Player);
								double hmr = !isPlayer ? 0 : KillauraUtils.hitRatioDiff(p, (Player) damagee);
								double threshold = ViolationMap.getInstance(p).getLevel(this) >= 2 || hmr >= 35 ? 6 : 7;
								if (absX >= threshold || absZ >= threshold)
								{
									double cdif = Math.abs(dir - Cache.get(uid, this.name + ".combined.direction-cache", 0.0D));
									if (cdif <= .2 && ang >= .99 && rot <= 12.0D && tbc <= 350L && !KillauraUtils.isAnimal(damagee) && (!Cooldowns.isCooldownEnded(uid, "sprint-jump") || (CombatUtils.hasHitEntity(p) && ((!isPlayer) || CombatUtils.hasHitByEntity((Player) damagee))) || (isPlayer && CombatUtils.hasCombat((Player) damagee) || !GroundChecks.isOnGround((Player) damagee)) || (!Cooldowns.isCooldownEnded(uid, this.name + ".advanced.yaw.suspicious") || !Cooldowns.isCooldownEnded(uid, this.name + ".advanced.pitch.suspicious"))))
									{
										violations.add("combined");
										vl += 5;
									}
								}
							}
							Cache.set(uid, this.name + ".combined.direction-cache", dir);
							// Comparison
							checkComparisonCache(p, true);
							boolean isPlayer = damagee instanceof Player;
							boolean ez2aim = KillauraUtils.isEasyToAim(p, damagee);
							double deltalimit = isPlayer ? .3 : ((isPlayer && !ez2aim) || PlayerUtils.getNearbyLivingsAndBoatsCount(p, 6.0D) > 5) ? .45 : .1;
							// double direction = MathUtils.getDirectionDelta(p, damagee);
							double dirCache = data.direction_cache.getOrDefault(p.getUniqueId(), 0D);
							double dirDelta = Math.abs(dir - dirCache);
							// long tbc = KillauraUtils.timeBetweenClicks.getOrDefault(uid, 0L);
							if (dirCache != 0 && dirDelta != 0 && tbc > 0L && tbc <= 550L)
							{
								// boolean detect = false;
								Location loc = MathUtils.getFacingPosition(p, damagee);
								double x = loc == null ? 0 : Math.abs(loc.getX());
								double z = loc == null ? 0 : Math.abs(loc.getZ());
								int violation = ViolationMap.getInstance(p).getLevel(this);
								if (dirDelta >= 2)
								{
									violations.add("direction");
									vl += 5;
								}
								else
								{
									double hmr = !isPlayer ? 0 : KillauraUtils.hitRatioDiff(p, (Player) damagee);
									if (hDist <= 3.5 || !MovementUtils.hasChasingPlayer(p))
									{
										if (dirDelta >= .01)
										{
											double threshold = (violation >= 2 || hmr >= 35) ? 6 : 7;
											if (x >= threshold || z >= threshold && Counter.increment1AndGetCount(uid, this.name + ".advanced.comparison.sensitive", 100) >= (hmr > 35 ? 2 : 3))
											{
												violations.add("comparison(sensitive)");
												vl += 3;
											}
										}
										if (dirDelta >= .1 && (x >= 6 || z >= 6) && Counter.increment1AndGetCount(uid, this.name + ".advanced.comparison.hard", 100) >= 2)
										{
											violations.add("comparison(hard)");
											vl += 5;
										}
									}
									if (dirDelta >= deltalimit && Counter.increment1AndGetCount(uid, this.name + ".advanced.comparison.normal", 100) >= 2)
									{
										violations.add("comparison(normal)");
										vl += 3;
									}
									if (dirDelta > 0 && ez2aim && Counter.increment1AndGetCount(uid, this.name + ".advanced.comparison.constant", 100) >= 2)
									{
										violations.add("comparison(constant)");
										vl += 3;
									}
								}
							}
						}
						else
						{
							if (data.direction_cache.containsKey(p.getUniqueId()))
								data.direction_cache.remove(p.getUniqueId());
						}
						// Rapid Hits
						if (!Cooldowns.isCooldownEnded(uid, this.name + ".advanced.rapidhits.cooldown"))
						{
							if (Counter.increment1AndGetCount(uid, this.name + ".advanced.rapidhits", 100) >= 6)
							{
								violations.add("rapid-hits");
								vl += 5;
							}
						}
						else
							Cooldowns.set(uid, this.name + ".advanced.rapidhits.cooldown", 2);
						// Hits Per Second
						int hits = Counter.increment1AndGetCount(uid, this.name + ".advanced.hitspersec", 20);
						if (hits >= 15)
						{
							violations.add("hps(" + hits + ")");
							vl += 5;
						}
						if (nearbyAttackables <= 5 && hDist >= 1.0)
						{
							// Hitbox
							if (GroundChecks.isOnGroundCache(p, 0, 0, 0) && GroundChecks.isOnGroundCache(p, 0, -.25, 0))
							{
								double abs_pitch = Math.abs(p.getLocation().getPitch());
								boolean detect = false;
								if (hDist >= 1 && abs_pitch >= 80)
									detect = true;
								else if (hDist >= 1.5 && abs_pitch >= 70)
									detect = true;
								else if (hDist >= 2 && abs_pitch >= 60)
									detect = true;
								else if (hDist >= 2.5 && abs_pitch >= 55)
									detect = true;
								else if (hDist >= 3 && abs_pitch >= 45)
									detect = true;
								else if (hDist >= 3.5 && abs_pitch >= 35)
									detect = true;
								if (detect)
								{
									double d2 = !(damagee instanceof Player) ? 0.0D : KillauraUtils.hitRatioDiff(p, (Player) damagee);
									if (Counter.increment1AndGetCount(uid, this.name + ".advanced.hitbox", 300) >= (d2 >= 35 ? 1 : 2))
									{
										violations.add("hitbox");
										vl += 10;
									}
								}
							}
							// Entity Raytrace
							Block b;
							for (double d = 0.0; d <= hDist; d += 1.0)
							{
								b = p.getLocation().add(p.getLocation().getDirection().multiply(d)).getBlock();
								for (Entity ent : p.getNearbyEntities(d, d, d))
								{
									if (ent instanceof LivingEntity && !KillauraUtils.isBigEntity(ent) && ent != damagee)
									{
										LivingEntity le2 = (LivingEntity) ent;
										double deltarotation = MathUtils.getRotationDelta(p, le2);
										double edb = le2.getLocation().distance(b.getLocation());
										double edd = le2.getLocation().distance(damagee.getLocation());
										if (edd >= 1 && edb <= .8 && deltarotation > 0 && deltarotation <= 7.5)
										{
											violations.add("entity-raytrace");
											vl += 2;
										}
									}
								}
							}
						}
						data.direction = dir;
						data.rotation = rot;
						data.angle = ang;
						data.pitch = pitch;
					}
					if (!violations.isEmpty())
					{
						suspect(p, vl, Violation.convertTagListtoTag(violations));
					}
					this.datas.put(p.getUniqueId(), data);
				}
				break;
			case 2:
				PlayerMoveEvent moveEvent = (PlayerMoveEvent) ev;
				Player p = moveEvent.getPlayer();
				KillauraData data = this.datas.getOrDefault(p.getUniqueId(), new KillauraData());
				Location loc = p.getLocation();
				List<Entity> nearbyEntities = p.getNearbyEntities(6, 6, 6);
				LivingEntity closestLivingToReticle = null;
				nearbyEntities.removeIf((ent) ->
				{
					return !(ent instanceof LivingEntity);
				});
				nearbyEntities.sort((ent, last) ->
				{
					if (p.getWorld().equals(ent.getWorld()))
					{
						double rotation_delta = MathUtils.getRotationDelta(p, (LivingEntity) ent);
						if (p.getWorld().equals(last.getWorld()))
						{
							double rotation_delta2 = MathUtils.getRotationDelta(p, (LivingEntity) last);
							return rotation_delta < rotation_delta2 ? -1 : 1;
						}
						return 0;
					}
					return 0;
				});
				closestLivingToReticle = !nearbyEntities.isEmpty() ? (LivingEntity) nearbyEntities.get(0) : null;
				ArrayList<String> violations = new ArrayList<>();
				int vl = 0;
				double yaw = loc.getYaw();
				double pitch = loc.getPitch();
				if (Cache.contains(p.getUniqueId(), this.name + ".rotations.yaw-cache") && Cache.contains(p.getUniqueId(), this.name + ".rotations.pitch-cache"))
				{
					double yawSpeed = diff_clamp180(Cache.get(p.getUniqueId(), this.name + ".rotations.yaw-cache", 0.0D), yaw);
					double pitchSpeed = Math.abs(Cache.get(p.getUniqueId(), this.name + ".rotations.pitch-cache", 0.0D) - pitch);
					Cache.set(p.getUniqueId(), this.name + ".rotations.yawspeed", yawSpeed);
					Cache.set(p.getUniqueId(), this.name + ".rotations.pitchspeed", pitchSpeed);
					Cooldowns.set(p.getUniqueId(), this.name + ".rotationchanged", 4);
					if (yawSpeed >= 40)
						if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".advanced.yaw.suspicious", 10) >= 5)
							Cooldowns.set(p.getUniqueId(), this.name + ".advanced.yaw.suspicious", 15);
					if (pitchSpeed >= 40)
						if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".advanced.pitch.suspicious", 10) >= 5)
							Cooldowns.set(p.getUniqueId(), this.name + ".advanced.pitch.suspicious", 15);
					if (closestLivingToReticle != null)
					{
						if (data.lastClosestToReticleEntityId != -1 && closestLivingToReticle.getEntityId() != data.lastClosestToReticleEntityId && MathUtils.getDirectionDelta(p, closestLivingToReticle) < 1 && data.lastClosestToReticleDirectionChecked && yawSpeed > 70 && Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".snapping", 100) > 5)
							violations.add("yaw-snap");
						data.lastClosestToReticleDirectionChecked = MathUtils.getDirectionDelta(p, closestLivingToReticle) < 1;
						double closest_rotation_delta_yaw = MathUtils.getRotationDelta_yaw(p, closestLivingToReticle);
						double closest_rotation_delta_pitch = MathUtils.getRotationDelta_pitch(p, closestLivingToReticle);
						double angle = MathUtils.getAngle(p, closestLivingToReticle);
						boolean hit = !Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".hitCooldown");
						// Real-time Rotation checks //
						if (nearbyEntities.size() <= 5 && closest_rotation_delta_yaw <= 10 && closest_rotation_delta_pitch <= 10)
						{
							// YawMovements //
							data.yaw_speed_collector.add(yawSpeed);
							data.pitch_speed_collector.add(pitchSpeed);
							data.yaw_rotation_delta_collector.add(closest_rotation_delta_yaw);
							data.pitch_rotation_delta_collector.add(closest_rotation_delta_pitch);
							if (hit)
							{
								if (data.yaw_speed_collector.getCount() > 10 && data.yaw_rotation_delta_collector.getCount() > 10)
								{
									double yawspeed_avg = data.yaw_speed_collector.getAverage();
									double yawspeed_minmax_delta = data.yaw_speed_collector.getMax() - data.yaw_speed_collector.getMin();
									double yaw_rotation_avg = data.yaw_rotation_delta_collector.getAverage();
									double yaw_rotation_minmax_delta = data.yaw_rotation_delta_collector.getMax() - data.yaw_rotation_delta_collector.getMin();
									if (angle >= .999)
									{
										int vl2 = 0;
										if (yawSpeed >= 30F && (yaw_rotation_avg < 3 || yaw_rotation_minmax_delta < 3) && Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".yaw.violation", 600) >= 3)
											vl2 += 5;
										else if (yawspeed_avg > 15F && yawspeed_minmax_delta < 5 && (yaw_rotation_avg < 3 || yaw_rotation_minmax_delta < 3) && Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".yaw.violation", 1200) >= 3)
											vl2 += 3;
										if (vl2 > 0)
										{
											violations.add("yaw");
											vl += vl2;
										}
									}
									data.yaw_speed_collector.reset();
									data.yaw_rotation_delta_collector.reset();
								}
								// Pitch Movements //
								if (data.pitch_speed_collector.getCount() > 5 && data.pitch_rotation_delta_collector.getCount() > 5)
								{
									double pitchspeed_avg = data.pitch_speed_collector.getAverage();
									double pitchspeed_minmax_delta = data.pitch_speed_collector.getMax() - data.pitch_speed_collector.getMin();
									double pitch_rotation_avg = data.pitch_rotation_delta_collector.getAverage();
									double pitch_rotation_minmax_delta = data.pitch_rotation_delta_collector.getMax() - data.pitch_rotation_delta_collector.getMin();
									if (angle >= .999)
									{
										int vl2 = 0;
										if (pitchspeed_avg >= 20F && (pitch_rotation_avg < 3 || pitch_rotation_minmax_delta < 3) && Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".yaw.violation", 600) >= 3)
											vl2 += 5;
										else if (pitchspeed_avg > 10F && pitchspeed_minmax_delta < 2 && (pitch_rotation_avg < 2 || pitch_rotation_minmax_delta < 2) && (Counter.getCount(p.getUniqueId(), "ticksUp") > 0 || Counter.getCount(p.getUniqueId(), "ticksDown") > 0) && Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".yaw.violation", 1200) >= 3)
											vl2 += 3;
										if (vl2 > 0)
										{
											violations.add("pitch");
											vl += vl2;
										}
									}
									data.pitch_speed_collector.reset();
									data.pitch_rotation_delta_collector.reset();
								}
							}
						}
					}
				}
				if (!violations.isEmpty())
					suspect(p, vl, Violation.convertTagListtoTag(violations));
				Cache.set(p.getUniqueId(), this.name + ".rotations.yaw-cache", yaw);
				Cache.set(p.getUniqueId(), this.name + ".rotations.pitch-cache", pitch);
				TimeUtils.putCurrentTime(p.getUniqueId(), this.name + ".lastmove");
				this.datas.put(p.getUniqueId(), data);
				break;
			case 3:
				PlayerInteractEvent interactEvent = (PlayerInteractEvent) ev;
				KillauraUtils.handleInteraction(interactEvent.getPlayer(), interactEvent.getAction());
				break;
			case 4:
				PlayerTeleportEvent tpEvent = (PlayerTeleportEvent) ev;
				Cache.set(tpEvent.getPlayer().getUniqueId(), this.name + ".advanced.modulo.teleport", 1);
				break;
		}
	}

	private void checkComparisonCache(Player p, boolean checkValid)
	{
		KillauraData data = this.datas.getOrDefault(p.getUniqueId(), new KillauraData());
		if (!checkValid || PlayerUtils.isValid(p))
		{
			p.getNearbyEntities(6, 6, 6).stream().filter(e -> e instanceof LivingEntity).forEach(ent ->
			{
				if (ent.getVehicle() == null && !p.isFlying())
				{
					double d = MathUtils.getHorizontalDistance(p.getLocation(), ent.getLocation());
					if (d >= 1.0)
						data.direction_cache.put(ent.getUniqueId(), MathUtils.getDirectionDelta(p, ent));
					else
						data.direction_cache.remove(ent.getUniqueId());
				}
			});
		}
		else
			data.direction_cache = new HashMap<>(); // Re-initialize direction cache
		this.datas.put(p.getUniqueId(), data);
	}

	public static double diff_clamp180(double paramDouble1, double paramDouble2)
	{
		double d = Math.abs(paramDouble1 - paramDouble2);
		return d <= 360.0D ? d : d - 360.0D;
	}

	private boolean killaura_modulo_nocheck(Player paramPlayer, Entity paramEntity)
	{
		if (!KillauraUtils.isBigEntity(paramEntity))
		{
			return false;
		}
		Location localLocation1 = paramPlayer.getLocation();
		Location localLocation2 = paramEntity.getLocation();
		localLocation1.setPitch(0.0F);
		double distance = localLocation1.distance(localLocation2);
		double eyeheight = ((LivingEntity) paramEntity).getEyeHeight();
		Location localLocation3 = localLocation1.clone().add(localLocation1.getDirection().multiply(distance));
		Location localLocation4 = localLocation1.clone().add(localLocation1.getDirection().multiply(distance + 1.0D));
		Location localLocation5 = localLocation3.clone().add(0.0D, 1.0D, 0.0D);
		Location localLocation6 = localLocation4.clone().add(0.0D, 1.0D, 0.0D);
		return (BlockUtils.isSolid(localLocation3.getBlock())) || (BlockUtils.isSolid(localLocation4.getBlock())) || ((eyeheight > 1.0D) && ((BlockUtils.isSolid(localLocation5.getBlock())) || (BlockUtils.isSolid(localLocation6.getBlock()))));
	}

	public void freezeCombat(Player p, int ticks)
	{
		Cooldowns.set(p.getUniqueId(), this.name + ".cancelCombat", ticks);
	}

	public boolean hasFreezeCombat(Player p)
	{
		return !Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".cancelCombat");
	}

	public static class KillauraData
	{
		AverageCollector yaw_speed_collector = new AverageCollector();
		AverageCollector yaw_rotation_delta_collector = new AverageCollector();
		AverageCollector pitch_speed_collector = new AverageCollector();
		AverageCollector pitch_rotation_delta_collector = new AverageCollector();
		HashMap<UUID, Double> direction_cache = new HashMap<>();
		int lastClosestToReticleEntityId = -1;
		boolean lastClosestToReticleDirectionChecked = false;
		int hits = 0;
		int misses = 0;
		double direction = 0D;
		double rotation = 0D;
		double angle = 0D;
		double pitch = 0D;
	}

	private void reset(UUID uid)
	{
		if (this.datas.containsKey(uid))
			this.datas.remove(uid);
	}

	public static String getWallcheck_tags(Player p, Location pos)
	{
		if (p.getWorld() != pos.getWorld())
		{
			return null;
		}
		Block posBlock = pos.getBlock();
		double distance = p.getLocation().distance(pos);
		for (double d2 = 0.0D; d2 <= distance; d2 += 1.0D)
		{
			Location currentRayPos = p.getLocation().add(0.0D, p.getEyeHeight(), 0.0D).add(p.getLocation().getDirection().multiply(d2));
			double ray_distance = p.getLocation().distance(currentRayPos);
			Block currentRayBlock = currentRayPos.getBlock();
			double posBlock_rayBlock_distance = p.getLocation().distance(posBlock.getLocation()) - p.getLocation().distance(currentRayBlock.getLocation());
			if ((BlockUtils.canRayTrace(currentRayPos, true)) && (ray_distance <= distance) && (posBlock_rayBlock_distance >= 0.4D) && (currentRayBlock.getLocation().getY() >= p.getLocation().getY()))
			{
				return "p-p: " + distance + ", p-r: " + ray_distance + ", n: " + currentRayBlock.getType().name() + ", r-b: " + posBlock_rayBlock_distance;
			}
		}
		return null;
	}
}
