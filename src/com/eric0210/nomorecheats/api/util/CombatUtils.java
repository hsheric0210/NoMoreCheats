package com.eric0210.nomorecheats.api.util;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;


public class CombatUtils
{
	public static HashMap<UUID, Long> lastHitByEntity = new HashMap<>();
	public static HashMap<UUID, Long> lastHitEntity = new HashMap<>();

	public static boolean hasCombat(Player p)
	{
		return !InternalUtils.elapsed(lastHitEntity.getOrDefault(p.getUniqueId(), -1L), 5000L)
				|| !InternalUtils.elapsed(lastHitByEntity.getOrDefault(p.getUniqueId(), -1L), 5000L);
	}

	public static boolean hasHitEntity(Player p)
	{
		return !InternalUtils.elapsed(lastHitEntity.getOrDefault(p.getUniqueId(), -1L), 5000L);
	}

	public static boolean hasHitByEntity(Player p)
	{
		return !InternalUtils.elapsed(lastHitByEntity.getOrDefault(p.getUniqueId(), -1L), 5000L);
	}
}
