package com.eric0210.nomorecheats.api.packet;

import net.minecraft.server.v1_7_R4.PacketPlayOutAnimation;
import net.minecraft.server.v1_7_R4.PacketPlayOutBlockChange;
import net.minecraft.server.v1_7_R4.PacketPlayOutCloseWindow;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_7_R4.PacketPlayOutKeepAlive;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_7_R4.PacketPlayOutPosition;
import net.minecraft.server.v1_7_R4.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_7_R4.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_7_R4.PacketPlayOutTransaction;

public enum PacketTypeOut
{
	KEEP_ALIVE,
	TRANSACTION,
	ENTITY_DESTROY,
	ENTITY_TELEPORT,
	SPAWN_ENTITY_LIVING,
	ANIMATION,
	BLOCK_CHANGE,
	CLOSE_WINDOW,
	ENTITY_EQUIPMENT,
	SPAWN_ENTITY_NAMED,
	PLAYER_INFO,
	ENTITY_METADATA,
	ENTITY_RELMOVE,
	ENTITY_RELMOVELOOK,
	ENTITY_POSITION,
	ENTITY_LOOK,
	ENTITY_HEADROTATION,
	UNKNOWN;
	public static <T extends net.minecraft.server.v1_7_R4.Packet> PacketTypeOut getTypeByPacket(T packet)
	{
		if (packet instanceof PacketPlayOutKeepAlive)
			return KEEP_ALIVE;
		if (packet instanceof PacketPlayOutTransaction)
			return TRANSACTION;
		if (packet instanceof PacketPlayOutEntityDestroy)
			return ENTITY_DESTROY;
		if (packet instanceof PacketPlayOutEntityTeleport)
			return ENTITY_TELEPORT;
		if (packet instanceof PacketPlayOutSpawnEntityLiving)
			return SPAWN_ENTITY_LIVING;
		if (packet instanceof PacketPlayOutAnimation)
			return ANIMATION;
		if (packet instanceof PacketPlayOutBlockChange)
			return BLOCK_CHANGE;
		if (packet instanceof PacketPlayOutCloseWindow)
			return CLOSE_WINDOW;
		if (packet instanceof PacketPlayOutEntityEquipment)
			return ENTITY_EQUIPMENT;
		if (packet instanceof PacketPlayOutEntityMetadata)
			return ENTITY_METADATA;
		if (packet instanceof PacketPlayOutNamedEntitySpawn)
			return SPAWN_ENTITY_NAMED;
		if (packet instanceof PacketPlayOutPlayerInfo)
			return PLAYER_INFO;
		if (packet instanceof PacketPlayOutRelEntityMove)
			return ENTITY_RELMOVE;
		if (packet instanceof PacketPlayOutRelEntityMoveLook)
			return ENTITY_RELMOVELOOK;
		if (packet instanceof PacketPlayOutPosition)
			return ENTITY_POSITION;
		if (packet instanceof PacketPlayOutEntityHeadRotation)
			return PacketTypeOut.ENTITY_HEADROTATION;
		return UNKNOWN;
	}
}
