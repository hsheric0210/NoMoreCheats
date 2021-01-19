package com.eric0210.nomorecheats.checks.movement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.eric0210.nomorecheats.Logging;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.Violation;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.TickTasks;
import com.eric0210.nomorecheats.api.util.CombatUtils;
import com.eric0210.nomorecheats.api.util.MathUtils;
import com.eric0210.nomorecheats.api.util.Reverter;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.GroundChecks;

public class Velocity extends Check implements EventListener
{
	public HashMap<UUID, VelocityData> velocity_datas = new HashMap<>();

	public Velocity()
	{
		super("Velocity");
		EventManager.onEntityDamageByEntity.add(new EventInfo(this, 0));
		TickTasks.addTask(() ->
		{
			for (Player p : Bukkit.getOnlinePlayers())
			{
				int scheduler_cooldown = Cooldowns.get(p.getUniqueId(), this.name + ".scheduler");
				if (((scheduler_cooldown == 1) || (scheduler_cooldown == 3)))
				{
					VelocityData data = this.velocity_datas.getOrDefault(p.getUniqueId(), new VelocityData());
					Location current_pos = p.getLocation();
					Location knockback_pos = data.velocityTookPosition;
					if (knockback_pos == null)
						return;
					if (current_pos.getWorld().equals(knockback_pos.getWorld()))
					{
						int verboseThreshold = 3;
						ArrayList<String> violationTags = new ArrayList<>();
						int vl = 0;
						Location revertPos = null;
						double knockback_vertical = current_pos.getY() - knockback_pos.getY();
						if (scheduler_cooldown == 1) // Check a tick after player took velocity(knockback).
						{
							double knockback_distance = current_pos.distance(knockback_pos);
							double knockback_horizontal = MathUtils.getHorizontalDistance(current_pos, knockback_pos);
							double total_knockback = knockback_vertical + knockback_horizontal;
							boolean knockback_by_projectile = !Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".projectile");
							Logging.debug("distance: " + knockback_distance + " h_dist: " + knockback_horizontal + " v_dist: " + knockback_vertical + " total: " + total_knockback);
							if ((knockback_distance >= 1.0D) && (!Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".knockback")))
							{
								// Extreme(Scheduler) //
								double total_knockback_abs = knockback_horizontal + Math.abs(knockback_vertical);
								double deltaKnockBack = Math.abs(knockback_distance - total_knockback_abs);
								if (deltaKnockBack >= 1.5D)
								{
									if (Counter.increment1AndGetCount(p.getUniqueId(), ".extreme-scheduler", 77) > verboseThreshold)
									{
										violationTags.add("extreme");
										vl += 10;
										revertPos = knockback_pos;
									}
								}
							}
							// Distance(Hard) //
							if (knockback_distance <= .3 && knockback_vertical != 0)
							{
								if (Counter.increment1AndGetCount(p.getUniqueId(), ".distance-hard", 77) > verboseThreshold)
								{
									violationTags.add("distance(hard)");
									vl += (.3 - knockback_distance) * 34;
								}
							}
							// Combined //
							if (knockback_distance == total_knockback && !knockback_by_projectile)
							{
								if (Counter.increment1AndGetCount(p.getUniqueId(), ".combined", 55) > verboseThreshold)
								{
									violationTags.add("combined");
									vl += 10;
								}
							}
							// Distance(Sensitivity) //
							if (knockback_distance <= 0.15D)
							{
								if (Counter.increment1AndGetCount(p.getUniqueId(), ".distance-sens", 66) > verboseThreshold)
								{
									violationTags.add("distance(sensitive)");
									vl += (.15 - knockback_distance) * 34;
								}
							}
							// Vertical //
							if (knockback_vertical > 0.0 && knockback_vertical <= 0.5D)
							{
								if (data.cachedKnockbackY != 0.0D)
								{
									double deltaKnockbackY = Math.abs(knockback_vertical - data.cachedKnockbackY);
									if (deltaKnockbackY <= 0.01D)
									{
										if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".vertical", 77) > verboseThreshold)
										{
											violationTags.add("vertical");
											vl += 5;
										}
									}
								}
								data.cachedKnockbackY = knockback_vertical;
							}
							else
							{
								data.cachedKnockbackY = 0.0D;
							}
							// Horizontal //
							if (knockback_vertical > 0.0 && knockback_horizontal == 0.0D)
							{
								if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".horizontal", 99) > verboseThreshold)
								{
									violationTags.add("horizontal");
									vl += 10;
								}
							}
							// Opposite //
							if (knockback_distance >= 2.5D && !knockback_by_projectile && Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".direction"))
							{
								Location expectedPos = knockback_pos.clone().add(knockback_pos.getDirection().clone().multiply(knockback_distance));
								double distance = expectedPos.distance(current_pos);
								if (distance <= 0.5D)
								{
									violationTags.add("opposite");
									vl += distance * 20;
								}
							}
							// Direction //
							if (data.lastAttackerLocation != null)
							{
								Location attackerPos = data.lastAttackerLocation;
								if (knockback_distance >= 3.0D && !knockback_by_projectile)
								{
									Location expectedPos = knockback_pos.clone().add(attackerPos.getDirection().clone().multiply(knockback_distance));
									double distance = MathUtils.getHorizontalDistance(current_pos, expectedPos);
									double distance_delta = distance - knockback_distance;
									if (distance >= 2.5D && distance_delta >= 1.0D)
									{
										violationTags.add("distance");
										vl += 10;
										revertPos = expectedPos;
									}
								}
							}
						}
						else if (scheduler_cooldown == 3 && CombatUtils.hasCombat(p) && (Counter.getCount(p.getUniqueId(), "airTicks") > 15))
						{
							// Prediction //
							boolean ground = GroundChecks.isOnGround(p.getLocation());
							boolean jumping = !Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".jumping");
							boolean pitchup = !Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".pitchup");
							boolean check = knockback_vertical >= 0
									? true
									: (ground || !pitchup ? false : (knockback_vertical < 0.0D ? true : (ground && knockback_vertical == 0) || (jumping && knockback_vertical > 0.0) || MathUtils.round(knockback_vertical, 1) == 0.3) ? true : false);
							if (!check)
							{
								if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".prediction", 77) >= verboseThreshold)
									violationTags.add("prediction");
							}
						}
						if (!violationTags.isEmpty() && vl != 0)
						{
							suspect(p, vl, Violation.convertTagListtoTag(violationTags));
							if (revertPos != null)
							{
								Reverter.getInstance(this).setPosition(p.getUniqueId(), revertPos);
								Reverter.getInstance(this).teleport(p);
							}
						}
					}
					this.velocity_datas.put(p.getUniqueId(), data);
				}
			}
		});
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent) ev;
		if (e2.getEntity() instanceof Player)
		{
			Player player = (Player) e2.getEntity();
			Entity damager = e2.getDamager();
			boolean isDamagedByEntity = e2.getCause() == DamageCause.ENTITY_ATTACK;
			boolean isDamagedByArrow = e2.getCause() == DamageCause.PROJECTILE && (damager instanceof Arrow);
			if (isDamagedByEntity || isDamagedByArrow)
			{
				VelocityData data = this.velocity_datas.getOrDefault(player.getUniqueId(), new VelocityData());
				double distdiff = player.getLocation().distance(damager.getLocation());
				data.velocityTookPosition = player.getLocation();
				data.lastAttackerLocation = damager.getLocation();
				Cooldowns.set(player.getUniqueId(), this.name + ".damage", 2);
				Cooldowns.set(player.getUniqueId(), this.name + ".scheduler", 5);
				if (distdiff <= 1.0D)
					Cooldowns.set(player.getUniqueId(), this.name + ".direction", 5);
				if (isDamagedByArrow)
				{
					Cooldowns.set(player.getUniqueId(), this.name + ".projectile", 5);
					Cooldowns.set(player.getUniqueId(), this.name + ".knockback", 5);
				}
				else if ((damager instanceof Player) && !player.equals(damager))
				{
					Player pdamager = (Player) damager;
					if (pdamager.getItemInHand() != null && pdamager.getItemInHand().containsEnchantment(Enchantment.KNOCKBACK) || pdamager.isFlying())
						Cooldowns.set(player.getUniqueId(), this.name + ".knockback", 5);
					if (isDamagedByEntity && damager.getLocation().getPitch() <= 0.0)
						Cooldowns.set(player.getUniqueId(), this.name + ".pitchup", 5);
				}
				this.velocity_datas.put(player.getUniqueId(), data);
			}
		}
	}

	public class VelocityData
	{
		public Location velocityTookPosition = null;
		public Location lastAttackerLocation = new Location(Bukkit.getWorld("world"), 0, 0, 0, 0, 0);
		public double cachedKnockbackY = 0.0D;

		public VelocityData()
		{
		}
	}
}
