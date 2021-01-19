package com.eric0210.nomorecheats.checks.player;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.PlayerUtils;

public class FastUse extends Check implements EventListener
{
	public int requiredItemUseTicks = 20;
	public HashSet<Material> blacklisted_blocks = new HashSet<>();

	public FastUse()
	{
		super("FastUse");
		EventManager.onPlayerInteract.add(new EventInfo(this, 0));
		EventManager.onPlayerItemConsume.add(new EventInfo(this, 1));
		this.requiredItemUseTicks = getConfig().getValue("UseTicksThreshold", 20);
		this.blacklisted_blocks.add(Material.SIGN);
		this.blacklisted_blocks.add(Material.SIGN_POST);
		this.blacklisted_blocks.add(Material.WALL_SIGN);
		this.blacklisted_blocks.add(Material.FENCE);
		this.blacklisted_blocks.add(Material.FENCE_GATE);
		this.blacklisted_blocks.add(Material.NETHER_FENCE);
		this.blacklisted_blocks.add(Material.CHEST);
		this.blacklisted_blocks.add(Material.TRAPPED_CHEST);
		this.blacklisted_blocks.add(Material.ANVIL);
		this.blacklisted_blocks.add(Material.CAULDRON);
		this.blacklisted_blocks.add(Material.FURNACE);
		this.blacklisted_blocks.add(Material.BURNING_FURNACE);
		this.blacklisted_blocks.add(Material.ENCHANTMENT_TABLE);
		this.blacklisted_blocks.add(Material.WORKBENCH);
		this.blacklisted_blocks.add(Material.JUKEBOX);
		this.blacklisted_blocks.add(Material.NOTE_BLOCK);
		this.blacklisted_blocks.add(Material.ENDER_CHEST);
		this.blacklisted_blocks.add(Material.ENCHANTMENT_TABLE);
		this.blacklisted_blocks.add(Material.ENCHANTMENT_TABLE);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				PlayerInteractEvent e1 = (PlayerInteractEvent) ev;
				if ((e1.getAction() == Action.RIGHT_CLICK_AIR || e1.getAction() == Action.RIGHT_CLICK_BLOCK) && (e1.getClickedBlock() == null || (e1.getClickedBlock() != null && !this.blacklisted_blocks.contains(e1.getClickedBlock().getType()))))
				{
					if (e1.getItem() != null && e1.getItem().getType().isEdible())
					{
						Cooldowns.set(e1.getPlayer().getUniqueId(), this.name + ".last-use", this.requiredItemUseTicks);
					}
				}
				break;
			case 1:
				PlayerItemConsumeEvent e2 = (PlayerItemConsumeEvent) ev;
				if (!Cooldowns.isCooldownEnded(e2.getPlayer().getUniqueId(), this.name + ".last-use"))
				{
					suspect(e2.getPlayer(), 1, "d: " + (this.requiredItemUseTicks - Cooldowns.get(e2.getPlayer().getUniqueId(), this.name + ".last-use")));
					e2.setCancelled(true);
					PlayerUtils.heldNextSlot(e2.getPlayer());
				}
				break;
			case 2:
				PlayerQuitEvent quit = (PlayerQuitEvent) ev;
				Cooldowns.reset(quit.getPlayer().getUniqueId(), this.name + ".last-use");
				break;
		}
	}
}
