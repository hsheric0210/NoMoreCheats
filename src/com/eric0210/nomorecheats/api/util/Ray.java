package com.eric0210.nomorecheats.api.util;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.World;
import org.bukkit.util.Vector;

import com.eric0210.nomorecheats.AntiCheat;

public class Ray implements Cloneable
{
	private Vector origin;
	private Vector direction;

	public Ray(Vector origin, Vector direction)
	{
		this.origin = origin;
		this.direction = direction;
	}

	public Vector getPointAtDistance(double distance)
	{
		Vector dir = new Vector(this.direction.getX(), this.direction.getY(), this.direction.getZ());
		Vector orig = new Vector(this.origin.getX(), this.origin.getY(), this.origin.getZ());
		return orig.add(dir.multiply(distance));
	}

	@Override
	public Ray clone()
	{
		Ray clone;
		try
		{
			clone = (Ray) super.clone();
			clone.origin = this.origin.clone();
			clone.direction = this.direction.clone();
			return clone;
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void highlight(World world, double blocksAway, double accuracy)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(AntiCheat.antiCheat(), () -> {
			for (double x = 0; x < blocksAway; x += accuracy)
			{
				world.playEffect(getPointAtDistance(x).toLocation(world), Effect.COLOURED_DUST, 1);
			}
		}, 0L);
	}

	@Override
	public String toString()
	{
		return "origin: " + this.origin + ", direction: " + this.direction;
	}

	public Vector getOrigin()
	{
		return this.origin;
	}

	public Vector getDirection()
	{
		return this.direction;
	}
}
