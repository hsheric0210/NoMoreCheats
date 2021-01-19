package com.eric0210.nomorecheats.checks.combat;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.TimeUtils;

public class FastShot extends Check implements EventListener
{
	public FastShot()
	{
		super("FastShot");
		EventManager.onEntityShootBow.add(new EventInfo(this, 0));
		EventManager.onProjectileLaunch.add(new EventInfo(this, 1));
		EventManager.onPlayerInteract.add(new EventInfo(this, 2));
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				EntityShootBowEvent bowShootEvent = (EntityShootBowEvent) ev;
				if (bowShootEvent.getProjectile() instanceof Arrow)
				{
					if (bowShootEvent.getEntity() != null && bowShootEvent.getEntity() instanceof Player)
					{
						Player shooter = (Player) bowShootEvent.getEntity();
						if (TimeUtils.contains(shooter.getUniqueId(), this.name + ".bowpull"))
						{
							long pullduration = TimeUtils.getTimeDiff(shooter.getUniqueId(), this.name + ".bowpull", 0L);
							float power = ((int) (pullduration * 20L / 1000L)) / 20f;
							power = power > 1f ? 1f : power;

							double delta;
							if ((delta = bowShootEvent.getForce() - power) > .25F)
							{
								suspect(shooter, (int) delta, "p: " + bowShootEvent.getForce(), "p_l: " + power, "t: " + pullduration);
								Cooldowns.set(shooter.getUniqueId(), this.name + ".cooldown", 100);
							}
						}
						if (!Cooldowns.isCooldownEnded(shooter.getUniqueId(), this.name + ".cooldown"))
							bowShootEvent.setCancelled(true);
					}
				}
				break;
			case 1:
				ProjectileLaunchEvent launchEvent = (ProjectileLaunchEvent) ev;
				if (launchEvent.getEntity() instanceof Arrow)
				{
					Arrow arrow = (Arrow) launchEvent.getEntity();
					if (arrow.getShooter() != null && arrow.getShooter() instanceof Player)
					{
						Player shooter = (Player) arrow.getShooter();
						Counter.increment1AndGetCount(shooter.getUniqueId(), this.name + ".launchedProjectiles", -1);
						if (!TimeUtils.contains(shooter.getUniqueId(), this.name + ".projTime"))
						{
							TimeUtils.putCurrentTime(shooter.getUniqueId(), this.name + ".projTime");
							return;
						}
						if (Counter.getCount(shooter.getUniqueId(), this.name + ".launchedProjectiles") > 10)
						{
							long time = TimeUtils.getTimeDiff(shooter.getUniqueId(), this.name + ".projTime", System.currentTimeMillis());
							Counter.remove(shooter.getUniqueId(), this.name + ".launchedProjectiles");
							TimeUtils.putCurrentTime(shooter.getUniqueId(), this.name + ".projTime");
							if (time < 1500)
							{
								suspect(shooter, 2, "t: " + time);
								Cooldowns.set(shooter.getUniqueId(), this.name + ".cooldown", 100);
							}
						}
						if (!Cooldowns.isCooldownEnded(shooter.getUniqueId(), this.name + ".cooldown"))
							launchEvent.setCancelled(true);
					}
				}
				break;
			case 2:
				PlayerInteractEvent interactEvent = (PlayerInteractEvent) ev;
				if (interactEvent.getItem() != null && interactEvent.getItem().getType() == Material.BOW && (interactEvent.getAction() == Action.RIGHT_CLICK_AIR || interactEvent.getAction() == Action.RIGHT_CLICK_BLOCK))
					TimeUtils.putCurrentTime(interactEvent.getPlayer().getUniqueId(), this.name + ".bowpull");
				break;
		}
	}
}
