package com.eric0210.nomorecheats.checks.combat.killaura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.packet.PacketInEvent;
import com.eric0210.nomorecheats.api.packet.PacketListener;
import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.packet.PacketOutEvent;
import com.eric0210.nomorecheats.api.packet.PacketTypeIn;
import com.eric0210.nomorecheats.api.packet.enums.EntityUseAction;
import com.eric0210.nomorecheats.api.packet.packets.in.UseEntityPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.AnimationPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityDestroyPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityEquipmentPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityHeadRotationPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityMetadataPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityRelativeMovePacket;
import com.eric0210.nomorecheats.api.packet.packets.out.EntityTeleportPacket;
import com.eric0210.nomorecheats.api.packet.packets.out.SpawnEntityNamedPacket;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.TimeUtils;
import com.eric0210.nomorecheats.api.util.InternalUtils;
import com.eric0210.nomorecheats.api.util.NMS;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.util.com.mojang.authlib.GameProfile;

/*
 * Killaura-Entity check
 */
public class KillauraNPC extends Check implements EventListener, PacketListener
{
	public HashMap<UUID, NPC> npcs = new HashMap<>();
	public static int Killaura_NPC_EntityID = 9999999;
	public static boolean enabled = true;
	private static long NPCActiveTimeWhenCombat = 3000;
	public long SpawnDelay;
	public long DestroyDelay;
	public long UpdateDelayOrigin;
	public long UpdateDelayBound;
	public long ItemChangeDelayOrigin;
	public long ItemChangeDelayBound;
	public long SwingDelayOrigin;
	public long SwingDelayBound;
	public long ActionDelayOrigin;
	public long ActionDelayBound;
	public boolean equipArmors = true;
	public boolean heldItem = true;
	public boolean doSwing = true;
	public double npc_increment_y_nonactive;
	public double npc_increment_back_nonactive;
	public double npc_y;
	public double npc_back;

	class NPC
	{
		EntityPlayer npc;
		Player npcPlayer;

		public NPC(EntityPlayer npc, Player npcPlayer)
		{
			this.npc = npc;
			this.npcPlayer = npcPlayer;
		}
	}

	// KillAura BOT
	public KillauraNPC()
	{
		super("KillAura");
		PacketManager.addListener(this);
//		enabled = true;
		Killaura_NPC_EntityID = getConfig().getValue("NPC.EntityID", 9999999);
		this.npc_y = getConfig().getValue("NPC.Y", 2.12D);
		this.npc_back = getConfig().getValue("NPC.Back", 2.12D);
		this.npc_increment_y_nonactive = getConfig().getValue("NPC.YIncNoncombat", 3.88D);
		this.npc_increment_back_nonactive = getConfig().getValue("NPC.BackIncNoncombat", 3.88D);
		this.SpawnDelay = getConfig().getValue("NPC.Delay.Spawn", 100); // Spawn After Destroy
		this.DestroyDelay = getConfig().getValue("NPC.Delay.Destroy", 20000); // Destroy After Spawn
		this.UpdateDelayOrigin = getConfig().getValue("NPC.Delay.Position.Min", 50);
		this.UpdateDelayBound = getConfig().getValue("NPC.Delay.Position.Max", 100);
		this.SwingDelayOrigin = getConfig().getValue("NPC.Delay.Swing.Min", 100);
		this.SwingDelayBound = getConfig().getValue("NPC.Delay.Swing.Max", 250);
		this.ItemChangeDelayOrigin = getConfig().getValue("NPC.Delay.ItemChange.Min", 500);
		this.ItemChangeDelayBound = getConfig().getValue("NPC.Delay.ItemChange.Max", 1000);
		this.ActionDelayOrigin = getConfig().getValue("NPC.Delay.Action.Min", 50);
		this.ActionDelayBound = getConfig().getValue("NPC.Delay.Action.Max", 150);
		UpdateWorker thread = new UpdateWorker();
		thread.setName("Anti-Killaura NPC Updater");
		thread.start();
	}

	public static final void disable()
	{
		enabled = false;
	}

	private Player getNPCPlayer(Player npcTarget)
	{
		Player[] offlinePlayers = Bukkit.getOnlinePlayers();
		ArrayList<Player> chooseable = new ArrayList<>();
		for (Player current : offlinePlayers)
		{
			if (current.getName() != null && current.getUniqueId() != null && !current.getName().equalsIgnoreCase(npcTarget.getName()) && npcTarget.canSee(current) && !current.hasPotionEffect(PotionEffectType.INVISIBILITY))
				chooseable.add(current);
		}
		if (chooseable.size() < 1)
			return null;
		return chooseable.get(chooseable.size() == 1 ? 0 : InternalUtils.random(0, chooseable.size() - 1));
	}

	public class UpdateWorker extends Thread
	{
		@Override
		public void run()
		{
			while (enabled)
			{
				if (!enabled)
					break;
				for (Player p : Bukkit.getOnlinePlayers())
				{
					if (p.isDead())
						continue;
					if (Bukkit.getOnlinePlayers().length <= 1)
					{
						if (Cache.get(p.getUniqueId(), KillauraNPC.this.name + ".haveNPC", true))
							destroyNPC(p);
					}
					else
					{
						boolean active = false;
						Cache.set(p.getUniqueId(), KillauraNPC.this.name + ".npc.active", (active = !TimeUtils.elapsed(p.getUniqueId(), KillauraNPC.this.name + ".lastCombat", true, NPCActiveTimeWhenCombat)));
						if (!Cache.get(p.getUniqueId(), KillauraNPC.this.name + ".haveNPC", false))
						{
							// Spawn NPC //
							if (TimeUtils.elapsed(p.getUniqueId(), KillauraNPC.this.name + ".npc.spawn", true, KillauraNPC.this.SpawnDelay))
							{
								spawnNPC(p, active);
								if (!TimeUtils.contains(p.getUniqueId(), KillauraNPC.this.name + ".npc.spawn"))
									TimeUtils.putCurrentTime(p.getUniqueId(), KillauraNPC.this.name + ".npc.spawn");
								TimeUtils.putCurrentTime(p.getUniqueId(), KillauraNPC.this.name + ".npc.destroy");
							}
						}
						else
						{
							if (TimeUtils.elapsed(p.getUniqueId(), KillauraNPC.this.name + ".npc.destroy", false, KillauraNPC.this.DestroyDelay))
							{
								destroyNPC(p);
								TimeUtils.putCurrentTime(p.getUniqueId(), KillauraNPC.this.name + ".npc.spawn");
							}
							if (TimeUtils.elapsed(p.getUniqueId(), KillauraNPC.this.name + ".npc.move", true, InternalUtils.random(KillauraNPC.this.UpdateDelayOrigin, KillauraNPC.this.UpdateDelayBound)))
							{
								updateNPC(p, active);
								TimeUtils.putCurrentTime(p.getUniqueId(), KillauraNPC.this.name + ".npc.move");
							}
						}
					}
				}
				try
				{
					sleep(50);
				}
				catch (InterruptedException ex)
				{
					new Exception("Failed to update the Killaura-Entity: Failed to sleep the thread", ex).printStackTrace();
				}
			}
		}

		public void spawnNPC(Player p, boolean active)
		{
			if (p.isDead())
				return;
			Player npcPlayer = getNPCPlayer(p);
			if (npcPlayer != null)
			{
				Cache.set(p.getUniqueId(), KillauraNPC.this.name + ".haveNPC", true);
				GameProfile npcProfile = new GameProfile(npcPlayer.getUniqueId(), npcPlayer.getName());
				EntityPlayer ep = new EntityPlayer(MinecraftServer.getServer(), NMS.asNMS(p.getWorld()), npcProfile, new PlayerInteractManager(NMS.asNMS(p.getWorld())));
				Location spawnPos = getNPCPosition(p, active);
				SpawnEntityNamedPacket npcSpawnPacket = new SpawnEntityNamedPacket(NMS.asNMS(p), ep);
				npcSpawnPacket.setEntity(Killaura_NPC_EntityID);
				npcSpawnPacket.setProfile(npcProfile);
				npcSpawnPacket.convertAndSetPosition(spawnPos.toVector());
				npcSpawnPacket.convertAndSetFacing(npcPlayer.getLocation().getYaw(), npcPlayer.getLocation().getPitch());
				PacketManager.sendPacket(p, npcSpawnPacket);
				KillauraNPC.this.npcs.put(p.getUniqueId(), new NPC(ep, npcPlayer));
				updateNPC(p, active);
			}
		}

		public void destroyNPC(Player p)
		{
			NPC npc = KillauraNPC.this.npcs.getOrDefault(p.getUniqueId(), null);
			if (npc != null)
				PacketManager.sendPacket(p, new EntityDestroyPacket(NMS.asNMS(p), new int[]
				{
						Killaura_NPC_EntityID
				}));
			KillauraNPC.this.npcs.remove(p.getUniqueId());
			Cache.set(p.getUniqueId(), KillauraNPC.this.name + ".haveNPC", false);
		}

		public void updateNPC(Player p, boolean active)
		{
			NPC npc = KillauraNPC.this.npcs.getOrDefault(p.getUniqueId(), null);
			if (npc != null)
			{
				// TODO NPC Random Stat
				npc.npc.setHealth(NMS.asNMS(npc.npcPlayer).getHealth());
				npc.npc.fireTicks = npc.npcPlayer.getFireTicks();
				npc.npc.setSprinting(npc.npcPlayer.isSprinting());
				npc.npc.setSneaking(npc.npcPlayer.isSneaking());
				npc.npc.onGround = npc.npcPlayer.isOnGround();
				if (TimeUtils.elapsed(p.getUniqueId(), KillauraNPC.this.name + ".npc.equip", true, InternalUtils.random(KillauraNPC.this.ItemChangeDelayOrigin, KillauraNPC.this.ItemChangeDelayBound)))
				{
					PacketManager.sendPacket(p, new EntityEquipmentPacket(NMS.asNMS(p), Killaura_NPC_EntityID, (short) 1, npc.npcPlayer.getInventory().getBoots()));
					PacketManager.sendPacket(p, new EntityEquipmentPacket(NMS.asNMS(p), Killaura_NPC_EntityID, (short) 2, npc.npcPlayer.getInventory().getLeggings()));
					PacketManager.sendPacket(p, new EntityEquipmentPacket(NMS.asNMS(p), Killaura_NPC_EntityID, (short) 3, npc.npcPlayer.getInventory().getChestplate()));
					PacketManager.sendPacket(p, new EntityEquipmentPacket(NMS.asNMS(p), Killaura_NPC_EntityID, (short) 4, npc.npcPlayer.getInventory().getHelmet()));
					PacketManager.sendPacket(p, new EntityEquipmentPacket(NMS.asNMS(p), Killaura_NPC_EntityID, (short) 0, npc.npcPlayer.getItemInHand()));
					TimeUtils.putCurrentTime(p.getUniqueId(), KillauraNPC.this.name + ".npc.equip");
				}
				if (TimeUtils.elapsed(p.getUniqueId(), KillauraNPC.this.name + ".npc.swing", true, InternalUtils.random(KillauraNPC.this.SwingDelayOrigin, KillauraNPC.this.SwingDelayBound)))
				{
					PacketManager.sendPacket(p, new AnimationPacket(NMS.asNMS(p), Killaura_NPC_EntityID, 0));
					TimeUtils.putCurrentTime(p.getUniqueId(), KillauraNPC.this.name + ".npc.swing");
				}
				PacketManager.sendPacket(p, new EntityMetadataPacket(NMS.asNMS(p), Killaura_NPC_EntityID, npc.npc.getDataWatcher(), false));
				Location npcPos = getNPCPosition(p, active);
				EntityTeleportPacket tp = new EntityTeleportPacket(NMS.asNMS(p), 0, 0, 0, 0, (byte) 0, (byte) 0, npc.npc.onGround);
				tp.setEntity(Killaura_NPC_EntityID);
				tp.convertAndSetPosition(npcPos.toVector());
				tp.convertAndSetFacing(npc.npcPlayer.getLocation().getYaw(), npc.npcPlayer.getLocation().getPitch());
				// 많은 안티 치트들과 Anti-killaura NPC들은 이 패킷을 보내지 않으나, 바닐라 마인크래프트 플레이어는 이 PacketPlayOutEntityRelMove패킷을 보냅니다. 이 패킷을 보냄으로써 몇몇 Hacked Client의 AntiBot이 이 패킷으로 해당 엔티티가 Anti-killaura NPC인지 판단하는 것을 Bypass하기 위해 이 코드를 넣었습니다.
				PacketManager.sendPacket(p, new EntityRelativeMovePacket(NMS.asNMS(p), Killaura_NPC_EntityID, (byte) 0, (byte) 0, (byte) 0, npc.npc.onGround));
				PacketManager.sendPacket(p, tp);
				EntityHeadRotationPacket headrot = new EntityHeadRotationPacket(NMS.asNMS(p), Killaura_NPC_EntityID, (byte) 0);
				headrot.convertAndSetYaw(npc.npcPlayer.getLocation().getYaw());
				PacketManager.sendPacket(p, headrot);
			}
		}
	}

	public Location getNPCPosition(Player p, boolean active)
	{
		Location loc = p.getLocation();
		float z = (float) (loc.getZ() + Math.sin(Math.toRadians(loc.getYaw() + 90)));
		float x = (float) (loc.getX() + Math.cos(Math.toRadians(loc.getYaw() + 90)));
		Vector trajectory = new Vector(x - loc.getX(), 0, z - loc.getZ());
		trajectory.setY(0).multiply(-Math.abs(this.npc_back) - (active ? 0 : this.npc_increment_back_nonactive));
		trajectory.setY(this.npc_y + (active ? 0 : this.npc_increment_y_nonactive));
		return loc.clone().add(trajectory);
//		return p.getLocation().clone().add(p.getLocation().clone().getDirection()
//				.multiply(-npc_back - (active ? 0 : npc_increment_back_nonactive)));
	}

	public double clamp_pitch(double pitch)
	{
		if (pitch > 90)
			return 90;
		else if (pitch < -90)
			return -90;
		else
			return pitch;
	}

	@Override
	public void onEvent(Event ev, int id)
	{
	}

	@Override
	public void onPacketIn(PacketInEvent e)
	{
		if (e.getPacket().getType() == PacketTypeIn.USE_ENTITY)
		{
			UseEntityPacket packet = (UseEntityPacket) e.getPacket();
			Player p = e.getPlayer();
			if (packet.getAction() == EntityUseAction.ATTACK)
			{
				int entityID = packet.getEntityId();
				// kaEntity Hit
				if (entityID == Killaura_NPC_EntityID)
				{
					long delay = TimeUtils.getTimeDiff(p.getUniqueId(), this.name + ".npc.hit", System.currentTimeMillis());
					if (delay > 0 && delay <= 250)
					{
						suspect(p, Math.min(Math.max(Math.abs(251 - delay), 1), 20), "npc");
					}
					TimeUtils.putCurrentTime(p.getUniqueId(), this.name + ".npc.hit");
					e.setCancelled(true);
				}
				TimeUtils.putCurrentTime(p.getUniqueId(), this.name + ".lastCombat");
			}
		}
	}

	@Override
	public void onPacketOut(PacketOutEvent e)
	{
		// TODO Auto-generated method stub
	}
}
