package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInKeepAlive;


public class KeepAlivePacket extends PacketIn
{
	private int id;

	public KeepAlivePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInKeepAlive packet)
	{
		super(owner, PacketTypeIn.KEEP_ALIVE, packet);
		this.id = packet.c();
	}

	public int getId()
	{
		return this.id;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInKeepAlive ka = new PacketPlayInKeepAlive();
		InternalUtils.setField(ka, "a", this.id);
		return ka;
	}
}
