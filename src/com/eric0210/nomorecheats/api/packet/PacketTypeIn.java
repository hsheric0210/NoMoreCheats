package com.eric0210.nomorecheats.api.packet;

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


public enum PacketTypeIn
{
	ARM_ANIMATION, CHAT, TAB_COMPLETE, CLIENT_COMMAND, SETTINGS, TRANSACTION, ENCHANT_ITEM, WINDOW_CLICK, CLOSE_WINDOW, CUSTOM_PAYLOAD, USE_ENTITY, KEEP_ALIVE, FLYING, ABILITIES, BLOCK_DIG, ENTITY_ACTION, STEER_VEHICLE, HELD_ITEM_SLOT, SET_CREATIVE_SLOT, UPDATE_SIGN, BLOCK_PLACE, UNKNOWN;
	public static <T extends net.minecraft.server.v1_7_R4.Packet> PacketTypeIn getTypeByPacket(T packet)
	{
		if (packet instanceof PacketPlayInArmAnimation)
			return ARM_ANIMATION;
		if (packet instanceof PacketPlayInChat)
			return CHAT;
		if (packet instanceof PacketPlayInTabComplete)
			return TAB_COMPLETE;
		if (packet instanceof PacketPlayInClientCommand)
			return CLIENT_COMMAND;
		if (packet instanceof PacketPlayInSettings)
			return SETTINGS;
		if (packet instanceof PacketPlayInTransaction)
			return TRANSACTION;
		if (packet instanceof PacketPlayInEnchantItem)
			return ENCHANT_ITEM;
		if (packet instanceof PacketPlayInWindowClick)
			return WINDOW_CLICK;
		if (packet instanceof PacketPlayInCloseWindow)
			return CLOSE_WINDOW;
		if (packet instanceof PacketPlayInCustomPayload)
			return CUSTOM_PAYLOAD;
		if (packet instanceof PacketPlayInUseEntity)
			return USE_ENTITY;
		if (packet instanceof PacketPlayInKeepAlive)
			return KEEP_ALIVE;
		if (packet instanceof PacketPlayInFlying)
			return FLYING;
		if (packet instanceof PacketPlayInAbilities)
			return ABILITIES;
		if (packet instanceof PacketPlayInBlockDig)
			return BLOCK_DIG;
		if (packet instanceof PacketPlayInEntityAction)
			return ENTITY_ACTION;
		if (packet instanceof PacketPlayInSteerVehicle)
			return STEER_VEHICLE;
		if (packet instanceof PacketPlayInHeldItemSlot)
			return HELD_ITEM_SLOT;
		if (packet instanceof PacketPlayInSetCreativeSlot)
			return SET_CREATIVE_SLOT;
		if (packet instanceof PacketPlayInUpdateSign)
			return UPDATE_SIGN;
		if (packet instanceof PacketPlayInBlockPlace)
			return BLOCK_PLACE;
		return UNKNOWN;
	}
}
