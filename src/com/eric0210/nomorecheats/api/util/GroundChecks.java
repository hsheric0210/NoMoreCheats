package com.eric0210.nomorecheats.api.util;

import java.util.HashSet;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GroundChecks
{
	public static boolean isOnGround(Player player)
	{
		return isOnGround(player.getLocation());
	}

	public static boolean isOnGround(Location pos)
	{
		return isOnGround(pos, .2);
	}

	public static boolean isOnGround(Location pos, double feetDepth)
	{
		HashSet<Block> blocks = new HashSet<>();
		blocks.add(pos.getBlock());
		blocks.add(pos.clone().subtract(0, feetDepth, 0).getBlock());
		blocks.addAll(BlockUtils.getBlocksInRadius2D(pos, .3, -feetDepth, true));
		boolean ret = false;
		for (Block b : blocks)
			if (BlockUtils.isSolid(b) || BlockUtils.compareType(b, BlockUtils.Climbable_Blocks))
			{
				ret = true;
				break;
			}
		if (BlockUtils.hasSteppableNearby(pos) && MathUtils.getFraction(pos.getY()) <= .5 + feetDepth) // Step, Stairs
			return true;
//		if (BlockUtils.hasBuggiesNearby(pos) && BlockUtils.hasBuggiesNearby(pos)) // For chests, etc...
//			return true;
//		if (BlockUtils.isMaterialSurround(pos.clone().subtract(0, feetDepth, 0), .3, false, BlockUtils.Glass_Pane_Blocks) && !BlockUtils.isMaterialSurround(pos.clone().add(0, feetDepth, 0), .3, false, BlockUtils.Glass_Pane_Blocks))
//			return true;
//		if (!BlockUtils.compareType(pos.getBlock(), BlockUtils.Fence_Blocks) && !BlockUtils.compareType(pos.clone().add(0, -.49999, 0).getBlock(), BlockUtils.Fence_Blocks) && BlockUtils.compareType(pos.clone().add(0, -.5, 0).getBlock(), BlockUtils.Fence_Blocks))
//			return true;
		for (Block b : BlockUtils.getBlocksInRadius2D(pos, .3, 0, true)) // Check player is 'in' buggy block
			if (BlockUtils.compareType(b, BlockUtils.Buggy_Blocks))
			return true;
		return ret;
	}

	public static boolean isOnGroundCache(Player p, double x, double y, double z)
	{
		String cooldownKey = p.getUniqueId().toString() + "." + x + "-" + y + "-" + z;
		if (!Cooldowns.isCooldownEnded(p.getUniqueId(), cooldownKey))
			return Cooldowns.isCooldownEnded(p.getUniqueId(), cooldownKey + ".ground");
		Cooldowns.set(p.getUniqueId(), cooldownKey, 1);
		if (isOnGround(p))
		{
			Cooldowns.set(p.getUniqueId(), cooldownKey + ".ground", 1);
			return true;
		}
		return isOnGround(p);
	}
}
