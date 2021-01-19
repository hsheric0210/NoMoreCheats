package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInHeldItemSlot;


public class HeldItemSlotPacket extends PacketIn
{
	private int index;
	public HeldItemSlotPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInHeldItemSlot packet)
	{
		super(owner, PacketTypeIn.HELD_ITEM_SLOT, packet);
		this.index = packet.c();
	}
	
	public int getIndex()
	{
		return this.index;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInHeldItemSlot slot = new PacketPlayInHeldItemSlot();
		InternalUtils.setField(slot, "itemInHandIndex", this.index);
		return slot;
	}
}
