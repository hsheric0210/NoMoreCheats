package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInArmAnimation;


public class ArmAnimationPacket extends PacketIn
{
	private int type;

	public ArmAnimationPacket(EntityPlayer owner, PacketPlayInArmAnimation packet)
	{
		super(owner, PacketTypeIn.ARM_ANIMATION, packet);
		this.type = packet.d();
	}

	public int getSwingType()
	{
		return this.type;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInArmAnimation aa = new PacketPlayInArmAnimation();
		InternalUtils.setField(aa, "b", this.type);
		return aa;
	}
}
