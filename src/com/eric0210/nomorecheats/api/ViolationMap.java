package com.eric0210.nomorecheats.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ViolationMap
{
	private Player player;
	private static HashMap<UUID, ViolationMap> violationMaps = new HashMap<>();
	private ArrayList<Violation> violations = new ArrayList<>();

	private ViolationMap(Player p)
	{
		this.player = p;
	}

	public final void reset(Check check)
	{
		Iterator<Violation> itr = this.violations.iterator();
		while (itr.hasNext())
		{
			if (itr.next().check == check)
				itr.remove();
		}
	}
	
	public static final void reset(Player p)
	{
		if (violationMaps.containsKey(p.getUniqueId()))
		{
			violationMaps.remove(p.getUniqueId());
		}
	}

	public static final ViolationMap getInstance(Player p)
	{
		if (!violationMaps.containsKey(p.getUniqueId()))
			violationMaps.put(p.getUniqueId(), new ViolationMap(p));
		return violationMaps.get(p.getUniqueId());
	}

	public static final int getViolationSum(Player p, Collection<Check> c)
	{
		int vl = 0;
		for (Check check : c)
		{
			ViolationMap vm = violationMaps.getOrDefault(check, new ViolationMap(p));
			vl += vm.getLevel(check);
		}
		return vl;
	}

	public final void plus(Violation vio)
	{
		this.violations.add(vio);
	}

	public final int getLevel(Check c)
	{
		int level = 0;
		for (Violation vio : this.violations)
		{
			if (vio.check == c)
				level += vio.level;
		}
		return level;
	}

	public final Player getOwner()
	{
		return this.player;
	}
}
