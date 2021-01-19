package com.eric0210.nomorecheats.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlockUtils
{
	static String[] HalfBlocksArray = new String[]
	{
			"pot", "flower", "steps", "slab", "snow", "detector", "daylight", "comparator", "repeater", "diode", "water", "lava", "ladder", "vine", "carpet", "sign", "pressure", "plate", "button", "mushroom", "torch", "frame", "armor", "banner", "lever", "hook", "redstone", "rail", "brewing", "rose", "skull", "enchantment", "cake", "bed"
	};
	public static HashSet<Material> Fully_Passable_Blocks = new HashSet<>();
//	public static HashSet<Material> fully_passable = new HashSet<Material>();
	public static HashSet<Material> Buggy_Blocks = new HashSet<>();
	public static HashSet<Material> Half_Passable_Blocks = new HashSet<>();
	public static HashSet<Material> Interactable_Blocks = new HashSet<>();
	public static HashSet<Material> Chest_Blocks = new HashSet<>();
	public static HashSet<Material> Plate_Blocks = new HashSet<>();
	public static HashSet<Material> Ice_Blocks = new HashSet<>();
	public static HashSet<Material> Glass_Pane_Blocks = new HashSet<>();
	public static HashSet<Material> Step_Blocks = new HashSet<>();
	public static HashSet<Material> Stair_Blocks = new HashSet<>();
	public static HashSet<Material> Climbable_Blocks = new HashSet<>();
	public static HashSet<Material> Door_Blocks = new HashSet<>();
	public static HashSet<Material> Fallable_Blocks = new HashSet<>();
	public static HashSet<Material> TrapDoor_Blocks = new HashSet<>();
	public static HashSet<Material> Liquid_Blocks = new HashSet<>();
	public static HashSet<Material> Piston_Blocks = new HashSet<>();
	public static HashSet<Material> Fence_Blocks = new HashSet<>();
//	public static HashSet<Material> steppable = new HashSet<Material>();
	private static HashSet<Material> Insta_Break_Blocks = new HashSet<>();
	private static HashSet<Material> Foods = new HashSet<>();
	@SuppressWarnings("unused")
	private static HashSet<Material> INTERACTABLE = new HashSet<Material>();
	private static HashMap<Material, Material> Combo = new HashMap<>();

	public static Set<Block> getBlocksInRadius3D(Location loc, double sens, double y)
	{
		double scanSens = sens / 2;
		HashSet<Block> blocks = new HashSet<>();
		for (double x = -sens; x <= sens; x += scanSens)
		{
			for (double z = -sens; z <= sens; z += scanSens)
			{
				if (loc.getWorld().getChunkAt((int) (x) >> 4, (int) (z) >> 4).isLoaded())
					for (double _y = -sens; _y <= sens; _y += scanSens)
					{
						double ypos = _y + y;
						if (ypos > 256)
							ypos = 256;
						if (ypos < 0)
							ypos = 0;
						blocks.add(loc.clone().add(x, ypos, z).getBlock());
					}
			}
		}
		blocks.removeIf(BlockUtils::isEmpty);
		return blocks;
	}

	public static Set<Block> getBlocksInRadius2D(Location loc, double sens, double y, boolean diagonal)
	{
		Set<Block> sample = new HashSet<>();
		sample.add(getBlockAsync(loc.clone().add(0, y, 0)));
		sample.add(getBlockAsync(loc.clone().add(0, y, sens)));
		sample.add(getBlockAsync(loc.clone().add(sens, y, 0)));
		sample.add(getBlockAsync(loc.clone().add(0, y, -sens)));
		sample.add(getBlockAsync(loc.clone().add(-sens, y, 0)));
		if (diagonal)
		{
			sample.add(getBlockAsync(loc.clone().add(sens, y, sens)));
			sample.add(getBlockAsync(loc.clone().add(-sens, y, -sens)));
			sample.add(getBlockAsync(loc.clone().add(sens, y, -sens)));
			sample.add(getBlockAsync(loc.clone().add(-sens, y, sens)));
		}
		sample.removeIf(BlockUtils::isEmpty);
		sample.removeIf((b) -> !isLoaded(b));
		return sample;
	}

	public static boolean compareType(Block block, Collection<Material> materials)
	{
		Material type = block.getType();
		Iterator<Material> itr = materials.iterator();
		while (itr.hasNext())
		{
			if (itr.next() == type)
				return true;
		}
		return false;
	}

	public static boolean isMaterialSurround(Location loc, double rad, boolean _3d, Collection<Material> m)
	{
		Set<Block> s = _3d ? getBlocksInRadius3D(loc, rad, 0) : getBlocksInRadius2D(loc, rad, 0, true);
		for (Block b : s)
		{
			if (compareType(b, m))
				return true;
		}
		return false;
	}

	public static boolean isMaterialSurround(Location loc, double rad, boolean _3d, Material... m)
	{
		return isMaterialSurround(loc, rad, _3d, Arrays.asList(m));
	}

	public static boolean isEmpty(Block b)
	{
		return b == null || b.getType() == null || b.getType() == Material.AIR;
	}

	public static boolean isFullyStuck(Player player)
	{
		Block block1 = player.getLocation().getBlock();
		Block block2 = player.getLocation().clone().add(0, player.getEyeHeight(), 0).getBlock();
		return (block1.getType().isSolid()) && (block2.getType().isSolid());
	}

	public static boolean isConfined(Location loc, double x, double y, double z)
	{
		return (BlockUtils.isSolid(loc.clone().add(0.0D, y, 0.0D).getBlock())) && (BlockUtils.isSolid(loc.clone().add(x, y, 0.0D).getBlock())) && (BlockUtils.isSolid(loc.clone().add(-x, y, 0.0D).getBlock())) && (BlockUtils.isSolid(loc.clone().add(0.0D, y, z).getBlock())) && (BlockUtils.isSolid(loc.clone().add(0.0D, y, -z).getBlock())) && (BlockUtils.isSolid(loc.clone().add(x, y, z).getBlock())) && (BlockUtils.isSolid(loc.clone().add(-x, y, -z).getBlock())) && (BlockUtils.isSolid(loc.clone().add(x, y, -z).getBlock())) && (BlockUtils.isSolid(loc.clone().add(-x, y, z).getBlock()));
	}

	public static boolean isPartiallyStuck(Player player)
	{
		if (player.getLocation().getBlock() == null)
		{
			return false;
		}
		Block block = player.getLocation().getBlock();
		if (isMaterialSurround(player.getLocation(), 0.3, false, Liquid_Blocks))
		{
			return false;
		}
		if (player.getLocation().getBlock().getRelative(BlockFace.UP).getType().isSolid())
		{
			return true;
		}
		if ((player.getLocation().clone().add(0.0D, player.getEyeHeight(), 0.0D).getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) || (player.getLocation().clone().add(0.0D, player.getEyeHeight(), 0.0D).getBlock().getType().isSolid()))
		{
			return true;
		}
		if (block.getType().isSolid())
		{
			return true;
		}
		return false;
	}

	public static boolean canRayTrace(Location paramLocation, boolean paramBoolean)
	{
		Material mat = paramLocation.getBlock().getType();
		int i = mat.getId();
		return ((!paramBoolean) || ((paramBoolean) && (BlockUtils.isSolid(paramLocation.getBlock())))) && (!(i == 107 || (i >= 183 && i <= 187))) && (!(i == 85 || i == 113 || (i >= 188 && i <= 192))) && (!((i == 53) || (i == 67) || (i == 108) || (i == 109) || (i == 114) || (i == 128) || ((i >= 134) && (i <= 136)) || (i == 156) || (i == 163) || (i == 164) || (i == 180) || (i == 203))) && (!compareType(paramLocation.getBlock(), Door_Blocks)) && (!mat.name().toLowerCase().endsWith("doors")) && (!BlockUtils.Glass_Pane_Blocks.contains(mat)) && (!BlockUtils.Chest_Blocks.contains(mat)) && (!BlockUtils.Plate_Blocks.contains(mat)) && (!(i >= 219 && i <= 234)) && (!BlockUtils.Buggy_Blocks.contains(mat));
	}

	public static boolean isPassable2D(Location loc, double sens, double y, boolean diagonal)
	{
		for (Block b : getBlocksInRadius2D(loc, sens, y, diagonal))
		{
			if (!isSolid(b))
				return true;
		}
		return false;
	}

	public static boolean isPassable3D(Location loc, double sens, double y)
	{
		for (Block b : getBlocksInRadius3D(loc, sens, y))
		{
			if (!isSolid(b))
				return true;
		}
		return false;
	}

	public static boolean isSolid(Block block)
	{
		if (block == null)
			return false;
		Material material = block.getType();
		return material.isBlock() && !Liquid_Blocks.contains(material) && !Fully_Passable_Blocks.contains(material) && !Door_Blocks.contains(material) && !Fence_Blocks.contains(material) && !Buggy_Blocks.contains(material);
	}

	public static boolean containsSameXZ(Location Location, Material Material)
	{
		for (int y = 0; y < 256; y++)
		{
			Block Current = Location.getWorld().getBlockAt((int) Location.getX(), y, (int) Location.getZ());
			if ((Current != null) && (Current.getType().equals(Material)))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isVisible(Location loc)
	{
		for (Block blocks : getBlocksInRadius3D(loc, .5, 0))
		{
			if (!blocks.getType().isOccluding())
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isFullyInLiquid(Location player)
	{
		return new Location(player.getWorld(), MathUtils.fixXAxis(player.getX()), Math.round(player.getY()), player.getBlockZ()).getBlock().isLiquid();
	}

	public static boolean isHoveringOverLiquid(Location player, int blocks)
	{
		for (int i = player.getBlockY(); i > player.getBlockY() - blocks; i--)
		{
			Block newloc = new Location(player.getWorld(), player.getBlockX(), i, player.getBlockZ()).getBlock();
			if (newloc.getType() != Material.AIR)
			{
				return newloc.isLiquid();
			}
		}
		return false;
	}

	public static boolean isHoveringOverLiquid(Location player)
	{
		return isHoveringOverLiquid(player, 25);
	}

	public static boolean isInstantBreak(Material m)
	{
		return Insta_Break_Blocks.contains(m);
	}

	public static boolean isFood(Material m)
	{
		return Foods.contains(m);
	}

	public static boolean isLoaded(Location pos)
	{
		return pos.getBlockY() >= 0 && pos.getBlockY() < 256 ? pos.getWorld().isChunkLoaded(pos.getBlockX() >> 4, pos.getBlockZ() >> 4) : false;
	}

	public static boolean isLoaded(Block block)
	{
		return isLoaded(block.getLocation());
	}

	public static Block getBlockAsync(Location loc)
	{
		if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4))
			return loc.getBlock();
		return null;
	}

	public static boolean isCollidingAABB(Block b1, AABB other)
	{
		for (AABB cBox : getCollisionBoxes(b1, b1.getLocation()))
		{
			if (cBox.isCollideWith(other))
				return true;
		}
		return false;
	}

	public static AABB[] getCollisionBoxes(Block b, Location loc)
	{
		// define boxes for funny blocks
		if (b == null)
			return null;
		if (b.getType() == Material.CARPET)
		{
			AABB[] aabbarr = new AABB[1];
			aabbarr[0] = new AABB(loc.toVector(), loc.toVector().add(new Vector(1, 0, 1)));
			return aabbarr;
		}
		List<AABB> bbs = new ArrayList<>();
		AABB cube = new AABB(loc.toVector(), loc.toVector().add(new Vector(1, 1, 1)));
		AABB aabb2 = new AABB(b);
		aabb2.add(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		if (cube.b(aabb2))
			bbs.add(aabb2);
		AABB[] collisionBoxes = new AABB[bbs.size()];
		for (int i = 0; i < bbs.size(); i++)
		{
			AABB collisionBox = bbs.get(i);
			collisionBoxes[i] = collisionBox;
		}
		return collisionBoxes;
	}

	public static final boolean hasSteppableNearby(Location loc)
	{
		HashSet<Material> set = new HashSet<>();
		set.addAll(Step_Blocks);
		set.addAll(Stair_Blocks);
		return isMaterialSurround(loc, 0.3, true, set);
	}

	public static final boolean hadSteppableNearby(Player p)
	{
		if (hasSteppableNearby(p.getLocation()))
		{
			Cooldowns.set(p.getUniqueId(), "hadSteppableNearby", 2);
			return true;
		}
		return !Cooldowns.isCooldownEnded(p.getUniqueId(), "hadSteppableNearby");
	}

	public static final boolean isLiquidNearby(Location loc)
	{
		return isMaterialSurround(loc, 0.3, false, Liquid_Blocks);
	}

	public static final boolean hasBuggiesNearby(Location loc)
	{
		HashSet<Material> set = new HashSet<>();
		set.addAll(Step_Blocks);
		set.addAll(Stair_Blocks);
		set.addAll(Liquid_Blocks);
		set.addAll(Buggy_Blocks);
		set.addAll(Half_Passable_Blocks);
		set.addAll(Interactable_Blocks);
		set.addAll(Glass_Pane_Blocks);
		set.addAll(Chest_Blocks);
		set.addAll(Door_Blocks);
		set.addAll(TrapDoor_Blocks);
		return isMaterialSurround(loc, 0.3, false, set);
	}

	public static final boolean hadBuggyNearby(Player p, double y_modifier, int cooldown)
	{
		if (hasBuggiesNearby(p.getLocation().add(0, y_modifier, 0)))
			Cooldowns.set(p.getUniqueId(), "hadBuggiesNearby_" + cooldown, cooldown);
		return Cooldowns.isCooldownEnded(p.getUniqueId(), "hadBuggiesNearby_" + cooldown);
	}

	public static final boolean isClimbableNearby(Location loc)
	{
		return isMaterialSurround(loc, 0.3, false, Climbable_Blocks);
	}

	public static final boolean isGlideableNearby(Location loc)
	{
		HashSet<Material> set = new HashSet<>();
		set.addAll(Liquid_Blocks);
		set.addAll(Climbable_Blocks);
		return isMaterialSurround(loc, 0.3, false, set);
	}

	public static final boolean isIceNearby(Location loc)
	{
		return isMaterialSurround(loc, 0.3, true, Ice_Blocks);
	}

	public static final boolean isPistonNearby(Location loc)
	{
		return isMaterialSurround(loc, 0.3, false, Piston_Blocks);
	}

	public static final boolean isOnLiquid(Location loc)
	{
		return isMaterialSurround(loc.subtract(0, .5, 0), 0.3, false, Liquid_Blocks) || isMaterialSurround(loc, 0.3, false, Liquid_Blocks);
	}

	public static final boolean isOnAir(Location loc)
	{
		Set<Block> blocks = getBlocksInRadius2D(loc, .3, -0.5, true);
		Iterator<Block> itr = blocks.iterator();
		while (itr.hasNext())
		{
			if (!isEmpty(itr.next()))
				return false;
		}
		return true;
	}

	static
	{
		Fully_Passable_Blocks.add(Material.AIR);
		Fully_Passable_Blocks.add(Material.STONE_BUTTON);
		Fully_Passable_Blocks.add(Material.WOOD_BUTTON);
		Fully_Passable_Blocks.add(Material.RED_ROSE);
		Fully_Passable_Blocks.add(Material.YELLOW_FLOWER);
		Fully_Passable_Blocks.add(Material.SIGN_POST);
		Fully_Passable_Blocks.add(Material.WALL_SIGN);
		Fully_Passable_Blocks.add(Material.BROWN_MUSHROOM);
		Fully_Passable_Blocks.add(Material.RED_MUSHROOM);
		Fully_Passable_Blocks.add(Material.TORCH);
		Fully_Passable_Blocks.add(Material.REDSTONE_TORCH_ON);
		Fully_Passable_Blocks.add(Material.TRIPWIRE);
		Fully_Passable_Blocks.add(Material.GOLD_PLATE);
		Fully_Passable_Blocks.add(Material.IRON_PLATE);
		Fully_Passable_Blocks.add(Material.STONE_PLATE);
		Fully_Passable_Blocks.add(Material.WOOD_PLATE);
		Fully_Passable_Blocks.add(Material.TRIPWIRE_HOOK);
		Fully_Passable_Blocks.add(Material.REDSTONE_WIRE);
		Fully_Passable_Blocks.add(Material.RAILS);
		Fully_Passable_Blocks.add(Material.ACTIVATOR_RAIL);
		Fully_Passable_Blocks.add(Material.DETECTOR_RAIL);
		Fully_Passable_Blocks.add(Material.POWERED_RAIL);
		Fully_Passable_Blocks.add(Material.SEEDS);
		Fully_Passable_Blocks.add(Material.MELON_SEEDS);
		Fully_Passable_Blocks.add(Material.PUMPKIN_SEEDS);
		Fully_Passable_Blocks.add(Material.CROPS);
		Fully_Passable_Blocks.add(Material.ENDER_PORTAL);
		Fully_Passable_Blocks.add(Material.PORTAL);
		Fully_Passable_Blocks.add(Material.PUMPKIN_STEM);
		Fully_Passable_Blocks.add(Material.MELON_STEM);
		Fully_Passable_Blocks.add(Material.CARROT);
		Fully_Passable_Blocks.add(Material.FIRE);
		Fully_Passable_Blocks.add(Material.POTATO);
		Fully_Passable_Blocks.add(Material.LEVER);
		Fully_Passable_Blocks.add(Material.DEAD_BUSH);
		Fully_Passable_Blocks.add(Material.LONG_GRASS);
		Fully_Passable_Blocks.add(Material.DOUBLE_PLANT);
		Fully_Passable_Blocks.add(Material.CARROT);
		Fully_Passable_Blocks.add(Material.POTATO);
		Fully_Passable_Blocks.add(Material.NETHER_WARTS);
		Fully_Passable_Blocks.add(Material.TRIPWIRE_HOOK);
		Fully_Passable_Blocks.add(Material.TRIPWIRE);
		Fully_Passable_Blocks.add(Material.SAPLING);
		Fully_Passable_Blocks.add(Material.SUGAR_CANE_BLOCK);
		Fully_Passable_Blocks.add(Material.REDSTONE_TORCH_OFF);
		Fully_Passable_Blocks.add(Material.REDSTONE_TORCH_ON);
		Fully_Passable_Blocks.add(Material.PISTON_MOVING_PIECE);
		Liquid_Blocks.add(Material.STATIONARY_LAVA);
		Liquid_Blocks.add(Material.STATIONARY_WATER);
		Half_Passable_Blocks.add(Material.BED_BLOCK);
		Half_Passable_Blocks.add(Material.CARPET);
		Half_Passable_Blocks.add(Material.LEAVES);
		Half_Passable_Blocks.add(Material.LEAVES_2);
		Half_Passable_Blocks.add(Material.VINE);
		Half_Passable_Blocks.add(Material.LADDER);
		Half_Passable_Blocks.add(Material.HUGE_MUSHROOM_1);
		Half_Passable_Blocks.add(Material.HUGE_MUSHROOM_2);
		Half_Passable_Blocks.add(Material.TNT);
		Half_Passable_Blocks.add(Material.REDSTONE_COMPARATOR_OFF);
		Half_Passable_Blocks.add(Material.DIODE_BLOCK_OFF);
		Half_Passable_Blocks.add(Material.REDSTONE_COMPARATOR_ON);
		Half_Passable_Blocks.add(Material.DIODE_BLOCK_ON);
		Half_Passable_Blocks.add(Material.WATER_LILY);
		Half_Passable_Blocks.add(Material.FLOWER_POT);
		Half_Passable_Blocks.add(Material.COCOA);
		Half_Passable_Blocks.add(Material.NETHERRACK);
		Half_Passable_Blocks.add(Material.GLASS);
		Half_Passable_Blocks.add(Material.STAINED_GLASS);
		Half_Passable_Blocks.add(Material.ICE);
		Half_Passable_Blocks.add(Material.PACKED_ICE);
		Half_Passable_Blocks.add(Material.SANDSTONE);
		Half_Passable_Blocks.add(Material.SANDSTONE_STAIRS);
		Half_Passable_Blocks.add(Material.QUARTZ_BLOCK);
		Half_Passable_Blocks.add(Material.QUARTZ_STAIRS);
		Half_Passable_Blocks.add(Material.DAYLIGHT_DETECTOR);
		Half_Passable_Blocks.add(Material.SNOW);
		Interactable_Blocks.add(Material.WORKBENCH);
		Interactable_Blocks.add(Material.FURNACE);
		Interactable_Blocks.add(Material.BURNING_FURNACE);
		Interactable_Blocks.add(Material.ENCHANTMENT_TABLE);
		Interactable_Blocks.add(Material.HOPPER);
		Interactable_Blocks.add(Material.BED_BLOCK);
		Interactable_Blocks.add(Material.JUKEBOX);
		Interactable_Blocks.add(Material.NOTE_BLOCK);
		Interactable_Blocks.add(Material.DROPPER);
		Interactable_Blocks.add(Material.BREWING_STAND);
		Interactable_Blocks.add(Material.REDSTONE_COMPARATOR_ON);
		Interactable_Blocks.add(Material.TRIPWIRE_HOOK);
		Interactable_Blocks.add(Material.ENDER_PORTAL_FRAME);
		Interactable_Blocks.add(Material.LEVER);
		Interactable_Blocks.add(Material.ANVIL);
		Interactable_Blocks.add(Material.WOOD_BUTTON);
		Chest_Blocks.add(Material.CHEST);
		Chest_Blocks.add(Material.TRAPPED_CHEST);
		Chest_Blocks.add(Material.ENDER_CHEST);
		Plate_Blocks.add(Material.GOLD_PLATE);
		Plate_Blocks.add(Material.IRON_PLATE);
		Plate_Blocks.add(Material.STONE_PLATE);
		Plate_Blocks.add(Material.WOOD_PLATE);
		Ice_Blocks.add(Material.ICE);
		Ice_Blocks.add(Material.PACKED_ICE);
		Glass_Pane_Blocks.add(Material.THIN_GLASS);
		Glass_Pane_Blocks.add(Material.STAINED_GLASS_PANE);
		Step_Blocks.add(Material.STEP);
		Step_Blocks.add(Material.WOOD_STEP);
		Stair_Blocks.add(Material.ACACIA_STAIRS);
		Stair_Blocks.add(Material.BIRCH_WOOD_STAIRS);
		Stair_Blocks.add(Material.BRICK_STAIRS);
		Stair_Blocks.add(Material.COBBLESTONE_STAIRS);
		Stair_Blocks.add(Material.DARK_OAK_STAIRS);
		Stair_Blocks.add(Material.JUNGLE_WOOD_STAIRS);
		Stair_Blocks.add(Material.NETHER_BRICK_STAIRS);
		Stair_Blocks.add(Material.QUARTZ_STAIRS);
		Stair_Blocks.add(Material.SANDSTONE_STAIRS);
		Stair_Blocks.add(Material.SMOOTH_STAIRS);
		Stair_Blocks.add(Material.SPRUCE_WOOD_STAIRS);
		Stair_Blocks.add(Material.WOOD_STAIRS);
		Climbable_Blocks.add(Material.LADDER);
		Climbable_Blocks.add(Material.VINE);
		Fallable_Blocks.add(Material.SAND);
		Fallable_Blocks.add(Material.GRAVEL);
		Door_Blocks.add(Material.IRON_DOOR_BLOCK);
		Door_Blocks.add(Material.WOODEN_DOOR);
		TrapDoor_Blocks.add(Material.TRAP_DOOR);
		Piston_Blocks.add(Material.PISTON_BASE);
		Piston_Blocks.add(Material.PISTON_EXTENSION);
		Piston_Blocks.add(Material.PISTON_MOVING_PIECE);
		Piston_Blocks.add(Material.PISTON_STICKY_BASE);
		Buggy_Blocks.add(Material.COCOA);
		Buggy_Blocks.add(Material.DRAGON_EGG);
		Buggy_Blocks.add(Material.ENDER_PORTAL_FRAME);
		Buggy_Blocks.add(Material.ENCHANTMENT_TABLE);
		Buggy_Blocks.add(Material.BED_BLOCK);
		Buggy_Blocks.add(Material.HOPPER);
		Buggy_Blocks.add(Material.FLOWER_POT);
		Buggy_Blocks.add(Material.BREWING_STAND);
		Buggy_Blocks.add(Material.ANVIL);
		Buggy_Blocks.add(Material.CAULDRON);
		Buggy_Blocks.add(Material.CARPET);
		Buggy_Blocks.add(Material.getMaterial(101));
		Buggy_Blocks.add(Material.COBBLE_WALL);
		Buggy_Blocks.add(Material.FENCE);
		Buggy_Blocks.add(Material.FENCE_GATE);
		Buggy_Blocks.add(Material.LADDER);
		Buggy_Blocks.add(Material.WEB);
		Buggy_Blocks.add(Material.VINE);
		Buggy_Blocks.add(Material.WATER_LILY);
		Buggy_Blocks.add(Material.REDSTONE_COMPARATOR_OFF);
		Buggy_Blocks.add(Material.REDSTONE_COMPARATOR_ON);
		Buggy_Blocks.add(Material.DIODE_BLOCK_ON);
		Buggy_Blocks.add(Material.DIODE_BLOCK_OFF);
		Buggy_Blocks.add(Material.SKULL);
		Buggy_Blocks.add(Material.SNOW);
		Buggy_Blocks.add(Material.PISTON_BASE);
		Buggy_Blocks.add(Material.PISTON_STICKY_BASE);
		Buggy_Blocks.add(Material.PISTON_EXTENSION);
		Buggy_Blocks.add(Material.CACTUS);
		Buggy_Blocks.add(Material.SOIL);
		Buggy_Blocks.add(Material.SOUL_SAND);
		Buggy_Blocks.add(Material.DAYLIGHT_DETECTOR);
		Buggy_Blocks.add(Material.getMaterial(178));
		Buggy_Blocks.add(Material.CAKE_BLOCK);
		Buggy_Blocks.add(Material.COCOA);
		Buggy_Blocks.add(Material.FIRE);
		Buggy_Blocks.add(Material.THIN_GLASS);
		Buggy_Blocks.add(Material.STAINED_GLASS_PANE);
		Fence_Blocks.add(Material.COBBLE_WALL);
		Fence_Blocks.add(Material.FENCE);
		Fence_Blocks.add(Material.FENCE_GATE);
		Insta_Break_Blocks.add(Material.RED_MUSHROOM);
		Insta_Break_Blocks.add(Material.RED_ROSE);
		Insta_Break_Blocks.add(Material.BROWN_MUSHROOM);
		Insta_Break_Blocks.add(Material.YELLOW_FLOWER);
		Insta_Break_Blocks.add(Material.REDSTONE);
		Insta_Break_Blocks.add(Material.REDSTONE_TORCH_OFF);
		Insta_Break_Blocks.add(Material.REDSTONE_TORCH_ON);
		Insta_Break_Blocks.add(Material.REDSTONE_WIRE);
		Insta_Break_Blocks.add(Material.LONG_GRASS);
		Insta_Break_Blocks.add(Material.PAINTING);
		Insta_Break_Blocks.add(Material.WHEAT);
		Insta_Break_Blocks.add(Material.SUGAR_CANE);
		Insta_Break_Blocks.add(Material.SUGAR_CANE_BLOCK);
		Insta_Break_Blocks.add(Material.DIODE);
		Insta_Break_Blocks.add(Material.DIODE_BLOCK_OFF);
		Insta_Break_Blocks.add(Material.DIODE_BLOCK_ON);
		Insta_Break_Blocks.add(Material.SAPLING);
		Insta_Break_Blocks.add(Material.TORCH);
		Insta_Break_Blocks.add(Material.CROPS);
		Insta_Break_Blocks.add(Material.SNOW);
		Insta_Break_Blocks.add(Material.TNT);
		Insta_Break_Blocks.add(Material.POTATO);
		Insta_Break_Blocks.add(Material.CARROT);
		Foods.add(Material.COOKED_BEEF);
		Foods.add(Material.COOKED_CHICKEN);
		Foods.add(Material.COOKED_FISH);
		Foods.add(Material.GRILLED_PORK);
		Foods.add(Material.PORK);
		Foods.add(Material.MUSHROOM_SOUP);
		Foods.add(Material.RAW_BEEF);
		Foods.add(Material.RAW_CHICKEN);
		Foods.add(Material.RAW_FISH);
		Foods.add(Material.APPLE);
		Foods.add(Material.GOLDEN_APPLE);
		Foods.add(Material.MELON);
		Foods.add(Material.COOKIE);
		Foods.add(Material.BREAD);
		Foods.add(Material.SPIDER_EYE);
		Foods.add(Material.ROTTEN_FLESH);
		Foods.add(Material.POTATO_ITEM);
		Combo.put(Material.SHEARS, Material.WOOL);
		Combo.put(Material.IRON_SWORD, Material.WEB);
		Combo.put(Material.DIAMOND_SWORD, Material.WEB);
		Combo.put(Material.STONE_SWORD, Material.WEB);
		Combo.put(Material.WOOD_SWORD, Material.WEB);
	}
}
