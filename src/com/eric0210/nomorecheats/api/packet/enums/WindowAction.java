package com.eric0210.nomorecheats.api.packet.enums;

public enum WindowAction
{
	LEFT_CLICK(0, 0, true, false),
	RIGHT_CLICK(0, 1, true, true),
	SHIFT_LEFT_CLICK(1, 0, true, false),
	SHIFT_RIGHT_CLICK(1, 1, true, true),
	NUMBER_1(2, 0, true, false),
	NUMBER_2(2, 1, true, false),
	NUMBER_3(2, 2, true, false),
	NUMBER_4(2, 3, true, false),
	NUMBER_5(2, 4, true, false),
	NUMBER_6(2, 5, true, false),
	NUMBER_7(2, 6, true, false),
	NUMBER_8(2, 7, true, false),
	NUMBER_9(2, 8, true, false),
	MIDDLE_CLICK(3, 2, true, false),
	DROP(4, 0, true, false),
	DROP_FULL_STACK(4, 1, true, true),
	LEFT_CLICK_OUT_BORDER(4, 0, false, false),
	RIGHT_CLICK_OUT_BORDER(4, 1, false, true),
	START_LEFT_DRAGGING(5, 0, false, false),
	START_RIGHT_DRAGGING(5, 4, false, false),
	START_MIDDLE_DRAGGING(5, 8, false, false),
	ADD_SLOT_LEFT_DRAGGING(5, 1, true, false),
	ADD_SLOT_RIGHT_DRAGGING(5, 5, true, false),
	ADD_SLOT_MIDDLE_DRAGGING(5, 9, true, false),
	END_LEFT_DRAGGING(5, 2, false, false),
	END_RIGHT_DRAGGING(5, 6, false, false),
	END_MIDDLE_DRAGGING(5, 10, false, false),
	DOUBLE_CLICK(6, 0, true, false);
	private int mode;
	private int button;
	private boolean has_slot;
	private boolean binbutton;

	private WindowAction(int mode, int button, boolean hasslot, boolean binarybutton)
	{
		this.mode = mode;
		this.button = button;
		this.has_slot = hasslot;
		this.binbutton = binarybutton;
	}

	public int getMode()
	{
		return this.mode;
	}

	public int getButton()
	{
		return this.button;
	}

	public static WindowAction byOptions(int mode, int button, int slot)
	{
		for (WindowAction act : values())
		{
			if (act.mode == mode)
				if (act.button == button || (act.binbutton && act.button == 1 && button >= 1))
					if ((slot == -999 && !act.has_slot) || act.has_slot)
						return act;
		}
		return null;
	}

	@Override
	public String toString()
	{
		return String.format("WindowAction[name=%s, mode=%s, button=%s]", new Object[]
		{
				name(), this.mode, this.button
		});
	}
}
