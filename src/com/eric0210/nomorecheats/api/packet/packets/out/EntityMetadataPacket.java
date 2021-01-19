package com.eric0210.nomorecheats.api.packet.packets.out;

import java.util.List;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;
import net.minecraft.server.v1_7_R4.DataWatcher;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityMetadata;

public class EntityMetadataPacket extends PacketOut
{
	private int entityid;
	private List<Object> metadata;

	public EntityMetadataPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutEntityMetadata packet)
	{
		super(owner, PacketTypeOut.ENTITY_METADATA, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.metadata = InternalUtils.getField(packet, "b");
	}

	public EntityMetadataPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int eid, DataWatcher datawatcher, boolean flag)
	{
		super(owner, PacketTypeOut.ENTITY_METADATA, new PacketPlayOutEntityMetadata(eid, datawatcher, flag));
		datawatcher.watch(6, 0.1F);
		this.entityid = eid;
		this.metadata = flag ? datawatcher.c() : datawatcher.b();
	}

	public void setEntity(int eid)
	{
		this.entityid = eid;
	}

	public int getEntity()
	{
		return this.entityid;
	}

	public List<Object> getData()
	{
		return this.metadata;
	}

	public void setData(List<Object> data)
	{
		this.metadata = data;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayOutEntityMetadata anim = new PacketPlayOutEntityMetadata();
		InternalUtils.setField(anim, "a", this.entityid);
		InternalUtils.setField(anim, "b", this.metadata);
		return anim;
	}
}
