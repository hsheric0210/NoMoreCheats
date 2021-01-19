package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInCloseWindow;


public class CloseWindowPacket extends PacketIn
{
	private int windowid;

	public CloseWindowPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInCloseWindow packet)
	{
		super(owner, PacketTypeIn.CLOSE_WINDOW, packet);
		this.windowid = InternalUtils.getField(packet, "a");
	}

	public int getWindowId()
	{
		return this.windowid;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInCloseWindow cw = new PacketPlayInCloseWindow();
		InternalUtils.setField(cw, "a", this.windowid);
		return cw;
	}
}
