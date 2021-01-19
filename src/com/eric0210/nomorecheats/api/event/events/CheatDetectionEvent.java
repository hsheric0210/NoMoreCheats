package com.eric0210.nomorecheats.api.event.events;

import com.eric0210.nomorecheats.api.Check;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class CheatDetectionEvent extends NMCEvent
{
	private static final HandlerList handlers = new HandlerList();
	private Player owner;
	private double violation_level;
	private Object[] tags;

	public CheatDetectionEvent(Check c, Player target, double vl, Object... tags)
	{
		super(c);
		this.owner = target;
		this.violation_level = vl;
		this.tags = tags;
	}

	public Player getPlayer()
	{
		return this.owner;
	}

	public void setPlayer(Player player)
	{
		this.owner = player;
	}

	public double getViolationLevel()
	{
		return this.violation_level;
	}

	public void setViolationLevel(double level)
	{
		this.violation_level = level;
	}

	public Object[] getViolationTags()
	{
		return this.tags;
	}

	public void setViolationTags(Object... args)
	{
		this.tags = args;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
