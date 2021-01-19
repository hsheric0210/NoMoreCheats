package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityTeleport;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class EntityTeleportPacket extends PacketOut
{
	private int entityid;
	private int x;
	private int y;
	private int z;
	private byte yaw;
	private byte pitch;
	private boolean onground;

	public EntityTeleportPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutEntityTeleport packet)
	{
		super(owner, PacketTypeOut.ENTITY_TELEPORT, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.x = InternalUtils.getField(packet, "b");
		this.y = InternalUtils.getField(packet, "c");
		this.z = InternalUtils.getField(packet, "d");
		this.yaw = InternalUtils.getField(packet, "e");
		this.pitch = InternalUtils.getField(packet, "f");
		this.onground = InternalUtils.getField(packet, "onGround");
	}

	public EntityTeleportPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int entityid, int x, int y, int z, byte yaw, byte pitch, boolean onground)
	{
		super(owner, PacketTypeOut.ENTITY_TELEPORT, new PacketPlayOutEntityTeleport(entityid, x, y, z, yaw, pitch, onground, false));
		this.entityid = entityid;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.onground = onground;
	}

	public EntityTeleportPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int entityid, Location loc, boolean ground)
	{
		this(owner, entityid, (int) Math.floor(loc.getX() * 32), (int) Math.floor(loc.getY() * 32), (int) Math.floor(loc.getZ() * 32), (byte) (loc.getYaw() * 256F / 360F), (byte) (loc.getPitch() * 256F / 360F), ground);
	}

	public EntityTeleportPacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, net.minecraft.server.v1_7_R4.Entity entity)
	{
		this(owner, entity.getId(), new Location(null, entity.locX, entity.locY, entity.locZ), entity.onGround);
	}

	public void setEntity(int id)
	{
		this.entityid = id;
	}

	public int getEntity()
	{
		return this.entityid;
	}

	public void setOnGround(boolean b)
	{
		this.onground = b;
	}

	public boolean isOnGround()
	{
		return this.onground;
	}

	public void setPosition(Vector vec)
	{
		this.x = vec.getBlockX();
		this.y = vec.getBlockY();
		this.z = vec.getBlockZ();
	}

	public void convertAndSetPosition(Vector vec)
	{
		this.x = (int) Math.floor(vec.getX() * 32);
		this.y = (int) Math.floor(vec.getY() * 32);
		this.z = (int) Math.floor(vec.getZ() * 32);
	}

	public Vector getConvertedPosition()
	{
		return new Vector(this.x, this.y, this.z);
	}

	public Vector getRealPosition()
	{
		return new Vector(this.x / 32, this.y / 32, this.z / 32);
	}

	public void setFacing(byte _yaw, byte _pitch)
	{
		this.yaw = _yaw;
		this.pitch = _pitch;
	}

	public void convertAndSetFacing(float _yaw, float _pitch)
	{
		this.yaw = (byte) (_yaw * 256F / 360F);
		this.pitch = (byte) (_pitch * 256F / 360F);
	}

	public float[] getConvertedFacing()
	{
		return new float[]
		{
				this.yaw, this.pitch
		};
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutEntityTeleport(this.entityid, this.x, this.y, this.z, this.yaw, this.pitch, this.onground, false);
	}
}
