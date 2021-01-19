package com.eric0210.nomorecheats.checks.movement;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityMountEvent;

import com.eric0210.nomorecheats.AntiCheat;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.packet.PacketInEvent;
import com.eric0210.nomorecheats.api.packet.PacketListener;
import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.packet.PacketOutEvent;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.BlockFace;
import com.eric0210.nomorecheats.api.packet.enums.EntityAction;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockDigPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockPlacePacket;
import com.eric0210.nomorecheats.api.packet.packets.in.EntityActionPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockDigPacket.DigAction;
import com.eric0210.nomorecheats.api.util.Protections;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.InternalUtils;
import com.eric0210.nomorecheats.api.util.Lag;
import com.eric0210.nomorecheats.api.util.MathUtils;
import com.eric0210.nomorecheats.api.util.MovementUtils;
import com.eric0210.nomorecheats.api.util.NMS;
import com.eric0210.nomorecheats.api.util.PlayerUtils;
import com.eric0210.nomorecheats.api.util.Reverter;
import com.eric0210.nomorecheats.api.util.YMap;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.GroundChecks;
import com.eric0210.nomorecheats.api.util.Protections.ProtectionType;

import net.minecraft.server.v1_7_R4.EnumAnimation;
import net.minecraft.server.v1_7_R4.ItemStack;

// TODO 다 갈아엎고 ACP처럼 Tick Speed Decreasement 계산 없이 해당 Status의 Max Speed만 검사하는 것부터 시작하도록 한다
public class Speed extends Check implements EventListener, PacketListener
{
	public HashMap<UUID, SpeedData> datas = new HashMap<>();

	public interface Limits
	{
		public static final double GROUND = .2872;
		public static final double AIR = .36;
		public static final double BLOCKABOVE_GROUND = .7;
		public static final double BLOCKABOVE_AIR = 1.0;
		public static final double SOULSAND = .2;
		public static final double WEB_HORIZONTAL = .135;
		public static final double WEB_VERTICAL = .021;
		public static final double SNEAK = .15D;
		public static final double WATER = .3;
		public static final double JUMPSTART_BOOST = .65;
		public static final double LANDING_BOOST = .421;
		public static final double VEHICLE_MINECART = .63;
		public static final double VEHICLE_PIG = .22;
		public static final double VEHICLE_HORSE = .79;
		public static final double VEHICLE_BOAT = .38;
		public static final int MAX_SNEAKS_PER_SECOND = 15;
		public static final int MAX_ITEM_TOGGLE_PER_SECOND = 15;
	}

	class SpeedData
	{
		double lastYDelta;
		double lastLimit;
		boolean wasOnGround;

		public SpeedData(double y, double speed, boolean ground)
		{
			this.lastYDelta = y;
			this.lastLimit = speed;
			this.wasOnGround = ground;
		}
	}

	public Speed()
	{
		super("Speed");
		EventManager.onPlayerMove.add(new EventInfo(this, 0));
		EventManager.onMount.add(new EventInfo(this, 1));
		EventManager.onEntityDamageByEntity.add(new EventInfo(this, 2));
		PacketManager.addListener(this);
		Bukkit.getScheduler().runTaskTimer(AntiCheat.antiCheat(), () ->
		{
			for (Player p : Bukkit.getOnlinePlayers())
			{
				Counter.remove(p.getUniqueId(), this.name + ".toggle.sneak.start");
				Counter.remove(p.getUniqueId(), this.name + ".toggle.sneak.stop");
				Counter.remove(p.getUniqueId(), this.name + ".toggle.item.place");
				Counter.remove(p.getUniqueId(), this.name + ".toggle.item.dig");
			}
		}, 0L, 20L);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				PlayerMoveEvent e = (PlayerMoveEvent) ev;
				Player p = e.getPlayer();
				Location from = e.getFrom();
				Location to = e.getTo();
				if ((from.getX() == to.getX()) && (from.getY() == to.getY()) && (from.getZ() == to.getZ()) || PlayerUtils.wasFlightAllowed(p) || PlayerUtils.wasFlying(p) || p.getGameMode() == GameMode.CREATIVE || MovementUtils.teleported(p))
					return;
				UUID uid = p.getUniqueId();
				SpeedData data = this.datas.getOrDefault(uid, new SpeedData(0, 0, false));
				double speed = MathUtils.getHorizontalDistance(e.getFrom(), e.getTo());
				double max_speed = .0D;
				double yDiff = calculateYDelta(p, from);
				double yDiffCurrent = to.getY() - from.getY();
				double yfloor = MathUtils.round(yDiff, 3, RoundingMode.FLOOR);
				double yfloor_c = MathUtils.round(yDiffCurrent, 3, RoundingMode.FLOOR);
				boolean isOnGround = GroundChecks.isOnGround(p.getLocation());
				if (p.isSprinting())
					Cooldowns.set(uid, this.name + ".sprinting", 2);
				boolean wasSprinting = !Cooldowns.isCooldownEnded(uid, this.name + ".sprinting");
				boolean lily = BlockUtils.isMaterialSurround(to, .3, false, Material.WATER_LILY, Material.CARPET, Material.SOUL_SAND);
				int groundticks = Counter.getCount(uid, "groundTicks");
				int airticks = Counter.getCount(uid, "airTicks");
				String state = "";
				if (p.isInsideVehicle() || !Cooldowns.isCooldownEnded(uid, this.name + ".mount"))
				{
					if (Counter.increment1AndGetCount(uid, this.name + ".vehicleTicks", -1) <= 2)
						max_speed = 6;
					else
					{
						if (p.getVehicle().getType().name().toLowerCase().contains("minecart"))
							max_speed = Limits.VEHICLE_MINECART;
						else if (p.getVehicle().getType() == EntityType.BOAT)
							max_speed = Limits.VEHICLE_BOAT;
						else if (p.getVehicle().getType() == EntityType.PIG)
							max_speed = Limits.VEHICLE_PIG;
						else if (p.getVehicle().getType() == EntityType.HORSE)
							max_speed = Limits.VEHICLE_HORSE;
					}
				}
				else
				{
					Counter.remove(uid, this.name + ".vehicleTicks");
					if ((BlockUtils.isLiquidNearby(from) || BlockUtils.isLiquidNearby(from.clone().add(0, 1, 0))) && !lily)
						max_speed = Limits.WATER;
					else if (p.isSneaking() && !wasSprinting)
						max_speed = Limits.SNEAK;
					else if (BlockUtils.isMaterialSurround(p.getLocation(), 0.3, false, Material.WEB))
						max_speed = Limits.WEB_HORIZONTAL;
					else if ((airticks >= 10 || isOnGround) && Math.abs(YMap.get(p).getY(0) - yfloor_c) <= .02) // Start-jumping Boost
					{
						max_speed = Limits.JUMPSTART_BOOST;
						state = "Jumpstart Boost";
					}
					else if (isOnGround)
					{
						if (groundticks <= 5)
						{
							if (PlayerUtils.hasBlockAbove(p)) // Ground Boost
							{
								max_speed = Limits.BLOCKABOVE_GROUND;
								state = "Blockabove Ground";
							}
							else
							{
								max_speed = Limits.LANDING_BOOST;
								state = "Landing boost";
							}
						}
						else
						{
							max_speed = Limits.GROUND;
							state = "Ground";
						}
					}
					else if (p.getLocation().getBlock().getType() == Material.SOUL_SAND)
						max_speed = Limits.SOULSAND;
					else
					{
						if (PlayerUtils.hasBlockAbove(p))
						{
							max_speed = Limits.BLOCKABOVE_AIR;
							state = "Blockabove Air";
						}
						else
						{
							max_speed = Limits.AIR;
							state = "Air";
						}
					}
					if (getCurrentUsingItemName(p) != null && Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".protection.startUsingItem")) // Blocking sword, eating food etc...
					{
						double descend = (from.getY() - to.getY());
						if (!GroundChecks.isOnGround(p))
							Cooldowns.set(uid, this.name + ".item_air", 10);
						boolean air = !Cooldowns.isCooldownEnded(uid, this.name + ".item_air");
						if (groundticks > 0)
							if (groundticks <= 3)
								max_speed = .15;
							else if (groundticks <= 10)
								max_speed = .115;
							else
								max_speed = .0875;
						if (air)
							if (yfloor_c > 0)
								max_speed += .13;
							else if (descend > 0)
								max_speed += .106;
						if (p.hasPotionEffect(PotionEffectType.SPEED))
							max_speed *= .1F * PlayerUtils.getPotionEffectLevel(p, PotionEffectType.SPEED);
					}
					if (BlockUtils.hasSteppableNearby(p.getLocation()))
						Cooldowns.set(uid, this.name + ".steppable", 5);
					if (!Cooldowns.isCooldownEnded(uid, this.name + ".steppable"))
						if (yDiff == .5 && yDiffCurrent == .5)
						{
							max_speed = 0.7;
							Cooldowns.set(uid, this.name + ".steppableBoost", 3);
						}
						else if (Cooldowns.isCooldownEnded(uid, this.name + ".steppableBoost"))
							max_speed = .281;
						else
							max_speed = .7;
					Location below = e.getPlayer().getLocation().clone().add(0, -0.02, 0);
					if (BlockUtils.isIceNearby(below)) // Player has Ice blocks under the feet
					{
						Cooldowns.set(uid, this.name + ".iceBoost", 6);
						if (PlayerUtils.hasBlockAbove(p))
							max_speed = 1.0D;
						else
							max_speed = .75D;
					}
					// Apply WalkSpeed and Speed PotionEffect modifier //
					float walkspeed = p.getWalkSpeed();
					float walkSpeedCalc = (walkspeed > .2F ? walkspeed - .015F : 0F);
					max_speed += walkSpeedCalc;
					for (PotionEffect effect : p.getActivePotionEffects())
					{
						if (effect.getType().equals(PotionEffectType.SPEED))
							if (isOnGround)
								max_speed += Limits.GROUND * (effect.getAmplifier() + 1);
							else
								max_speed += Limits.AIR * (effect.getAmplifier() + 1);
					}
					if (PlayerUtils.isOnIceAndUnderTrapdoor(p))
						max_speed = 2.0D;
					if (Protections.hasProtection(p, ProtectionType.SPEED))
						max_speed = .65;
				}
				if (speed > max_speed && (speed > 1 || Counter.increment1AndGetCount(uid, this.name + ".violation.speed_limit_normal", 99) >= Lag.correction(p, 3)))
				{
					suspect(p, Math.min(Math.ceil(Math.abs((speed - max_speed) * 100)), 50), "t: unusual", "d: " + MathUtils.round(speed, 4), "s: (" + state + ")");
					Reverter.getInstance("Speed_Unusual").teleport(p);
				}
				else if (isOnGround)
					Reverter.getInstance("Speed_Unusual").setPosition(uid, from);
				if ((p.getFoodLevel() < 6 || p.hasPotionEffect(PotionEffectType.BLINDNESS)) && p.isSprinting())
				{
					suspect(p, 15, "t: illegal", "r: " + (p.hasPotionEffect(PotionEffectType.BLINDNESS) ? "(blindness)" : "(hunger)"));
					Reverter.getInstance("Speed_Illegal").teleport(p);
				}
				else
					Reverter.getInstance("Speed_Illegal").setPosition(uid, from);
				if (MathUtils.getHorizontalDistance(from, to) > 0 || MathUtils.getVerticalDistance(from, to) > 0)
					Cooldowns.set(uid, this.name + ".lastmove", 2);
				Cache.set(uid, this.name + ".position_cache", p.getLocation());
				data.lastLimit = max_speed;
				data.lastYDelta = yfloor;
				data.wasOnGround = isOnGround;
				this.datas.put(uid, data);
				break;
			case 1:
				EntityMountEvent mt = (EntityMountEvent) ev;
				if (!(mt.getEntity() instanceof Player))
					return;
				Cooldowns.set(mt.getEntity().getUniqueId(), this.name + ".mount", 2);
				break;
		}
	}

	private double calculateYDelta(Player p, Location modifier)
	{
		if (modifier.getWorld() != p.getWorld())
			return 0D;
		Location lastPos = Cache.get(p.getUniqueId(), this.name + ".position_cache", p.getLocation());
		Location currentPos = p.getLocation();
		if (lastPos != null && lastPos.getWorld() == currentPos.getWorld())
		{
			double currentY = currentPos.getY();
			return Math.abs(currentY - modifier.getY() - (currentY - lastPos.getY())) * ((currentY - lastPos.getY()) < 0 ? -1 : 1);
		}
		return 0D;
	}

	private String getCurrentUsingItemName(Player p)
	{
		if (NMS.asNMS(p).by()) // A method that return player is using item
		{
			ItemStack is = ((ItemStack) InternalUtils.getField(NMS.asNMS(p), "f"));
			if (is.getItem().d(is) != EnumAnimation.NONE) // A method that returns item has Animation(Blocking, Eating, etc...)
				return is.getItem().d(is).name().toLowerCase();
		}
		return null;
	}

	@Override
	public void onPacketIn(PacketInEvent e)
	{
		Player ply = e.getPlayer();
		if (e.getPacket().getType() == PacketTypeIn.ENTITY_ACTION)
		{
			EntityActionPacket packet = (EntityActionPacket) e.getPacket();
			// TODO Fast Toggle Sneak check
			if (packet.getAction() == EntityAction.START_SNEAKING)
			{
				if (Counter.increment1AndGetCount(ply.getUniqueId(), this.name + ".toggle.sneak.start", -1) > Limits.MAX_SNEAKS_PER_SECOND)
				{
					suspect(ply, 50, "t: toggle", "a: sneak", "p_t: " + packet.getAction());
				}
			}
			else if (packet.getAction() == EntityAction.STOP_SNEAKING)
			{
				if (Counter.increment1AndGetCount(ply.getUniqueId(), this.name + ".toggle.sneak.stop", -1) > Limits.MAX_SNEAKS_PER_SECOND)
				{
					suspect(ply, 50, "t: toggle", "a: sneak", "p_t: " + packet.getAction());
				}
			}
		}
		if (e.getPacket().getType() == PacketTypeIn.BLOCK_PLACE)
		{
			BlockPlacePacket packet = (BlockPlacePacket) e.getPacket();
			if (packet.getBlockFace() == BlockFace.FAILED_PLACE && packet.getPosition().equals(new Vector(-1, -1, -1)) && ply.getItemInHand() != null)
			{
				// TODO Fast Toggle Use check
				String str = ply.getItemInHand().getType().name().toLowerCase();
				if (str.contains("sword") || ply.getItemInHand().getType() == Material.BOW || ply.getItemInHand().getType().isEdible())
				{
					int i = 0;
					if ((i = Counter.increment1AndGetCount(ply.getUniqueId(), this.name + ".toggle.item.place", -1)) > Limits.MAX_ITEM_TOGGLE_PER_SECOND)
					{
						suspect(ply, 50, "t: toggle", "a: item", "p_t: place", "p_p_s: " + i);
						Reverter.getInstance("Speed_NoSlow_Packet").teleport(ply);
					}
					Cooldowns.set(ply.getUniqueId(), this.name + ".protection.startUsingItem", 15);
				}
			}
		}
		if (e.getPacket().getType() == PacketTypeIn.BLOCK_DIG)
		{
			BlockDigPacket packet = (BlockDigPacket) e.getPacket();
			if (packet.getAction() == DigAction.OTHER_INTERACT && packet.getPosition().equals(new Vector()) && packet.getBlockFace() == BlockFace.BOTTOM)
			{
				// TODO Fast Toggle Use check
				String str = ply.getItemInHand().getType().name().toLowerCase();
				if (str.contains("sword") || ply.getItemInHand().getType() == Material.BOW || ply.getItemInHand().getType().isEdible())
				{
					int i = 0;
					if ((i = Counter.increment1AndGetCount(ply.getUniqueId(), this.name + ".toggle.item.dig", -1)) > Limits.MAX_ITEM_TOGGLE_PER_SECOND)
					{
						suspect(ply, 50, "t: toggle", "a: item", "p_t: dig", "p_p_s: " + i);
						Reverter.getInstance("Speed_NoSlow_Packet").teleport(ply);
					}
				}
			}
		}
	}

	@Override
	public void onPacketOut(PacketOutEvent e)
	{
	}
}
