package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInChat;

public class ChatPacket extends PacketIn
{
	private String message;

	public ChatPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInChat packet)
	{
		super(owner, PacketTypeIn.CHAT, packet);
		this.message = packet.c();
	}

	public String getMessage()
	{
		return this.message;
	}

	public void setMessage(String msg)
	{
		this.message = msg;
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayInChat(this.message);
	}
}
