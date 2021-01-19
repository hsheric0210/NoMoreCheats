package com.eric0210.nomorecheats.api.util;

import org.bukkit.Bukkit;


public class VersionUtils
{
	private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

	public static final boolean is1_7()
	{
		return version.contains("1_7");
	}
}
