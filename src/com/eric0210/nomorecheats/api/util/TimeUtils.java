package com.eric0210.nomorecheats.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;


public class TimeUtils
{
	private static final HashMap<UUID, HashMap<String, Long>> map = new HashMap<>();

	public static boolean contains(UUID uid, String paramString)
	{
		return (map.containsKey(uid)) && ((map.get(uid)).containsKey(paramString));
	}

	public static long getTimeDiff(UUID uid, String paramString, long default_value)
	{
		return !contains(uid, paramString) ? default_value : System.currentTimeMillis() - map.get(uid).get(paramString);
	}

	public static long get(UUID uid, String paramString, long default_value)
	{
		return !contains(uid, paramString) ? default_value : map.get(uid).get(paramString);
	}

	public static boolean elapsed(UUID uid, String paramString, boolean default_value, long required)
	{
		return getTimeDiff(uid, paramString, required + (default_value ? 1 : 0)) > required;
	}

	public static void putCurrentTime(UUID uid, String paramString)
	{
		if (!map.containsKey(uid))
			map.put(uid, new HashMap<String, Long>());
		map.get(uid).put(paramString, System.currentTimeMillis());
	}

	public static void removeValue(UUID uid, String paramString)
	{
		if (map.containsKey(uid))
			map.get(uid).remove(paramString);
	}

	public static void removeAll(UUID uid)
	{
		map.remove(uid);
	}

	public static void removeWhitelist(ArrayList<String> whitelist)
	{
		if (map.size() == 0)
			return;
		ArrayList<String> removeQueue = new ArrayList<>();
		for (Iterator<UUID> uidItr = map.keySet().iterator(); uidItr.hasNext();)
		{
			UUID uid = uidItr.next();
			for (Iterator<String> keySetItr = map.get(uid).keySet().iterator(); keySetItr.hasNext();)
			{
				String currentKey = keySetItr.next();
				boolean keep = false;
				for (String key : whitelist)
					if (currentKey.contains(key))
						keep = true;
				if (!keep)
					removeQueue.add(currentKey);
			}
			for (Iterator<String> localIterator2 = removeQueue.iterator(); localIterator2.hasNext();)
			{
				String str = localIterator2.next();
				map.get(uid).remove(str);
			}
		}
		removeQueue.clear();
	}

	public static void removeBlacklist(UUID uid, String blacklist)
	{
		if ((map.size() == 0) || (!map.containsKey(uid)))
		{
			return;
		}
		ArrayList<String> removeQueue = new ArrayList<>();
		for (Iterator<String> keySetItr = map.get(uid).keySet().iterator(); keySetItr.hasNext();)
		{
			String currentKey = keySetItr.next();
			if (currentKey.contains(blacklist))
			{
				removeQueue.add(currentKey);
			}
		}
		String currentRemove;
		for (Iterator<String> removerItr = removeQueue.iterator(); removerItr.hasNext();)
		{
			currentRemove = removerItr.next();
			map.get(uid).remove(currentRemove);
		}
		removeQueue.clear();
	}
}
