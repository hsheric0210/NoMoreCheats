package com.eric0210.nomorecheats.checks.combat;

import java.util.HashMap;
import java.util.UUID;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.Lag;
import com.eric0210.nomorecheats.api.util.MathUtils;
import com.eric0210.nomorecheats.api.util.Counter;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class HitReach extends Check implements EventListener
{
	public HashMap<UUID, Location> lastLocation = new HashMap<>();
	public HashMap<UUID, Double> lastSpeed = new HashMap<>();
	public double verticalMaxReach = 4.501;
	public double horizontalDistanceThreshold = 4.05;

	public HitReach()
	{
		super("HitReach");
		EventManager.onEntityDamageByEntity.add(new EventInfo(this, 0));
		EventManager.onPlayerMove.add(new EventInfo(this, 1));
		this.verticalMaxReach = getConfig().getValue("VerticalDistanceLimit", 4.501D);
		this.horizontalDistanceThreshold = getConfig().getValue("HorizontalDistanceLimit", 4.05D);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) ev;
				if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity)
				{
					Player p = (Player) e.getDamager();
					LivingEntity victim = (LivingEntity) e.getEntity();
					if (p.getGameMode() != GameMode.CREATIVE)
					{
						double verticalReach = MathUtils.getVerticalDistance(p.getLocation(), victim.getLocation());
						double verticalReachMax = this.verticalMaxReach;
						verticalReachMax = (verticalReachMax <= 4.5) ? 4.501 : verticalReachMax;
						if (verticalReach >= this.verticalMaxReach && !BlockUtils.isClimbableNearby(victim.getLocation()))
						{
							if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".vertical", 99) >= Lag.correction(p, 1))
								suspect(p, 2, "t: vertical", "d: " + verticalReach);
							e.setCancelled(true);
						}
						else
						{
							boolean haveSpeed = p.hasPotionEffect(PotionEffectType.SPEED) && p.getWalkSpeed() >= .35D;
							double reach = MathUtils.getHorizontalDistance(p.getLocation(), victim.getLocation());
							double maxReach = this.horizontalDistanceThreshold + (haveSpeed ? 1.0 : 0.0);
							maxReach = maxReach < 3.6 ? 3.6 : maxReach;
							int verboseLimit = (Counter.getCount(p.getUniqueId(), this.name + ".horizontal") >= 3 || this.lastSpeed.getOrDefault(p.getUniqueId(), 0.0D) <= 0.1D) ? 2 : 4;
							if (reach >= maxReach && Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".horizontal", 100) >= Lag.correction(p, verboseLimit))
							{
								suspect(p, Math.abs(reach - maxReach), "t: horizontal", "d: " + reach);
								e.setCancelled(true);
							}
						}
					}
				}
				break;
			case 1:
				PlayerMoveEvent pe = (PlayerMoveEvent) ev;
				if (this.lastLocation.containsKey(pe.getPlayer().getUniqueId()))
					this.lastSpeed.put(pe.getPlayer().getUniqueId(), MathUtils.getHorizontalDistance(this.lastLocation.get(pe.getPlayer().getUniqueId()), pe.getPlayer().getLocation()));

				this.lastLocation.put(pe.getPlayer().getUniqueId(), pe.getPlayer().getLocation());
				break;
		}
	}
}
