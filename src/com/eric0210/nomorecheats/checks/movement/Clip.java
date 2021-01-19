package com.eric0210.nomorecheats.checks.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Door;
import org.bukkit.material.Gate;
import org.bukkit.material.TrapDoor;
import org.bukkit.material.Directional;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.packet.PacketInEvent;
import com.eric0210.nomorecheats.api.packet.PacketListener;
import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.packet.PacketOutEvent;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.packets.in.FlyingPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.PositionPacket;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.MovementUtils;
import com.eric0210.nomorecheats.api.util.NMS;
import com.eric0210.nomorecheats.api.util.PlayerUtils;

// Thanks, DaedalusAAC!
public class Clip extends Check implements EventListener, PacketListener
{
	private static ArrayList<Material> allowed = new ArrayList<>();
	private static ArrayList<Material> semi = new ArrayList<>();
	private static HashSet<UUID> blacklist = new HashSet<>();
	private static HashMap<UUID, Location> lastPositions = new HashMap<>();
	/*
	 * private final ImmutableSet<Material> blockedPearlTypes = Sets.immutableEnumSet( Material.THIN_GLASS, new Material[] { Material.IRON_FENCE, Material.FENCE, Material.NETHER_FENCE, Material.FENCE_GATE, Material.ACACIA_STAIRS,
	 * Material.BIRCH_WOOD_STAIRS, Material.BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.DARK_OAK_STAIRS, Material.JUNGLE_WOOD_STAIRS, Material.NETHER_BRICK_STAIRS, Material.QUARTZ_STAIRS, Material.SANDSTONE_STAIRS, Material.SMOOTH_STAIRS,
	 * Material.SPRUCE_WOOD_STAIRS, Material.WOOD_STAIRS } );
	 */
	static
	{
		allowed.add(Material.SIGN);
		allowed.add(Material.SIGN_POST);
		allowed.add(Material.WALL_SIGN);
		allowed.add(Material.SUGAR_CANE_BLOCK);
		allowed.add(Material.WHEAT);
		allowed.add(Material.POTATO);
		allowed.add(Material.CARROT);
		allowed.add(Material.STEP);
		allowed.add(Material.AIR);
		allowed.add(Material.WOOD_STEP);
		allowed.add(Material.SOUL_SAND);
		allowed.add(Material.CARPET);
		allowed.add(Material.STONE_PLATE);
		allowed.add(Material.WOOD_PLATE);
		allowed.add(Material.LADDER);
		allowed.add(Material.CHEST);
		allowed.add(Material.WATER);
		allowed.add(Material.STATIONARY_WATER);
		allowed.add(Material.LAVA);
		allowed.add(Material.STATIONARY_LAVA);
		allowed.add(Material.REDSTONE_COMPARATOR);
		allowed.add(Material.REDSTONE_COMPARATOR_OFF);
		allowed.add(Material.REDSTONE_COMPARATOR_ON);
		allowed.add(Material.IRON_PLATE);
		allowed.add(Material.GOLD_PLATE);
		allowed.add(Material.DAYLIGHT_DETECTOR);
		allowed.add(Material.STONE_BUTTON);
		allowed.add(Material.WOOD_BUTTON);
		allowed.add(Material.HOPPER);
		allowed.add(Material.RAILS);
		allowed.add(Material.ACTIVATOR_RAIL);
		allowed.add(Material.DETECTOR_RAIL);
		allowed.add(Material.POWERED_RAIL);
		allowed.add(Material.TRIPWIRE_HOOK);
		allowed.add(Material.TRIPWIRE);
		allowed.add(Material.SNOW_BLOCK);
		allowed.add(Material.REDSTONE_TORCH_OFF);
		allowed.add(Material.REDSTONE_TORCH_ON);
		allowed.add(Material.DIODE_BLOCK_OFF);
		allowed.add(Material.DIODE_BLOCK_ON);
		allowed.add(Material.DIODE);
		allowed.add(Material.SEEDS);
		allowed.add(Material.MELON_SEEDS);
		allowed.add(Material.PUMPKIN_SEEDS);
		allowed.add(Material.DOUBLE_PLANT);
		allowed.add(Material.LONG_GRASS);
		allowed.add(Material.WEB);
		allowed.add(Material.SNOW);
		allowed.add(Material.FLOWER_POT);
		allowed.add(Material.BREWING_STAND);
		allowed.add(Material.CAULDRON);
		allowed.add(Material.CACTUS);
		allowed.add(Material.WATER_LILY);
		allowed.add(Material.RED_ROSE);
		allowed.add(Material.ENCHANTMENT_TABLE);
		allowed.add(Material.ENDER_PORTAL_FRAME);
		allowed.add(Material.PORTAL);
		allowed.add(Material.ENDER_PORTAL);
		allowed.add(Material.ENDER_CHEST);
		allowed.add(Material.NETHER_FENCE);
		allowed.add(Material.NETHER_WARTS);
		allowed.add(Material.REDSTONE_WIRE);
		allowed.add(Material.LEVER);
		allowed.add(Material.YELLOW_FLOWER);
		allowed.add(Material.CROPS);
		allowed.add(Material.WATER);
		allowed.add(Material.LAVA);
		allowed.add(Material.SKULL);
		allowed.add(Material.TRAPPED_CHEST);
		allowed.add(Material.FIRE);
		allowed.add(Material.BROWN_MUSHROOM);
		allowed.add(Material.RED_MUSHROOM);
		allowed.add(Material.DEAD_BUSH);
		allowed.add(Material.SAPLING);
		allowed.add(Material.TORCH);
		allowed.add(Material.MELON_STEM);
		allowed.add(Material.PUMPKIN_STEM);
		allowed.add(Material.COCOA);
		allowed.add(Material.BED);
		allowed.add(Material.BED_BLOCK);
		allowed.add(Material.PISTON_EXTENSION);
		allowed.add(Material.PISTON_MOVING_PIECE);
		semi.add(Material.IRON_FENCE);
		semi.add(Material.THIN_GLASS);
		semi.add(Material.STAINED_GLASS_PANE);
		semi.add(Material.COBBLE_WALL);
	}

	public Clip()
	{
		super("Clip");
		PacketManager.addListener(this);
		EventManager.onPlayerDeath.add(new EventInfo(this, 0));
		EventManager.onPlayerRespawn.add(new EventInfo(this, 1));
		EventManager.onPlayerTeleport.add(new EventInfo(this, 2));
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				PlayerDeathEvent e3 = (PlayerDeathEvent) ev;
				blacklist.add(e3.getEntity().getUniqueId());
				break;
			case 1:
				PlayerRespawnEvent e4 = (PlayerRespawnEvent) ev;
				blacklist.add(e4.getPlayer().getUniqueId());
				break;
			case 2:
				PlayerTeleportEvent e5 = (PlayerTeleportEvent) ev;
				if (e5.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN)
					blacklist.add(e5.getPlayer().getUniqueId());
				break;
		}
	}

	@Override
	public void onPacketIn(PacketInEvent e)
	{
		if (e.getPacket().getType() == PacketTypeIn.FLYING)
		{
			FlyingPacket packet = (FlyingPacket) e.getPacket();
			Player p = e.getPlayer();
			UUID playerId = p.getUniqueId();
			Location to = packet.getTo().toLocation(p.getWorld());
			if (!packet.hasPos() || p.isDead())
				return;

			Location lastpos = lastPositions.containsKey(playerId) ? lastPositions.get(playerId) : to;
			Location pos = to;

			if (pos.getY() < 0 || pos.getY() > 256)
				return;

			if (!lastPositions.containsKey(playerId))
				lastPositions.put(playerId, pos);
			if (PlayerUtils.wasFlightAllowed(p) || PlayerUtils.wasFlying(p) || p.getGameMode().equals(GameMode.CREATIVE) || p.getVehicle() != null || p.isInsideVehicle() || MovementUtils.teleported(p))
				blacklist.add(p.getUniqueId());

			if (lastpos.getWorld() == pos.getWorld() && !blacklist.contains(playerId) && lastpos.distance(pos) > 10.0D)
			{
				if (pos.getBlock().getType().isSolid() || (pos.clone().add(0.0D, 1.0D, 0.0D).getBlock().getType().isSolid() && p.getVehicle() == null))
				{
					if (!pos.add(0, 1, 0).getBlock().getType().name().toLowerCase().contains("doors"))
					{
						if (!lastpos.equals(pos))
							revert(p, lastpos);

						suspect(p, 5, "t: teleport", "from: " + lastpos.toString(), "to: " + pos.toString(), "d: " + lastpos.distance(pos), "b: " + pos.getBlock().getType().name());
						return;
					}
				}

				revert(p, lastpos);
				suspect(p, 3, "t: teleport", "from: " + lastpos.toString(), "to: " + pos.toString());
			}
			else if (isLegit(p, lastpos, pos) || pos.add(0, 1, 0).getBlock().getType().name().toLowerCase().contains("doors"))
			{
				lastPositions.put(playerId, pos);
			}
			else if (lastPositions.containsKey(playerId) && !blacklist.contains(playerId))
			{
				if ((pos.getBlock().getType().isSolid()) || ((pos.clone().add(0.0D, 1.0D, 0.0D).getBlock().getType().isSolid()) && (p.getVehicle() == null)))
					if (!pos.add(0, 1, 0).getBlock().getType().name().toLowerCase().contains("doors"))
					{
						if (!lastpos.equals(pos))
							revert(p, lastpos);

						suspect(p, 5, "t: skip", "from: " + lastpos.toString(), "to: " + pos.toString(), "b: " + pos.getBlock().getType().name());
						return;
					}
				
				revert(p, lastpos);
				suspect(p, 3, "t: skip", "from: " + lastpos.toString(), "to: " + pos.toString());
			}
		}
	}
	
	private void revert(Player p, Location pos)
	{
		PacketManager.sendPacket(p, new PositionPacket(NMS.asNMS(p), pos.clone().add(0, p.getEyeHeight(), 0), false));
	}

	@Override
	public void onPacketOut(PacketOutEvent e)
	{
	}

	public boolean isLegit(Player p, Location lastpos, Location pos)
	{
		if (lastpos.getWorld() != pos.getWorld())
			return true;

		if (blacklist.remove(p.getUniqueId()))
			return true;

		int bbMaxX = Math.max(lastpos.getBlockX(), pos.getBlockX());
		int bbMaxY = Math.max(lastpos.getBlockY(), pos.getBlockY()) + 1;
		int bbMaxZ = Math.max(lastpos.getBlockZ(), pos.getBlockZ());

		int bbMinX = Math.min(lastpos.getBlockX(), pos.getBlockX());
		int bbMinY = Math.min(lastpos.getBlockY(), pos.getBlockY());
		int bbMinZ = Math.min(lastpos.getBlockZ(), pos.getBlockZ());

		if (bbMaxY > 256)
			bbMaxX = 256;

		if (bbMinY > 256)
			bbMinY = 256;

		for (int x = bbMinX; x <= bbMaxX; x++)
			for (int z = bbMinZ; z <= bbMaxZ; z++)
				for (int y = bbMinY; y <= bbMaxY; y++)
				{
					Block block = lastpos.getWorld().getBlockAt(x, y, z);
					if (((y != bbMinY) || (lastpos.getBlockY() == pos.getBlockY())) && (hasPhased(block, lastpos, pos, p)))
						return false;
				}
		return true;
	}

	private boolean hasPhased(Block block, Location lastpos, Location pos, Player p)
	{
		if ((allowed.contains(block.getType())) || (BlockUtils.hasSteppableNearby(block.getLocation())) || (BlockUtils.isClimbableNearby(block.getLocation())) || (block.isLiquid()))
			return false;
		double bbMaxX = Math.max(lastpos.getX(), pos.getX());
		double bbMaxY = Math.max(lastpos.getY(), pos.getY()) + 1.8D;
		double bbMaxZ = Math.max(lastpos.getZ(), pos.getZ());
		double bbMinX = Math.min(lastpos.getX(), pos.getX());
		double bbMinY = Math.min(lastpos.getY(), pos.getY());
		double bbMinZ = Math.min(lastpos.getZ(), pos.getZ());

		double blockMaxX = block.getLocation().getBlockX() + 1;
		double blockMaxY = block.getLocation().getBlockY() + 2;
		double blockMaxZ = block.getLocation().getBlockZ() + 1;
		double blockMinX = block.getLocation().getBlockX();
		double blockMinY = block.getLocation().getBlockY();
		double blockMinZ = block.getLocation().getBlockZ();
		if (blockMinY > bbMinY)
			blockMaxY -= 1.0D;

		if ((block.getType().equals(Material.IRON_DOOR_BLOCK)) || (block.getType().equals(Material.WOODEN_DOOR)))
		{
			Door door = (Door) block.getType().getNewData(block.getData());
			if (door.isTopHalf())
				return false;

			BlockFace facing = door.getFacing();
			if (door.isOpen())
			{
				Block up = block.getRelative(BlockFace.UP);
				boolean hinge;
				if ((up.getType().equals(Material.IRON_DOOR_BLOCK)) || (up.getType().equals(Material.WOODEN_DOOR)))
					hinge = (up.getData() & 0x1) == 1;
				else
					return false;

				if (facing == BlockFace.NORTH)
					facing = hinge ? BlockFace.WEST : BlockFace.EAST;
				else if (facing == BlockFace.EAST)
					facing = hinge ? BlockFace.NORTH : BlockFace.SOUTH;
				else if (facing == BlockFace.SOUTH)
					facing = hinge ? BlockFace.EAST : BlockFace.WEST;
				else
					facing = hinge ? BlockFace.SOUTH : BlockFace.NORTH;
			}
			if (facing == BlockFace.WEST)
				blockMaxX -= 0.8D;
			if (facing == BlockFace.EAST)
				blockMinX += 0.8D;
			if (facing == BlockFace.NORTH)
				blockMaxZ -= 0.8D;
			if (facing == BlockFace.SOUTH)
				blockMinZ += 0.8D;
		}
		else if (block.getType().equals(Material.FENCE_GATE))
		{
			if (((Gate) block.getType().getNewData(block.getData())).isOpen())
				return false;

			BlockFace face = ((Directional) block.getType().getNewData(block.getData())).getFacing();
			if ((face == BlockFace.NORTH) || (face == BlockFace.SOUTH))
			{
				blockMaxX -= 0.2D;
				blockMinX += 0.2D;
			}
			else
			{
				blockMaxZ -= 0.2D;
				blockMinZ += 0.2D;
			}
		}
		else if (block.getType().equals(Material.TRAP_DOOR))
		{
			TrapDoor door = (TrapDoor) block.getType().getNewData(block.getData());
			if (door.isOpen())
				return false;

			if (door.isInverted())
				blockMinY += 0.85D;
			else
				blockMaxY -= (blockMinY > bbMinY ? 0.85D : 1.85D);
		}
		else if ((block.getType().equals(Material.FENCE)) || (semi.contains(block.getType())))
		{
			blockMaxX -= 0.2D;
			blockMinX += 0.2D;
			blockMaxZ -= 0.2D;
			blockMinZ += 0.2D;
			if (((bbMaxX > blockMaxX) && (bbMinX > blockMaxX) && (bbMaxZ > blockMaxZ) && (bbMinZ > blockMaxZ)) || ((bbMaxX < blockMinX) && (bbMinX < blockMinX) && (bbMaxZ > blockMaxZ) && (bbMinZ > blockMaxZ)) || ((bbMaxX > blockMaxX) && (bbMinX > blockMaxX) && (bbMaxZ < blockMinZ) && (bbMinZ < blockMinZ)) || ((bbMaxX < blockMinX) && (bbMinX < blockMinX) && (bbMaxZ < blockMinZ) && (bbMinZ < blockMinZ)))
				return false;

			if (block.getRelative(BlockFace.EAST).getType() == block.getType())
				blockMaxX += 0.2D;
			if (block.getRelative(BlockFace.WEST).getType() == block.getType())
				blockMinX -= 0.2D;
			if (block.getRelative(BlockFace.SOUTH).getType() == block.getType())
				blockMaxZ += 0.2D;
			if (block.getRelative(BlockFace.NORTH).getType() == block.getType())
				blockMinZ -= 0.2D;
		}
		boolean x = lastpos.getX() < pos.getX();
		boolean y = lastpos.getY() < pos.getY();
		boolean z = lastpos.getZ() < pos.getZ();
		double distance = lastpos.distance(pos) - Math.abs(lastpos.getY() - pos.getY());
		if ((distance > 0.5D) && (block.getType().isSolid()))
		{
			return true;
		}
		return ((bbMinX != bbMaxX) && (bbMinY <= blockMaxY) && (bbMaxY >= blockMinY) && (bbMinZ <= blockMaxZ) && (bbMaxZ >= blockMinZ) && (((x) && (bbMinX <= blockMinX) && (bbMaxX >= blockMinX)) || ((!x) && (bbMinX <= blockMaxX) && (bbMaxX >= blockMaxX)))) || ((bbMinY != bbMaxY) && (bbMinX <= blockMaxX) && (bbMaxX >= blockMinX) && (bbMinZ <= blockMaxZ) && (bbMaxZ >= blockMinZ) && (((y) && (bbMinY <= blockMinY) && (bbMaxY >= blockMinY)) || ((!y) && (bbMinY <= blockMaxY) && (bbMaxY >= blockMaxY)))) || ((bbMinZ != bbMaxZ) && (bbMinX <= blockMaxX) && (bbMaxX >= blockMinX) && (bbMinY <= blockMaxY) && (bbMaxY >= blockMinY) && (((z) && (bbMinZ <= blockMinZ) && (bbMaxZ >= blockMinZ)) || ((!z) && (bbMinZ <= blockMaxZ) && (bbMaxZ >= blockMaxZ))));
	}
}
