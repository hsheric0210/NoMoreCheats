package com.eric0210.nomorecheats.api.util;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_7_R4.AxisAlignedBB;
import net.minecraft.server.v1_7_R4.Entity;

public class AABB implements Cloneable
{
	private Vector min;
	private Vector max;

	public AABB(Vector min, Vector max)
	{
		this.min = min;
		this.max = max;
	}

	public AABB(Block block)
	{
		net.minecraft.server.v1_7_R4.Block nms = NMS.asNMS(block);
		this.min = new Vector(nms.x(), nms.y(), nms.z());
		this.max = new Vector(nms.A(), nms.B(), nms.C());
	}

	public AABB(org.bukkit.entity.Entity ent)
	{
		Entity e = ((CraftEntity) ent).getHandle();
		AxisAlignedBB aabb = e.boundingBox;
		Vector min = new Vector(aabb.a, aabb.b, aabb.c);
		Vector max = new Vector(aabb.d, aabb.e, aabb.f);
		this.min = min;
		this.max = max;
	}

	/**
	 * Calculates intersection with the given ray between x certain distance
	 * interval.
	 *
	 * Ray-box intersection is using IEEE numerical properties to ensure the test is
	 * both robust and efficient, as described in:
	 *
	 * Amy Williams, Steve Barrus, R. Keith Morley, and Peter Shirley: "An Efficient
	 * and Robust Ray-Box Intersection Algorithm" Journal of graphics tools,
	 * 10(1):49-54, 2005
	 *
	 * @param ray     incident ray
	 * @param minDist minimum distance
	 * @param maxDist maximum distance
	 * @return intersection point on the bounding box (only the first is returned)
	 *         or null if no intersection
	 */
	public Vector intersectsRay(Ray ray, float minDist, float maxDist)
	{
		Vector invDir = new Vector(1f / ray.getDirection().getX(), 1f / ray.getDirection().getY(),
				1f / ray.getDirection().getZ());
		boolean signDirX = invDir.getX() < 0;
		boolean signDirY = invDir.getY() < 0;
		boolean signDirZ = invDir.getZ() < 0;
		Vector bbox = signDirX ? this.max : this.min;
		double tmin = (bbox.getX() - ray.getOrigin().getX()) * invDir.getX();
		bbox = signDirX ? this.min : this.max;
		double tmax = (bbox.getX() - ray.getOrigin().getX()) * invDir.getX();
		bbox = signDirY ? this.max : this.min;
		double tymin = (bbox.getY() - ray.getOrigin().getY()) * invDir.getY();
		bbox = signDirY ? this.min : this.max;
		double tymax = (bbox.getY() - ray.getOrigin().getY()) * invDir.getY();
		if ((tmin > tymax) || (tymin > tmax))
		{
			return null;
		}
		if (tymin > tmin)
		{
			tmin = tymin;
		}
		if (tymax < tmax)
		{
			tmax = tymax;
		}
		bbox = signDirZ ? this.max : this.min;
		double tzmin = (bbox.getZ() - ray.getOrigin().getZ()) * invDir.getZ();
		bbox = signDirZ ? this.min : this.max;
		double tzmax = (bbox.getZ() - ray.getOrigin().getZ()) * invDir.getZ();
		if ((tmin > tzmax) || (tzmin > tmax))
		{
			return null;
		}
		if (tzmin > tmin)
		{
			tmin = tzmin;
		}
		if (tzmax < tmax)
		{
			tmax = tzmax;
		}
		if ((tmin < maxDist) && (tmax > minDist))
		{
			return ray.getPointAtDistance(tmin);
		}
		return null;
	}

	public void add(Vector vector)
	{
		this.min.add(vector);
		this.max.add(vector);
	}

	public boolean b(AABB aabb)
	{
		if (aabb.getMax().getX() <= this.min.getX() || aabb.getMin().getX() >= this.max.getX())
		{
			return false;
		}
		if (aabb.getMax().getY() <= this.min.getY() || aabb.getMin().getY() >= this.max.getY())
		{
			return false;
		}
		if (aabb.max.getZ() <= this.min.getZ() || aabb.min.getZ() >= this.max.getZ())
		{
			return false;
		}
		return true;
	}

	public boolean isCollideWith(AABB other)
	{
		if (this.max.getX() < other.getMin().getX() || this.min.getX() > other.getMax().getX())
		{
			return false;
		}
		if (this.max.getY() < other.getMin().getY() || this.min.getY() > other.getMax().getY())
		{
			return false;
		}
		if (this.max.getZ() < other.getMin().getZ() || this.min.getZ() > other.getMax().getZ())
		{
			return false;
		}
		return true;
	}

	@Override
	public AABB clone()
	{
		AABB clone;
		try
		{
			clone = (AABB) super.clone();
			clone.min = this.min.clone();
			clone.max = this.max.clone();
			return clone;
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public double getVolume()
	{
		return (this.max.getX() - this.min.getX()) * (this.max.getY() - this.min.getY()) * (this.max.getZ() - this.min.getZ());
	}

	public Vector getMax()
	{
		return this.max;
	}

	public Vector getMin()
	{
		return this.min;
	}
}
