package com.eric0210.nomorecheats.api.packet;

import com.eric0210.nomorecheats.api.packet.packets.out.AnimationPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.CloseWindowPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityDestroyPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityEquipmentPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityHeadRotationPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityLookPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityMetadataPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityRelativeMoveLookPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityRelativeMovePacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityTeleportPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.KeepAlivePacket;
import com.eric0210.nomorecheats.api.packet.packets.out.PlayerInfoPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.PositionPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.SpawnEntityLivingPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.SpawnEntityNamedPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.TransactionPacket;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutAnimation;
import net.minecraft.server.v1_7_R4.PacketPlayOutCloseWindow;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityLook;
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

public abstract class PacketOut extends Packet
{
	public PacketOut(EntityPlayer owner, PacketTypeOut type, net.minecraft.server.v1_7_R4.Packet packet)
	{
		super(owner, packet);
		this.type = type;
	}

	private PacketTypeOut type;

	public final PacketTypeOut getType()
	{
		return this.type;
	}

	public static <T extends net.minecraft.server.v1_7_R4.Packet> PacketOut byNMS(net.minecraft.server.v1_7_R4.EntityPlayer owner, T p)
	{
		PacketTypeOut type = PacketTypeOut.getTypeByPacket(p);
		switch (type)
		{
			case KEEP_ALIVE:
				return new KeepAlivePacket(owner, (PacketPlayOutKeepAlive) p);
			case ANIMATION:
				return new AnimationPacket(owner, (PacketPlayOutAnimation) p);
			case CLOSE_WINDOW:
				return new CloseWindowPacket(owner, (PacketPlayOutCloseWindow) p);
			case ENTITY_DESTROY:
				return new EntityDestroyPacket(owner, (PacketPlayOutEntityDestroy) p);
			case ENTITY_EQUIPMENT:
				return new EntityEquipmentPacket(owner, (PacketPlayOutEntityEquipment) p);
			case ENTITY_HEADROTATION:
				return new EntityHeadRotationPacket(owner, (PacketPlayOutEntityHeadRotation) p);
			case ENTITY_LOOK:
				return new EntityLookPacket(owner, (PacketPlayOutEntityLook) p);
			case ENTITY_METADATA:
				return new EntityMetadataPacket(owner, (PacketPlayOutEntityMetadata) p);
			case ENTITY_POSITION:
				return new PositionPacket(owner, (PacketPlayOutPosition) p);
			case ENTITY_RELMOVE:
				return new EntityRelativeMovePacket(owner, (PacketPlayOutRelEntityMove) p);
			case ENTITY_RELMOVELOOK:
				return new EntityRelativeMoveLookPacket(owner, (PacketPlayOutRelEntityMoveLook) p);
			case ENTITY_TELEPORT:
				return new EntityTeleportPacket(owner, (PacketPlayOutEntityTeleport) p);
			case PLAYER_INFO:
				return new PlayerInfoPacket(owner, (PacketPlayOutPlayerInfo) p);
			case SPAWN_ENTITY_LIVING:
				return new SpawnEntityLivingPacket(owner, (PacketPlayOutSpawnEntityLiving) p);
			case SPAWN_ENTITY_NAMED:
				return new SpawnEntityNamedPacket(owner, (PacketPlayOutNamedEntitySpawn) p);
			case TRANSACTION:
				return new TransactionPacket(owner, (PacketPlayOutTransaction) p);
			case UNKNOWN:
				return new UnknownPacket(owner, p);
			default:
				break;
		}
		return new UnknownPacket(owner, p);
	}

	private static class UnknownPacket extends PacketOut
	{
		public UnknownPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, net.minecraft.server.v1_7_R4.Packet p)
		{
			super(owner, PacketTypeOut.UNKNOWN, p);
		}

		@Override
		public net.minecraft.server.v1_7_R4.Packet toNMS()
		{
			return getNMSPacket();
		}
	}
}
