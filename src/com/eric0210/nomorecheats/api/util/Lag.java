package com.eric0210.nomorecheats.api.util;

import org.bukkit.entity.Player;

import com.eric0210.nomorecheats.checks.player.FalsePackets;

import net.minecraft.server.v1_7_R4.MinecraftServer;

public class Lag implements Runnable
{
	public static final void initialize()
	{
		TickTasks.addTask(() -> {
			TICKS[TICK_COUNT % TICKS.length] = System.currentTimeMillis();
			++TICK_COUNT;
		});
	}

	public static int TICK_COUNT = 0;
	public static long[] TICKS = new long[600];
	public static long LAST_TICK = 0L;

	public static double getTPS()
	{
		return getTPS(100);
	}

	public static double getTPS(int ticks)
	{
		if (TICK_COUNT < ticks)
		{
			return 20.0;
		}
		try
		{
			int target = (TICK_COUNT - 1 - ticks) % TICKS.length;
			long elapsed = System.currentTimeMillis() - TICKS[target];
			return ticks / (elapsed / 1000.0);
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			return MinecraftServer.getServer().recentTps[0];
		}
	}

	public static long getElapsed(int tickID)
	{
		long time = TICKS[tickID % TICKS.length];
		return System.currentTimeMillis() - time;
	}

	@Override
	public void run()
	{
	}

	private static double getLagModifier(Player p)
	{
		boolean pingspoofDetected = Counter.getCount(p.getUniqueId(), Checks.falsePackets.pingspoof_keepalive_detections) > 0;
		int latency_affect = pingspoofDetected ? 300 : PlayerUtils.getPing(p);
		boolean j = ((!pingspoofDetected) && (latency_affect <= 175)) || (latency_affect > 1000)
				|| (FalsePackets.checkPingspoofPacketsViolation(p.getUniqueId()));
		latency_affect = j ? 0 : latency_affect / 50;
		double tps_affect = getTPS();
		tps_affect = tps_affect >= 18.0D ? 0.0D : 20.0D - tps_affect;
		return tps_affect > latency_affect ? tps_affect : latency_affect;
	}

	public static double correction(Player p, double value)
	{
		double effect = getLagModifier(p);
		return effect == 0.0D ? value : effect / 2.0D * value;
	}

	public static int correction(Player p, int value)
	{
		double effect = getLagModifier(p);
		return effect == 0.0D ? value : (int) (Math.floor(effect) / 2 * value);
	}
}
