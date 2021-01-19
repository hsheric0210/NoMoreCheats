package com.eric0210.nomorecheats;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Logging
{
	public static final void logBukkit(String msg)
	{
		Bukkit.getConsoleSender().sendMessage(AntiCheat.antiCheat().getConfiguration().ConsolePrefix() + msg);
	}

	public static final void broadcast(String msg, boolean adminonly)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (adminonly)
			{
				if (p.isOp())
					p.sendMessage(AntiCheat.antiCheat().getConfiguration().IngamePrefix() + msg);
				else
					continue;
			}
			else
				p.sendMessage(AntiCheat.antiCheat().getConfiguration().IngamePrefix() + msg);
		}
	}

	public static final void send(CommandSender target, String msg)
	{
		if (target == null)
			return;
		target.sendMessage(AntiCheat.antiCheat().getConfiguration().ConsolePrefix() + msg);
	}

	public static final void debug(String msg)
	{
		if (AntiCheat.antiCheat().getConfiguration().loggingDebugEnabled())
			Bukkit.getConsoleSender().sendMessage(AntiCheat.antiCheat().getConfiguration().ConsolePrefix() + "[Debug] " + msg);
	}
}
