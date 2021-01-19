package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.EntityAction;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInEntityAction;

public class EntityActionPacket extends PacketIn
{
	private int entityid;
	private EntityAction action;
	private int horseJumpboost;

	public EntityActionPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInEntityAction packet)
	{
		super(owner, PacketTypeIn.ENTITY_ACTION, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.action = EntityAction.byId(packet.d());
		this.horseJumpboost = packet.e();
	}

	public int getEntityId()
	{
		return this.entityid;
	}

	public EntityAction getAction()
	{
		return this.action;
	}

	public int getHorseJumpBoost()
	{
		return this.horseJumpboost;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInEntityAction act = new PacketPlayInEntityAction();
		InternalUtils.setField(act, "a", this.entityid);
		InternalUtils.setField(act, "animation", this.action.id);
		InternalUtils.setField(act, "c", this.horseJumpboost);
		return act;
	}
}
