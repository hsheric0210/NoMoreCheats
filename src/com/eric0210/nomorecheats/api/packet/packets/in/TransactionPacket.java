package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInTransaction;

public class TransactionPacket extends PacketIn
{
	private int windowid;
	private short actionid;
	private boolean accepted;

	public TransactionPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInTransaction packet)
	{
		super(owner, PacketTypeIn.TRANSACTION, packet);
		this.windowid = packet.c();
		this.actionid = packet.d();
		this.accepted = InternalUtils.getField(packet, "c");
	}

	public int getWindowId()
	{
		return this.windowid;
	}

	public short getActionId()
	{
		return this.actionid;
	}

	public boolean isAccepted()
	{
		return this.accepted;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInTransaction t = new PacketPlayInTransaction();
		InternalUtils.setField(t, "a", this.windowid);
		InternalUtils.setField(t, "b", this.actionid);
		InternalUtils.setField(t, "c", this.accepted);
		return t;
	}
}
