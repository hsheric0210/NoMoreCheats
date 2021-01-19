package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityDestroy;

import org.bukkit.entity.Entity;

public class EntityDestroyPacket extends PacketOut
{
	private int[] entities;

	public EntityDestroyPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutEntityDestroy packet)
	{
		super(owner, PacketTypeOut.ENTITY_DESTROY, packet);
		this.entities = InternalUtils.getField(packet, "a");
	}

	public EntityDestroyPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int[] ids)
	{
		super(owner, PacketTypeOut.ENTITY_DESTROY, new PacketPlayOutEntityDestroy(ids));
		this.entities = ids;
	}

	public void setEntityIds(int[] ids)
	{
		this.entities = ids;
	}

	public void setEntity(Entity e)
	{
		this.entities = new int[]
		{
				e.getEntityId()
		};
	}

	public int[] getEntities()
	{
		return this.entities;
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutEntityDestroy(this.entities);
	}
}
