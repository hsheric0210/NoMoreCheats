package com.eric0210.nomorecheats.api.util;

import java.util.UUID;

import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Protections implements EventListener
{
	private enum KnockbackCause
	{
		ENTITY_ATTACK,
		EXPLOSION,
		PROJECTILE,
		OTHER_DAMAGE;
		private static KnockbackCause getCauseByEvent(EntityDamageEvent e)
		{
			DamageCause dc = e.getCause();
			switch (dc)
			{
				case ENTITY_ATTACK:
					return ENTITY_ATTACK;
				case BLOCK_EXPLOSION:
				case ENTITY_EXPLOSION:
					return EXPLOSION;
				case PROJECTILE:
					EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent) e;
					Projectile proj = (Projectile) edee.getDamager();
					if (proj.getShooter() == null || proj.getShooter().getUniqueId() != e.getEntity().getUniqueId())
						return PROJECTILE;
					break;
				case POISON:
				case WITHER:
				case VOID:
				case DROWNING:
				case FALLING_BLOCK:
				case MELTING:
				case STARVATION:
				case SUFFOCATION:
				case LAVA:
				case FIRE:
				case FIRE_TICK:
				case CONTACT:
				case CUSTOM:
				case FALL:
				case LIGHTNING:
				case MAGIC:
				case SUICIDE:
				case THORNS:
					return OTHER_DAMAGE;
			}
			return OTHER_DAMAGE;
		}
	}

	public enum ProtectionType
	{
		SPEED("speed"),
		UNNORMAL_Y("y_irregular"),
		ASCENSION("y_up"),
		FISHING_HOOK_CAUGHT("hookcaught"),
		TELEPORT("tp"),
		MORE_PACKETS("packets"),
		BOW("bow"),
		DAMAGE("damage"),
		LIQUID("liquids"),
		BLINK("no_packets");
		private String cooldown_id;

		private ProtectionType(String cooldown_identifier)
		{
			this.cooldown_id = cooldown_identifier;
		}
	}

	public Protections()
	{
		EventManager.onEntityDamage.add(new EventInfo(this, 0));
		EventManager.onPlayerFish.add(new EventInfo(this, 1));
		EventManager.onPlayerMove.add(new EventInfo(this, 2));
		EventManager.onPlayerTeleport.add(new EventInfo(this, 3));
		EventManager.onEntityShootBow.add(new EventInfo(this, 4));
		EventManager.onPlayerRespawn.add(new EventInfo(this, 5));
		EventManager.onPlayerJoin.add(new EventInfo(this, 6));
		EventManager.onPlayerChangeWorld.add(new EventInfo(this, 7));
		for (Player p : Bukkit.getOnlinePlayers())
			putProtection(p, ProtectionType.MORE_PACKETS, 60);
	}

	public static final boolean hasProtection(Player p, ProtectionType type)
	{
		return !Cooldowns.isCooldownEnded(p.getUniqueId(), "Protections." + type.cooldown_id);
	}

	public static final void putProtection(Player p, ProtectionType type, int cooldown)
	{
		Cooldowns.set(p.getUniqueId(), "Protections." + type.cooldown_id, cooldown);
	}

	private static final void putSelfhit(Player p, UUID uuidProjectile)
	{
		Cooldowns.set(p.getUniqueId(), "Protections.selfhit" + (uuidProjectile != null ? ("." + uuidProjectile.toString()) : ("")), 200);
	}

	private static final void putBowShoot(UUID uid, UUID proj)
	{
		Cooldowns.set(uid, "Protections.bow" + (proj != null ? "." + proj.toString() : ""), 200);
	}

	private static boolean hasBowShoot(UUID uid, Arrow proj)
	{
		return !Cooldowns.isCooldownEnded(uid, "Protections.bow." + proj.getUniqueId().toString());
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				EntityDamageEvent e = (EntityDamageEvent) ev;
				if (e.getEntity() instanceof Player)
				{
					Player p = (Player) e.getEntity();
					KnockbackCause kc = KnockbackCause.getCauseByEvent(e);
					if (kc != null)
					{
						switch (kc)
						{
							case ENTITY_ATTACK:
							case PROJECTILE:
								putProtection(p, ProtectionType.UNNORMAL_Y, 10);
								putProtection(p, ProtectionType.ASCENSION, 10);
								putProtection(p, ProtectionType.SPEED, 10);
							case EXPLOSION:
								putProtection(p, ProtectionType.UNNORMAL_Y, 15);
								putProtection(p, ProtectionType.ASCENSION, 15);
								putProtection(p, ProtectionType.SPEED, 15);
								break;
							case OTHER_DAMAGE:
								putProtection(p, ProtectionType.SPEED, 5);
								putProtection(p, ProtectionType.ASCENSION, 5);
								putProtection(p, ProtectionType.UNNORMAL_Y, 5);
								break;
						}
					}
				}
				if (e instanceof EntityDamageByEntityEvent)
				{
					EntityDamageByEntityEvent _e = (EntityDamageByEntityEvent) ev;
					Entity damager = _e.getDamager();
					if (e.getEntity() instanceof Player)
					{
						Player p = (Player) e.getEntity();
						if (_e.getCause() == DamageCause.PROJECTILE && damager instanceof Arrow)
						{
							Arrow arrow = (Arrow) damager;
							if (arrow.getShooter() == null || isSelfHit_Bow(p, (Arrow) damager))
							{
								putSelfhit(p, null);
							}
							if (arrow.getShooter() != null && hasBowShoot(arrow.getShooter().getUniqueId(), arrow))
							{
								putProtection(p, ProtectionType.BOW, 100);
							}
						}
					}
				}
				if (e.getEntity() instanceof Player)
				{
					putProtection((Player) e.getEntity(), ProtectionType.DAMAGE, 60); // NEED TO FIX //
//				if (e.getCause() == DamageCause.ENTITY_EXPLOSION || e.getCause() == DamageCause.BLOCK_EXPLOSION)
//					putProtection((Player) e.getEntity(), ProtectionType.EXPLOSION, 80);
				}
				break;
			case 1:
				PlayerFishEvent e2 = (PlayerFishEvent) ev;
				if (e2.getState() == State.CAUGHT_ENTITY)
				{
					Entity caught = e2.getCaught();
					if (caught instanceof Player)
					{
						Player cp = (Player) caught;
						putProtection(cp, ProtectionType.FISHING_HOOK_CAUGHT, 40);
					}
				}
				break;
			case 2:
				PlayerMoveEvent move = (PlayerMoveEvent) ev;
				Location from = move.getFrom();
				Location to = move.getTo();
				if (BlockUtils.isLiquidNearby(from) || BlockUtils.isLiquidNearby(to))
					putProtection(move.getPlayer(), ProtectionType.LIQUID, 5);
				break;
			case 3:
				PlayerTeleportEvent e3 = (PlayerTeleportEvent) ev;
				putProtection(e3.getPlayer(), ProtectionType.TELEPORT, 10);
				if (e3.getCause() == TeleportCause.ENDER_PEARL && !PlayerUtils.isInClosedChamber(e3.getPlayer().getLocation()))
					putProtection(e3.getPlayer(), ProtectionType.TELEPORT, 20);
				// Cooldowns.put(e3.getPlayer().getUniqueId(), "bpprotection.teleport", 8);
				break;
			case 4:
				EntityShootBowEvent e4 = (EntityShootBowEvent) ev;
				if (e4.getEntity() instanceof Player)
				{
					Player p = (Player) e4.getEntity();
					if (p.getLocation().getPitch() <= -75F)
					{
						putSelfhit(p, e4.getProjectile().getUniqueId());
						// Cooldowns.put(p.getUniqueId(),
						// "protection.selfhit." + e4.getProjectile().getUniqueId().toString(), 200);
					}
					if (e4.getBow().containsEnchantment(Enchantment.ARROW_KNOCKBACK) || e4.getBow().containsEnchantment(Enchantment.KNOCKBACK))
					{
						putBowShoot(p.getUniqueId(), e4.getProjectile().getUniqueId());
					}
				}
				break;
			case 5:
				PlayerRespawnEvent e5 = (PlayerRespawnEvent) ev;
				putProtection(e5.getPlayer(), ProtectionType.MORE_PACKETS, 20);
				break;
			case 6:
				PlayerJoinEvent e6 = (PlayerJoinEvent) ev;
				putProtection(e6.getPlayer(), ProtectionType.MORE_PACKETS, 50);
				break;
			case 7:
				PlayerChangedWorldEvent e7 = (PlayerChangedWorldEvent) ev;
				putProtection(e7.getPlayer(), ProtectionType.MORE_PACKETS, 50);
				Reverter.resetAllPositions();
				break;
		}
	}

	private boolean isSelfHit_Bow(Player p, Arrow arrow)
	{
		return (!GroundChecks.isOnGroundCache(p, 0, 0, 0) && hasProtection(p, ProtectionType.BOW) && hasProtection(p, ProtectionType.DAMAGE) && Cache.get(p.getUniqueId(), "moveSpeed", 0D) >= .18 && Cooldowns.isCooldownEnded(p.getUniqueId(), "Protection.selfhit." + arrow.getUniqueId().toString()) && !BlockUtils.isLiquidNearby(p.getLocation()) && !BlockUtils.isLiquidNearby(p.getLocation().add(0, 1, 0)));
	}
}
