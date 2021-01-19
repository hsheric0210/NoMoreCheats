package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutTransaction;

public class TransactionPacket extends PacketOut
{
	private int windowid;
	private short actionid;
	private boolean accepted;

	public TransactionPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutTransaction packet)
	{
		super(owner, PacketTypeOut.TRANSACTION, packet);
		this.windowid = InternalUtils.getField(packet, "a");
		this.actionid = InternalUtils.getField(packet, "b");
		this.accepted = InternalUtils.getField(packet, "c");
	}

	public TransactionPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int wid, short uid, boolean acpt)
	{
		super(owner, PacketTypeOut.TRANSACTION, new PacketPlayOutTransaction(wid, uid, acpt));
		this.windowid = wid;
		this.actionid = uid;
		this.accepted = acpt;
	}

	public void setWindowId(int id)
	{
		this.windowid = id;
	}

	public int getWindowId()
	{
		return this.windowid;
	}

	public void setActionId(short id)
	{
		this.actionid = id;
	}

	public short getActionId()
	{
		return this.actionid;
	}

	public void setAccepted(boolean bl)
	{
		this.accepted = bl;
	}

	public boolean isAccepted()
	{
		return this.accepted;
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutTransaction(this.windowid, this.actionid, this.accepted);
	}
}
