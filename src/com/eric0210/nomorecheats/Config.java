package com.eric0210.nomorecheats;

import java.io.File;
import java.io.IOException;

import com.eric0210.nomorecheats.api.Check;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config
{
	private static AntiCheat antiCheat;

	public Config(AntiCheat anticheat)
	{
		antiCheat = anticheat;
		if (!new File(anticheat.getDataFolder(), "config.yml").exists())
			anticheat.saveDefaultConfig();
		anticheat.reloadConfig();
	}

	private final <T> T getValue(String key, T defaultValue)
	{
		if (!antiCheat.getConfig().contains(key))
		{
			antiCheat.getConfig().set(key, defaultValue);
			antiCheat.reloadConfig();
		}
		T returnValue = null;
		try
		{
			returnValue = (T) antiCheat.getConfig().get(key);
		}
		catch (ClassCastException e)
		{
			returnValue = defaultValue;
		}
		return returnValue;
	}

	public final boolean isSilentMode()
	{
		return getValue("Silently", true);
	}

	public final String anticheatName()
	{
		return getValue("AnticheatName", "AntiCheat");
	}

	public final boolean loggingEnabled()
	{
		return getValue("Logging.Enabled", true);
	}

	public final boolean loggingDebugEnabled()
	{
		return getValue("Logging.DebugLogEnabled", false);
	}

	public final String IngamePrefix()
	{
		return ChatColor.translateAlternateColorCodes('&', getValue("Prefix.Ingame", "&r&6[&c%name%&6]> &6").replaceAll("%name%", anticheatName()));
	}

	public final String ConsolePrefix()
	{
		return ChatColor.translateAlternateColorCodes('&', getValue("Prefix.Console", "&r&8[&c%name%&8]> &8").replaceAll("%name%", anticheatName()));
	}

	public final String logCheatFormat()
	{
		return ChatColor.translateAlternateColorCodes('&',
				getValue("Logging.DetectionFormat", "%player% failed %check% [tags: %tags%] [VL: %vl%]"));
	}

	public static class CheckConfig
	{
		private Check check;
		private FileConfiguration config;
		private File configfile;

		public CheckConfig(Check c)
		{
			this.check = c;
			try
			{
				this.configfile = new File(antiCheat.getDataFolder(), "checks.yml");
				if (!this.configfile.exists())
					this.configfile.createNewFile();
				this.config = YamlConfiguration.loadConfiguration(this.configfile);
			}
			catch (IOException e)
			{
			}
		}

		public final <T> T getValue(String key, T defaultValue)
		{
			if (!this.config.contains(this.check.getName() + '.' + key))
			{
				this.config.set(this.check.getName() + '.' + key, defaultValue);
				try
				{
					this.config.save(this.configfile);
				}
				catch (IOException e)
				{
				}
			}
			T returnValue = null;
			try
			{
				returnValue = (T) this.config.get(this.check.getName() + '.' + key);
			}
			catch (ClassCastException e)
			{
				returnValue = defaultValue;
			}
			return returnValue;
		}

		public final boolean isCheckEnabled()
		{
			return getValue("enabled", true);
		}

		public final String getCommand(int vl)
		{
			ConfigurationSection config2 = this.config.getConfigurationSection("thresholds");
			if (config2 != null && config2.contains(String.valueOf(vl)))
			{
				return String.valueOf(config2.get(String.valueOf(vl)));
			}
			return null;
		}
	}
}
