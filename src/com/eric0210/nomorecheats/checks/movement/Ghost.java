package com.eric0210.nomorecheats.checks.movement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
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
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.Protections;
import com.eric0210.nomorecheats.api.util.Protections.ProtectionType;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.TickTasks;
import com.eric0210.nomorecheats.api.util.Reverter;

public class Ghost extends Check implements EventListener, PacketListener
{
	// public HashMap<UUID, Integer> ticks = new HashMap<>();
	public Ghost()
	{
		super("Ghost");
		EventManager.onPlayerMove.add(new EventInfo(this, 0));
		PacketManager.addListener(this);
		TickTasks.addTask(() ->
		{
			for (Player p : Bukkit.getOnlinePlayers())
			{
				if (Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".blink.last-packet") && !Protections.hasProtection(p, ProtectionType.BLINK))
				{
					int _ticks = Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".blinkTicks", -1);
					suspect(p, 1, "t: unusual", "t: " + _ticks);
					Reverter.getInstance("Blink").teleport(p);
				}
				else
					Reverter.getInstance("Blink").setPosition(p.getUniqueId(), p.getLocation());
			}
		});
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		PlayerMoveEvent e = (PlayerMoveEvent) ev;
		Player p = e.getPlayer();
		Location from = e.getFrom();
		Location to = e.getTo();
		if ((((Damageable) p).getHealth() == 0.0 && (from.getY() - to.getY() > 0.1625 || Math.abs(from.getX() - to.getX()) > 0.0 || Math.abs(from.getZ() - to.getZ()) > 0.0) || p.isDead() || p.isDead() && ((Damageable) p).getHealth() > 0.0) && (Math.abs(from.getX() - to.getX()) > 0.42 || Math.abs(from.getZ() - to.getZ()) > 0.42))
		{
			suspect(p, 1, "t: illegal");
			Reverter.getInstance("MoveAfterDead").teleport(p);
		}
		else
			Reverter.getInstance("MoveAfterDead").setPosition(p.getUniqueId(), from);
	}

	private void addBlink(Player p)
	{
		Cooldowns.set(p.getUniqueId(), this.name + ".blink.last-packet", 100); // 5 seconds
	}

	@Override
	public void onPacketIn(PacketInEvent pe)
	{
		if (pe.getPacket().getType() == PacketTypeIn.FLYING)
		{
			if (Cooldowns.isCooldownEnded(pe.getPlayer().getUniqueId(), this.name + ".blink.teleportProtection"))
				addBlink(pe.getPlayer());
		}
	}

	@Override
	public void onPacketOut(PacketOutEvent e)
	{
		// TODO Auto-generated method stub
	}
}
