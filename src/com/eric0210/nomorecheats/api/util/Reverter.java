package com.eric0210.nomorecheats.api.util;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import com.eric0210.nomorecheats.AntiCheat;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.util.Protections.ProtectionType;

public class Reverter
{
	public static boolean allowRevert = true;
	private static HashMap<String, Reverter> reverterInstances = new HashMap<>();
	private String id;
	private HashMap<UUID, Location> revertPoints = new HashMap<>();

	private Reverter(String name)
	{
		this.id = name;
		allowRevert = !AntiCheat.antiCheat().getConfiguration().isSilentMode();
	}

	public static final void resetAllPositions()
	{
		for (String id : reverterInstances.keySet())
		{
			Reverter inst = reverterInstances.get(id);
			inst.revertPoints.clear();
			reverterInstances.put(id, inst);
		}
	}

	public static final void setAllPositions(Location point)
	{
		for (String str : reverterInstances.keySet())
		{
			Reverter inst = reverterInstances.get(str);
			for (UUID uid : inst.revertPoints.keySet())
				inst.revertPoints.put(uid, point);
			reverterInstances.put(str, inst);
		}
	}

	public static final void setAllPositions(UUID uuidPlayer, Location point)
	{
		for (String str : reverterInstances.keySet())
		{
			Reverter inst = reverterInstances.get(str);
			inst.revertPoints.put(uuidPlayer, point);
			reverterInstances.put(str, inst);
		}
	}

	public static final Reverter getInstance(Check check)
	{
		return getInstance(check.getName() + "-" + check.getClass().getSimpleName());
	}

	public static final Reverter getInstance(String name)
	{
		// String name = check.getName();
		Reverter instance;
		if (reverterInstances.containsKey(name))
			instance = reverterInstances.get(name);
		else
		{
			instance = new Reverter(name);
			reverterInstances.put(name, instance);
		}
		return instance;
	}

	public final String getId()
	{
		return this.id;
	}

	public final void setPosition(UUID puid, Location point)
	{
		this.revertPoints.put(puid, point.clone());
	}

	public final void teleport(Player p)
	{
		if (allowRevert && this.revertPoints.containsKey(p.getUniqueId()))
		{
			Location point = this.revertPoints.get(p.getUniqueId());
			Vector velocity = p.getVelocity();
			p.setVelocity(new Vector());
			point = fixLocation(point);
			float fallDistance = Cache.get(p.getUniqueId(), "fallDistance", 0.0F);
			Protections.putProtection(p, ProtectionType.TELEPORT, 1);
			Protections.putProtection(p, ProtectionType.ASCENSION, 1);
			Protections.putProtection(p, ProtectionType.UNNORMAL_Y, 1);

			p.teleport(point, TeleportCause.UNKNOWN);
			if (GroundChecks.isOnGround(p))
			{
				if (fallDistance > 1.5)
				{
					p.damage(Math.floor(fallDistance + velocity.getY()));
				}
			}
		}
	}

	private Location fixLocation(Location point)
	{
		double x = point.getX();
		double z = point.getZ();
		double bx = point.getBlockX();
		double bz = point.getBlockZ();
		double xdelta = x - bx;
		double zdelta = z - bz;
		Location loc2 = point.clone();
		if (xdelta < .3)
			loc2.setX(bx - .3);
		else if (xdelta > .7)
			loc2.setX(bx + .7);
		if (zdelta < .3)
			loc2.setZ(bz - .3);
		else if (zdelta > .7)
			loc2.setZ(bz + .7);
		return point;
	}

	public final boolean hasPosition(UUID puid)
	{
		return this.revertPoints.containsKey(puid);
	}

	public final Location getPosition(UUID puid)
	{
		return this.revertPoints.get(puid);
	}
}
