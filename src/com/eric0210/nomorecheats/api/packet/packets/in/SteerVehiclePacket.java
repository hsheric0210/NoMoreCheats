package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInSteerVehicle;


public class SteerVehiclePacket extends PacketIn
{
	private float side;
	private float forward;
	private boolean jump;
	private boolean unmount;

	public SteerVehiclePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInSteerVehicle packet)
	{
		super(owner, PacketTypeIn.STEER_VEHICLE, packet);
		this.side = packet.c();
		this.forward = packet.d();
		this.jump = packet.e();
		this.unmount = packet.f();
	}

	public float getLeftAccelerlation()
	{
		return this.side > 0 ? this.side : 0;
	}

	public float getRightAccelerlation()
	{
		return this.side < 0 ? Math.abs(this.side) : 0;
	}

	public float getForwardAccelerlation()
	{
		return this.forward;
	}

	public boolean isJump()
	{
		return this.jump;
	}

	public boolean isUnMount()
	{
		return this.unmount;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInSteerVehicle sv = new PacketPlayInSteerVehicle();
		InternalUtils.setField(sv, "a", this.side);
		InternalUtils.setField(sv, "b", this.forward);
		InternalUtils.setField(sv, "c", this.jump);
		InternalUtils.setField(sv, "d", this.unmount);
		return sv;
	}
}
