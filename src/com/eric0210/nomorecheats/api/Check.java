package com.eric0210.nomorecheats.api;

import java.math.RoundingMode;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.eric0210.nomorecheats.AntiCheat;
import com.eric0210.nomorecheats.Config;
import com.eric0210.nomorecheats.Config.CheckConfig;
import com.eric0210.nomorecheats.api.event.events.CheatDetectionEvent;
import com.eric0210.nomorecheats.api.util.MathUtils;

public abstract class Check
{
	protected String name = "";
	private Config.CheckConfig checkconfig;

	public Check(String name)
	{
		this.name = name;
		this.checkconfig = new CheckConfig(this);
		this.checkconfig.isCheckEnabled();
	}

	public final boolean isEnabled()
	{
		return this.checkconfig.isCheckEnabled();
	}

	public final String getName()
	{
		return this.name;
	}

	public final CheckConfig getConfig()
	{
		return this.checkconfig;
	}

	public final void suspect(Player target, double vl, Object... tags)
	{
		if (target.isDead() || !target.isOnline())
			return;
		vl = MathUtils.round(vl, 1, RoundingMode.FLOOR);
		CheatDetectionEvent event = new CheatDetectionEvent(this, target, vl, tags);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;
		Player target2 = event.getPlayer();
		double vl2 = event.getViolationLevel();
		Object[] tags2 = event.getViolationTags();
		new Violation(target2, this, vl2, tags2).plus();
		if (this.getConfig().getCommand((int) Math.round(vl2)) != null)
		{
			String cmd = this.getConfig().getCommand((int) Math.round(vl2));
			if (cmd != null)
			{
				cmd = cmd.replaceAll("%player%", target2.getName());
				AntiCheat.antiCheat().getServer()
						.dispatchCommand(AntiCheat.antiCheat().getServer().getConsoleSender(), cmd);
			}
		}
	}
}
