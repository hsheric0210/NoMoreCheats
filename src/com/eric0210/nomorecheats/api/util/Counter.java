package com.eric0210.nomorecheats.api.util;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Counter
{
	public static ConcurrentHashMap<UUID, ConcurrentHashMap<String, Integer>> maps = new ConcurrentHashMap<>();

	public static final boolean contains(UUID uid, String key)
	{
		return maps.containsKey(uid) && maps.get(uid).containsKey(key);
	}

	public static final int getCount(UUID uid, String key)
	{
		return (contains(uid, key) ? maps.get(uid).get(key) : 0);
	}

	private static int setCount(UUID uid, String key, int value)
	{
		if (!maps.containsKey(uid))
			maps.put(uid, new ConcurrentHashMap<String, Integer>());
		int prevvalue = getCount(uid, key);
		if (value < 0)
			value = 0;
		maps.get(uid).put(key, value);
		return prevvalue;
	}

	public static final int incrementAndGetCount(UUID uid, String key, int num2add, int nextResetAfterTicks)
	{
		if (nextResetAfterTicks > 0)
		{
			if (Cooldowns.isCooldownEnded(uid, "ResetCounter." + key))
			{
				if (getCount(uid, key) > 0)
					setCount(uid, key, 0);
				Cooldowns.set(uid, "ResetCounter." + key, nextResetAfterTicks);
			}
		}
		int i = 0;
		setCount(uid, key, (i = (getCount(uid, key) + num2add)));
		return i;
	}

	public static final int increment1AndGetCount(UUID uid, String key, int nextResetAfterTicks)
	{
		return incrementAndGetCount(uid, key, 1, nextResetAfterTicks);
	}

	public static final int decrement1AndGetCount(UUID uid, String key, int nextResetAfterTicks)
	{
		return incrementAndGetCount(uid, key, -1, nextResetAfterTicks);
	}

	public static final int getAndSetCount(UUID uid, String key, int val, int nextResetAfterTicks)
	{
		if (nextResetAfterTicks > 0)
		{
			if (Cooldowns.isCooldownEnded(uid, "Counter." + key + ".reset"))
			{
				if (getCount(uid, key) > 0)
					setCount(uid, key, 0);
				Cooldowns.set(uid, "ResetCounter." + key, nextResetAfterTicks);
			}
		}
		int i = getCount(uid, key);
		setCount(uid, key, val);
		return i;
	}

	public static final void remove(UUID uid, String key)
	{
		if (maps.containsKey(uid))
		{
			maps.get(uid).remove(key);
			Cooldowns.reset(uid, "ResetCounter." + key);
		}
	}

	public static final int getResetCooldown(UUID uid, String key)
	{
		return getCount(uid, "ResetCounter." + key);
	}
}
