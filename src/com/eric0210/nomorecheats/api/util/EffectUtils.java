package com.eric0210.nomorecheats.api.util;

import com.eric0210.nomorecheats.AntiCheat;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutWorldEvent;
import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.util.org.apache.commons.lang3.Validate;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.CraftEffect;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;


public class EffectUtils
{
	public static final void colorDust(Player p, Location start, Location end, Color dustColor)
	{
		double accuracy = 0.1;
		Bukkit.getScheduler().runTask(AntiCheat.antiCheat(), () ->
			{
				for (double x = start.getX(); x <= end.getX(); x += accuracy)
				{
					for (double y = start.getY(); y <= end.getY(); y += accuracy)
					{
						for (double z = start.getZ(); z <= end.getZ(); z += accuracy)
						{
							Location loc = new Location(p.getWorld(), x, y, z);
							p.getWorld().playEffect(loc, Effect.COLOURED_DUST, 1);
							p.getWorld().playEffect(loc, Effect.COLOURED_DUST, 1);
						}
					}
				}
			});
	}

	public static final <T> void sendPacketEffect(Player p, Location loc, Effect eff, T tdata, Vector offset,
			float speed, int pcount, int rad)
	{
		boolean istdatamd = false;
		int data = 0;
		int id = 0;
		if (tdata != null)
		{
			Validate.isTrue(tdata.getClass().equals(eff.getData()), "Wrong kind of tdata for this effect!");
		}
		else
		{
			Validate.isTrue(eff.getData() == null, "Wrong kind of tdata for this effect!");
		}
		if (tdata != null && tdata.getClass().equals(MaterialData.class))
		{
			MaterialData md = (MaterialData) tdata;
			Validate.isTrue(md.getItemType().isBlock(), "Material must be block");
			id = md.getItemTypeId();
			data = md.getData();
			istdatamd = true;
		}
		float offx = 0F;
		float offy = 0F;
		float offz = 0F;
		if (offset != null)
		{
			offx = (float) offset.getX();
			offy = (float) offset.getY();
			offz = (float) offset.getZ();
		}
		Packet pkt;
		Validate.notNull(p, "player cannot be null");
		Validate.notNull(loc, "location cannot be null");
		Validate.notNull(eff, "effect cannot be null");
		Validate.notNull(p.getWorld(), "world cannot be null");
		if (!istdatamd)
			id = tdata == null ? 0 : CraftEffect.getDataValue(eff, tdata);
		if (eff.getType() != Effect.Type.PARTICLE)
		{
			int pktdata = eff.getId();
			pkt = new PacketPlayOutWorldEvent(pktdata, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), id, false);
		}
		else
		{
			StringBuilder pfn = new StringBuilder();
			pfn.append(eff.getName());
			if (eff.getData() != null
					&& (eff.getData().equals(Material.class) || eff.getData().equals(MaterialData.class)))
			{
				pfn.append('_').append(id);
			}
			if (eff.getData() != null && eff.getData().equals(MaterialData.class))
			{
				pfn.append('_').append(data);
			}
			pkt = new PacketPlayOutWorldParticles(pfn.toString(), (float) loc.getX(), loc.getYaw(),
					(float) loc.getZ(), offx, offy, offz, speed, pcount);
		}
		rad *= rad;
		PlayerConnection pc = NMS.asNMS(p).playerConnection;
		if (pc != null && loc.getWorld().equals(p.getWorld()) && p.getLocation().distanceSquared(loc) > rad)
			return;
		pc.sendPacket(pkt);
	}

	public static final void colorDustByBlock(Player p, Location bl, Color dustColor)
	{
		colorDustByAABB(p, new AABB(bl.getBlock()), dustColor);
	}

	public static final void colorDustByAABB(Player p, AABB aabb, Color dustColor)
	{
		Location start = new Location(p.getWorld(), aabb.getMin().getX(), aabb.getMin().getY(), aabb.getMin().getZ());
		Location end = new Location(p.getWorld(), aabb.getMax().getX(), aabb.getMax().getY(), aabb.getMax().getZ());
		colorDust(p, start, end, dustColor);
	}

	public static final void breakBlockPacketEffect(Player p, Location loc)
	{
		if (loc.getWorld().equals(p.getWorld()) && loc.getBlock().getType() != Material.AIR)
		{
			Material mat = loc.getBlock().getType();
			p.getWorld().getBlockAt(loc).setType(Material.AIR);
			// p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, mat);
			sendPacketEffect(p, loc, Effect.STEP_SOUND, mat, null, 1F, 1, 16);
		}
	}
}
