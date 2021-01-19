package com.eric0210.nomorecheats.api;

import org.bukkit.entity.Player;

import com.eric0210.nomorecheats.Logging;

import java.util.List;

import com.eric0210.nomorecheats.AntiCheat;
import com.eric0210.nomorecheats.Config;

public class Violation
{
	public Player owner;
	public double level;
	public Check check;
	public Object[] tags;

	public Violation(Player target, Check check, double vl, Object... tags)
	{
		this.owner = target;
		this.level = vl;
		this.check = check;
		this.tags = tags;
	}

	public void plus()
	{
		ViolationMap.getInstance(this.owner).plus(this);
		Config config = AntiCheat.antiCheat().getConfiguration();
		if (config.loggingEnabled())
		{
			String format = config.logCheatFormat();
			String tag = "";
			StringBuilder tagBuilder = new StringBuilder();
			if (this.tags != null && this.tags.length > 0)
			{
				for (Object t : this.tags)
				{
					if (t != null)
						tagBuilder.append(", " + String.valueOf(t));
				}
				tag = tagBuilder.toString().substring(2);
			}
			format = format.replaceAll("%player%", this.owner.getName());
			format = format.replaceAll("%total_vl%", String.valueOf(ViolationMap.getInstance(this.owner).getLevel(this.check)));
			format = format.replaceAll("%vl%", String.valueOf(this.level));
			format = format.replaceAll("%check%", this.check.getName());
			format = format.replaceAll("%tags%", tag);
			Logging.logBukkit(format);
		}
	}

	public static final String convertTagListtoTag(List<String> tags)
	{
		String tag = "";
		if (tags != null && !tags.isEmpty())
		{
			StringBuilder tagBuilder = new StringBuilder();
			for (String t : tags)
			{
				if (t != null)
					tagBuilder.append(" + " + t);
			}
			if (tagBuilder.length() > 3)
				tag = tagBuilder.toString().substring(3);
		}
		return tag;
	}
}
