package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutKeepAlive;

public class KeepAlivePacket extends PacketOut
{
	private int id;

	public KeepAlivePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutKeepAlive packet)
	{
		super(owner, PacketTypeOut.KEEP_ALIVE, packet);
		this.id = InternalUtils.getField(packet, "a");
	}

	public KeepAlivePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int id)
	{
		super(owner, PacketTypeOut.KEEP_ALIVE, new PacketPlayOutKeepAlive(id));
		this.id = id;
	}

	public int getId()
	{
		return this.id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutKeepAlive(getId());
	}
}
