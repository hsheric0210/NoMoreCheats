package com.eric0210.nomorecheats.api.util;

import java.math.RoundingMode;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class YMap
{
	private static HashMap<Integer, YMap> maps = new HashMap<>();
	private HashMap<Integer, Double> map = new HashMap<>();
	static
	{
		YMap norm = new YMap();		// Normal Jump
		norm.set(0, 0.419);
		norm.set(1, 0.333);
		norm.set(2, 0.248);
		norm.set(3, 0.164);
		norm.set(4, 0.083);
		norm.set(5, 0.419);
		maps.put(0, norm);
		YMap one = new YMap();		// Jump with Jump Effect level 1
		one.set(0, 0.519);
		one.set(1, 0.431);
		one.set(2, 0.344);
		one.set(3, 0.258);
		one.set(4, 0.175);
		one.set(5, 0.093);
		one.set(6, 0.013);
		maps.put(1, one);
		YMap two = new YMap();		// Jump with Jump Effect level 2
		two.set(0, 0.62);
		two.set(1, 0.529);
		two.set(2, 0.44);
		two.set(3, 0.353);
		two.set(4, 0.267);
		two.set(5, 0.183);
		two.set(6, 0.101);
		two.set(7, 0.021);
		maps.put(2, two);
	}

	public static YMap get(int modifier)
	{
		return maps.get(modifier);
	}

	public static YMap get(Player p)
	{
		return get(PlayerUtils.getPotionEffectLevel(p, PotionEffectType.JUMP));
	}

	private void set(int index, double y)
	{
		this.map.put(index, y);
	}

	public boolean containsY(double y)
	{
		return this.map.containsValue(MathUtils.round(y, 3, RoundingMode.FLOOR));
	}

	public boolean containsY(double y, double d)
	{
		for (double _y : this.map.values())
		{
			return Math.abs(_y - y) <= d;
		}
		return false;
	}

	public boolean containsTicksUp(int index)
	{
		return this.map.containsKey(index);
	}

	public double getY(int index)
	{
		return this.map.containsKey(index) ? this.map.get(index) : 0;
	}

	public int size()
	{
		return this.map.size();
	}
}
