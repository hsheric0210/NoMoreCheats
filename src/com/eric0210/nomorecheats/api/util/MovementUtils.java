package com.eric0210.nomorecheats.api.util;

import com.eric0210.nomorecheats.api.ViolationMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class MovementUtils
{
	public static boolean canStandCorrectly(Location loc)
	{
		if (loc.getY() < 1)
			return false;
		Block groundBlock = loc.getBlock().getRelative(BlockFace.DOWN);
		if (groundBlock.isLiquid() || groundBlock.isEmpty())
			return false;
		if (!groundBlock.getType().isSolid())
			return false;
		return true;
	}

	public static boolean teleported(Player p)
	{
		return Cache.get(p.getUniqueId(), "teleported", false);
	}

	public static boolean isChasingPlayer(Player p)
	{
		if (ViolationMap.getInstance(p).getLevel(Checks.hitReach) > 1)
		{
			return false;
		}
		if (p.isSprinting() || Counter.getCount(p.getUniqueId(), "ticksUp") > 0 || (CombatUtils.hasHitEntity(p) && (Cache.get(p.getUniqueId(), "moveSpeed", 0.0D) >= 0.21D)))
		{
			for (Entity nearbyEntity : p.getNearbyEntities(6.0D, 6.0D, 6.0D))
			{
				if ((nearbyEntity instanceof Player))
				{
					double distance = MathUtils.getHorizontalDistance(p.getLocation(), nearbyEntity.getLocation());
					if (distance >= 3.5D)
					{
						Player nearbyPlayer = (Player) nearbyEntity;
						Location nearbyPlayerPos = nearbyPlayer.getLocation();
						if ((!CombatUtils.hasHitEntity(nearbyPlayer)) && (nearbyPlayer.isSprinting() || Counter.getCount(nearbyPlayer.getUniqueId(), "ticksUp") > 0 || (CombatUtils.hasHitByEntity(nearbyPlayer) && (Cache.get(nearbyPlayer.getUniqueId(), "moveSpeed", 0.0D) >= 0.16D))))
						{
							Location lookingPosition = p.getLocation();
							lookingPosition.setPitch(0);
							lookingPosition = lookingPosition.add(lookingPosition.getDirection().multiply(distance));
							return MathUtils.getHorizontalDistance(lookingPosition, nearbyPlayerPos) <= 1.25D;
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean hasChasingPlayer(Player p)
	{
		return !Cooldowns.isCooldownEnded(p.getUniqueId(), "chasing") || isChasingPlayer(p);
	}

	public static float getAdditionalSpeed(Player p)
	{
		float f = Math.max(p.getWalkSpeed() - .2F, p.getFlySpeed() - .1F);
		return f < 0 ? 0F : f;
	}

	public static boolean containsArmorEnchantment(Player player, Enchantment e)
	{
		ItemStack[] arrayOfItemStack;
		int j = (arrayOfItemStack = player.getInventory().getArmorContents()).length;
		for (int i = 0; i < j; i++)
		{
			ItemStack is = arrayOfItemStack[i];
			if ((is != null) && (is.containsEnchantment(e)))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isSprintFlying(Player player)
	{
		return (player.isSprinting()) || (player.isFlying());
	}

	public static boolean check_y_values(Player p, double y, double y2)
	{
		if (YMap.get(p) == null)
			return true;
		if (YMap.get(p).containsY(y))
		{
			double fixedspeed = getMoveSpeedFixed(p, y2);
			return (y == y2) || (y == fixedspeed) || YMap.get(p).containsY(y);
		}
		return false;
	}

	public static double getMoveSpeedFixed(Player p, double subfrom)
	{
		double speed = Cache.get(p.getUniqueId(), "moveSpeed", 0.0D);
		if (subfrom - speed < 0)
			subfrom = subfrom - speed + 1;
		else
			subfrom -= speed;
		return subfrom;
	}

	public static double expectedYDifferenceDelta(Player p)
	{
		return 0.0784000015258D - Counter.getCount(p.getUniqueId(), "airTicks") * 7.58E-4D;
	}

	public static boolean check_y_normal_jump_similar(Player p, double y)
	{
		return YMap.get(p).containsY(y, 0.02D);
	}

	public static double getJumpPE_YModifier(Player p, double y)
	{
		return y + (PlayerUtils.getPotionEffectLevel(p, PotionEffectType.JUMP) + 1) * .1 + .001;
	}

	public static boolean hasLowJumpEffect(Player p)
	{
		return (p.hasPotionEffect(PotionEffectType.JUMP) && (PlayerUtils.getPotionEffectLevel(p, PotionEffectType.JUMP) < 128 || PlayerUtils.getPotionEffectLevel(p, PotionEffectType.JUMP) > 250));
	}

	public static boolean checkY(double ydiff, int jumpMod)
	{
		YMap map = YMap.get(jumpMod);
		if (map == null)
			return true;
		return map.containsY(ydiff);
	}
}
