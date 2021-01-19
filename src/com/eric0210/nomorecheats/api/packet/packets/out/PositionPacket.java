package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;

import net.minecraft.server.v1_7_R4.PacketPlayOutPosition;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class PositionPacket extends PacketOut
{
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	private boolean flag;

	public PositionPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutPosition packet)
	{
		super(owner, PacketTypeOut.ENTITY_POSITION, packet);
		this.x = InternalUtils.getField(packet, "a");
		this.y = InternalUtils.getField(packet, "b");
		this.z = InternalUtils.getField(packet, "c");
		this.yaw = InternalUtils.getField(packet, "d");
		this.pitch = InternalUtils.getField(packet, "e");
		this.flag = InternalUtils.getField(packet, "f");
	}

	public PositionPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, double x, double y, double z, float yaw, float pitch, boolean flag)
	{
		super(owner, PacketTypeOut.ENTITY_POSITION, new PacketPlayOutPosition(x, y, z, yaw, pitch, flag));
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.flag = flag;
	}

	public PositionPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, Location loc, boolean ground)
	{
		this(owner, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), ground);
	}

	public void setFlag(boolean b)
	{
		this.flag = b;
	}

	public boolean flag()
	{
		return this.flag;
	}

	public void setPosition(Vector vec)
	{
		this.x = vec.getX();
		this.y = vec.getY();
		this.z = vec.getZ();
	}

	public Vector getPosition()
	{
		return new Vector(this.x, this.y, this.z);
	}

	public void setFacing(byte _yaw, byte _pitch)
	{
		this.yaw = _yaw;
		this.pitch = _pitch;
	}

	public float[] getFacing()
	{
		return new float[]
		{
				this.yaw, this.pitch
		};
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutPosition(this.x, this.y, this.z, this.yaw, this.pitch, this.flag);
	}
}
