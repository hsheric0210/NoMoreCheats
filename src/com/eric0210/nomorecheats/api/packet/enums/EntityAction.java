package com.eric0210.nomorecheats.api.packet.enums;

public enum EntityAction
{
	START_SNEAKING(1),
	STOP_SNEAKING(2),
	LEAVE_BED(3),
	START_SPRINTING(4),
	STOP_SPRINTING(5),
	START_HORSE_JUMP(6),
	STOP_HORSE_JUMP(7),
	OPEN_HORSE_INVENTORY(8);
	public int id;

	private EntityAction(int id)
	{
		this.id = id;
	}

	public static EntityAction byId(int id)
	{
		for (EntityAction act : values())
		{
			if (act.id == id)
				return act;
		}
		return null;
	}
}
