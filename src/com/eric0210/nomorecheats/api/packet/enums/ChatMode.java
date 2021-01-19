package com.eric0210.nomorecheats.api.packet.enums;

public enum ChatMode
{
	FULL(0, "options.chat.visibility.full"), COMMANDS(1, "options.chat.visibility.system"), HIDDEN(2,
			"options.chat.visibility.hidden");
	public int index;
	public String id;

	private ChatMode(int idx, String id)
	{
		this.index = idx;
		this.id = id;
	}

	public static ChatMode byId(int id)
	{
		for (ChatMode cmd : values())
		{
			if (cmd.index == id)
				return cmd;
		}
		return null;
	}
}
