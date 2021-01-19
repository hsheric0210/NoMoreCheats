package com.eric0210.nomorecheats.api.packet.packets.in;

import com.eric0210.nomorecheats.api.packet.PacketIn;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.ChatMode;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.EnumChatVisibility;
import net.minecraft.server.v1_7_R4.EnumDifficulty;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInSettings;

import org.bukkit.Difficulty;

public class SettingsPacket extends PacketIn
{
	private String lang;
	private int render_distance;
	private ChatMode chatmode;
	private boolean colors;
	private boolean cape;
	private boolean jacket;
	private boolean left_sleeve;
	private boolean right_sleeve;
	private boolean left_pants;
	private boolean right_pants;
	private boolean hat;
	public int version;
	public int flags;
	private boolean cape2;
	private Difficulty dif;

	public SettingsPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayInSettings packet)
	{
		super(owner, PacketTypeIn.SETTINGS, packet);
		this.lang = packet.c();
		this.render_distance = packet.d();
		this.chatmode = ChatMode.byId(packet.e().a());
		this.colors = packet.f();
		this.cape2 = packet.h();
		this.flags = packet.flags;
		this.cape = (this.flags & 1) != 0;
		this.jacket = (this.flags & 2) != 0;
		this.left_sleeve = (this.flags & 4) != 0;
		this.right_sleeve = (this.flags & 8) != 0;
		this.left_pants = (this.flags & 10) != 0;
		this.right_pants = (this.flags & 20) != 0;
		this.hat = (this.flags & 40) != 0;
		this.version = packet.version;
		this.dif = Difficulty.getByValue(packet.g() != null ? packet.g().ordinal() : -1);
	}

	public String getMinecraftLanguage()
	{
		return this.lang;
	}

	public int getRenderDistance()
	{
		return this.render_distance;
	}

	public ChatMode getChatMode()
	{
		return this.chatmode;
	}

	public boolean isChatColorEnabled()
	{
		return this.colors;
	}

	public boolean hasCape()
	{
		return this.cape;
	}

	public boolean hasJacket()
	{
		return this.jacket;
	}

	public boolean hasLeftSleeve()
	{
		return this.left_sleeve;
	}

	public boolean hasRightSleeve()
	{
		return this.right_sleeve;
	}

	public boolean hasLeftPants()
	{
		return this.left_pants;
	}

	public boolean hasRightPants()
	{
		return this.right_pants;
	}

	public boolean hasHat()
	{
		return this.hat;
	}

	public Difficulty getDifficulty()
	{
		return this.dif;
	}

	public boolean enableCape()
	{
		return this.cape2;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayInSettings s = new PacketPlayInSettings();
		InternalUtils.setField(s, "a", this.lang);
		InternalUtils.setField(s, "b", this.render_distance);
		InternalUtils.setField(s, "c", EnumChatVisibility.values()[Math.min(2, this.chatmode.index)]);
		InternalUtils.setField(s, "d", this.colors);
		if (this.dif != null && EnumDifficulty.getById(this.dif.getValue()) != null)
			InternalUtils.setField(s, "e", EnumDifficulty.getById(this.dif.getValue()));
		InternalUtils.setField(s, "f", this.cape2);
		s.version = this.version;
		s.flags = this.flags;
		return s;
	}
}
