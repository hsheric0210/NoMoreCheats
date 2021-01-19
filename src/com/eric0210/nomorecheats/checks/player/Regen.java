package com.eric0210.nomorecheats.checks.player;

import java.util.HashMap;
import org.bukkit.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffectType;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.PlayerUtils;
import com.eric0210.nomorecheats.api.util.TimeUtils;

public class Regen extends Check implements EventListener
{
	private HashMap<Integer, Long> healthRegainSpeeds = new HashMap<>();

	public Regen()
	{
		super("Regen");
		EventManager.onEntityRegainHealth.add(new EventInfo(this, 0));
		this.healthRegainSpeeds.put(0, 2500L);
		this.healthRegainSpeeds.put(1, 1250L);
		this.healthRegainSpeeds.put(2, 600L);
		this.healthRegainSpeeds.put(3, 300L);
		this.healthRegainSpeeds.put(4, 150L);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		EntityRegainHealthEvent e = (EntityRegainHealthEvent) ev;
		if (e.getEntity() instanceof Player)
		{
			Player p = (Player) e.getEntity();
			if (e.getRegainReason() != RegainReason.CUSTOM && e.getRegainReason() != RegainReason.MAGIC)
			{
				int regenEffectAmp = PlayerUtils.getPotionEffectLevel(p, PotionEffectType.REGENERATION);
				long deltaTime = TimeUtils.getTimeDiff(p.getUniqueId(), this.name + ".lastRegainHealth", 3950L);
				long deltaTimeRequired = p.getWorld().getDifficulty() == Difficulty.PEACEFUL ? 950L
						: (p.hasPotionEffect(PotionEffectType.REGENERATION)
								? this.healthRegainSpeeds.getOrDefault(regenEffectAmp - 1, 50L)
								: 3950L);
				if (deltaTime < deltaTimeRequired && deltaTimeRequired - deltaTime > 50L)
				{
					suspect(p, 1, "r: " + deltaTime, "r_n: " + deltaTimeRequired,
							"r: (" + e.getRegainReason().name().toUpperCase() + ")");
					e.setCancelled(true);
				}
				TimeUtils.putCurrentTime(p.getUniqueId(), this.name + ".lastRegainHealth");
			}
		}
	}
}
