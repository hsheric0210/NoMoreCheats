package com.eric0210.nomorecheats.api.packet.packets.in;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInUpdateSign;

import org.bukkit.util.Vector;

public class UpdateSignPacket extends PacketIn
{
	private Vector pos;
	private List<String> lines;

	public UpdateSignPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInUpdateSign packet)
	{
		super(owner, PacketTypeIn.UPDATE_SIGN, packet);
		this.pos = new Vector(packet.c(), packet.d(), packet.e());
		this.lines = new ArrayList<>(Arrays.asList(packet.f()));
	}

	public Vector getPosition()
	{
		return this.pos;
	}

	public List<String> getLines()
	{
		return this.lines;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInUpdateSign s = new PacketPlayInUpdateSign();
		InternalUtils.setField(s, "a", this.pos.getBlockX());
		InternalUtils.setField(s, "b", this.pos.getBlockY());
		InternalUtils.setField(s, "c", this.pos.getBlockZ());
		InternalUtils.setField(s, "d", this.lines.toArray(new String[0]));
		return s;
	}
}
