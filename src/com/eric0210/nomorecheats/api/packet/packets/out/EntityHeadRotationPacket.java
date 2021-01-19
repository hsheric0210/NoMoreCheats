package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityHeadRotation;

public class EntityHeadRotationPacket extends PacketOut
{
	private int entityid;
	private byte yaw;

	public EntityHeadRotationPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutEntityHeadRotation packet)
	{
		super(owner, PacketTypeOut.ENTITY_TELEPORT, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.yaw = InternalUtils.getField(packet, "b");
	}

	public EntityHeadRotationPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int entityid, byte yaw)
	{
		super(owner, PacketTypeOut.ENTITY_TELEPORT, new PacketPlayOutEntityHeadRotation(new Entity(null)
		{
			@Override
			protected void c()
			{
				// TODO Auto-generated method stub
			}

			@Override
			protected void b(NBTTagCompound var1)
			{
				// TODO Auto-generated method stub
			}

			@Override
			protected void a(NBTTagCompound var1)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public int getId()
			{
				return entityid;
			}
		}, yaw));
		this.entityid = entityid;
		this.yaw = yaw;
	}

	public EntityHeadRotationPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, net.minecraft.server.v1_7_R4.Entity entity)
	{
		this(owner, entity.getId(), (byte) (entity.yaw * 256F / 360F));
	}

	public void setEntity(int id)
	{
		this.entityid = id;
	}

	public int getEntity()
	{
		return this.entityid;
	}

	public void setYaw(byte _yaw)
	{
		this.yaw = _yaw;
	}

	public void convertAndSetYaw(float _yaw)
	{
		this.yaw = (byte) (_yaw * 256F / 360F);
	}

	public float getYaw()
	{
		return this.yaw;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayOutEntityHeadRotation headrotation = new PacketPlayOutEntityHeadRotation();
		InternalUtils.setField(headrotation, "a", this.entityid);
		InternalUtils.setField(headrotation, "b", this.yaw);
		return headrotation;
	}
}
