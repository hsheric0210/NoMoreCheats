package com.eric0210.nomorecheats.checks.player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.eric0210.nomorecheats.AntiCheat;
import com.eric0210.nomorecheats.Logging;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketInEvent;
import com.eric0210.nomorecheats.api.packet.PacketListener;
import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.packet.PacketOutEvent;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.packet.enums.BlockFace;
import com.eric0210.nomorecheats.api.packet.packets.in.AbilitiesPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockDigPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockPlacePacket;
import com.eric0210.nomorecheats.api.packet.packets.in.TransactionPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockDigPacket.DigAction;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.AverageCollector;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.TimeUtils;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.TickTasks;
import com.eric0210.nomorecheats.api.util.InternalUtils;
import com.eric0210.nomorecheats.api.util.NMS;
import com.eric0210.nomorecheats.api.util.PlayerUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class FalsePackets extends Check implements PacketListener, EventListener
{
	public HashMap<UUID, AverageCollector> badPackets_B_Average_Packets = new HashMap<>();
	private static int pingspoof_normal_verbose_limit;
	private static int pingspoof_normal_delta_limit;
	public boolean use_ping_modifier = false;
	private int pingspoof_scheduler_required_tick = 20;
	private int requiredKeepAliveDelayTicks = 40;
	private final String pingspoof_keepalive_delay = this.name + ".pingspoof.keepalive.delay";
	private final String pingspoof_keepalive_transaction_identifier = this.name + ".pingspoof.keepalive.packet";
	private final String pingspoof_keepalive_attempts = this.name + ".pingspoof.keepalive.delay.attempts";
	public final String pingspoof_keepalive_detections = this.name + ".pingspoof.keepalive.delay.detections";

	public FalsePackets()
	{
		super("FalsePackets");
		PacketManager.addListener(this);
		EventManager.onPlayerMove.add(new EventInfo(this, 0));
		pingspoof_normal_verbose_limit = getConfig().getValue("PingSpoof.Threshold", 94);
		pingspoof_normal_delta_limit = getConfig().getValue("PingSpoof.PingDeltaThreshold", 350);
		this.use_ping_modifier = getConfig().getValue("PingCalculation", false);
		TickTasks.addTask(() -> checkPingspoofTick(Arrays.asList(Bukkit.getOnlinePlayers())));
	}

	private void checkPingspoofTick(List<Player> players)
	{
		Iterator<Player> itr;
		Player p;
		if (this.pingspoof_scheduler_required_tick == 0)
		{
			this.pingspoof_scheduler_required_tick = 20;
			for (itr = players.iterator(); itr.hasNext();)
			{
				p = itr.next();
				if (!Cooldowns.isCooldownEnded(p.getUniqueId(), "movement"))
				{
					int currentPing = PlayerUtils.getPing(p);
					if (Cache.contains(p.getUniqueId(), this.name + ".pingspoof.scheduler.lastping"))
					{
						int lastPing = Cache.get(p.getUniqueId(), this.name + ".pingspoof.scheduler.lastping", currentPing);
						int delta = Math.abs(currentPing - lastPing);
						if (delta <= 5 && currentPing >= 400 && lastPing >= 400)
							if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".scheduler.attempts", 240) >= 12)
								suspect(p, 5, "t: ping-spoof", "d: " + delta);
					}
					Cache.set(p.getUniqueId(), this.name + ".pingspoof.scheduler.lastping", currentPing);
				}
			}
		}
		else
			for (itr = players.iterator(); itr.hasNext();)
			{
				p = itr.next();
				if (PlayerUtils.getPing(p) >= 200 && hasPingspoofKeepaliveDetections(p))
				{
					Cooldowns.set(p.getUniqueId(), this.name + ".pingspoof.packets", 1200);
					suspect(p, 1, "t: ping-spoof");
				}
			}
		--this.pingspoof_scheduler_required_tick;
	}

	@Override
	public void onPacketIn(PacketInEvent e)
	{
		Player p = e.getPlayer();
		PacketIn pkt = e.getPacket();
		if (pkt.getType() == PacketTypeIn.ABILITIES)
		{
			AbilitiesPacket packet = (AbilitiesPacket) pkt;
			if (packet.isFlying() && !p.getAllowFlight())
			{
				suspect(p, 1, "t: abilities", "a: flying");
				e.setCancelled(true);
				return;
			}
			// if (packet.e() && (!p.getAllowFlight() || p.getGameMode() !=
			// GameMode.CREATIVE) && !p.isFlying())
			// {
			// detected(p, 1, "invalid-abilities", "FlightEnabled");
			// pe.setCancelled(true);
			// return;
			// }
		}
		if (pkt.getType() == PacketTypeIn.BLOCK_DIG)
		{
			BlockDigPacket packet = (BlockDigPacket) pkt;
			DigAction action = packet.getAction();
			if (action == DigAction.INVALID)
			{
				suspect(p, 1, "t: dig", "r: wrong action");
				e.setCancelled(true);
				return;
			}
			BlockFace face = packet.getBlockFace();
			if (face == BlockFace.INVALID)
			{
				suspect(p, 1, "t: dig", "r: wrong face");
				e.setCancelled(true);
				return;
			}
			int x = packet.getPosition().getBlockX(), y = packet.getPosition().getBlockY(), z = packet.getPosition().getBlockZ();
			Block b = p.getWorld().getBlockAt(x, y, z);
			if (((!b.getType().isSolid() && b.isLiquid())) && (action.name().toLowerCase().contains("digging")))
			{
				suspect(p, 1, "t: dig", "r: non-existent block", "x: " + x + ", y: " + y + ", z: " + z + ", ( m: " + InternalUtils.serializeEnum(b.getType()) + ", a: " + InternalUtils.serializeEnum(action));
				e.setCancelled(true);
				return;
			}
		}
		if (pkt.getType() == PacketTypeIn.BLOCK_PLACE)
		{
			BlockPlacePacket packet = (BlockPlacePacket) pkt;
			int x = packet.getPosition().getBlockX(), y = packet.getPosition().getBlockY(), z = packet.getPosition().getBlockZ();
			ItemStack item = packet.getItemStack();
			BlockFace face = packet.getBlockFace();
			if (face == null)
			{
				suspect(p, 1, "t: place", "r: wrong face");
				e.setCancelled(true);
				return;
			}
			if (item != null && item.getType().isBlock())
			{
				if (face != BlockFace.FAILED_PLACE)
				{
					Location loc = new Location(p.getWorld(), x + face.getXMod(), y + face.getYMod(), z + face.getZMod());
					if (!loc.getBlock().isEmpty() && !loc.getBlock().isLiquid() && !BlockUtils.compareType(loc.getBlock(), BlockUtils.Step_Blocks) && !BlockUtils.compareType(loc.getBlock(), BlockUtils.Stair_Blocks) && !BlockUtils.compareType(loc.getBlock(), BlockUtils.Buggy_Blocks))
					{
						suspect(p, 1, "t: place", "r: (already-existent block)", "b: " + InternalUtils.serializeEnum(loc.getBlock().getType()));
						e.setCancelled(true);
						return;
					}
					if (loc.equals(p.getLocation()) || loc.equals(p.getEyeLocation()))
					{
						suspect(p, 1, "t: place", "r: (player collision)", "b: " + InternalUtils.serializeEnum(loc.getBlock().getType()));
						e.setCancelled(true);
						return;
					}
				}
			}
		}
		if (pkt.getType() == PacketTypeIn.KEEP_ALIVE)
		{
			UUID uid = p.getUniqueId();
			Logging.debug("[MorePackets] Player " + p.getName() + " KeepAlive recive. delay: " + (this.requiredKeepAliveDelayTicks - Cooldowns.get(p.getUniqueId(), this.name + ".keepaliveDelay")) + ", required: " + this.requiredKeepAliveDelayTicks);
			if (!Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".keepaliveDelay") && Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".keepaliveDelay", 60) > 3)
			{
				double keepaliveTicks = (this.requiredKeepAliveDelayTicks - Cooldowns.get(p.getUniqueId(), this.name + ".keepaliveDelay"));
				suspect(p, 1, "t: keepalive", "p_d: " + keepaliveTicks, "p_d_r: " + this.requiredKeepAliveDelayTicks);
				e.setCancelled(true);
			}
			Cooldowns.set(p.getUniqueId(), this.name + ".keepaliveDelay", this.requiredKeepAliveDelayTicks);
			TimeUtils.putCurrentTime(uid, this.name + ".pingspoof.packet.keepalive");
			Cooldowns.set(uid, this.pingspoof_keepalive_delay, 2);
		}
		if (pkt.getType() == PacketTypeIn.TRANSACTION)
		{
			TransactionPacket packet = (TransactionPacket) pkt;
			UUID uid = p.getUniqueId();
			if (Cooldowns.isCooldownEnded(uid, this.pingspoof_keepalive_delay) && packet.getActionId() == Cache.get(uid, this.pingspoof_keepalive_transaction_identifier, -1))
			{
				if (Counter.increment1AndGetCount(uid, this.pingspoof_keepalive_attempts, 240) >= 3)
					Counter.increment1AndGetCount(uid, this.pingspoof_keepalive_detections, -1);
			}
		}
	}

	@Override
	public void onPacketOut(PacketOutEvent e)
	{
		if (e.getPacket().getType() == PacketTypeOut.KEEP_ALIVE)
		{
			short s = (short) ((short) new Random().nextInt(32767) * (new Random().nextBoolean() ? 1 : -1));
			Bukkit.getScheduler().runTaskLater(AntiCheat.antiCheat(), () -> sendTransactionPacket(e.getPlayer(), new com.eric0210.nomorecheats.api.packet.packets.out.TransactionPacket(NMS.asNMS(e.getPlayer()), 0, s, false), s), 1L);
		}
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		PlayerMoveEvent e = (PlayerMoveEvent) ev;
		Player player = e.getPlayer();
		UUID uuid2 = player.getUniqueId();
		double dist = e.getFrom().distance(e.getTo());
		if (player.getVehicle() == null && PlayerUtils.getPing(player) >= 400 && dist >= .215)
		{
			int i = 20;
			String str = this.name + ".pingspoof.moves";
			int j = Counter.increment1AndGetCount(uuid2, str, i);
			if ((j + Counter.getResetCooldown(uuid2, str) - 1 == i) && j == i)
			{
				if (Counter.increment1AndGetCount(uuid2, this.name + ".pingspoof.event.attempts", 200) >= 9)
				{
					Counter.remove(uuid2, this.name + ".pingspoof.event.attempts");
					suspect(player, 5, "t: ping-spoof");
				}
			}
		}
		// Old Ping-Spoof check
		if (TimeUtils.contains(uuid2, this.name + ".pingspoof.old.moveperiod"))
		{
			double movePacket_Period = TimeUtils.getTimeDiff(uuid2, this.name + ".pingspoof.old.moveperiod", 0);
			double keepAlivePacket_Ping = PlayerUtils.getPing(player);
			double diff = Math.abs(movePacket_Period - keepAlivePacket_Ping);
			if (movePacket_Period != keepAlivePacket_Ping && diff > pingspoof_normal_delta_limit)
			{
				TimeUtils.putCurrentTime(uuid2, this.name + ".pingspoof.old.moveperiod");
				int minVerbose = pingspoof_normal_verbose_limit;
				if (Counter.increment1AndGetCount(uuid2, this.name + ".pingspoof.old.verbose", 1200) > minVerbose)
				{
					suspect(player, 10, "t: ping-spoof", "mp_p: " + movePacket_Period, "kap_p: " + keepAlivePacket_Ping);
					Counter.increment1AndGetCount(uuid2, "pingspoof.detections", -1);
					Counter.remove(uuid2, this.name + ".pingspoof.old.verbose");
				}
			}
			else
			{
				TimeUtils.putCurrentTime(uuid2, this.name + ".pingspoof.old.moveperiod");
			}
		}
		else
		{
			TimeUtils.putCurrentTime(uuid2, this.name + ".pingspoof.old.moveperiod");
		}
	}

	private boolean hasPingspoofKeepaliveDetections(Player p)
	{
		if (Counter.getCount(p.getUniqueId(), this.pingspoof_keepalive_detections) != 0)
		{
			Counter.remove(p.getUniqueId(), this.pingspoof_keepalive_detections);
			return true;
		}
		return false;
	}

	private void sendTransactionPacket(Player owner, com.eric0210.nomorecheats.api.packet.packets.out.TransactionPacket transaction, short s)
	{
		try
		{
			PacketManager.sendPacket(owner, transaction);
			Cache.set(owner.getUniqueId(), this.pingspoof_keepalive_transaction_identifier, s);
		}
		catch (Exception ex)
		{
			// ex.printStackTrace();
			Cache.remove(owner.getUniqueId(), this.pingspoof_keepalive_transaction_identifier);
		}
	}

	public static boolean checkPingspoofPacketsViolation(UUID uid)
	{
		return !Cooldowns.isCooldownEnded(uid, "FalsePackets.pingspoof.packets");
	}
}
