package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.DataWatcher;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityLiving;

public class SpawnEntityLivingPacket extends PacketOut
{
	private int entityid;
	private int entitytype;
	private int x;
	private int y;
	private int z;
	private byte yaw;
	private byte pitch;
	private byte aO;
	private int velX;
	private int velY;
	private int velZ;
	private DataWatcher dw;

	public SpawnEntityLivingPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutSpawnEntityLiving packet)
	{
		super(owner, PacketTypeOut.SPAWN_ENTITY_LIVING, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.entitytype = InternalUtils.getField(packet, "b");
		this.x = InternalUtils.getField(packet, "c");
		this.y = InternalUtils.getField(packet, "d");
		this.z = InternalUtils.getField(packet, "e");
		this.velX = InternalUtils.getField(packet, "f");
		this.velY = InternalUtils.getField(packet, "g");
		this.velZ = InternalUtils.getField(packet, "h");
		this.yaw = InternalUtils.getField(packet, "i");
		this.pitch = InternalUtils.getField(packet, "j");
		this.aO = InternalUtils.getField(packet, "k");
		this.dw = InternalUtils.getField(packet, "l");
	}

	public SpawnEntityLivingPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, EntityLiving entityliving)
	{
		super(owner, PacketTypeOut.SPAWN_ENTITY_LIVING, new PacketPlayOutSpawnEntityLiving(entityliving));
		this.entityid = entityliving.getId();
		this.entitytype = (byte) EntityTypes.a(entityliving);
		this.x = entityliving.as.a(entityliving.locX);
		this.y = (int) Math.floor(entityliving.locY * 32.0);
		this.z = entityliving.as.a(entityliving.locZ);
		this.yaw = (byte) (entityliving.yaw * 256.0f / 360.0f);
		this.pitch = (byte) (entityliving.pitch * 256.0f / 360.0f);
		this.aO = (byte) (entityliving.aO * 256.0f / 360.0f);
		double convertValue = 3.9;
		double mx = entityliving.motX;
		double my = entityliving.motY;
		double mz = entityliving.motZ;
		if (mx < -convertValue)
		{
			mx = -convertValue;
		}
		if (my < -convertValue)
		{
			my = -convertValue;
		}
		if (mz < -convertValue)
		{
			mz = -convertValue;
		}
		if (mx > convertValue)
		{
			mx = convertValue;
		}
		if (my > convertValue)
		{
			my = convertValue;
		}
		if (mz > convertValue)
		{
			mz = convertValue;
		}
		this.velX = (int) (mx * 8000.0);
		this.velY = (int) (my * 8000.0);
		this.velZ = (int) (mz * 8000.0);
		this.dw = entityliving.getDataWatcher();
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
		InternalUtils.setField(packet, "a", this.entityid);
		InternalUtils.setField(packet, "b", this.entitytype);
		InternalUtils.setField(packet, "c", this.x);
		InternalUtils.setField(packet, "d", this.y);
		InternalUtils.setField(packet, "e", this.z);
		InternalUtils.setField(packet, "f", this.velX);
		InternalUtils.setField(packet, "g", this.velY);
		InternalUtils.setField(packet, "h", this.velZ);
		InternalUtils.setField(packet, "i", this.yaw);
		InternalUtils.setField(packet, "j", this.pitch);
		InternalUtils.setField(packet, "k", this.aO);
		InternalUtils.setField(packet, "l", this.dw);
		return packet;
	}
}
