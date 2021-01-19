package com.eric0210.nomorecheats.api.packet.packets.out;

import com.eric0210.nomorecheats.api.packet.PacketOut;
import com.eric0210.nomorecheats.api.packet.PacketTypeOut;
import com.eric0210.nomorecheats.api.util.InternalUtils;

import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutRelEntityMove;

import org.bukkit.util.Vector;

public class EntityRelativeMovePacket extends PacketOut
{
	private int entityid;
	private byte relative_x;
	private byte relative_y;
	private byte relative_z;
	private boolean onground;

	public EntityRelativeMovePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, PacketPlayOutRelEntityMove packet)
	{
		super(owner, PacketTypeOut.ENTITY_RELMOVE, packet);
		this.entityid = InternalUtils.getField(packet, "a");
		this.relative_x = InternalUtils.getField(packet, "b");
		this.relative_y = InternalUtils.getField(packet, "c");
		this.relative_z = InternalUtils.getField(packet, "d");
		this.onground = InternalUtils.getField(packet, "onGround");
	}

	public EntityRelativeMovePacket(net.minecraft.server.v1_7_R4.EntityPlayer owner, int entityid, byte xacc, byte yacc, byte zacc, boolean onground)
	{
		super(owner, PacketTypeOut.ENTITY_RELMOVE, new PacketPlayOutRelEntityMove(entityid, xacc, yacc, zacc, onground));
		this.entityid = entityid;
		this.relative_x = xacc;
		this.relative_y = yacc;
		this.relative_z = zacc;
		this.onground = onground;
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

	public void convertAndSetRelativeDeltaPosition(Vector currentpos, Vector lastpos)
	{
		this.relative_x = (byte) (Math.ceil(currentpos.getX() * 32 - lastpos.getX() * 32) * 2);
		this.relative_y = (byte) (Math.ceil(currentpos.getY() * 32 - lastpos.getY() * 32) * 2);
		this.relative_z = (byte) (Math.ceil(currentpos.getZ() * 32 - lastpos.getZ() * 32) * 2);
	}

	public void setRelativeDeltaPosition(Vector vec)
	{
		this.relative_x = (byte) vec.getX();
		this.relative_y = (byte) vec.getY();
		this.relative_z = (byte) vec.getZ();
	}

	public Vector getRelativeDeltaPosition()
	{
		return new Vector(this.relative_x, this.relative_y, this.relative_z);
	}

	@Override
	public Packet toNMS()
	{
		return new PacketPlayOutRelEntityMove(this.entityid, this.relative_x, this.relative_y, this.relative_z, this.onground);
	}
}
