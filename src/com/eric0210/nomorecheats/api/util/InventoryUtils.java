package com.eric0210.nomorecheats.api.util;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils
{
	public static final String getGUIActionViolation(Player p)
	{
		if (p.isSprinting())
		{
			return "sprinting";
		}
		else if (p.isSneaking())
		{
			return "sneaking";
		}
		else if (p.isBlocking())
		{
			return "blocking";
		}
		else if (p.isSleeping())
		{
			return "sleeping";
		}
		else if (p.isConversing())
		{
			return "conversing";
		}
		else if (p.isDead())
		{
			return "dead";
		}
		else if (p.getLocation().getBlock().getType() == Material.PORTAL || p.getLocation().add(0, 1, 0).getBlock().getType() == Material.PORTAL)
		{
			return "portal";
		}
		else if (!Cooldowns.isCooldownEnded(p.getUniqueId(), "inventory.jump"))
		{
			return "jumping";
		}
		else if (Cache.get(p.getUniqueId(), "_moveSpeed", 0D) > 0.085 && Cache.get(p.getUniqueId(), "_moveSpeed", 0D) >= Cache.get(p.getUniqueId(), "_oldMoveSpeed", 1D))
		{
			if (Counter.increment1AndGetCount(p.getUniqueId(), "Inventory.walking", 99) > 5 && !Cooldowns.isCooldownEnded(p.getUniqueId(), "move"))
			{
				return "walking";
			}
		}
		return null;
	}

	public static final void handleMove(PlayerMoveEvent e)
	{
		Player p = e.getPlayer();
		Location from = e.getFrom();
		Location to = e.getTo();
		if (Cache.contains(p.getUniqueId(), "_moveSpeed"))
			Cache.set(p.getUniqueId(), "_oldMoveSpeed", Cache.get(p.getUniqueId(), "_moveSpeed", 0D));
		Cache.set(p.getUniqueId(), "_moveSpeed", MathUtils.getHorizontalDistance(from, to));
		if ((to.getY() - from.getY()) >= .4)
		{
			Cooldowns.set(p.getUniqueId(), "inventory.jump", 2);
		}
		Cooldowns.set(p.getUniqueId(), "move", 2);
	}

	public static final int randomFirstEmpty(Inventory inv, int slotstart, int slotend)
	{
		ItemStack[] inventory = inv.getContents().clone();
		ArrayList<Integer> slots = new ArrayList<>();
		for (int i = 0; i < inventory.length; ++i)
		{
			if (inventory[i] != null || i < slotstart || i > slotend)
				continue;
			slots.add(i);
		}
		if (slots.isEmpty())
			return -1;
		Collections.shuffle(slots);
		return slots.get(0);
	}
}
