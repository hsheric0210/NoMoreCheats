package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutAnimation;

public class AnimationPacket extends PacketOut
{
	private int entityid;
	private int swingType;

	public AnimationPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutAnimation packet)
	{
		super(owner, PacketTypeOut.ANIMATION, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.swingType = InternalUtils.getField(packet, "b");
	}

	public AnimationPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int eid, int type)
	{
		super(owner, PacketTypeOut.ANIMATION, new PacketPlayOutAnimation(new Entity(null)
		{
			@Override
			protected void c()
			{
			}

			@Override
			protected void b(NBTTagCompound var1)
			{
			}

			@Override
			protected void a(NBTTagCompound var1)
			{
			}

			@Override
			public int getId()
			{
				return eid;
			}
		}, type));
		this.entityid = eid;
		this.swingType = type;
	}

	public void setEntity(int eid)
	{
		this.entityid = eid;
	}

	public int getEntity()
	{
		return this.entityid;
	}

	public void setSwingType(int type)
	{
		this.swingType = type;
	}

	public int getSwingType()
	{
		return this.swingType;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayOutAnimation anim = new PacketPlayOutAnimation();
		InternalUtils.setField(anim, "a", this.entityid);
		InternalUtils.setField(anim, "b", this.swingType);
		return anim;
	}
}
