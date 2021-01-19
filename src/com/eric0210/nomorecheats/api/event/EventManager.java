package com.eric0210.nomorecheats.api.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import com.eric0210.nomorecheats.Logging;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.ViolationMap;
import com.eric0210.nomorecheats.api.packet.listeners.PlayerConnectionFilter;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.GroundChecks;
import com.eric0210.nomorecheats.api.util.TimeUtils;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.CombatUtils;
import com.eric0210.nomorecheats.api.util.InventoryUtils;
import com.eric0210.nomorecheats.api.util.MathUtils;
import com.eric0210.nomorecheats.api.util.Reverter;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.PlayerConnection;

public class EventManager implements Listener
{
	public static ArrayList<EventInfo> onEntityDamage = new ArrayList<>();
	public static ArrayList<EventInfo> onEntityDamageByEntity = new ArrayList<>();
	public static ArrayList<EventInfo> onEntityDamageByBlock = new ArrayList<>();

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e)
	{
		execute(onEntityDamage, e);
		if (e instanceof EntityDamageByEntityEvent)
		{
			execute(onEntityDamageByEntity, e);
			if (e.getEntity() instanceof Player)
				CombatUtils.lastHitByEntity.put(e.getEntity().getUniqueId(), System.currentTimeMillis());
			if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player)
				CombatUtils.lastHitEntity.put(((EntityDamageByEntityEvent) e).getDamager().getUniqueId(), System.currentTimeMillis());
		}
		if (e instanceof EntityDamageByBlockEvent)
		{
			execute(onEntityDamageByBlock, e);
		}
	}

	public static ArrayList<EventInfo> onPlayerAnimation = new ArrayList<>();

	@EventHandler
	public void onPlayerAnimation(PlayerAnimationEvent e)
	{
		execute(onPlayerAnimation, e);
	}

	public static ArrayList<EventInfo> onEntityDeath = new ArrayList<>();
	public static ArrayList<EventInfo> onPlayerDeath = new ArrayList<>();

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e)
	{
		Reverter.resetAllPositions();
		execute(onEntityDeath, e);
		if (e instanceof PlayerDeathEvent)
		{
			PlayerDeathEvent e2 = (PlayerDeathEvent) e;
			Player p = (Player) e.getEntity();
			Cache.set(p.getUniqueId(), "__cached_fallDistance", 0.0F);
			Cache.set(p.getUniqueId(), "fallDistance", 0.0F);
			execute(onPlayerDeath, e2);
		}
	}

	public static ArrayList<EventInfo> onEntityTarget = new ArrayList<>();

	@EventHandler
	public void onEntityTarget(EntityTargetEvent e)
	{
		execute(onEntityTarget, e);
	}

	public static ArrayList<EventInfo> onFoodLevelChange = new ArrayList<>();

	@EventHandler
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent e)
	{
		execute(onFoodLevelChange, e);
	}

	public static ArrayList<EventInfo> onPlayerTeleport = new ArrayList<>();

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e)
	{
		if (!e.isCancelled())
		{
			if (e.getCause() == TeleportCause.ENDER_PEARL || e.getCause() == TeleportCause.COMMAND)
				Reverter.setAllPositions(e.getPlayer().getUniqueId(), e.getTo());
			Counter.increment1AndGetCount(e.getPlayer().getUniqueId(), "teleport", -1);
		}
		execute(onPlayerTeleport, e);
	}

	public static ArrayList<EventInfo> onEntityRegainHealth = new ArrayList<>();

	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent e)
	{
		execute(onEntityRegainHealth, e);
	}

	public static ArrayList<EventInfo> onPlayerDropItem = new ArrayList<>();

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e)
	{
		execute(onPlayerDropItem, e);
	}

	public static ArrayList<EventInfo> onPlayerRespawn = new ArrayList<>();

	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent e)
	{
		Reverter.resetAllPositions();
		Cache.set(e.getPlayer().getUniqueId(), "__cached_fallDistance", 0.0F);
		Cache.set(e.getPlayer().getUniqueId(), "fallDistance", 0.0F);
		execute(onPlayerRespawn, e);
	}

	public static ArrayList<EventInfo> onPlayerInteract = new ArrayList<>();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		execute(onPlayerInteract, e);
	}

	public static ArrayList<EventInfo> onPlayerMove = new ArrayList<>();

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerMoveEvent(PlayerMoveEvent e)
	{
		Player p = e.getPlayer();
		UUID uid = p.getUniqueId();
		if (Counter.getCount(uid, "teleport") > 0)
		{
			Cache.set(uid, "teleported", true);
			System.out.println("marked teleported movement");
			Counter.decrement1AndGetCount(uid, "teleport", -1);
		}
		else
			Cache.set(uid, "teleported", false);
		
		double speed = MathUtils.getHorizontalDistance(e.getFrom(), e.getTo());
		double y_speed = MathUtils.getVerticalDistance(e.getFrom(), e.getTo());
		TimeUtils.putCurrentTime(uid, "lastMove");
		if (p.isSneaking())
			Cooldowns.set(uid, "sneak", 8);
		Cooldowns.set(uid, "movement", -1);
		if (speed > .26D && y_speed > .164D && (Counter.getCount(uid, "ticksUp") > 0 || Counter.getCount(uid, "ticksDown") < 3))
			if (speed > .34)
				Cooldowns.set(p.getUniqueId(), "sprint-jump", 5);
			else
				Cooldowns.set(p.getUniqueId(), "jump", 6);
		if (BlockUtils.getBlocksInRadius2D(p.getLocation(), .4, 0, true).isEmpty())
		{
			Cache.set(uid, "nearBlocks", false);
			Counter.remove(uid, "nearBlocks");
		}
		else
		{
			Cache.set(uid, "nearBlocks", true);
			Counter.increment1AndGetCount(uid, "nearBlocks", -1);
		}
		if (Cache.get(uid, "nearBlocks", false) && Counter.getCount(uid, "nearIce") > 10)
			if (!BlockUtils.isIceNearby(p.getLocation()))
				Cache.set(uid, "nearIce", false);
			else
				Counter.increment1AndGetCount(uid, "nearIce", -1);
		if (BlockUtils.isIceNearby(p.getLocation()))
			if (Counter.getCount(uid, "nearIce") < 60)
				Counter.increment1AndGetCount(uid, "nearIce", -1);
			else if (Counter.getCount(uid, "nearIce") > 0)
				Counter.increment1AndGetCount(uid, "nearIce", -1);
		if (BlockUtils.isIceNearby(p.getLocation()) && !Cache.get(uid, "nearIce", false))
		{
			Cache.set(uid, "nearIce", true);
			Counter.increment1AndGetCount(uid, "nearIce", -1);
		}
		else if (BlockUtils.isIceNearby(p.getLocation()))
		{
			Counter.increment1AndGetCount(uid, "nearIce", -1);
		}
		float distance = (float) MathUtils.getVerticalDistance(e.getFrom(), e.getTo());
		boolean onGround = GroundChecks.isOnGround(p);
		if (!onGround && e.getFrom().getY() > e.getTo().getY())
		{
			Cache.set(uid, "fallDistance", Cache.get(uid, "__cached_fallDistance", 0.0F));
			Cache.set(uid, "__cached_fallDistance", Cache.get(uid, "__cached_fallDistance", 0.0F) + distance);
		}
		else if (onGround)
		{
			Cache.set(uid, "__cached_fallDistance", 0.0F);
			Cache.set(uid, "fallDistance", 0.0F);
		}
		if (onGround)
		{
			Counter.increment1AndGetCount(uid, "groundTicks", -1);
			Counter.remove(uid, "airTicks");
		}
		else
		{
			Counter.increment1AndGetCount(uid, "airTicks", -1);
			Counter.remove(uid, "groundTicks");
		}
		if (BlockUtils.isLiquidNearby(p.getLocation()) || BlockUtils.isLiquidNearby(p.getLocation().add(0.0, 1.0, 0.0)))
			Counter.increment1AndGetCount(uid, "waterTicks", -1);
		else if (Counter.getCount(uid, "waterTicks") > 0)
			Counter.increment1AndGetCount(uid, "waterTicks", -1);
		Cache.set(uid, "moveSpeed", speed);
		InventoryUtils.handleMove(e);
		execute(onPlayerMove, e);
	}

	public static ArrayList<EventInfo> onProjectileHit = new ArrayList<>();

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e)
	{
		execute(onProjectileHit, e);
	}

	public static ArrayList<EventInfo> onProjectileLaunch = new ArrayList<>();

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent e)
	{
		execute(onProjectileLaunch, e);
	}

	public static ArrayList<EventInfo> onBlockPlace = new ArrayList<>();

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e)
	{
		execute(onBlockPlace, e);
	}

	public static ArrayList<EventInfo> onSignChange = new ArrayList<>();

	@EventHandler
	public void onSignChange(SignChangeEvent e)
	{
		execute(onSignChange, e);
	}

	public static ArrayList<EventInfo> onBlockBreak = new ArrayList<>();

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		execute(onBlockBreak, e);
	}

	public static ArrayList<EventInfo> onBlockDamage = new ArrayList<>();

	@EventHandler
	public void onBlockDamage(BlockDamageEvent e)
	{
		execute(onBlockDamage, e);
	}

	public static ArrayList<EventInfo> onPlayerItemConsume = new ArrayList<>();

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent e)
	{
		execute(onPlayerItemConsume, e);
	}

	public static ArrayList<EventInfo> onPlayerJoin = new ArrayList<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Logging.debug("Player " + e.getPlayer().getName() + " joined to this server.");
		PlayerConnection oldConnection = ((CraftPlayer) e.getPlayer()).getHandle().playerConnection;
		((CraftPlayer) e.getPlayer()).getHandle().playerConnection = new PlayerConnectionFilter(MinecraftServer.getServer(), oldConnection.networkManager, ((CraftPlayer) e.getPlayer()).getHandle());
		Logging.debug("Player " + e.getPlayer().getName() + " PlayerConnection has replaced to PlayerConnectionFilter");
		ViolationMap.reset(e.getPlayer());
		// Checks.initalizeChecks(e.getPlayer());
		Reverter.resetAllPositions();
		execute(onPlayerJoin, e);
	}

	public static ArrayList<EventInfo> onPlayerQuit = new ArrayList<>();

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		ViolationMap.reset(e.getPlayer());
		execute(onPlayerQuit, e);
	}

	public static ArrayList<EventInfo> onPlayerInteractEntity = new ArrayList<>();

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e)
	{
		execute(onPlayerInteractEntity, e);
	}

	public static ArrayList<EventInfo> onAsyncPlayerChat = new ArrayList<>();

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e)
	{
		execute(onAsyncPlayerChat, e);
	}

	public static ArrayList<EventInfo> onPlayerKick = new ArrayList<>();

	@EventHandler
	public void onPlayerKick(PlayerKickEvent e)
	{
		execute(onPlayerKick, e);
	}

	public static ArrayList<EventInfo> onPlayerSneak = new ArrayList<>();

	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent e)
	{
		execute(onPlayerSneak, e);
	}

	public static ArrayList<EventInfo> onInventoryClick = new ArrayList<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent e)
	{
		execute(onInventoryClick, e);
	}

	public static ArrayList<EventInfo> onInventoryOpen = new ArrayList<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryOpen(InventoryOpenEvent e)
	{
		execute(onInventoryOpen, e);
	}

	public static ArrayList<EventInfo> onInventoryClose = new ArrayList<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent e)
	{
		execute(onInventoryClose, e);
	}

	public static ArrayList<EventInfo> onInventoryPickupItem = new ArrayList<>();

	@EventHandler
	public void onInventoryPickupItem(InventoryPickupItemEvent e)
	{
		execute(onInventoryPickupItem, e);
	}

	public static ArrayList<EventInfo> onInventoryInteract = new ArrayList<>();

	@EventHandler
	public void onInventoryInteract(InventoryInteractEvent e)
	{
		execute(onInventoryInteract, e);
	}

	public static ArrayList<EventInfo> onPlayerLogin = new ArrayList<>();

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e)
	{
		execute(onPlayerLogin, e);
	}

	public static ArrayList<EventInfo> onPlayerPortal = new ArrayList<>();

	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent e)
	{
		execute(onPlayerPortal, e);
	}

	public static ArrayList<EventInfo> onPlayerLevelChange = new ArrayList<>();

	@EventHandler
	public void onPlayerLevelChange(PlayerLevelChangeEvent e)
	{
		execute(onPlayerLevelChange, e);
	}

	public static ArrayList<EventInfo> onPlayerExpChange = new ArrayList<>();

	@EventHandler
	public void onPlayerExpChange(PlayerExpChangeEvent e)
	{
		execute(onPlayerExpChange, e);
	}

	public static ArrayList<EventInfo> onPlayerEmptyBucket = new ArrayList<>();

	@EventHandler
	public void onPlayerEmptyBucket(PlayerBucketEmptyEvent e)
	{
		execute(onPlayerEmptyBucket, e);
	}

	public static ArrayList<EventInfo> onPlayerFillBucket = new ArrayList<>();

	@EventHandler
	public void onPlayerFillBucket(PlayerBucketFillEvent e)
	{
		execute(onPlayerFillBucket, e);
	}

	public static ArrayList<EventInfo> onPlayerEditBook = new ArrayList<>();

	@EventHandler
	public void onEditBook(PlayerEditBookEvent e)
	{
		execute(onPlayerEditBook, e);
	}

	public static ArrayList<EventInfo> onPlayerFish = new ArrayList<>();

	@EventHandler
	public void onPlayerFish(PlayerFishEvent e)
	{
		execute(onPlayerFish, e);
	}

	public static ArrayList<EventInfo> onEntityShootBow = new ArrayList<>();

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent e)
	{
		execute(onEntityShootBow, e);
	}

	public static ArrayList<EventInfo> onEntityExplode = new ArrayList<>();

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e)
	{
		execute(onEntityExplode, e);
	}

	public static ArrayList<EventInfo> onPlayerItemHeld = new ArrayList<>();

	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent e)
	{
		execute(onPlayerItemHeld, e);
	}

	public static ArrayList<EventInfo> onPlayerChangeWorld = new ArrayList<>();

	@EventHandler
	public void onPlayerItemHeld(PlayerChangedWorldEvent e)
	{
		Reverter.resetAllPositions();
		execute(onPlayerChangeWorld, e);
	}

	public static ArrayList<EventInfo> onVelocity = new ArrayList<>();

	@EventHandler
	public void onVelocity(PlayerVelocityEvent e)
	{
		execute(onVelocity, e);
	}

	public static ArrayList<EventInfo> onMount = new ArrayList<>();

	@EventHandler
	public void onEntityMount(EntityMountEvent e)
	{
		execute(onMount, e);
	}

	public void execute(List<EventInfo> infos, Event e)
	{
		for (EventInfo info : infos)
		{
			try
			{
				if (info.listener instanceof Check && !((Check) info.listener).isEnabled())
					continue;
				info.listener.onEvent(e, info.id);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
}
