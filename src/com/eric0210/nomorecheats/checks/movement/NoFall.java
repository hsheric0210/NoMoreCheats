package com.eric0210.nomorecheats.checks.movement;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.Violation;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.GroundChecks;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.PlayerUtils;

public class NoFall extends Check implements EventListener
{
	static ArrayList<Player> cancel = new ArrayList<>();

	public NoFall()
	{
		super("NoFall");
		EventManager.onPlayerQuit.add(new EventInfo(this, 0));
		EventManager.onPlayerMove.add(new EventInfo(this, 1));
		EventManager.onPlayerTeleport.add(new EventInfo(this, 2));
		EventManager.onPlayerDeath.add(new EventInfo(this, 3));
		EventManager.onPlayerJoin.add(new EventInfo(this, 4));
		EventManager.onPlayerTeleport.add(new EventInfo(this, 5));
		EventManager.onBlockPlace.add(new EventInfo(this, 6));
		EventManager.onPlayerRespawn.add(new EventInfo(this, 7));
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				PlayerQuitEvent e1 = (PlayerQuitEvent) ev;
				if (e1.getPlayer() != null)
				{
					if (cancel.contains(e1.getPlayer()))
						cancel.remove(e1.getPlayer());
				}
				break;
			case 1:
				PlayerMoveEvent e = (PlayerMoveEvent) ev;
				if (e.getPlayer() != null)
				{
					Player p = e.getPlayer();
					Location to = e.getTo();
					Location from = e.getFrom();
					// double yDiffAbs = Math.abs(to.getY() - from.getY());
					if (PlayerUtils.wasFlightAllowed(p) || PlayerUtils.wasFlying(p) || p.getVehicle() != null || cancel.remove(p) || BlockUtils.isClimbableNearby(p.getLocation()))
					{
						return;
					}
					Damageable damageable = e.getPlayer();
					if (damageable.getHealth() <= 0.0D)
					{
						return;
					}
					if (to.getY() > from.getY())
						return;
					if (!Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".block-place"))
					{
						Cooldowns.reset(p.getUniqueId(), this.name + ".block-place");
						return;
					}
					float realFallDistance = Cache.get(p.getUniqueId(), "fallDistance", 0.0F);
					ArrayList<String> violations = new ArrayList<>();
					int vl = 0;
					boolean dmgByPacketFalldistance = false;
					if (!BlockUtils.isLiquidNearby(p.getLocation()) && !BlockUtils.isClimbableNearby(p.getLocation()) && !BlockUtils.isMaterialSurround(p.getLocation(), 0.3, false, Material.WEB))
					{
						if (Counter.getCount(p.getUniqueId(), "airTicks") > 10 && Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".whitelist"))
						{
							if (realFallDistance == 0 || BlockUtils.hasSteppableNearby(p.getLocation()))
								return;
							if (((CraftPlayer) p).getHandle().onGround && !GroundChecks.isOnGround(p))
							{
								violations.add("ground-spoof");
								vl += 5;
							}
						}
						
						if (!p.isOnGround() && Counter.getCount(p.getUniqueId(), "groundTicks") > 10 && GroundChecks.isOnGround(p.getLocation()))
						{
							violations.add("air-spoof");
							vl += 5;
							dmgByPacketFalldistance = true;
						}
						if (p.getFallDistance() >= 3.0 && GroundChecks.isOnGround(p.getLocation()))
						{
							violations.add("ground-falldistance-spoof");
							vl += 2;
							dmgByPacketFalldistance = true;
						}
					}
					if (!violations.isEmpty())
					{
						suspect(p, vl, Violation.convertTagListtoTag(violations));
						if (dmgByPacketFalldistance)
							takeFallDamage(p, p.getFallDistance());
						else
							takeFallDamage(p, realFallDistance);
					}
				}
				break;
			case 2:
				PlayerTeleportEvent e2 = (PlayerTeleportEvent) ev;
				if (e2.getPlayer() != null)
					cancel.add(e2.getPlayer());
				break;
			case 3:
				PlayerDeathEvent e3 = (PlayerDeathEvent) ev;
				if (e3.getEntity() != null)
					cancel.add(e3.getEntity());
				break;
			case 4:
				PlayerJoinEvent e4 = (PlayerJoinEvent) ev;
				Cooldowns.set(e4.getPlayer().getUniqueId(), this.name + ".whitelist", 20);
				break;
			case 5:
				PlayerTeleportEvent e5 = (PlayerTeleportEvent) ev;
				Cooldowns.set(e5.getPlayer().getUniqueId(), this.name + ".whitelist", 10);
				break;
			case 6:
				BlockPlaceEvent e6 = (BlockPlaceEvent) ev;
				Cooldowns.set(e6.getPlayer().getUniqueId(), this.name + ".block-place", 10);
				break;
			case 7:
				PlayerRespawnEvent e7 = (PlayerRespawnEvent) ev;
				Cooldowns.set(e7.getPlayer().getUniqueId(), this.name + ".whitelist", 10);
				break;
		}
	}

	public final void takeFallDamage(Player p, float fallDistance)
	{
		p.damage(Math.max(1, getExpectedDamage(p, fallDistance)));
	}

	/**
	 * @param  profile
	 *                      The profile of the player.
	 * @param  fallDistance
	 *                      The distance the player has fallen.
	 * @return              How much their armour EPF reduces their fall damage.
	 */
	private double getExpectedDamage(Player p, double fallDistance)
	{
		/*
		 * Some formulae used from here: http://www.minecraftforum.net/forums/minecraft-discussion/survival- mode/2577601-facts-fall-damage Thank you to beaudigi for making this!
		 */
		/*
		 * Always round up to avoid false positives. Take the smallest out of the calculated and the player's maximum health.
		 */
		double damage = Math.min(Math.ceil(Math.max(Cache.get(p.getUniqueId(), "lastYPos", 0.0D) - p.getLocation().getY(), fallDistance)) - 2, ((Damageable) p).getMaxHealth());
		// Check to see if the player has feather falling.
		double epf = 0.0;
		// The amount of damage reduction enchantments give.
		double enchantmentReduction = 0.0;
		final ItemStack boots = p.getInventory().getBoots();
		if (boots != null)
		{
			if (boots.containsEnchantment(Enchantment.PROTECTION_FALL))
			{
				final int level = boots.getEnchantmentLevel(Enchantment.PROTECTION_FALL);
				// 2.5 is the type modifier for feather falling.
				epf += getEPFExtra(level, 2.5);
			}
			if (boots.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL))
			{
				final int level = boots.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
				// 0.75 is the type modifier for protection.
				epf += getEPFExtra(level, 0.75);
			}
			// Cap the EPF at 20.
			enchantmentReduction = Math.min(epf, 20.0) * 4;
		}
		double potionReduction = 0.0;
		for (PotionEffect effect : p.getActivePotionEffects())
		{
			if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE))
			{
				potionReduction += damage - ((1 - effect.getAmplifier() * 0.2) * fallDistance - 3);
			}
			if (effect.getType().equals(PotionEffectType.JUMP))
			{
				potionReduction += damage - (fallDistance - 3 - effect.getAmplifier());
			}
		}
		// The maths just works, ok? :D
		return damage - enchantmentReduction - potionReduction - 1;
	}

	private double getEPFExtra(int level, double typeModifier)
	{
		return (6 + level * level) * typeModifier / 3.0;
	}
}
