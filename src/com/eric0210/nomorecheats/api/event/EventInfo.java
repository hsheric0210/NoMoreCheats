package com.eric0210.nomorecheats.api.event;

public class EventInfo
{
	public EventListener listener;
	public int id;
	
	public EventInfo(EventListener listener, int id)
	{
		this.listener = listener;
		this.id = id;
	}
}
