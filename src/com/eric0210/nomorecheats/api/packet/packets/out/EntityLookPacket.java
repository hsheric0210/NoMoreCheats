package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityLook;

public class EntityLookPacket extends PacketOut
{
	private int entityid;
	private byte yaw;
	private byte pitch;
	private boolean onGround;

	public EntityLookPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutEntityLook packet)
	{
		super(owner, PacketTypeOut.ENTITY_LOOK, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.yaw = InternalUtils.getField(packet, "b");
	}

	public EntityLookPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int entityid, byte yaw, byte pitch, boolean isOnGround)
	{
		super(owner, PacketTypeOut.ENTITY_TELEPORT, new PacketPlayOutEntityLook(entityid, yaw, pitch, isOnGround));
		this.entityid = entityid;
		this.yaw = yaw;
		this.pitch = pitch;
		this.onGround = isOnGround;
	}

	public EntityLookPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, net.minecraft.server.v1_7_R4.Entity entity)
	{
		this(owner, entity.getId(), (byte) (entity.yaw * 256F / 360F), (byte) (entity.pitch * 256F / 360F), entity.onGround);
	}

	public void setEntity(int id)
	{
		this.entityid = id;
	}

	public int getEntity()
	{
		return this.entityid;
	}

	public void setRotation(byte _yaw, byte _pitch)
	{
		this.yaw = _yaw;
		this.pitch = _pitch;
	}

	public void convertAndSetRotation(float _yaw, float _pitch)
	{
		this.yaw = (byte) (_yaw * 256F / 360F);
		this.pitch = (byte) (_pitch * 256F / 360F);
	}

	public byte[] getRotation()
	{
		return new byte[]
		{
				this.yaw, this.pitch
		};
	}

	public void setOnGround(boolean ground)
	{
		this.onGround = ground;
	}

	public boolean onGround()
	{
		return this.onGround;
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutEntityLook(this.entityid, this.yaw, this.pitch, this.onGround);
	}
}
