package com.eric0210.nomorecheats.api.packet.listeners;

import com.eric0210.nomorecheats.api.packet.PacketManager;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInAbilities;
import net.minecraft.server.v1_7_R4.PacketPlayInArmAnimation;
import net.minecraft.server.v1_7_R4.PacketPlayInBlockDig;
import net.minecraft.server.v1_7_R4.PacketPlayInBlockPlace;
import net.minecraft.server.v1_7_R4.PacketPlayInChat;
import net.minecraft.server.v1_7_R4.PacketPlayInClientCommand;
import net.minecraft.server.v1_7_R4.PacketPlayInCloseWindow;
import net.minecraft.server.v1_7_R4.PacketPlayInCustomPayload;
import net.minecraft.server.v1_7_R4.PacketPlayInEnchantItem;
import net.minecraft.server.v1_7_R4.PacketPlayInEntityAction;
import net.minecraft.server.v1_7_R4.PacketPlayInFlying;
import net.minecraft.server.v1_7_R4.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_7_R4.PacketPlayInKeepAlive;
import net.minecraft.server.v1_7_R4.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_7_R4.PacketPlayInSettings;
import net.minecraft.server.v1_7_R4.PacketPlayInSteerVehicle;
import net.minecraft.server.v1_7_R4.PacketPlayInTabComplete;
import net.minecraft.server.v1_7_R4.PacketPlayInTransaction;
import net.minecraft.server.v1_7_R4.PacketPlayInUpdateSign;
import net.minecraft.server.v1_7_R4.PacketPlayInUseEntity;
import net.minecraft.server.v1_7_R4.PacketPlayInWindowClick;
import net.minecraft.server.v1_7_R4.PlayerConnection;

public class PlayerConnectionFilter extends PlayerConnection
{
	public static PlayerConnectionFilter instance;

	public PlayerConnectionFilter(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer)
	{
		super(minecraftserver, networkmanager, entityplayer);
		instance = this;
	}

	@Override
	public void sendPacket(Packet packet)
	{
		if ((packet = PacketManager.handleNMSSend(this.player, packet)) != null)
			super.sendPacket(packet);
	}

	@Override
	public void a(PacketPlayInArmAnimation packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInChat packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInTabComplete packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInClientCommand packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInSettings packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInTransaction packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInEnchantItem packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInWindowClick packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInCloseWindow packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInCustomPayload packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInUseEntity packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInKeepAlive packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInFlying packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInAbilities packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInBlockDig packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInEntityAction packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInSteerVehicle packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInHeldItemSlot packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInSetCreativeSlot packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInUpdateSign packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}

	@Override
	public void a(PacketPlayInBlockPlace packet)
	{
		if ((packet = PacketManager.handleNMSRecv(this.player, packet)) != null)
			super.a(packet);
	}
}
