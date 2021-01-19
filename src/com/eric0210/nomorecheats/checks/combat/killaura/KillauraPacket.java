package com.eric0210.nomorecheats.checks.combat.killaura;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.packet.PacketInEvent;
import com.eric0210.nomorecheats.api.packet.PacketListener;
import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.packet.PacketOutEvent;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.packets.in.FlyingPacket;

/*
 An kill-aura checks with direct packet check API
 SKIDDED some checks from Azure anticheat
*/
public class KillauraPacket extends Check implements PacketListener
{
	public HashMap<UUID, KillauraData> datas = new HashMap<>();

	public KillauraPacket()
	{
		super("KillAura");
		PacketManager.addListener(this);
	}

	@Override
	public void onPacketIn(PacketInEvent e)
	{
		if (e.getPacket().getType() == PacketTypeIn.FLYING)
		{
			Player p = e.getPlayer();
			KillauraData data = this.datas.getOrDefault(p.getUniqueId(), new KillauraData());
			FlyingPacket packet = (FlyingPacket) e.getPacket();
			float yaw = packet.getYaw();
			float pitch = packet.getPitch();
			if (data.prevYaw != -1f && data.prevPitch != -1f)
			{
				float yawChange = Math.abs(yaw - data.prevYaw);
				float pitchChange = Math.abs(pitch - data.prevPitch);
				if (data.prevYawChange != -1f && data.prevPitchChange != -1f)
				{
					float yawChangeDelta = Math.abs(yawChange - data.prevYawChange);
					float pitchChangeDelta = Math.abs(pitchChange - data.prevPitchChange);
					// Too many false positives
//					if (yawChange > yawChangeDelta && yawChangeDelta > .3 && pitchChange > 0 && pitchChangeDelta <= pitchChangeDelta && pitchChangeDelta < .1)
//						suspect(p, 5, "t: pitch");
//					if (yawChange > yawChangeDelta && yawChangeDelta > 0 && yawChangeDelta < .1 && pitchChange > .08)
//						suspect(p, 5, "t: GCD");
//					if (yawChange > yawChangeDelta && yawChangeDelta > 0.0 && pitchChange > 0 && pitchChange < 0.02 && pitchChangeDelta > pitchChange * 2)
//						suspect(p, 5, "t: random");
//					if (yawChangeDelta > 900 && pitchChange > 0 && pitchChangeDelta < 10)
//						suspect(p, 5, "t: toggle");
//					if (yawChangeDelta > 0 && Math.abs(Math.floor(yawChangeDelta) - yawChangeDelta) < 0.0000000001)
//						suspect(p, 5, "t: consistency");
				}
				data.prevYawChange = yawChange;
				data.prevPitchChange = pitchChange;
			}
			data.prevYaw = yaw;
			data.prevPitch = pitch;
			this.datas.put(p.getUniqueId(), data);
		}
	}

	@Override
	public void onPacketOut(PacketOutEvent e)
	{
	}

	public static class KillauraData
	{
		float prevYaw = -1f;
		float prevPitch = -1f;
		float prevYawChange = -1f;
		float prevPitchChange = -1f;
	}
}
