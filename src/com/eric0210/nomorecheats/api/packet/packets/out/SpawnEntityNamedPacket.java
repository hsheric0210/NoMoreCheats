package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.DataWatcher;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedEntitySpawn;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.util.Vector;

public class SpawnEntityNamedPacket extends PacketOut
{
	private int entityid;
	private GameProfile profile;
	private int x;
	private int y;
	private int z;
	private byte yaw;
	private byte pitch;
	private int heldItemId;
	private DataWatcher dw;

	public SpawnEntityNamedPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutNamedEntitySpawn packet)
	{
		super(owner, PacketTypeOut.SPAWN_ENTITY_NAMED, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.profile = InternalUtils.getField(packet, "b");
		this.x = InternalUtils.getField(packet, "c");
		this.y = InternalUtils.getField(packet, "d");
		this.z = InternalUtils.getField(packet, "e");
		this.yaw = InternalUtils.getField(packet, "f");
		this.pitch = InternalUtils.getField(packet, "g");
		this.heldItemId = InternalUtils.getField(packet, "h");
		this.dw = InternalUtils.getField(packet, "i");
//		if (dw != null && entityid != owner.getId() && dw.getFloat(6) > 1f)
//			dw.watch(6, 1f);
	}

	public SpawnEntityNamedPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, EntityHuman entityhuman)
	{
		super(owner, PacketTypeOut.SPAWN_ENTITY_NAMED, new PacketPlayOutNamedEntitySpawn(entityhuman));
		this.entityid = entityhuman.getId();
		this.profile = entityhuman.getProfile();
		this.x = (int) Math.floor(entityhuman.locX * 32.0);
		this.y = (int) Math.floor(entityhuman.locY * 32.0);
		this.z = (int) Math.floor(entityhuman.locZ * 32.0);
		this.yaw = (byte) (entityhuman.yaw * 256.0f / 360.0f);
		this.pitch = (byte) (entityhuman.pitch * 256.0f / 360.0f);
		this.heldItemId = entityhuman.getBukkitEntity().getItemInHand() != null ? entityhuman.getBukkitEntity().getItemInHand().getTypeId() : 0;
		this.dw = entityhuman.getDataWatcher();
	}

	public void setEntity(int id)
	{
		this.entityid = id;
	}

	public int getEntity()
	{
		return this.entityid;
	}

	public void setProfile(GameProfile profile)
	{
		this.profile = profile;
	}

	public GameProfile getProfile()
	{
		return this.profile;
	}

	public void setPosition(Vector pos)
	{
		this.x = pos.getBlockX();
		this.y = pos.getBlockY();
		this.z = pos.getBlockZ();
	}

	public void convertAndSetPosition(Vector pos)
	{
		this.x = (int) Math.floor(pos.getX() * 32);
		this.y = (int) Math.floor(pos.getY() * 32);
		this.z = (int) Math.floor(pos.getZ() * 32);
	}

	public Vector getConvertedPosition()
	{
		return new Vector(this.x, this.y, this.z);
	}

	public Vector getRealPosition()
	{
		return new Vector(this.x / 32, this.y / 32, this.z / 32);
	}

	public void setFacing(byte[] facing)
	{
		this.yaw = facing[0];
		this.pitch = facing[1];
	}

	public void convertAndSetFacing(float yaw, float pitch)
	{
		this.yaw = (byte) (yaw * 256F / 360F);
		this.pitch = (byte) (pitch * 256F / 360F);
	}

	public byte[] getConvertedFacing()
	{
		return new byte[]
		{
				this.yaw, this.pitch
		};
	}

	public float[] getRealFacing()
	{
		return new float[]
		{
				this.yaw * 360F / 256F, this.pitch * 360F / 256F
		};
	}

	@Override
	public Packet toNMS()
	{
		PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
		InternalUtils.setField(packet, "a", this.entityid);
		InternalUtils.setField(packet, "b", this.profile);
		InternalUtils.setField(packet, "c", this.x);
		InternalUtils.setField(packet, "d", this.y);
		InternalUtils.setField(packet, "e", this.z);
		InternalUtils.setField(packet, "f", this.yaw);
		InternalUtils.setField(packet, "g", this.pitch);
		InternalUtils.setField(packet, "h", this.heldItemId);
		InternalUtils.setField(packet, "i", this.dw);
		return packet;
	}
}
