package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInCustomPayload;

public class CustomPayloadPacket extends PacketIn
{
	private String tag;
	private int length;
	private byte[] data;

	public CustomPayloadPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInCustomPayload packet)
	{
		super(owner, PacketTypeIn.CUSTOM_PAYLOAD, packet);
		this.tag = packet.c();
		this.length = packet.length;
		this.data = packet.e();
	}

	public String getTag()
	{
		return this.tag;
	}

	public int getLength()
	{
		return this.length;
	}

	public byte[] getData()
	{
		return this.data;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInCustomPayload pl = new PacketPlayInCustomPayload();
		InternalUtils.setField(pl, "tag", this.tag);
		InternalUtils.setField(pl, "length", this.length);
		InternalUtils.setField(pl, "data", this.data);
		return pl;
	}
}
