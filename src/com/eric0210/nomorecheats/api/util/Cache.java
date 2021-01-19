package com.eric0210.nomorecheats.api.util;

import java.util.HashMap;
import java.util.UUID;

import com.eric0210.nomorecheats.Logging;

public class Cache
{
	private static final HashMap<UUID, HashMap<String, Object>> caches = new HashMap<>();

	public static final boolean contains(UUID uid, String key)
	{
		return (caches.containsKey(uid)) && (caches.get(uid).containsKey(key));
	}

	public static final <T> T get(UUID uid, String key, T defaultvalue)
	{
		if (defaultvalue == null)
			throw new IllegalArgumentException("default value cannot be null");
		T ret = defaultvalue;
		try
		{
			ret = contains(uid, key) ? (T) caches.get(uid).get(key) : defaultvalue;
		}
		catch (ClassCastException ex)
		{
			Logging.debug("ClassCastException while get \"" + key + "\" value. Type class type mismatched. (" + caches.get(uid).get(key).getClass().getSimpleName() + '/' + defaultvalue.getClass().getSimpleName() + ").");
		}
		return ret;
	}

	public static final Object set(UUID uid, String key, Object value)
	{
		if (value == null)
			throw new IllegalArgumentException("value cannot be null");
		Object lastValue = null;
		if (!caches.containsKey(uid))
		{
			caches.put(uid, new HashMap<String, Object>());
		}
		else
			lastValue = caches.get(uid).get(key);
		caches.get(uid).put(key, value);
		return lastValue;
	}

	public static final void remove(UUID uid, String key)
	{
		if (caches.containsKey(uid))
		{
			caches.get(uid).remove(key);
		}
	}
}
