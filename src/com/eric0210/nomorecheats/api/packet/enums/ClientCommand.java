package com.eric0210.nomorecheats.api.packet.enums;

public enum ClientCommand
{
	PERFORM_RESPAWN, REQUEST_STATS, OPEN_INVENTORY_ACHIEVEMENT;
	public static ClientCommand byId(int ordinal)
	{
		for (ClientCommand cmd : values())
		{
			if (cmd.ordinal() == ordinal)
				return cmd;
		}
		return null;
	}
}
