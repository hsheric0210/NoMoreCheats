package com.eric0210.nomorecheats.checks.block;

import org.bukkit.event.Event;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;


public class FastBreak extends Check implements EventListener
{
	//private HashMap<UUID, LocationTime> lastDamagedBlock = new HashMap<UUID,LocationTime>();
	//private HashMap<UUID, Boolean> isLastInstaBreak = new HashMap<UUID,Boolean>();
	public FastBreak()
	{
		super("FastBreak");
		EventManager.onBlockDamage.add(new EventInfo(this, 0));
		EventManager.onBlockBreak.add(new EventInfo(this, 1));
	}
	
	@Override
	public void onEvent(Event ev, int id)
	{
//		switch(id)
//		{
//		case 0:
//			BlockDamageEvent e = (BlockDamageEvent)ev;
//			if (e.getPlayer() != null && e.getBlock() != null)
//			{
//				Block y = e.getBlock();
//				lastDamagedBlock.put(e.getPlayer().getUniqueId(), new LocationTime(e.getBlock().getLocation(), System.currentTimeMillis()));
//			}
//			break;
//		case 1:
//			BlockBreakEvent e2 = (BlockBreakEvent)ev;
//			if (e2.getPlayer() != null && e2.getBlock() != null)
//			{
//				Player p = e2.getPlayer();
//				Block y = e2.getBlock();
//				if (lastDamagedBlock.containsKey(p.getUniqueId()))
//				{
//					LocationTime lt =  lastDamagedBlock.get(p.getUniqueId()));
//					Block last = lt.location.getBlock();
//					if (last != null && last.getType() != Material.AIR && !last.isLiquid())
//					{
//						if (last.equals(y))
//						{
//							long timeElapsed = System.currentTimeMillis() - lt.time;
//							long timeExpected = Math.max(0L, Math.round(BlockUtils.getBreakingDuration(last.getType()) * (double)fastBreakModSurvival / 100.0));
//						}
//					}
//				}
//			}
//			break;
//		}
	}

//	private class LocationTime
//	{
//	}
}
