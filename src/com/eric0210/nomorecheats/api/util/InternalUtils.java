package com.eric0210.nomorecheats.api.util;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Random;

public class InternalUtils
{
	@SuppressWarnings("unchecked")
	public static <T> T getField(Object from, String name)
	{
		if (from == null)
			return null;
		if (name == null)
			return null;
		Class<?> checkClass = from.getClass();
		do
		{
			try
			{
				Field field = checkClass.getDeclaredField(name);
				field.setAccessible(true);
				return (T) field.get(from);
			}
			catch (NoSuchFieldException | IllegalAccessException e)
			{
			}
		}
		while (checkClass.getSuperclass() != Object.class && ((checkClass = checkClass.getSuperclass()) != null));
		return null;
	}

	public static String[] getCommands(String command)
	{
		return command.replaceAll("COMMAND\\[", "").replaceAll("]", "").split(";");
	}

	public static String removeWhitespace(String string)
	{
		return string.replaceAll(" ", "");
	}

	public static <T> void setField(Object from, String name, T data)
	{
		if (from == null)
			return;
		if (name == null)
			return;
		Class<?> checkClass = from.getClass();
		do
		{
			try
			{
				Field field = checkClass.getDeclaredField(name);
				field.setAccessible(true);
				field.set(from, data);
			}
			catch (NoSuchFieldException | IllegalAccessException e)
			{
			}
		}
		while (checkClass.getSuperclass() != Object.class && ((checkClass = checkClass.getSuperclass()) != null));
	}

	public static final int random(int origin, int bound)
	{
		if (origin == bound)
			return origin;
		return new Random().nextInt(bound - origin) + origin;
	}

	public static final long random(long origin, long bound)
	{
		Random rand = new Random();
		long r = rand.nextLong();
		long n = bound - origin, m = n - 1;
		if ((n & m) == 0L) // power of two
			r = (r & m) + origin;
		else if (n > 0L)
		{ // reject over-represented candidates
			for (long u = r >>> 1; // ensure nonnegative
					u + m - (r = u % n) < 0L; // rejection check
					u = rand.nextLong() >>> 1) // retry
				;
			r += origin;
		}
		else
		{ // range not representable as long
			while (r < origin || r >= bound)
				r = rand.nextLong();
		}
		return r;
	}

	public static final double random(double origin, double bound)
	{
		return origin + new Random().nextFloat() * (bound - origin);
	}

	public static final <T1, T2> Map.Entry<T1, T2> simpleEntry(T1 key, T2 value)
	{
		return new AbstractMap.SimpleEntry<>(key, value);
	}

	public static final boolean elapsed(long from, long required)
	{
		return ((System.currentTimeMillis() - from) > required);
	}
	
	public static final <T> String serializeEnum(T e)
	{
		return e.getClass().getSimpleName() + "." + e.toString();
	}
}
