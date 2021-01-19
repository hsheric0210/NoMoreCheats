package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutCloseWindow;

public class CloseWindowPacket extends PacketOut
{
	private int windowid;

	public CloseWindowPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutCloseWindow packet)
	{
		super(owner, PacketTypeOut.CLOSE_WINDOW, packet);
		this.windowid = InternalUtils.getField(packet, "a");
	}

	public CloseWindowPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int wid)
	{
		super(owner, PacketTypeOut.CLOSE_WINDOW, new PacketPlayOutCloseWindow(wid));
		this.windowid = wid;
	}

	public void setWindowId(int id)
	{
		this.windowid = id;
	}

	public int getWindowId()
	{
		return this.windowid;
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutCloseWindow(this.windowid);
	}
}
