package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInEnchantItem;

public class EnchantItemPacket extends PacketIn
{
	private int windowid;
	private int enchantment_gui_button_id;

	public EnchantItemPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInEnchantItem packet)
	{
		super(owner, PacketTypeIn.ENCHANT_ITEM, packet);
		this.windowid = packet.c();
		this.enchantment_gui_button_id = packet.d();
	}

	public int getWindowId()
	{
		return this.windowid;
	}

	public int getButtonId()
	{
		return this.enchantment_gui_button_id;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInEnchantItem ei = new PacketPlayInEnchantItem();
		InternalUtils.setField(ei, "a", this.windowid);
		InternalUtils.setField(ei, "b", this.enchantment_gui_button_id);
		return ei;
	}
}
