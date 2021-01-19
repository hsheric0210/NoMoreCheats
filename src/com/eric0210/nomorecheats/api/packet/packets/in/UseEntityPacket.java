package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.EntityUseAction;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.EnumEntityUseAction;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInUseEntity;

public class UseEntityPacket extends PacketIn
{
	private int entityid;
	private EntityUseAction action;

	public UseEntityPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInUseEntity packet)
	{
		super(owner, PacketTypeIn.USE_ENTITY, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.action = EntityUseAction.byId(packet.c() != null ? packet.c().ordinal() : -1);
	}

	public int getEntityId()
	{
		return this.entityid;
	}

	public EntityUseAction getAction()
	{
		return this.action;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInUseEntity use = new PacketPlayInUseEntity();
		InternalUtils.setField(use, "a", this.entityid);
		InternalUtils.setField(use, "action", this.action != null ? EnumEntityUseAction.values()[this.action.ordinal()] : null);
		return use;
	}
}
