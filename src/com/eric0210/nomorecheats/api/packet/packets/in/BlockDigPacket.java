package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.BlockFace;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInBlockDig;

import org.bukkit.util.Vector;


public class BlockDigPacket extends PacketIn
{
	private Vector pos;
	private BlockFace face;
	private DigAction action;

	public BlockDigPacket(EntityPlayer owner, PacketPlayInBlockDig packet)
	{
		super(owner, PacketTypeIn.BLOCK_DIG, packet);
		this.pos = new Vector(packet.c(), packet.d(), packet.e());
		this.face = BlockFace.byId(packet.f());
		if (this.face == null)
			this.face = BlockFace.INVALID;
		this.action = DigAction.byId(packet.g());
	}

	public Vector getPosition()
	{
		return this.pos;
	}

	public BlockFace getBlockFace()
	{
		return this.face;
	}

	public DigAction getAction()
	{
		return this.action;
	}

	public enum DigAction
	{
		START_DIGGING(0), CANCELL_DIGGING(1), FINISH_DIGGING(2), DROP_ITEM_STACK(3), DROP_ITEM(4), OTHER_INTERACT(
				5), SWAP_ITEM_HAND(6), INVALID(-1);
		private int id;

		private DigAction(int id)
		{
			this.id = id;
		}

		public int getId()
		{
			return this.id;
		}

		public static DigAction byId(int id)
		{
			for (DigAction action : values())
			{
				if (action.id == id)
				{
					return action;
				}
			}
			return DigAction.INVALID;
		}
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInBlockDig dig = new PacketPlayInBlockDig();
		InternalUtils.setField(dig, "a", this.pos.getBlockX());
		InternalUtils.setField(dig, "b", this.pos.getBlockY());
		InternalUtils.setField(dig, "c", this.pos.getBlockZ());
		InternalUtils.setField(dig, "face", this.face.getId());
		InternalUtils.setField(dig, "e", this.action.getId());
		return dig;
	}
}
