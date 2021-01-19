package com.eric0210.nomorecheats.checks.movement;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.packet.PacketInEvent;
import com.eric0210.nomorecheats.api.packet.PacketListener;
import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.packet.PacketOutEvent;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.packets.in.SettingsPacket;
import com.eric0210.nomorecheats.api.util.InventoryUtils;
import com.eric0210.nomorecheats.api.util.NMS;

public class Derp extends Check implements EventListener, PacketListener
{
	public Derp()
	{
		super("Derp");
		EventManager.onPlayerMove.add(new EventInfo(this, 0));
		PacketManager.addListener(this);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		checkPitch((PlayerMoveEvent) ev);
	}

	public void checkPitch(PlayerMoveEvent e)
	{
		Player p = e.getPlayer();
		Location loc = p.getLocation();
		float pitch = loc.getPitch();
		if ((pitch > 90.1D) || (pitch < -90.1D))
		{
			suspect(e.getPlayer(), 1, "t: pitch", "r: " + pitch);
			Location defLoc = e.getPlayer().getLocation();
			NMS.asNMS(p).setLocation(defLoc.getX(), defLoc.getY(), defLoc.getZ(), 0, 0);
		}
	}

	@Override
	public void onPacketIn(PacketInEvent e)
	{
		if (e.getPacket().getType() == PacketTypeIn.SETTINGS)
		{
			SettingsPacket packet = (SettingsPacket) e.getPacket();
			int flags = packet.flags;
			if (flags >= 0 && flags <= 111)
			{
				String violation = null;
				if ((violation = InventoryUtils.getGUIActionViolation(e.getPlayer())) != null)
				{
					suspect(e.getPlayer(), 10, "t: skin", "gui_r: (" + violation + ")");
					e.setCancelled(true);
				}
			}
		}
	}

	@Override
	public void onPacketOut(PacketOutEvent e)
	{
		// TODO Auto-generated method stub
	}
}
