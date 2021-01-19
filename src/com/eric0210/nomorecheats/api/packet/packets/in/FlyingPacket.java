package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInFlying;
import net.minecraft.server.v1_7_R4.PacketPlayInLook;
import net.minecraft.server.v1_7_R4.PacketPlayInPosition;
import net.minecraft.server.v1_7_R4.PacketPlayInPositionLook;

import org.bukkit.util.Vector;

public class FlyingPacket extends PacketIn
{
	private Vector to;
	private double stance;
	private Vector rotation;
	private boolean hasPos;
	private boolean hasLook;
	private boolean isOnGround;

	public FlyingPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInFlying packet)
	{
		super(owner, PacketTypeIn.FLYING, packet);
		if (this.hasPos = packet.j())
		{
			this.to = new Vector(packet.c(), packet.d(), packet.e());
			this.stance = packet.f();
		}
		else
		{
			this.to = new Vector();
			this.stance = 0;
		}
		if (this.hasLook = packet.k())
			this.rotation = new Vector(packet.g(), packet.h(), 0);
		this.isOnGround = packet.i();
	}

	public Vector getTo()
	{
		return this.to;
	}

	public boolean hasPos()
	{
		return this.hasPos;
	}

	public boolean isOnGround()
	{
		return this.isOnGround;
	}

	public void setOnGround(boolean og)
	{
		this.isOnGround = og;
	}

	public float getYaw()
	{
		if (this.hasLook)
			return (float) this.rotation.getX();
		return 0F;
	}

	public float getPitch()
	{
		if (this.hasLook)
			return (float) this.rotation.getY();
		return 0F;
	}

	public boolean hasLook()
	{
		return this.hasLook;
	}

	public double getStance()
	{
		return this.stance;
	}

	public void setStance(double d)
	{
		this.stance = d;
	}

	public void setTo(Vector vec)
	{
		if (this.hasPos)
			this.to = vec.clone();
	}

	public void setYaw(float y)
	{
		if (this.hasLook)
			this.rotation.setX(y);
	}

	public void setPitch(float p)
	{
		if (this.hasLook)
			this.rotation.setY(p);
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInFlying flying = new PacketPlayInFlying();
		if (this.hasPos)
			flying = new PacketPlayInPosition();
		else if (this.hasLook)
			flying = new PacketPlayInLook();
		if (this.hasPos && this.hasLook)
			flying = new PacketPlayInPositionLook();
		if (this.hasPos)
		{
			InternalUtils.setField(flying, "x", this.to.getX());
			InternalUtils.setField(flying, "y", this.to.getY());
			InternalUtils.setField(flying, "z", this.to.getZ());
		}
		if (this.hasLook)
		{
			InternalUtils.setField(flying, "yaw", (float) this.rotation.getX());
			InternalUtils.setField(flying, "pitch", (float) this.rotation.getY());
		}
		InternalUtils.setField(flying, "stance", this.stance);
		InternalUtils.setField(flying, "g", this.isOnGround);
		return flying;
	}
}
