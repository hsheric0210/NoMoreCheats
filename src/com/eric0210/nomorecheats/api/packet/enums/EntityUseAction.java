package com.eric0210.nomorecheats.api.packet.enums;

public enum EntityUseAction
{
	INTERACT, ATTACK, INTERACT_AT;
	public static EntityUseAction byId(int id)
	{
		for (EntityUseAction act : values())
		{
			if (act.ordinal() == id)
				return act;
		}
		return null;
	}
}
