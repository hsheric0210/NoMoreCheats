package com.eric0210.nomorecheats.api.util;

import org.bukkit.World;
import org.bukkit.entity.Entity;


public class ServerUtils
{
	public static Entity getEntityById(World w, int id, boolean livingsonly)
	{
		for (Entity e : (livingsonly ? w.getLivingEntities() : w.getEntities()))
			if (e.getEntityId() == id)
				return e;
		return null;
	}
}
