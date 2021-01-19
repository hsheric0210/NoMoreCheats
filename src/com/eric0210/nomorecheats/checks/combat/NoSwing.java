package com.eric0210.nomorecheats.checks.combat;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.packet.PacketInEvent;
import com.eric0210.nomorecheats.api.packet.PacketListener;
import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.packet.PacketOutEvent;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.EntityUseAction;
import com.eric0210.nomorecheats.api.packet.packets.in.ArmAnimationPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockDigPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockDigPacket.DigAction;
import com.eric0210.nomorecheats.api.packet.packets.in.UseEntityPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.BlockUpdatePacket;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.NMS;

import org.bukkit.entity.Player;

public class NoSwing extends Check implements PacketListener
{
	private int swingValidTicks;

	public NoSwing()
	{
		super("NoSwing");
		PacketManager.addListener(this);
		this.swingValidTicks = getConfig().getValue("SwingValidTicks", 1);
	}

	@Override
	public void onPacketIn(PacketInEvent pe)
	{
		Player p = pe.getPlayer();
		
		if (pe.getPacket().getType() == PacketTypeIn.ARM_ANIMATION)
		{
			if (((ArmAnimationPacket) pe.getPacket()).getSwingType() != 1)
				return;
			Cooldowns.set(p.getUniqueId(), this.name + ".swing", this.swingValidTicks);
		}
		
		if (pe.getPacket().getType() == PacketTypeIn.USE_ENTITY)
		{
			UseEntityPacket packet = (UseEntityPacket) pe.getPacket();
			if (packet.getAction() != EntityUseAction.ATTACK || NMS.asNMS(p.getWorld()).getEntity(packet.getEntityId()) == null)
				return;
			if (Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".swing"))
			{
				suspect(p, 1, "a: attack", "e: " + packet.getEntityId());
				pe.setCancelled(true);
			}
		}
		
		if (pe.getPacket().getType() == PacketTypeIn.BLOCK_DIG)
		{
			BlockDigPacket packet = (BlockDigPacket) pe.getPacket();
			if (packet.getAction() == DigAction.FINISH_DIGGING)
			{
				if (Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".swing"))
				{
					suspect(p, 1, "a: block-dig", "b: " + packet.getPosition().toLocation(p.getWorld()).getBlock().getType().name());
					pe.setCancelled(true);
					PacketManager.sendPacket(p, new BlockUpdatePacket(NMS.asNMS(p), packet.getPosition().toLocation(p.getWorld())));
				}
			}
		}
	}

	@Override
	public void onPacketOut(PacketOutEvent e)
	{
	}
}
