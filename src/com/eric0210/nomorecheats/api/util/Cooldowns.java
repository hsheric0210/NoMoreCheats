package com.eric0210.nomorecheats.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;

public class Cooldowns
{
	public static HashMap<UUID, HashMap<String, Integer>> cooldowns = new HashMap<>();

	public static final void init()
	{
		TickTasks.addTask(() ->
		{
			for (UUID uid : cooldowns.keySet())
			{
				if (Bukkit.getPlayer(uid) == null || !Bukkit.getPlayer(uid).isOnline())
					return;
				for (String key : cooldowns.get(uid).keySet())
				{
					HashMap<String, Integer> cooldown = cooldowns.get(uid);
					if (cooldown.get(key) > 0)
						cooldown.put(key, cooldown.get(key) - 1);
					cooldowns.put(uid, cooldown);
				}
			}
		});
	}

	public static final boolean isCooldownEnded(UUID uid, String key)
	{
		return get(uid, key) == 0;
	}

	public static final int get(UUID uid, String key)
	{
		if (contains(uid, key))
			return cooldowns.get(uid).get(key).intValue();

		return 0;
	}

	public static final void set(UUID uid, String key, int value)
	{
		if (value <= 0)
			return;
		if (!cooldowns.containsKey(uid))
			cooldowns.put(uid, new HashMap<String, Integer>());
		cooldowns.get(uid).put(key, value);
	}

	public static final void reset(UUID uid, String key)
	{
		if (!contains(uid, key))
			return;
		cooldowns.get(uid).put(key, 0);
	}

	public static boolean contains(UUID uid, String key)
	{
		return cooldowns.containsKey(uid) && cooldowns.get(uid).containsKey(key);
	}

	public static void remove(UUID uid, String key)
	{
		if (cooldowns.size() == 0 || !cooldowns.containsKey(uid))
			return;
		ArrayList<String> removeQueue = new ArrayList<>();
		for (Iterator<String> itr = cooldowns.get(uid).keySet().iterator(); itr.hasNext();)
		{
			String str = itr.next();
			if (str.contains(key))
				removeQueue.add(str);
		}
		for (Iterator<String> itr = removeQueue.iterator(); itr.hasNext();)
		{
			cooldowns.get(uid).remove(itr.next());
		}
		removeQueue.clear();
	}
}
