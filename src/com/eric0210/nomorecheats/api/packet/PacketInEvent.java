package com.eric0210.nomorecheats.api.packet;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class PacketInEvent implements Cancellable
{
	private PacketIn packet;
	private Player player;
	private boolean cancelled;

	public PacketInEvent(Player player, PacketIn packet)
	{
		this.player = player;
		this.packet = packet;
	}

	public Player getPlayer()
	{
		return this.player;
	}

	public PacketIn getPacket()
	{
		return this.packet;
	}

	public void setPacket(PacketIn packet)
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
