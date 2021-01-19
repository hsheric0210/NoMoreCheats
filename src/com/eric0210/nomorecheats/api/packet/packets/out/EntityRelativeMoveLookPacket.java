package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutRelEntityMoveLook;

import org.bukkit.util.Vector;

public class EntityRelativeMoveLookPacket extends PacketOut
{
	private int entityid;
	private byte relative_x;
	private byte relative_y;
	private byte relative_z;
	private byte yaw;
	private byte pitch;
	private boolean onground;

	public EntityRelativeMoveLookPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutRelEntityMoveLook packet)
	{
		super(owner, PacketTypeOut.ENTITY_RELMOVELOOK, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.relative_x = InternalUtils.getField(packet, "b");
		this.relative_y = InternalUtils.getField(packet, "c");
		this.relative_z = InternalUtils.getField(packet, "d");
		this.yaw = InternalUtils.getField(packet, "e");
		this.pitch = InternalUtils.getField(packet, "f");
		this.onground = InternalUtils.getField(packet, "onGround");
	}

	public EntityRelativeMoveLookPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int entityid, byte xacc, byte yacc, byte zacc, byte yaw, byte pitch, boolean onground)
	{
		super(owner, PacketTypeOut.ENTITY_RELMOVELOOK, new PacketPlayOutRelEntityMoveLook(entityid, xacc, yacc, zacc, yaw, pitch, onground));
		this.entityid = entityid;
		this.relative_x = xacc;
		this.relative_y = yacc;
		this.relative_z = zacc;
		this.yaw = yaw;
		this.pitch = pitch;
		this.onground = onground;
	}

	public void setEntity(int id)
	{
		this.entityid = id;
	}

	public int getEntity()
	{
		return this.entityid;
	}

	public void setOnGround(boolean b)
	{
		this.onground = b;
	}

	public boolean isOnGround()
	{
		return this.onground;
	}

	public void convertAndSetRelativeDeltaPosition(Vector currentpos, Vector lastpos)
	{
		this.relative_x = (byte) (Math.ceil(currentpos.getX() * 32 - lastpos.getX() * 32) * 2);
		this.relative_y = (byte) (Math.ceil(currentpos.getY() * 32 - lastpos.getY() * 32) * 2);
		this.relative_z = (byte) (Math.ceil(currentpos.getZ() * 32 - lastpos.getZ() * 32) * 2);
	}

	public void setRelativeDeltaPosition(Vector vec)
	{
		this.relative_x = (byte) vec.getX();
		this.relative_y = (byte) vec.getY();
		this.relative_z = (byte) vec.getZ();
	}

	public Vector getRelativeDeltaPosition()
	{
		return new Vector(this.relative_x, this.relative_y, this.relative_z);
	}

	public void convertAndSetRotation(float yaw, float pitch)
	{
		this.yaw = (byte) (yaw * 256F / 360F);
		this.pitch = (byte) (pitch * 256F / 360F);
	}

	public void setRotation(byte yaw, byte pitch)
	{
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public byte[] getRotation()
	{
		return new byte[]
		{
				this.yaw, this.pitch
		};
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutRelEntityMoveLook(this.entityid, this.relative_x, this.relative_y, this.relative_z, this.yaw, this.pitch, this.onground);
	}
}
