package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.util.com.mojang.authlib.GameProfile;

public class PlayerInfoPacket extends PacketOut
{
	public enum Action
	{
		ADD_PLAYER(0),
		UPDATE_GAMEMODE(1),
		UPDATE_LATENCY(2),
		UPDATE_DISPLAY_NAME(3),
		REMOVE_PLAYER(4);
		int actionid;

		private Action(int id)
		{
			this.actionid = id;
		}
	}

	private int action;
	private GameProfile profile;
	private int gamemode;
	private int ping;
	private String username;

	public PlayerInfoPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutPlayerInfo packet)
	{
		super(owner, PacketTypeOut.PLAYER_INFO, packet);
		this.action = InternalUtils.getField(packet, "action");
		this.profile = InternalUtils.getField(packet, "player");
		this.gamemode = InternalUtils.getField(packet, "gamemode");
		this.ping = InternalUtils.getField(packet, "ping");
		this.username = InternalUtils.getField(packet, "username");
	}

	public PlayerInfoPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, Action action, EntityPlayer p)
	{
		super(owner, PacketTypeOut.PLAYER_INFO, action == Action.ADD_PLAYER
				? PacketPlayOutPlayerInfo.addPlayer(p)
				: (action == Action.UPDATE_GAMEMODE
						? PacketPlayOutPlayerInfo.updateGamemode(p)
						: (action == Action.UPDATE_LATENCY
								? PacketPlayOutPlayerInfo.updatePing(p)
								: (action == Action.UPDATE_DISPLAY_NAME ? PacketPlayOutPlayerInfo.updateDisplayName(p) : (action == Action.REMOVE_PLAYER ? PacketPlayOutPlayerInfo.removePlayer(p) : null)))));
		this.action = action.actionid;
		this.profile = p.getProfile();
		this.gamemode = p.playerInteractManager.getGameMode().getId();
		this.ping = p.ping;
		this.username = p.listName;
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo();
		InternalUtils.setField(info, "action", this.action);
		InternalUtils.setField(info, "player", this.profile);
		InternalUtils.setField(info, "gamemode", this.gamemode);
		InternalUtils.setField(info, "ping", this.ping);
		InternalUtils.setField(info, "username", this.username);
		return info;
	}
}
