package com.eric0210.nomorecheats.checks.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.Violation;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.GroundChecks;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.MathUtils;
import com.eric0210.nomorecheats.api.util.PlayerUtils;
import com.eric0210.nomorecheats.api.util.TimeUtils;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.AverageCollector;

public class Scaffold extends Check implements EventListener
{
	public static HashMap<UUID, AverageCollector> timingsConsistencyCollectors = new HashMap<>();
	public static double ROTATION_INSTANT_PITCH = 35.0D;
	public boolean cancelEvent;
	public boolean cooldown = false;

	public Scaffold()
	{
		super("Scaffold");
		EventManager.onPlayerMove.add(new EventInfo(this, 0));
		EventManager.onBlockPlace.add(new EventInfo(this, 1));
		ROTATION_INSTANT_PITCH = getConfig().getValue("PitchMovementThreshold", ROTATION_INSTANT_PITCH);
		this.cancelEvent = getConfig().getValue("CancelBlockPlace", false);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				PlayerMoveEvent e1 = (PlayerMoveEvent) ev;
				Player ply = e1.getPlayer();
				Location from = e1.getFrom();
				Location to = e1.getTo();
				if (Math.abs(from.getPitch() - to.getPitch()) > ROTATION_INSTANT_PITCH)
				{
					Cooldowns.set(ply.getUniqueId(), this.name + ".extreme-pitch", 5);
				}
				break;
			case 1:
				BlockPlaceEvent e = (BlockPlaceEvent) ev;
				Player p = e.getPlayer();
				// Block placed = e.getBlockPlaced();
				UUID uid = p.getUniqueId();
				if (PlayerUtils.wasFlightAllowed(p) || PlayerUtils.wasFlying(p))
					return;
				if (!Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".cooldown"))
					e.setCancelled(true);
				Block block = e.getBlock();
				Block blockAgainst = e.getBlockAgainst();
				if (block == null || blockAgainst == null)
					return;
				ArrayList<String> scaffoldReason = new ArrayList<>();
				int scaffoldVL = 0;
				if (blockAgainst.getFace(block) == BlockFace.UP && blockAgainst.getY() < p.getLocation().getY() && p.getLocation().clone().subtract(0, .25, 0).getBlock().equals(block))
				{
					long timeDelta = TimeUtils.getTimeDiff(uid, this.name + ".lastTowering", 1000L);
					double rotation_delta = Math.hypot(getScaffoldRotationDifference(p, block, blockAgainst)[0], getScaffoldRotationDifference(p, block, blockAgainst)[1]);
					if (rotation_delta < 1 && Counter.incrementAndGetCount(uid, this.name + ".vrotation", rotation_delta < 0.5 ? 2 : 1, 99) > 10)
					{
						scaffoldReason.add("rotation");
						scaffoldVL += 5;
					}
					if (timeDelta < 300L && Counter.increment1AndGetCount(uid, this.name + ".time.normal", 200) > 3)
					{
						scaffoldReason.add("time");
						scaffoldVL += 5;
					}
					TimeUtils.putCurrentTime(uid, this.name + ".lastTowering");
				}
				if (GroundChecks.isOnGround(p) && (block.getRelative(BlockFace.DOWN).isEmpty() || block.getRelative(BlockFace.DOWN).isLiquid()) && block.getY() == blockAgainst.getY() && p.getLocation().clone().subtract(0, 0.25, 0).getBlock().equals(block))
				{
					AverageCollector timings_consistency_collector = timingsConsistencyCollectors.getOrDefault(uid, new AverageCollector());
					double yaw_rotation_delta = getScaffoldRotationDifference(p, block, blockAgainst)[0];
					int vl = 0;
					if (yaw_rotation_delta < .5)
						vl = 2;
					else if (yaw_rotation_delta < 1.0)
						vl = 1;
					else if ((p.isSprinting() || p.hasPotionEffect(PotionEffectType.SPEED)) && !p.isSneaking())
						vl *= 2;
					long timeDelta = TimeUtils.getTimeDiff(uid, this.name + ".lastBridging", 1000L);
					timings_consistency_collector.add(timeDelta);
					double timings_consistency = timings_consistency_collector.getMax() - timings_consistency_collector.getMin();
					if (!Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".extreme-pitch"))
					{
						scaffoldReason.add("pitch");
						scaffoldVL += 50;
					}
					if (vl > 0 && Counter.incrementAndGetCount(uid, this.name + ".rotations.normal", vl, 99) > 10)
					{
						scaffoldReason.add("rotation");
						scaffoldVL += 5;
					}
					if (timeDelta < 350 && timings_consistency <= 1L && Counter.increment1AndGetCount(uid, this.name + ".time.consistency", 400) > 5)
					{
						scaffoldReason.add("consistent_time");
						scaffoldVL += 2;
					}
					if (timeDelta < 300L && Counter.increment1AndGetCount(uid, this.name + ".time.normal", 200) > 5)
					{
						scaffoldReason.add("time");
						scaffoldVL += 5;
					}
					TimeUtils.putCurrentTime(uid, this.name + ".lastBridging");
					if (timings_consistency_collector.getCount() > 10)
						timings_consistency_collector.reset();
					timingsConsistencyCollectors.put(uid, timings_consistency_collector);
				}
				if (!scaffoldReason.isEmpty())
				{
					suspect(p, scaffoldVL, Violation.convertTagListtoTag(scaffoldReason));
					if (this.cancelEvent)
					{
						e.setCancelled(true);
						if (this.cooldown)
							Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 10);
					}
				}
				if (p.getLocation().clone().subtract(0, 0.25, 0).getBlock().equals(block))
					Counter.increment1AndGetCount(uid, this.name + ".vertical", 20);
				Counter.increment1AndGetCount(uid, this.name + ".horizontal", 20);
				break;
		}
	}

	private float[] getScaffoldRotationDifference(Player p, Block b, Block ba)
	{
		Vector player_rotation = new Vector(p.getLocation().getYaw(), p.getLocation().getPitch(), 0);
		Vector expected_rotation = getScaffoldRotation(p, ba, getBlockFace(b, ba));
		return new float[]
		{
				MathUtils.getRotationDifference0(player_rotation, expected_rotation)[0], MathUtils.getRotationDifference0(player_rotation, expected_rotation)[1]
		};
	}

	private Vector getScaffoldRotation(Player player, Block block_against, BlockFace target_face)
	{
		Location playerPos = player.getLocation();
		Vector targetPos = block_against.getLocation().toVector().add(new Vector(target_face.getModX() * .5, target_face.getModY() * .5, target_face.getModZ() * .5));
		double deltaX = targetPos.getX() - playerPos.getX() + .5;
		double deltaY = targetPos.getY() - (playerPos.getY() + player.getEyeHeight()) + .5;
		double deltaZ = targetPos.getZ() - playerPos.getZ() + .5;
		double dist = MathUtils.getDistance(deltaX, deltaZ);
		float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
		float pitch = (float) Math.toDegrees(-Math.atan2(deltaY, dist));
		return new Vector(MathUtils.clamp180F(yaw), MathUtils.clamp180F(pitch), 0);
	}

	public static final BlockFace getBlockFace(Block place, Block placeAgainst)
	{
		BlockFace[] bfs = BlockFace.values();
		for (BlockFace face : bfs)
		{
			Block b = placeAgainst.getRelative(face);
			if (b != null && b.getType() == place.getType() && b.getLocation().equals(place.getLocation()))
			{
				return face;
			}
		}
		return BlockFace.SELF;
	}
}
