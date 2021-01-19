package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.NMS;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutBlockChange;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class BlockUpdatePacket extends PacketOut
{
	private int x;
	private int y;
	private int z;
	private net.minecraft.server.v1_7_R4.Block block;
	private int data;

	public BlockUpdatePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutBlockChange packet)
	{
		super(owner, PacketTypeOut.BLOCK_CHANGE, packet);
		this.x = InternalUtils.getField(packet, "a");
		this.y = InternalUtils.getField(packet, "b");
		this.z = InternalUtils.getField(packet, "c");
		this.block = packet.block;
		this.data = packet.data;
	}

	public BlockUpdatePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, Location pos)
	{
		super(owner, PacketTypeOut.BLOCK_CHANGE, new PacketPlayOutBlockChange(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), NMS.asNMS(pos.getWorld())));
		this.x = pos.getBlockX();
		this.y = pos.getBlockY();
		this.z = pos.getBlockZ();
		this.block = NMS.asNMS(pos.getWorld().getBlockAt(pos));
		this.data = pos.getWorld().getBlockAt(pos).getData();
	}

	public Vector getPosition()
	{
		return new Vector(this.x, this.y, this.z);
	}

	public void setPosition(Vector pos)
	{
		this.x = pos.getBlockX();
		this.y = pos.getBlockY();
		this.z = pos.getBlockZ();
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange();
		InternalUtils.setField(packet, "a", this.x);
		InternalUtils.setField(packet, "b", this.y);
		InternalUtils.setField(packet, "c", this.z);
		packet.block = this.block;
		packet.data = this.data;
		return packet;
	}
}
