package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;
import com.eric0210.nomorecheats.api.util.NMS;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityEquipment;
import org.bukkit.inventory.ItemStack;

public class EntityEquipmentPacket extends PacketOut
{
	private int entityid;
	private int slot;
	private ItemStack stack;

	public EntityEquipmentPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutEntityEquipment packet)
	{
		super(owner, PacketTypeOut.ENTITY_EQUIPMENT, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.slot = InternalUtils.getField(packet, "b");
		this.stack = NMS.asBukkit(InternalUtils.getField(packet, "c"));
	}

	public EntityEquipmentPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int entityid, short slot, ItemStack is)
	{
		super(owner, PacketTypeOut.ENTITY_EQUIPMENT, new PacketPlayOutEntityEquipment(entityid, slot, NMS.asNMS(is)));
		this.entityid = entityid;
		this.slot = slot;
		this.stack = is;
	}

	public void setEntity(int eid)
	{
		this.entityid = eid;
	}

	public int getEntity()
	{
		return this.entityid;
	}

	public void setSlot(short s)
	{
		this.slot = s;
	}

	public int getSlot()
	{
		return this.slot;
	}

	public void setItemStack(ItemStack is)
	{
		this.stack = is;
	}

	public ItemStack getItemStack()
	{
		return this.stack;
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutEntityEquipment(this.entityid, this.slot, NMS.asNMS(this.stack));
	}
}
