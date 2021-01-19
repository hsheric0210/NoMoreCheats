package com.eric0210.nomorecheats.checks.player;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import com.eric0210.nomorecheats.AntiCheat;
import com.eric0210.nomorecheats.api.Check;
import com.eric0210.nomorecheats.api.ViolationMap;
import com.eric0210.nomorecheats.api.event.EventInfo;
import com.eric0210.nomorecheats.api.event.EventListener;
import com.eric0210.nomorecheats.api.event.EventManager;
import com.eric0210.nomorecheats.api.util.BlockUtils;
import com.eric0210.nomorecheats.api.util.Cache;
import com.eric0210.nomorecheats.api.util.Cooldowns;
import com.eric0210.nomorecheats.api.util.Counter;
import com.eric0210.nomorecheats.api.util.InternalUtils;
import com.eric0210.nomorecheats.api.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Inventory extends Check implements EventListener
{
	public HashMap<UUID, Long> times = new HashMap<>();
	public HashMap<UUID, Long> lastInvInteract = new HashMap<>();

	public Inventory()
	{
		super("Inventory");
		EventManager.onPlayerMove.add(new EventInfo(this, 0));
		EventManager.onInventoryClick.add(new EventInfo(this, 1));
		EventManager.onPlayerDropItem.add(new EventInfo(this, 2));
		EventManager.onPlayerInteract.add(new EventInfo(this, 3));
		EventManager.onInventoryOpen.add(new EventInfo(this, 4));
		Bukkit.getScheduler().runTaskTimer(AntiCheat.antiCheat(), () ->
		{
			for (Player p : Bukkit.getOnlinePlayers())
			{
				if (p.getGameMode() == GameMode.CREATIVE)
					continue;
				int firstEmpty = InventoryUtils.randomFirstEmpty(p.getInventory(), 9, 35);
				if (firstEmpty != -1)
				{
					String key = String.valueOf(InternalUtils.random(Long.MIN_VALUE, Long.MAX_VALUE));
					ItemStack i = randomFakeTrash(key);
					p.getInventory().setItem(firstEmpty, i);
					putFakeItem(p.getUniqueId(), i, "invcleaner");
					Bukkit.getScheduler().runTaskLater(AntiCheat.antiCheat(), () ->
					{
						ItemStack[] inventory = p.getInventory().getContents();
						for (int i2 = 0; i2 < inventory.length; ++i2)
							if (isFakeItem(p.getUniqueId(), inventory[i2], "invcleaner"))
								p.getInventory().setItem(i2, null);
					}, 2);
				}
			}
		}, 5L, 100L);
	}

	@Override
	public void onEvent(Event ev, int id)
	{
		switch (id)
		{
			case 0:
				PlayerMoveEvent e2 = (PlayerMoveEvent) ev;
				if (e2.getFrom() != null && e2.getTo() != null)
				{
					Player p = e2.getPlayer();
					String violation = InventoryUtils.getGUIActionViolation(p);
					if (p.getVehicle() == null && violation != null && p.getOpenInventory().getCursor().getType() != Material.AIR)
					{
						suspect(p, 1, "t: move", "a: cursor-use", "r: (" + violation + ")");
						p.closeInventory();
					}
				}
				break;
			case 1:
				InventoryClickEvent e1 = (InventoryClickEvent) ev;
				HumanEntity clicker = e1.getWhoClicked();
				if (clicker instanceof Player)
				{
					Player p = (Player) clicker;
					p.sendMessage("Inventory activity: " + toStringEvent(e1));
					String invmoveViolation = InventoryUtils.getGUIActionViolation(p);
					ClickType type = e1.getClick();
					if (e1.getCurrentItem() != null && !type.isCreativeAction() && !type.isKeyboardClick() && p.getVehicle() == null && p.getOpenInventory().countSlots() >= 36)
					{
						if (invmoveViolation != null && !BlockUtils.isIceNearby(p.getLocation()))
						{
							suspect(p, 1, "t: move", "a: click", "r: (" + invmoveViolation + ")");
							e1.setCancelled(true);
//							p.closeInventory();
						}
						boolean flag = false;
						boolean isShift = type.isShiftClick();
						String itemName = e1.getCurrentItem().getType().toString().toLowerCase().replaceAll("_", "-");
						if (isShift && p.getOpenInventory().getCursor().getType() != Material.AIR)
						{
							if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".shift", 5) >= 10)
							{
								suspect(p, 1, "t: shift", "i: " + itemName);
								flag = true;
								Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 20);
							}
						}
						else if (this.lastInvInteract.containsKey(p.getUniqueId()))
						{
							long l = System.currentTimeMillis() - this.lastInvInteract.get(p.getUniqueId());
							if (l < 50)
							{
								if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".fast", 20) >= 4)
								{
									suspect(p, 3, "t: click-speed", "l: " + l, "i: " + itemName);
									flag = true;
									Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 35);
								}
							}
							else if (l < 100)
							{
								if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".normal", 20) >= 6)
								{
									suspect(p, 2, "t: click-speed", "l: " + l, "i: " + itemName);
									flag = true;
									Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 20);
								}
							}
							else if (l < 150)
							{
								if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".slow", 20) >= 5)
								{
									suspect(p, 1, "t: click-speed", "l: " + l, "i: " + itemName);
									flag = true;
									Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 15);
								}
							}
							if (!Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".cooldown"))
								flag = true;
						}

						if (!Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".invopen"))
						{
							if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".stealer", 100) >= 3 || isFakeItem(p.getUniqueId(), e1.getCurrentItem(), "stealer"))
							{
								suspect(p, 1, "t: stealer");
								e1.setCurrentItem(new ItemStack(Material.AIR));
								flag = true;
								Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 60);
							}
						}
						if (isFakeItem(p.getUniqueId(), e1.getCurrentItem(), "invcleaner"))
						{
							suspect(p, 1, "t: invcleaner", "a: click");
							e1.setCurrentItem(new ItemStack(Material.AIR));
							flag = true;
							Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 40);
						}
						if (flag)
						{
							e1.setCancelled(true);
//							p.closeInventory();
						}
						this.lastInvInteract.put(p.getUniqueId(), System.currentTimeMillis());
					}
				}
				break;
			case 2:
				PlayerDropItemEvent e3 = (PlayerDropItemEvent) ev;
				Player p = e3.getPlayer();
				if (e3.getItemDrop() != null && e3.getItemDrop().getItemStack() != null && isFakeItem(p.getUniqueId(), e3.getItemDrop().getItemStack(), "invcleaner"))
				{
					suspect(p, 1, "t: invcleaner", "a: drop");
					e3.getItemDrop().setItemStack(new ItemStack(Material.AIR));
					Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 40);
				}
				if (!Cooldowns.isCooldownEnded(p.getUniqueId(), this.name + ".cooldown"))
				{
					e3.setCancelled(true);
				}
				else if (this.times.containsKey(p.getUniqueId()))
				{
					long l = System.currentTimeMillis() - this.times.get(p.getUniqueId());
					if (l <= 50L)
					{
						if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".attempts", 20) >= 6)
						{
							suspect(p, 3, "t: drop-speed", "l: " + l);
							Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 20);
							e3.setCancelled(true);
						}
					}
					else if (l <= 100L)
					{
						if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".attempts", 20) >= 8)
						{
							suspect(p, 2, "t: drop-speed", "l: " + l);
							Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 30);
							e3.setCancelled(true);
						}
					}
					else if (l <= 150L)
					{
						if (Counter.increment1AndGetCount(p.getUniqueId(), this.name + ".attempts", 40) >= 15)
						{
							suspect(p, 1, "t: drop-speed", "l: " + l);
							Cooldowns.set(p.getUniqueId(), this.name + ".cooldown", 40);
							e3.setCancelled(true);
						}
					}
				}
				this.times.put(p.getUniqueId(), System.currentTimeMillis());
				break;
			case 3:
				PlayerInteractEvent e4 = (PlayerInteractEvent) ev;
				if (!e4.isCancelled() && e4.getAction() == Action.RIGHT_CLICK_BLOCK && e4.getClickedBlock().getType() == Material.STATIONARY_WATER && e4.getPlayer().getItemInHand().getType() == Material.GLASS_BOTTLE)
				{
					ViolationMap.getInstance(e4.getPlayer()).reset(this);
				}
				break;
			case 4:
				InventoryOpenEvent e5 = (InventoryOpenEvent) ev;
				if (!e5.isCancelled())
				{
					Cooldowns.set(e5.getPlayer().getUniqueId(), this.name + ".invopen", 2);
					int fakedItemSlot = -1;
					if (e5.getInventory().getType() == InventoryType.CHEST)
					{
						if (e5.getInventory().getSize() > 27)
							fakedItemSlot = InventoryUtils.randomFirstEmpty(e5.getInventory(), 0, 53); // Large chest
						else
							fakedItemSlot = InventoryUtils.randomFirstEmpty(e5.getInventory(), 0, 26); // Chest
					}
					if (fakedItemSlot != -1)
					{
						String key = String.valueOf(InternalUtils.random(Long.MIN_VALUE, Long.MAX_VALUE));
						ItemStack i = randomFakeItem(key);
						e5.getInventory().setItem(fakedItemSlot, i);
						putFakeItem(e5.getPlayer().getUniqueId(), i, "stealer");
						Bukkit.getScheduler().runTaskLater(AntiCheat.antiCheat(), () ->
						{
							ItemStack[] inventory = e5.getInventory().getContents();
							for (int i2 = 0; i2 < inventory.length; ++i2)
								if (isFakeItem(e5.getPlayer().getUniqueId(), inventory[i2], "stealer"))
									e5.getInventory().setItem(i2, null);
						}, 2);
					}
				}
				break;
		}

	}

	private String toStringEvent(InventoryClickEvent e1)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("clickType: " + e1.getClick());
		sb.append(", action: " + e1.getAction());
		sb.append(", slotType: " + e1.getSlotType());
		sb.append(", slot(raw): " + e1.getRawSlot());
		return sb.toString();
	}

	private ItemStack randomFakeItem(String displayName)
	{
		Material type = null;
		switch (new Random().nextInt(6))
		{
			case 0:
				type = Material.DIAMOND_SWORD;
				break;
			case 1:
				type = Material.STONE;
				break;
			case 2:
				type = Material.DIAMOND_CHESTPLATE;
				break;
			case 3:
				type = Material.COOKED_BEEF;
				break;
			case 4:
				type = Material.GOLDEN_APPLE;
				break;
			case 5:
				type = Material.BOW;
				break;
		}
		ItemStack i = new ItemStack(type, 1);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(displayName);
		i.setItemMeta(meta);
		return i;
	}

	private ItemStack randomFakeTrash(String displayName)
	{
		Material type = null;
		switch (new Random().nextInt(6))
		{
			case 0:
				type = Material.STRING;
				break;
			case 1:
				type = Material.REDSTONE;
				break;
			case 2:
				type = Material.BRICK;
				break;
			case 3:
				type = Material.CLAY;
				break;
			case 4:
				type = Material.CACTUS;
				break;
			case 5:
				type = Material.BOOK;
				break;
		}
		ItemStack i = new ItemStack(type, 1);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(displayName);
		i.setItemMeta(meta);
		return i;
	}

	private boolean isFakeItem(UUID uuidPlayer, ItemStack i, String identifierCacheKey)
	{
		return i != null && i.hasItemMeta() && i.getItemMeta().hasDisplayName() && Cache.contains(uuidPlayer, this.name + "." + identifierCacheKey + ".itemtype") && Cache.contains(uuidPlayer, this.name + "." + identifierCacheKey + ".itemname") && Cache.get(uuidPlayer, this.name + "." + identifierCacheKey + ".itemtype", Material.AIR) == i.getType() && Cache.get(uuidPlayer, this.name + "." + identifierCacheKey + ".itemname", "").equalsIgnoreCase(i.getItemMeta().getDisplayName());
	}

	private void putFakeItem(UUID uuidPlayer, ItemStack i, String identifierCacheKey)
	{
		if (i != null && i.hasItemMeta() && i.getItemMeta().hasDisplayName())
		{
			Cache.set(uuidPlayer, this.name + "." + identifierCacheKey + ".itemtype", i.getType());
			Cache.set(uuidPlayer, this.name + "." + identifierCacheKey + ".itemname", i.getItemMeta().getDisplayName());
		}
	}
}
