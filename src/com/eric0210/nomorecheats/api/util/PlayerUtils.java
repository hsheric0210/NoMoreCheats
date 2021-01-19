package com.eric0210.nomorecheats.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.eric0210.nomorecheats.api.packet.PacketManager;
import com.eric0210.nomorecheats.api.packet.packets.out.CloseWindowPacket;
import com.eric0210.nomorecheats.api.util.Protections.ProtectionType;

public class PlayerUtils
{
	public static int getNearbyLivingsAndBoatsCount(Player paramPlayer, double paramDouble)
	{
		int i = 0;
		for (Entity localEntity : paramPlayer.getNearbyEntities(paramDouble, paramDouble, paramDouble))
		{
			if ((localEntity != null) && (((localEntity instanceof LivingEntity)) || ((localEntity instanceof Boat))))
			{
				i++;
			}
		}
		return i;
	}

	public static void clear(Player player)
	{
		player.setGameMode(GameMode.SURVIVAL);
		player.setAllowFlight(false);
		player.setSprinting(false);
		player.setFoodLevel(20);
		player.setSaturation(3.0F);
		player.setExhaustion(0.0F);
		player.setMaxHealth(20.0D);
		player.setHealth(((Damageable) player).getMaxHealth());
		player.setFireTicks(0);
		player.setFallDistance(0.0F);
		player.setLevel(0);
		player.setExp(0.0F);
		player.setWalkSpeed(0.2F);
		player.setFlySpeed(0.1F);
		player.getInventory().clear();
		player.getInventory().setHelmet((ItemStack) null);
		player.getInventory().setChestplate((ItemStack) null);
		player.getInventory().setLeggings((ItemStack) null);
		player.getInventory().setBoots((ItemStack) null);
		player.updateInventory();
		for (PotionEffect potion : player.getActivePotionEffects())
		{
			player.removePotionEffect(potion.getType());
		}
	}

	public static Location getEyeLocation(Player player)
	{
		Location eye = player.getLocation();
		eye.setY(eye.getY() + player.getEyeHeight());
		return eye;
	}

	public static boolean wasFlying(Player p)
	{
		boolean flying = p.isFlying();
		boolean ground = GroundChecks.isOnGround(p);
		if (flying)
		{
			if (!Cache.get(p.getUniqueId(), "abilities.flying", false))
				Cache.set(p.getUniqueId(), "abilities.flying", true);
			return true;
		}
		else if (ground)
		{
			if (Cache.get(p.getUniqueId(), "abilities.flying", false))
				Cache.set(p.getUniqueId(), "abilities.flying", false);
		}
		return Cache.get(p.getUniqueId(), "abilities.flying", false);
	}

	public static boolean wasFlightAllowed(Player p)
	{
		boolean allowed = p.getGameMode() == GameMode.CREATIVE || p.getAllowFlight();
		boolean ground = GroundChecks.isOnGround(p);
		if (allowed)
		{
			if (!Cache.get(p.getUniqueId(), "abilities.flightallowed", false))
				Cache.set(p.getUniqueId(), "abilities.flightallowed", true);
			return true;
		}
		else if (ground)
		{
			if (Cache.get(p.getUniqueId(), "abilities.flightallowed", false))
				Cache.set(p.getUniqueId(), "abilities.flightallowed", false);
		}
		return Cache.get(p.getUniqueId(), "abilities.flightallowed", false);
	}

	public static int getLevelForEnchantment(Player player, Enchantment enchantment)
	{
		try
		{
			ItemStack[] arrayOfItemStack;
			int j = (arrayOfItemStack = player.getInventory().getArmorContents()).length;
			for (int i = 0; i < j; i++)
			{
				ItemStack item = arrayOfItemStack[i];
				if (item.containsEnchantment(enchantment))
				{
					return item.getEnchantmentLevel(enchantment);
				}
			}
		}
		catch (Exception e)
		{
			return -1;
		}
		return -1;
	}

	public static List<Entity> getNearbyVehicles(Location loc, double distance)
	{
		List<Entity> entities = new ArrayList<>();
		for (Entity entity : loc.getWorld().getEntities())
		{
			if ((entity.getType().equals(EntityType.HORSE)) || (entity.getType().equals(EntityType.BOAT)
					|| entity.getType().name().toLowerCase().contains("minecart")))
			{
				if (entity.getLocation().distance(loc) <= distance)
				{
					entities.add(entity);
				}
			}
		}
		return entities;
	}

	public static int getPing(Player p)
	{
		return ((CraftPlayer) p).getHandle().ping;
	}

	public static int invFirstNoHotbar(Inventory inv, ItemStack item, boolean withAmount)
	{
		if (item == null)
			return -2;
		ItemStack[] inventory = inv.getContents();
		for (int i = inventory.length - 1; i > 8; i--)
		{
			if (inventory[i] != null)
			{
				if (withAmount ? item.equals(inventory[i]) : item.isSimilar(inventory[i]))
				{
					return i;
				}
			}
		}
		return -1;
	}

	public static int getPotionEffectLevel(Player p, PotionEffectType type)
	{
		for (PotionEffect pe : p.getActivePotionEffects())
		{
			if (!pe.getType().equals(type))
				continue;
			return pe.getAmplifier() + 1;
		}
		return 0;
	}

	public static boolean hasBlockAbove(Player player)
	{
		for (Block b : BlockUtils.getBlocksInRadius2D(player.getLocation(), .3, 1 + player.getEyeHeight(), true))
		{
			if (BlockUtils.isSolid(b))
				return true;
		}
		return false;
	}

	public static boolean hadBlockAbove(Player player, int protect_ticks)
	{
		if (hasBlockAbove(player))
			Cooldowns.set(player.getUniqueId(), "blockabove_" + protect_ticks, protect_ticks);
		return !Cooldowns.isCooldownEnded(player.getUniqueId(), "blockabove_" + protect_ticks);
	}

	public static boolean isParkour(Location loc, double yDiff)
	{
		boolean isEmptyForwardFloor = getTrajectory(loc, -1, 1).getBlock().isEmpty();
		boolean isEmptyForward = getTrajectory(loc, 0, 1).getBlock().isEmpty();
		boolean isEmptyForwardEye = getTrajectory(loc, 1, 1).getBlock().isEmpty();
		boolean isJumping = yDiff > .25D;
		return isEmptyForward && isEmptyForwardEye && isEmptyForwardFloor && isJumping;
	}

	public static Location getTrajectory(Location loc, double y, double forward)
	{
		double z = (loc.getZ() + Math.sin(Math.toRadians(loc.getYaw() + 90)));
		double x = (loc.getX() + Math.cos(Math.toRadians(loc.getYaw() + 90)));
		Vector tr = new Vector(x - loc.getX(), 0, z - loc.getZ());
		tr.setY(y).multiply(forward);
		return loc.clone().add(tr);
	}

	public static void heldNextSlot(Player p)
	{
		PlayerInventory inv = p.getInventory();
		int slot = inv.getHeldItemSlot();
		int selSlot = slot + 1;
		if (selSlot + 1 > 9)
		{
			selSlot = 9;
		}
		inv.setHeldItemSlot(selSlot);
	}

	public static boolean isOnIceAndUnderTrapdoor(Player p)
	{
		return p.getLocation().subtract(0, .3, 0).getBlock().getType().name().toLowerCase().contains("ices")
				&& (p.getLocation().add(0, 1, 0).getBlock().getType().name().toLowerCase().contains("doors")
						|| p.getLocation().add(0, 1.15, 0).getBlock().getType().name().toLowerCase().contains("doors"));
	}

	public static void forceCloseInventory(Player p)
	{
		PacketManager.sendPacket(p, new CloseWindowPacket(NMS.asNMS(p), NMS.asNMS(p).activeContainer.windowId));
	}

	public static boolean isInClosedChamber(Location location)
	{
		Set<Block> loc = BlockUtils.getBlocksInRadius2D(location, .3, 0, true);
		Set<Block> eyeloc = BlockUtils.getBlocksInRadius2D(location, .3, 1, true);
		for (Block b : loc)
		{
			if (b.isEmpty())
				return false;
		}
		for (Block b : eyeloc)
		{
			if (b.isEmpty())
				return false;
		}
		if (location.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).isEmpty())
			return false;
		if (location.getBlock().getRelative(BlockFace.DOWN).isEmpty())
			return false;
		return true;
	}

	public static boolean isValid(Player p)
	{
		return !p.isDead() && !p.isSleeping() && !p.isFlying() && !Protections.hasProtection(p, ProtectionType.SPEED);
	}
}
