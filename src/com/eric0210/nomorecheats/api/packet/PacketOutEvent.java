package com.eric0210.nomorecheats.api.packet;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class PacketOutEvent implements Cancellable
{
	private PacketOut packet;
	private Player player;
	private boolean cancelled;

	public PacketOutEvent(Player player, PacketOut packet)
	{
		this.player = player;
		this.packet = packet;
	}

	public Player getPlayer()
	{
		return this.player;
	}

	public PacketOut getPacket()
	{
		return this.packet;
	}

	public void setPacket(PacketOut packet)
	{
		this.packet = packet;
	}

	@Override
	public void setCancelled(boolean b)
	{
		this.cancelled = b;
	}

	@Override
	public boolean isCancelled()
	{
		return this.cancelled;
	}
}
