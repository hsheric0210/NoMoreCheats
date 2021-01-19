package com.eric0210.nomorecheats.checks.combat;

import java.util.UUID;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.AverageCollector;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.TickTasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class AutoClicker extends Check implements EventListener
{
	private static int MaxCPS = 15;

	public AutoClicker()
	{
		super("AutoClicker");
		EventManager.onPlayerInteract.add(new EventInfo(this, 0));
		EventManager.onBlockBreak.add(new EventInfo(this, 1));
		MaxCPS = getConfig().getValue("MaxCPS", 17);
		TickTasks.addTask(() ->
		{
			for (Player p : Bukkit.getOnlinePlayers())
			{
				UUID uid = p.getUniqueId();
				ACData data = Cache.get(uid, this.name + ".data", new ACData());
				if (System.currentTimeMillis() >= data.timer)
				{
					int lcps = data.leftClick;
					int rcps = data.rightClick;

					data.leftClick_AverageCollector.add(lcps);
					data.rightClick_AverageCollector.add(rcps);

					data.leftClick = 0;
					data.rightClick = 0;

					Cache.set(uid, "left_cps", lcps);

					if (lcps > MaxCPS)
					{
						suspect(p, Math.min(Math.abs(lcps - MaxCPS) * 10, 50), "b: left", "t: normal", "c: " + lcps);
						Cooldowns.set(uid, this.name + ".cooldown.left", 60);
						data.leftClick_AverageCollector.reset();
					}

					Cache.set(uid, "right_cps", rcps);

					if (rcps > MaxCPS)
					{
						suspect(p, Math.min(Math.abs(rcps - MaxCPS) * 10, 50), "b: right", "t: normal", "c: " + rcps);
						Cooldowns.set(uid, this.name + ".cooldown.right", 60);
						data.rightClick_AverageCollector.reset();
					}

					if (data.leftClick_AverageCollector.getCount() >= 5)
					{
						double delta;
						if (data.leftClick_AverageCollector.getAverage() > 9 && (delta = data.leftClick_AverageCollector.getMax() - data.leftClick_AverageCollector.getMin()) <= 2)
							suspect(p, Math.min(delta * 25, 50), "b: left", "t: consistency", "c: " + data.leftClick_AverageCollector.getAverage(), "d: " + delta);
						data.leftClick_AverageCollector.reset();
					}

					if (data.rightClick_AverageCollector.getCount() >= 5)
					{
						double delta;
						if (data.rightClick_AverageCollector.getAverage() > 9 && (delta = data.rightClick_AverageCollector.getMax() - data.rightClick_AverageCollector.getMin()) <= 2)
							suspect(p, Math.min(delta * 25, 50), "b: right", "t: consistency", "c: " + data.rightClick_AverageCollector.getAverage(), "d: " + delta);
						data.rightClick_AverageCollector.reset();
					}

					data.timer = System.currentTimeMillis() + 1000L;
				}
				Cache.set(uid, this.name + ".data", data);
			}
		});
	}

	private class ACData
	{
		public AverageCollector leftClick_AverageCollector;
		public AverageCollector rightClick_AverageCollector;
		public int leftClick;
		public int rightClick;
		public long timer;

		public ACData()
		{
			this.leftClick_AverageCollector = new AverageCollector();
			this.rightClick_AverageCollector = new AverageCollector();
			this.leftClick = 0;
			this.rightClick = 0;
			this.timer = System.currentTimeMillis() + 1000L;
		}
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				PlayerInteractEvent e = (PlayerInteractEvent) ev;
				UUID uid = e.getPlayer().getUniqueId();
				ACData data = Cache.get(uid, this.name + ".data", new ACData());
				switch (e.getAction())
				{
					case LEFT_CLICK_AIR:
					case LEFT_CLICK_BLOCK:
						if (!Cooldowns.isCooldownEnded(uid, this.name + ".cooldown.left"))
							e.setCancelled(true);

						++data.leftClick;
						if (!Cooldowns.isCooldownEnded(uid, this.name + ".blockbreak"))
						{
							int count = Cache.get(uid, this.name + ".unusualClicks", 0);
							if (count++ > 5 && Counter.increment1AndGetCount(uid, this.name + ".blockbreak", 200) >= 2)
								suspect(e.getPlayer(), count / 2, "b: left", "t: unusual", "c: " + count);
							Cache.set(uid, this.name + ".unusualClicks", count);
						}
						break;
					case RIGHT_CLICK_AIR:
					case RIGHT_CLICK_BLOCK:
						if (!Cooldowns.isCooldownEnded(uid, this.name + ".cooldown.right"))
							e.setCancelled(true);

						++data.rightClick;
						break;
					default:
						break;
				}
				Cache.set(uid, this.name + ".data", data);
				break;
			case 1:
				BlockBreakEvent e2 = (BlockBreakEvent) ev;
				if (!e2.isCancelled())
				{
					Cooldowns.set(e2.getPlayer().getUniqueId(), this.name + ".blockbreak", 10);
					Cache.remove(e2.getPlayer().getUniqueId(), this.name + ".unusualClicks");
				}
				break;
		}
	}
}
