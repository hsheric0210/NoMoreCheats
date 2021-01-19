package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.BlockFace;
import com.eric0210.nomorecheats.api.util.InternalUtils;
import com.eric0210.nomorecheats.api.util.NMS;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInBlockPlace;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BlockPlacePacket extends PacketIn
{
	private Vector pos;
	private BlockFace face;
	private ItemStack item;
	private Vector fgh;

	public BlockPlacePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInBlockPlace packet)
	{
		super(owner, PacketTypeIn.BLOCK_PLACE, packet);
		this.pos = new Vector(packet.c(), packet.d(), packet.e());
		this.face = BlockFace.byId(packet.getFace());
		if (this.face == null)
			this.face = BlockFace.INVALID;
		this.item = NMS.asBukkit(packet.getItemStack());
		this.fgh = new Vector(packet.h(), packet.i(), packet.j());
	}

	public Vector getPosition()
	{
		return this.pos;
	}

	public BlockFace getBlockFace()
	{
		return this.face;
	}

	public ItemStack getItemStack()
	{
		return this.item;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInBlockPlace place = new PacketPlayInBlockPlace();
		InternalUtils.setField(place, "a", this.pos.getBlockX());
		InternalUtils.setField(place, "b", this.pos.getBlockY());
		InternalUtils.setField(place, "c", this.pos.getBlockZ());
		InternalUtils.setField(place, "d", this.face.getId());
		InternalUtils.setField(place, "e", NMS.asNMS(this.item));
		InternalUtils.setField(place, "f", (float) this.fgh.getX());
		InternalUtils.setField(place, "g", (float) this.fgh.getY());
		InternalUtils.setField(place, "h", (float) this.fgh.getZ());
		return place;
	}
}
