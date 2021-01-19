package com.eric0210.nomorecheats.api.util;

import java.util.ArrayList;

import com.eric0210.nomorecheats.AntiCheat;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;


public class TickTasks
{
	private static final ArrayList<Runnable> tasks = new ArrayList<>();
	private static BukkitTask task = null;
	
	public static final void addTask(Runnable r)
	{
		tasks.add(r);
	}
	
	public static final void runTasks()
	{
		try
		{
			task = Bukkit.getScheduler().runTaskTimer(AntiCheat.antiCheat(), () ->
				{
					if (!tasks.isEmpty())
						tasks.forEach(t -> t.run());
				}, 0L, 1L);
		}
		catch (Exception ex)
		{
			if (task != null)
				task.cancel();
		}
	}
}
