package com.eric0210.nomorecheats.api.packet.enums;

public enum BlockFace
{
	BOTTOM(0, 0, -1, 0), TOP(1, 0, 1, 0), NORTH(2, 0, 0, -1), SOUTH(3, 0, 0, 1), WEST(4, -1, 0, 0), EAST(5, 1, 0,
			0), FAILED_PLACE(255, 0, 0, 0), INVALID(-1, -1, -1, -1);
	private int id;
	private int x;
	private int y;
	private int z;

	private BlockFace(int id, int x, int y, int z)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getId()
	{
		return this.id;
	}

	public int getXMod()
	{
		return this.x;
	}

	public int getYMod()
	{
		return this.y;
	}

	public int getZMod()
	{
		return this.z;
	}

	public static BlockFace byId(int id)
	{
		for (BlockFace face : values())
		{
			if (face.id == id)
			{
				return face;
			}
		}
		return null;
	}
}
