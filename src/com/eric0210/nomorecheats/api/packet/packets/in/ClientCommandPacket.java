package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.ClientCommand;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.EnumClientCommand;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInClientCommand;

public class ClientCommandPacket extends PacketIn
{
	private ClientCommand cmd;

	public ClientCommandPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInClientCommand packet)
	{
		super(owner, PacketTypeIn.CLIENT_COMMAND, packet);
		this.cmd = ClientCommand.byId(packet.c().ordinal());
	}

	public ClientCommand getCommand()
	{
		return this.cmd;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInClientCommand cc = new PacketPlayInClientCommand();
		InternalUtils.setField(cc, "a", EnumClientCommand.values()[this.cmd.ordinal()]);
		return cc;
	}
}
