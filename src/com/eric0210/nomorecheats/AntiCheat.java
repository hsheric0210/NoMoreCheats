package com.eric0210.nomorecheats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.util.Protections;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.TickTasks;
import com.eric0210.nomorecheats.api.util.Checks;
import com.eric0210.nomorecheats.api.util.Lag;
import com.eric0210.nomorecheats.api.util.Reverter;
import com.eric0210.nomorecheats.checks.combat.killaura.KillauraNPC;

public class AntiCheat extends JavaPlugin
{
	private static AntiCheat instance;
	private Config config;

	public Config getConfiguration()
	{
		return this.config;
	}
	
	public static AntiCheat antiCheat()
	{
		if (instance == null)
			throw new IllegalStateException("Trying to get instance before initialized");
		return instance;
	}

	@Override
	public void onEnable()
	{
		instance = this;
		this.config = new Config(this);
		if (!Bukkit.getVersion().contains("1.7.10"))
		{
			Logging.logBukkit(this.config.anticheatName() + " is only dedicated for Minecraft 1.7.10(1_7_R4)");
			setEnabled(false);
			return;
		}
		Bukkit.getPluginManager().registerEvents(new EventManager(), this);
		PacketManager.applyPacketFilter(Arrays.asList(Bukkit.getOnlinePlayers()));
		Checks.initalizeChecks();
		Cooldowns.init();
		Lag.initialize();
		Reverter.resetAllPositions();
		TickTasks.runTasks();
		new Protections();
	}

	@Override
	public void onDisable() // invoked on server stopped or reloading
	{
		KillauraNPC.disable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command c, String label, String[] arguments)
	{
		if ((c.getName().equalsIgnoreCase("nmc")) || (c.getName().equalsIgnoreCase("nomorecheats")))
		{
			Logging.send(sender,
					String.format("%s %s", new Object[] { this.config.anticheatName(), getDescription().getVersion() }));
			return true;
		}
		return false;
	}
}
