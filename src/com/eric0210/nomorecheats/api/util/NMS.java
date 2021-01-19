package com.eric0210.nomorecheats.api.util;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R4.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.eric0210.nomorecheats.api.packet.listeners.PlayerConnectionFilter;

import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.DedicatedPlayerList;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.Vec3D;
import net.minecraft.server.v1_7_R4.WorldServer;

public class NMS
{
	public static final EntityPlayer asNMS(Player p)
	{
		return ((CraftPlayer) p).getHandle();
	}

	public static final DedicatedPlayerList asNMS(Server server)
	{
		return ((CraftServer) server).getHandle();
	}

	public static final net.minecraft.server.v1_7_R4.ItemStack asNMS(org.bukkit.inventory.ItemStack itemstack)
	{
		return CraftItemStack.asNMSCopy(itemstack);
	}

	public static final org.bukkit.inventory.ItemStack asBukkit(net.minecraft.server.v1_7_R4.ItemStack itemstack)
	{
		return CraftItemStack.asBukkitCopy(itemstack);
	}

	public static WorldServer asNMS(World world)
	{
		return ((CraftWorld) world).getHandle();
	}

	public static Block asNMS(org.bukkit.block.Block b)
	{
		return CraftMagicNumbers.getBlock(b);
	}

	public static Entity asNMS(org.bukkit.entity.Entity damagee)
	{
		return ((CraftEntity) damagee).getHandle();
	}

	public static Vec3D vecterToVec3d(Vector vec)
	{
		return Vec3D.a(vec.getX(), vec.getY(), vec.getZ());
	}

	public static Integer getCurrentTick()
	{
		return MinecraftServer.currentTick;
	}

	public static PlayerConnectionFilter replacePlayerconnection(Player p)
	{
		PlayerConnection oldConnection = ((CraftPlayer) p).getHandle().playerConnection;
		PlayerConnectionFilter listener = new PlayerConnectionFilter(MinecraftServer.getServer(), oldConnection.networkManager,
				((CraftPlayer) p).getHandle());
		((CraftPlayer) p).getHandle().playerConnection = listener;
		return listener;
	}
}
