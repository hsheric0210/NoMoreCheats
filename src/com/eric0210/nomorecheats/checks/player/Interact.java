package com.eric0210.nomorecheats.checks.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.eric0210.nomorecheats.AntiCheat;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.los.BlockPathFinder;

public class Interact extends Check implements EventListener, Runnable
{
	int PlaceLimit = 20;
	int InteractLimit = 50;
	double BlockReachLimit = 4.5;

	public Interact()
	{
		super("Interact");
		EventManager.onBlockPlace.add(new EventInfo(this, 0));
		EventManager.onPlayerInteract.add(new EventInfo(this, 1));
		EventManager.onBlockBreak.add(new EventInfo(this, 2));
		this.PlaceLimit = getConfig().getValue("Interact.PlacePerSecond", 20);
		this.InteractLimit = getConfig().getValue("Interact.InteractPerSecond", 50);
		this.BlockReachLimit = getConfig().getValue("Interact.BlockReachLimit", 4.5D);
		Bukkit.getScheduler().runTaskTimer(AntiCheat.antiCheat(), this, 20L, 20L);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				BlockPlaceEvent e1 = (BlockPlaceEvent) ev;
				Player player = e1.getPlayer();
				int pps = 0;
				if ((pps = Counter.increment1AndGetCount(e1.getPlayer().getUniqueId(), this.name + ".blockplaces", -1)) >= this.PlaceLimit)
				{
					suspect(player, 1, "a: block-place", "t: speed", "c: " + pps);
					e1.setCancelled(true);
				}
				if (!e1.isCancelled())
					checkPlace(e1);
				break;
			case 1:
				PlayerInteractEvent e2 = (PlayerInteractEvent) ev;
				if ((e2.getAction() == Action.LEFT_CLICK_BLOCK || e2.getAction() == Action.RIGHT_CLICK_BLOCK) && !e2.isCancelled())
				{
					Player player2 = e2.getPlayer();
					int ips = 0;
					if ((ips = Counter.increment1AndGetCount(e2.getPlayer().getUniqueId(), this.name + ".interactions", -1)) >= this.InteractLimit)
					{
						suspect(player2, 1, "a: interact", "t: speed", "c: " + ips);
						e2.setCancelled(true);
					}
					checkInteract(e2);
				}
				break;
			case 2:
				BlockBreakEvent e3 = (BlockBreakEvent) ev;
				if (!e3.isCancelled())
					checkBreak(e3);
				break;
		}
	}

	public void checkInteract(PlayerInteractEvent e)
	{
		if (!(e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK || e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST && e.getClickedBlock().getType() != Material.ENDER_CHEST))
		{
			Player p = e.getPlayer();
			boolean throughWall = !BlockPathFinder.line(p.getPlayer().getEyeLocation(), e.getClickedBlock().getLocation()).contains(e.getClickedBlock());
			double blockReach = e.getClickedBlock().getLocation().distance(p.getLocation());
			double BlockReachDistance = this.BlockReachLimit;
			if (e.getClickedBlock().getLocation().distance(p.getPlayer().getEyeLocation()) > 2.0 && !e.isCancelled())
			{
				if (throughWall && p.getLocation().getY() > 0 && p.getLocation().getY() < 255)
				{
					suspect(p, 1, "a: interact", "t: wall");
					e.setCancelled(true);
				}
				if (blockReach > BlockReachDistance)
				{
					suspect(p, 1, "a: interact", "t: reach", "d: " + String.valueOf(blockReach), "d_l: " + BlockReachDistance);
					e.setCancelled(true);
				}
			}
		}
	}

	public void checkPlace(BlockPlaceEvent e)
	{
		Player p = e.getPlayer();
		boolean throughWall = !BlockPathFinder.line(p.getPlayer().getEyeLocation(), e.getBlock().getLocation()).contains(e.getBlock());
		double blockReach = e.getBlock().getLocation().distance(e.getPlayer().getEyeLocation());
		double BlockReachDistance = this.BlockReachLimit;
		if (e.getBlock().getLocation().distance(p.getPlayer().getEyeLocation()) > 2.0 && !e.isCancelled())
		{
			if (throughWall && p.getLocation().getY() > 0 && p.getLocation().getY() < 255)
			{
				suspect(p, 1, "a: block-place", "t: wall");
				e.setCancelled(true);
			}
			if (blockReach > BlockReachDistance)
			{
				suspect(p, 1, "a: block-place", "t: reach", "d: " + blockReach, "d_l: " + BlockReachDistance);
				e.setCancelled(true);
			}
		}
		double ypos = e.getBlockPlaced().getY() - p.getLocation().getY();
		double bdist = p.getLocation().distance(e.getBlockPlaced().getLocation());
		double badist = p.getLocation().distance(e.getBlockAgainst().getLocation()) + .4;
		if ((bdist >= 1.3D) && (bdist > badist) && (ypos <= 0.5D))
		{
			suspect(p, 1, "a: block-place", "t: unusual", "b_d: " + bdist, "ab_d: " + badist, "y: " + ypos);
			e.setCancelled(true);
		}
		if (e.getBlockAgainst() == null || e.getBlockAgainst().getType() == Material.AIR || (e.getBlockAgainst().isLiquid() && e.getBlock().getType() != Material.WATER_LILY))
		{
			suspect(p, 1, "a: block-place", "t: illegal", "ab_t: " + e.getBlock().getType().name().toUpperCase());
			e.setCancelled(true);
		}
	}

	public void checkBreak(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		boolean throughWall = !BlockPathFinder.line(p.getPlayer().getEyeLocation(), e.getBlock().getLocation()).contains(e.getBlock());
		double blockReach = e.getBlock().getLocation().distance(e.getPlayer().getEyeLocation());
		double BlockReachDistance = this.BlockReachLimit;
		if (e.getBlock().getLocation().distance(p.getPlayer().getEyeLocation()) > 2.0 && !e.isCancelled())
		{
			if (throughWall && p.getLocation().getY() > 0 && p.getLocation().getY() < 256)
			{
				suspect(p, 1, "a: block-break", "t: raytrace");
				e.setCancelled(true);
			}
			if (blockReach > BlockReachDistance)
			{
				suspect(p, 1, "a: block-break", "t: reach", "d: " + blockReach, "d_l: " + this.BlockReachLimit);
				e.setCancelled(true);
			}
			if (e.getBlock().isLiquid())
			{
				suspect(p, 1, "a: block-break", "t: illegal", "b_t: " + e.getBlock().getType().name().toUpperCase());
				e.setCancelled(true);
			}
		}
	}

	@Override
	public void run()
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			Counter.remove(p.getUniqueId(), this.name + ".blockplaces");
			Counter.remove(p.getUniqueId(), this.name + ".interactions");
		}
	}
}
