package com.eric0210.nomorecheats.api.event.events;

import com.eric0210.nomorecheats.api.Check;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class NMCEvent extends Event implements Cancellable
{
	private Check check;
	private boolean cancelled;

	public NMCEvent(Check check)
	{
		this.check = check;
	}

	public Check getCheck()
	{
		return this.check;
	}

	@Override
	public boolean isCancelled()
	{
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel)
	{
		this.cancelled = cancel;
	}
}
