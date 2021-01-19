package com.eric0210.nomorecheats.api.packet;

abstract class Packet
{
	protected net.minecraft.server.v1_7_R4.Packet nmsPacket;
	protected net.minecraft.server.v1_7_R4.EntityPlayer owner; 

	public Packet(net.minecraft.server.v1_7_R4.EntityPlayer owner, net.minecraft.server.v1_7_R4.Packet packet)
	{
		this.nmsPacket = packet;
		this.owner = owner;
	}

	public final net.minecraft.server.v1_7_R4.Packet getNMSPacket()
	{
		return this.nmsPacket;
	}
	
	public final net.minecraft.server.v1_7_R4.EntityPlayer getOwner()
	{
		return this.owner;
	}

	public abstract net.minecraft.server.v1_7_R4.Packet toNMS();
}
