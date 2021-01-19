package com.eric0210.nomorecheats.checks.combat;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.GroundChecks;
import com.eric0210.nomorecheats.api.util.PlayerUtils;
import com.eric0210.nomorecheats.api.util.BlockUtils;

public class Criticals extends Check implements EventListener
{
	private double fallDistanceThreshold;

	public Criticals()
	{
		super("Criticals");
		EventManager.onEntityDamageByEntity.add(new EventInfo(this, 0));
		this.fallDistanceThreshold = getConfig().getValue("FallDistanceThreshold", 0.115D);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) ev;
		if (e.getDamager() instanceof Player)
		{
			Player p = (Player) e.getDamager();
			if (PlayerUtils.wasFlightAllowed(p) || PlayerUtils.wasFlying(p) || p.getVehicle() != null || p.isInsideVehicle() || p.hasPotionEffect(PotionEffectType.BLINDNESS) || p.getFallDistance() == 0.0F)
				return;

			float fallDistance = p.getFallDistance();

			if (!GroundChecks.isOnGround(p))
			{
				if (p.getLocation().getY() % 1.0D == 0) // nearly improbable
				{
					if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".location", 100) >= 2)
					{
						suspect(p, 5, "t: unusual", "y: " + p.getLocation().getY());
						e.setCancelled(true);
					}
				}
			}

			if (Counter.getCount(p.getUniqueId(), "blockAbove") > 0 || BlockUtils.isMaterialSurround(p.getLocation(), 0.3, false, Material.WEB) || Counter.getCount(p.getUniqueId(), "waterTicks") > 0 || BlockUtils.hasSteppableNearby(p.getLocation()) || p.getFallDistance() > this.fallDistanceThreshold)
				return;

			if (fallDistance > 0 && Cache.get(p.getUniqueId(), "fallDistance", 0.0F) == 0.0F)
				if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".packet", 80) > 2)
				{
					suspect(p, 1, "t: illegal", "falldist: " + fallDistance);
					e.setCancelled(true);
				}

			if (fallDistance > 0 && fallDistance <= 0.07F && (GroundChecks.isOnGround(p.getLocation(), -fallDistance) || GroundChecks.isOnGround(p.getLocation(), (-fallDistance + .1))))
			{
				suspect(p, 3, "t: mini-jump", "falldist: " + fallDistance);
				e.setCancelled(true);
			}

			double byDelta = Math.abs(p.getLocation().getY() - p.getLocation().getBlockY());
			double lastDelta = Cache.get(p.getUniqueId(), this.name + ".yfraction", 0D);
			if (Counter.getCount(p.getUniqueId(), "airTicks") >= 20 && (byDelta >= 0.15D && byDelta <= 0.75D) && lastDelta == byDelta)
			{
				suspect(p, 7, "t: repeated-value", "y: " + byDelta, "l_y: " + lastDelta);
				e.setCancelled(true);
			}
			Cache.set(p.getUniqueId(), this.name + ".yfraction", byDelta);
		}
	}
}
