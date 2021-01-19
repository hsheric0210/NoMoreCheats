package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;
import com.eric0210.nomorecheats.api.util.NMS;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInSetCreativeSlot;

import org.bukkit.inventory.ItemStack;

public class SetCreativeSlotPacket extends PacketIn
{
	private int slot;
	private ItemStack item;

	public SetCreativeSlotPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInSetCreativeSlot packet)
	{
		super(owner, PacketTypeIn.SET_CREATIVE_SLOT, packet);
		this.slot = packet.c();
		this.item = NMS.asBukkit(packet.getItemStack());
	}

	public int getSlot()
	{
		return this.slot;
	}

	public ItemStack getItemStack()
	{
		return this.item;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInSetCreativeSlot c = new PacketPlayInSetCreativeSlot();
		InternalUtils.setField(c, "slot", this.slot);
		InternalUtils.setField(c, "b", NMS.asNMS(this.item));
		return c;
	}
}
