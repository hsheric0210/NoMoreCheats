package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInAbilities;

public class AbilitiesPacket extends PacketIn
{
	private boolean inv;
	private boolean flying;
	private boolean canfly;
	private boolean instabuild;
	private float flyspeed;
	private float walkspeed;

	public AbilitiesPacket(EntityPlayer owner, PacketPlayInAbilities packet)
	{
		super(owner, PacketTypeIn.ABILITIES, packet);
		this.inv = packet.c();
		this.flying = packet.isFlying();
		this.canfly = packet.e();
		this.instabuild = packet.f();
		this.flyspeed = packet.g();
		this.walkspeed = packet.h();
	}

	public boolean isInvulnerable()
	{
		return this.inv;
	}

	public boolean isFlying()
	{
		return this.flying;
	}

	public boolean isFlightAllowed()
	{
		return this.canfly;
	}

	public boolean canInstantlyBuild()
	{
		return this.instabuild;
	}

	public float getFlySpeed()
	{
		return this.flyspeed;
	}

	public float getWalkSpeed()
	{
		return this.walkspeed;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInAbilities ab = new PacketPlayInAbilities();
		InternalUtils.setField(ab, "a", this.inv);
		InternalUtils.setField(ab, "b", this.flying);
		InternalUtils.setField(ab, "c", this.canfly);
		InternalUtils.setField(ab, "d", this.instabuild);
		InternalUtils.setField(ab, "e", this.flyspeed);
		InternalUtils.setField(ab, "f", this.walkspeed);
		return ab;
	}
}
