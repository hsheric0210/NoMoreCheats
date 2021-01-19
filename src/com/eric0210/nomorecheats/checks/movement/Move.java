package com.eric0210.nomorecheats.checks.movement;

import java.util.UUID;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.packet.PacketInEvent;
import com.eric0210.nomorecheats.api.packet.PacketListener;
import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.packet.PacketOutEvent;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.packets.in.FlyingPacket;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.Protections;
import com.eric0210.nomorecheats.api.util.TimeUtils;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.MathUtils;
import com.eric0210.nomorecheats.api.util.NMS;
import com.eric0210.nomorecheats.api.util.PlayerUtils;
import com.eric0210.nomorecheats.api.util.Protections.ProtectionType;

import net.minecraft.server.v1_7_R4.MinecraftServer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Move extends Check implements EventListener, PacketListener
{
	private final double movedTooQuicklyThreshold = 100;

	public Move()
	{
		super("Move");
		EventManager.onPlayerTeleport.add(new EventInfo(this, 0));
		PacketManager.addListener(this);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		PlayerTeleportEvent tp = (PlayerTeleportEvent) ev;
		if (!tp.isCancelled())
			Counter.increment1AndGetCount(tp.getPlayer().getUniqueId(), "_teleported", -1);
	}

	@Override
	public void onPacketIn(PacketInEvent pe)
	{
		if (pe.getPacket().getType() == PacketTypeIn.FLYING)
		{
			FlyingPacket packet = (FlyingPacket) pe.getPacket();
			Player p = pe.getPlayer();
			UUID uid = p.getUniqueId();
			if (!PlayerUtils.wasFlightAllowed(p) && !PlayerUtils.wasFlying(p) && !p.isInsideVehicle() && !BlockUtils.isFullyStuck(p) && !BlockUtils.isPartiallyStuck(p))
			{
				if (TimeUtils.contains(uid, this.name + ".morepackets.lastpacketrecv"))
				{
					long delta = TimeUtils.getTimeDiff(uid, this.name + ".morepackets.lastpacketrecv", -1l);
					if (delta >= 100L || Protections.hasProtection(p, ProtectionType.MORE_PACKETS))
					{
						Cache.set(uid, this.name + ".morepackets.timercheckblacklist", true);
					}
					else if (Cache.get(uid, this.name + ".morepackets.timercheckblacklist", false))
						Cache.remove(uid, this.name + ".morepackets.timercheckblacklist");
					else
					{
						// Timer check
						int count = Counter.getCount(uid, this.name + ".morepackets.timerpackets") - Counter.getCount(uid, "_teleported");
						if ((count > 20) && !BlockUtils.isFullyStuck(p) && !BlockUtils.isPartiallyStuck(p))
							suspect(p, 1, "t: timer", "r: (send " + count + " movements in a second)");

						if (Cooldowns.isCooldownEnded(uid, this.name + ".morepackets.checktimer"))
						{
							Cooldowns.set(uid, this.name + ".morepackets.checktimer", 20); // wait 1 second (20 ticks)
							Counter.remove(uid, "_teleported");
							Counter.remove(uid, this.name + ".morepackets.timerpackets");
						}
					}
				}
				Counter.increment1AndGetCount(uid, this.name + ".morepackets.timerpackets", 0);
				TimeUtils.putCurrentTime(uid, this.name + ".morepackets.lastpacketrecv");
			}
			// Notchian-servers Default server-side Movements checks re-make

			Location p_pos = p.getLocation();
			Location to = p_pos.clone();
			if (packet.hasPos())
				to = packet.getTo().toLocation(p.getWorld());
			if (packet.hasLook())
			{
				to.setYaw(packet.getYaw());
				to.setPitch(packet.getPitch());
			}
			if (to == null)
				return;
			Location to_fix = to.clone();
			boolean flag = false;
			if (Double.isNaN(to_fix.getX()) || Double.isInfinite(to_fix.getX()) || to_fix.getX() > 3.0E7D || to_fix.getX() < -3.0E7D)
			{
				to_fix.setX(p_pos.getX());
				flag = true;
			}
			if (Double.isNaN(to_fix.getY()) || Double.isInfinite(to_fix.getY()))
			{
				to_fix.setY(p_pos.getY());
				flag = true;
			}
			if (Double.isNaN(to_fix.getZ()) || Double.isInfinite(to_fix.getZ()) || to_fix.getZ() > 3.0E7D || to_fix.getZ() < -3.0E7D)
			{
				to_fix.setZ(p_pos.getZ());
				flag = true;
			}
			if (Double.isNaN(to_fix.getYaw()) || Double.isInfinite(to_fix.getYaw()))
			{
				to_fix.setYaw(p_pos.getYaw());
				flag = true;
			}
			if (Double.isNaN(to_fix.getPitch()) || Double.isInfinite(to_fix.getPitch()))
			{
				to_fix.setPitch(p_pos.getPitch());
				flag = true;
			}
			double stance = packet.getStance() - to.getY();
			if (Double.isNaN(packet.getStance()) || Double.isInfinite(packet.getStance()) || (to.getY() != 0) && ((p.isSleeping() && stance > 1.65) || (stance < .1)) && MathUtils.getDistance3D(p.getLocation(), to) > 0)
			{
				double fixStance = to_fix.getY() + 1.62;
				suspect(p, 5, "t: illegal", "r: stance", "(s: " + packet.getStance(), "y: " + to.getY(), "stance: " + stance + ") -> (s: " + fixStance, "y: " + to_fix.getY(), "stance: " + (fixStance - to_fix.getY()) + ")");
				packet.setStance(fixStance);
			}
			if (flag)
			{
				suspect(p, 5, "t: illegal", "r: position", "(" + to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch() + ") -> (" + to_fix.getX(), to_fix.getY(), to_fix.getZ(), to_fix.getYaw(), to_fix.getPitch() + ")");
				to = to_fix.clone();
				packet.setTo(to_fix.toVector());
				packet.setYaw(to_fix.getYaw());
				packet.setPitch(to_fix.getPitch());
			}
			if (!PlayerUtils.wasFlightAllowed(p) && !PlayerUtils.wasFlying(p))
			{
				double xDelta = Math.abs(p_pos.getX() - to.getX());
				double yDelta = Math.abs(p_pos.getY() - to.getY());
				double zDelta = Math.abs(p_pos.getZ() - to.getZ());
				double xMot = Math.max(xDelta, p.getVelocity().getX());
				double yMot = Math.max(yDelta, p.getVelocity().getY());
				double zMot = Math.max(zDelta, p.getVelocity().getZ());
				double momentum_square_sum = xMot * xMot + yMot * yMot + zMot * zMot;
				if (momentum_square_sum > this.movedTooQuicklyThreshold && NMS.asNMS(p).playerConnection.checkMovement && (!MinecraftServer.getServer().N() || MinecraftServer.getServer().M().equals(p.getName())))
				{
					suspect(p, (momentum_square_sum / 100), "t: unusual", "x: " + xDelta, "y: " + yDelta, "z: " + zDelta, "m_x: " + xMot, "m_y: " + yMot, "m_z: " + zMot);
					pe.setCancelled(true);
					p.teleport(p_pos, TeleportCause.UNKNOWN);
				}
			}
			pe.setPacket(packet);

		}
	}

	@Override
	public void onPacketOut(PacketOutEvent e)
	{
	}
}
