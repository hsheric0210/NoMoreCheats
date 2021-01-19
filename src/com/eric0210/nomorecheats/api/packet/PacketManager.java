package com.eric0210.nomorecheats.api.packet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.entity.Player;

import com.eric0210.nomorecheats.Logging;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.packet.listeners.PlayerConnectionFilter;
import com.eric0210.nomorecheats.api.util.NMS;
import net.minecraft.server.v1_7_R4.EntityPlayer;

public class PacketManager
{
	private static ArrayList<PacketListener> listeners = new ArrayList<>();
	private static long last = System.currentTimeMillis();

	public static void addListener(PacketListener listener)
	{
		if (listener != null)
			listeners.add(listener);
	}

	public static void applyPacketFilter(Collection<Player> players)
	{
		for (Player p : players)
		{
			PlayerConnectionFilter listener = NMS.replacePlayerconnection(p);
			Logging.debug("Applied Packet Filter(" + String.valueOf(listener) + ") to " + p.getName());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends net.minecraft.server.v1_7_R4.Packet> T handleNMSRecv(EntityPlayer owner, T nmspacket)
	{
		if ((owner == null) || (nmspacket == null))
			return null;
		PacketIn packet = PacketIn.byNMS(owner, nmspacket);
		System.out.println("[" + (System.currentTimeMillis() - last) + "ms] " + packet.getType().name() + " : " + nmspacket.b());
		last = System.currentTimeMillis();
		PacketInEvent event = new PacketInEvent(owner.getBukkitEntity(), packet);
		try
		{
			for (PacketListener listener : listeners)
			{
				if (listener instanceof Check && !((Check) listener).isEnabled())
					continue;
				listener.onPacketIn(event);
			}
		}
		catch (Throwable t)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			Logging.debug("[PacketManager] An exception thrown while checking the took(in) packet. packetName: " + nmspacket.getClass().getSimpleName() + " nms packet data: \"" + nmspacket.b() + "\" exception info: " + " (" + t.getClass().getSimpleName() + ": " + t.getMessage() + "): \n" + sw.toString());
			pw.close();
		}
		if (event.isCancelled())
			return null;
		return (T) event.getPacket().toNMS();
	}

	@SuppressWarnings("unchecked")
	public static <T extends net.minecraft.server.v1_7_R4.Packet> T handleNMSSend(EntityPlayer owner, T nmspacket)
	{
		if ((owner == null) || (nmspacket == null))
			return null;
		PacketOut packet = PacketOut.byNMS(owner, nmspacket);
//		if (nmspacket instanceof PacketPlayOutRelEntityMove)
//			Cache.set(owner.getWorld().getEntity(InternalUtils.getField(nmspacket, "a")).uniqueID,
//					"packetRelativeSpeed", nmspacket.b());
		PacketOutEvent event = new PacketOutEvent(owner.getBukkitEntity(), packet);
		try
		{
			for (PacketListener listener : listeners)
			{
				if (listener instanceof Check && !((Check) listener).isEnabled())
					continue;
				listener.onPacketOut(event);
			}
		}
		catch (Throwable t)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			Logging.debug("[PacketManager] An exception thrown while checking the took(in) packet. packetName: " + nmspacket.getClass().getSimpleName() + " nms packet data: \"" + nmspacket.b() + "\" exception info: " + " (" + t.getClass().getSimpleName() + ": " + t.getMessage() + "): \n" + sw.toString());
			pw.close();
		}
		if (event.isCancelled())
			return null;
		return (T) event.getPacket().toNMS();
	}

//	public static <T extends net.minecraft.server.v1_7_R4.Packet> void sendPacket(Player entityPlayer, T packet)
//	{
//		sendPacket(NMS.asNMS(entityPlayer), packet);
//	}
//
//	public static <T extends net.minecraft.server.v1_7_R4.Packet> void sendPacket(EntityPlayer player, T packet)
//	{
//		player.playerConnection.sendPacket(packet);
//	}
	public static <T extends PacketOut> void sendPacket(Player entityPlayer, T packet)
	{
		sendPacket(NMS.asNMS(entityPlayer), packet);
	}

	public static <T extends PacketOut> void sendPacket(EntityPlayer player, T packet)
	{
		player.playerConnection.sendPacket(packet.toNMS());
	}
}
