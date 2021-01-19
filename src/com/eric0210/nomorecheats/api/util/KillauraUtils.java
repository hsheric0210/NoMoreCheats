package com.eric0210.nomorecheats.api.util;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

public class KillauraUtils
{
	public static int TBC_AVERAGE_CALCULATE_CLICKS = 7;
	public static HashMap<UUID, Long> lastClick = new HashMap<>();
	public static HashMap<UUID, Long> timeBetweenClickActions = new HashMap<>();
	public static HashMap<UUID, Long> lastTBC = new HashMap<>();
	private static HashMap<UUID, Long> avgCalculater_sum = new HashMap<>();
	private static HashMap<UUID, Integer> avgCalculater_clickscount = new HashMap<>();
	public static HashMap<UUID, Long> timeBetweenClicks = new HashMap<>();
	private static HashMap<UUID, AccuracyData> accDatas = new HashMap<>();
	private static HashMap<UUID, Long> accuracyEvent = new HashMap<>();
	public static HashMap<UUID, Double> accuracyPercentage = new HashMap<>();
	private static HashMap<UUID, HashMap<UUID, HitRatio>> hrdata = new HashMap<>();

	public static void handleInteraction(Player p, Action act)
	{
		UUID uid = p.getUniqueId();
		if (act == Action.LEFT_CLICK_AIR)
		{
			lastClick.put(uid, System.currentTimeMillis());
			if (timeBetweenClickActions.containsKey(uid))
			{
				long tdiff = System.currentTimeMillis() - timeBetweenClickActions.get(uid);
				lastTBC.put(uid, tdiff);
				if (!avgCalculater_sum.containsKey(uid))
					avgCalculater_sum.put(uid, tdiff);
				else
					avgCalculater_sum.put(uid, avgCalculater_sum.get(uid) + tdiff);
				avgCalculater_clickscount.put(uid, avgCalculater_clickscount.getOrDefault(uid, 0) + 1);
				if (avgCalculater_clickscount.get(uid) >= TBC_AVERAGE_CALCULATE_CLICKS)
				{
					avgCalculater_clickscount.remove(uid);
					if (avgCalculater_sum.containsKey(uid))
					{
						long clksAvg = avgCalculater_sum.get(uid) / TBC_AVERAGE_CALCULATE_CLICKS;
						timeBetweenClicks.put(uid, clksAvg);
						avgCalculater_sum.remove(uid);
					}
				}
			}
			timeBetweenClickActions.put(uid, System.currentTimeMillis());
		}
		if (act == Action.LEFT_CLICK_AIR && getNearbyAttackables(p, 6) > 1
				&& !InternalUtils.elapsed(accuracyEvent.getOrDefault(p.getUniqueId(), 0L), 500L))
			calculateAccuracy(p, false);
	}

	public static boolean isEasyToAim(Player p, Entity e)
	{
		Location loc = p.getLocation();
		Location loc2 = e.getLocation();
		loc.setPitch(0F);
		double dist = loc.distance(loc2);
		double eyeheight = ((LivingEntity) e).getEyeHeight();
		Location loc3 = loc.clone().add(loc.getDirection().multiply(dist));
		Location loc4 = loc.clone().add(loc.getDirection().multiply(dist + 1.0D));
		Location loc5 = loc3.clone().add(0, 1, 0);
		Location loc6 = loc4.clone().add(0, 1, 0);
		return (BlockUtils.isSolid(loc3.getBlock()) || BlockUtils.isSolid(loc4.getBlock())
				|| ((eyeheight > 1.0) && (BlockUtils.isSolid(loc5.getBlock()))) || BlockUtils.isSolid(loc6.getBlock()));
	}

	public static double getAngle2(Player paramPlayer, Entity paramEntity)
	{
		Location player_pos = paramPlayer.getLocation();
		player_pos.setY(0.0D);
		player_pos.setPitch(0.0F);
		Location entity_pos = paramEntity.getLocation();
		entity_pos.setPitch(0.0F);
		entity_pos.setY(0.0D);
		if (player_pos.distance(entity_pos) < 1.0D)
			return 0.0D;
		Vector delta = player_pos.subtract(entity_pos).toVector();
		delta = delta.normalize();
		Vector playerdir_norm = player_pos.getDirection().normalize();
		playerdir_norm.setX(playerdir_norm.getX() * -1.0D);
		playerdir_norm.setY(playerdir_norm.getY() * -1.0D);
		playerdir_norm.setZ(playerdir_norm.getZ() * -1.0D);
		return delta.angle(playerdir_norm);
	}

	public static boolean isBigEntity(Entity e)
	{
		return (e instanceof Horse) || (e instanceof Giant) || (e instanceof EnderDragon) || (e instanceof Wither)
				|| (e instanceof Ghast) || (e instanceof IronGolem);
	}

	public static boolean isAnimal(Entity e)
	{
		return (e instanceof Animals);
	}

	public static int getNearbyAttackables(Player p, double range)
	{
		int entities = 0;
		for (Entity e : p.getNearbyEntities(range, range, range))
		{
			if (e != null && (e instanceof LivingEntity) || (e instanceof Boat))
			{
				entities++;
			}
		}
		return entities;
	}

	public static int getNearbyAttackableEntitiesCountAdv(Player p, double range)
	{
		int entities = 0;
		for (Entity e : p.getNearbyEntities(range, range, range))
		{
			if (e instanceof Monster)
				entities++;
			else if (e instanceof Player)
			{
				Player p2 = (Player) e;
				if (CombatUtils.hasCombat(p2))
					entities++;
			}
		}
		return entities;
	}

	// public static boolean isCombatPossible(Player p)
	// {
	// ItemStack ih = p.getItemInHand();
	// if (ih != null && ih.getType().name().toLowerCase().contains("sword"))
	// {
	// return getNearbyAttackables(p, 6) > 1;
	// }
	// return false;
	// }
	public static void handleAttack(Player p, Entity damagee)
	{
		if (damagee instanceof Player)
		{
			Player pdamagee = (Player) damagee;
			boolean b = true;
			for (int i = 0; i <= 2; i++)
			{
				if (!BlockUtils.isConfined(p.getLocation(), 1.0, i, 1.0)
						&& !BlockUtils.isConfined(p.getLocation(), 1.0, i, 1.0))
				{
					b = false;
					break;
				}
			}
			if (b)
			{
				double dist = p.getLocation().distance(pdamagee.getLocation());
				if (dist >= 3.0D)
				{
					calculateAccuracy(pdamagee, true);
				}
			}
			accuracyEvent.put(p.getUniqueId(), System.currentTimeMillis());
			if (verifyHR(p, pdamagee))
			{
				HitRatio phd = hrdata.get(p.getUniqueId()).get(pdamagee.getUniqueId());
				phd.dealt++;
				hrdata.get(p.getUniqueId()).put(pdamagee.getUniqueId(), phd);
				HitRatio pdhd = hrdata.get(pdamagee.getUniqueId()).get(p.getUniqueId());
				pdhd.took++;
				hrdata.get(pdamagee.getUniqueId()).put(p.getUniqueId(), pdhd);
			}
		}
	}

	public static boolean verifyHR(Player p, Player p2)
	{
		return CombatUtils.hasCombat(p) && CombatUtils.hasCombat(p2) && (hrdata.containsKey(p.getUniqueId()))
				&& (hrdata.containsKey(p2.getUniqueId()))
				&& (hrdata.get(p.getUniqueId()).containsKey(p2.getUniqueId())
						&& (hrdata.get(p2.getUniqueId())).containsKey(p.getUniqueId())
						&& (hrdata.get(p.getUniqueId()).get(p2.getUniqueId()).dealt >= 3.0)
						&& (hrdata.get(p2.getUniqueId()).get(p.getUniqueId()).dealt >= 3.0));
	}

	public static double getHitRatio(Player p, Player p2)
	{
		HitRatio hd = hrdata.get(p.getUniqueId()).get(p2.getUniqueId());
		int dealt = hd.dealt;
		int received = hd.took;
		return dealt / (dealt + received) * 100D;
	}

	public static double hitRatioDiff(Player p, Player p2)
	{
		return !verifyHR(p, p2) ? 0 : getHitRatio(p, p2) - getHitRatio(p2, p);
	}

	public static void calculateAccuracy(Player p, boolean ishit)
	{
		AccuracyData accd = accDatas.getOrDefault(p.getUniqueId(), new AccuracyData());
		if (ishit)
			accd.hits++;
		else
			accd.miss++;
		accd.count++;
		if (accd.count >= 20)
		{
			int hits = accd.hits;
			int misses = accd.miss;
			int count2 = hits + misses;
			double accPercent = count2 <= 0.0D ? 100.0D : hits / count2 * 100.0D;
			accuracyPercentage.put(p.getUniqueId(), accPercent);
			accd = new AccuracyData();
		}
		accDatas.put(p.getUniqueId(), accd);
	}

	public static class AccuracyData
	{
		int count = 0;
		int hits = 0;
		int miss = 0;
	}

	public static class HitRatio
	{
		int dealt = 0;
		int took = 0;
	}

	public static void removeAccuracy(Player p)
	{
		accDatas.remove(p.getUniqueId());
		accuracyEvent.remove(p.getUniqueId());
		accuracyPercentage.remove(p.getUniqueId());
	}
}
