package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.WindowAction;
import com.eric0210.nomorecheats.api.util.InternalUtils;
import com.eric0210.nomorecheats.api.util.NMS;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInWindowClick;

import org.bukkit.inventory.ItemStack;

public class WindowClickPacket extends PacketIn
{
	private int windowId;
	private WindowAction action;
	private ItemStack itemClicked;
	private int slot;
	private short transactionId;

	public WindowClickPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInWindowClick packet)
	{
		super(owner, PacketTypeIn.WINDOW_CLICK, packet);
		this.windowId = packet.c();
		this.action = WindowAction.byOptions(packet.h(), packet.e(), packet.d());
		this.itemClicked = NMS.asBukkit(packet.g());
		this.slot = packet.d();
		this.transactionId = packet.f();
	}

	public int getWindowId()
	{
		return this.windowId;
	}

	public WindowAction getAction()
	{
		return this.action;
	}

	public ItemStack getItemStack()
	{
		return this.itemClicked;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInWindowClick c = new PacketPlayInWindowClick();
		InternalUtils.setField(c, "a", this.windowId);
		InternalUtils.setField(c, "slot", this.slot);
		InternalUtils.setField(c, "d", this.transactionId);
		InternalUtils.setField(c, "item", NMS.asNMS(this.itemClicked));
		if (this.action != null)
		{
			InternalUtils.setField(c, "button", this.action.getButton());
			InternalUtils.setField(c, "shift", this.action.getMode());
		}
		return c;
	}
}
