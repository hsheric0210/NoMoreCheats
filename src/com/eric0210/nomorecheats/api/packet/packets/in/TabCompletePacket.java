package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInTabComplete;


public class TabCompletePacket extends PacketIn
{
	private String msg;
	public TabCompletePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInTabComplete packet)
	{
		super(owner, PacketTypeIn.TAB_COMPLETE, packet);
		this.msg = packet.c();
	}
	
	public String getMessage()
	{
		return this.msg;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInTabComplete tc = new PacketPlayInTabComplete();
		InternalUtils.setField(tc, "a", this.msg);
		return tc;
	}
}
