package com.eric0210.nomorecheats.api.packet;

public interface PacketListener
{
	public void onPacketIn(PacketInEvent e);

	public void onPacketOut(PacketOutEvent e);
}
