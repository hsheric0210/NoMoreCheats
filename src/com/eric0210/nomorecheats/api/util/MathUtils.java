package com.eric0210.nomorecheats.api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EnumMovingObjectType;
import net.minecraft.server.v1_7_R4.MathHelper;
import net.minecraft.server.v1_7_R4.MovingObjectPosition;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class MathUtils
{
	public static Random random = new Random();
	public static String DEFAULT_ENCODING = "UTF-8";
	private static Vector vec1 = new Vector();
	private static Vector vec2 = new Vector();
	public static double fRadToGrad = 57.29577951308232;
	public static double DIRECTION_PRECISION = 2.6;
	public static double DIRECTION_LOOP_PRECISION = 0.5;
	private static float[] sinTable = new float[65536];

	public static Location getFacingPosition(Player p, Entity e)
	{
		Location player_pos = p.getLocation();
		Location entity_pos = e.getLocation();
		double xdelta = player_pos.getX() - entity_pos.getX();
		double ydelta = player_pos.getY() - entity_pos.getY();
		double zdelta = player_pos.getZ() - entity_pos.getZ();
		double distance = player_pos.distance(entity_pos);
		Vector facing_pos = player_pos.getDirection().normalize().multiply(distance);
		Vector pos_delta = new Vector(xdelta, ydelta, zdelta);
		facing_pos = pos_delta.subtract(facing_pos);
		return new Location(p.getWorld(), facing_pos.getX(), facing_pos.getY(), facing_pos.getZ(), player_pos.getYaw(), player_pos.getPitch());
	}

	public static double getAngle(Player p, Entity le)
	{
		Location player_pos = p.getLocation();
		Location entity_pos = le.getLocation();
		player_pos.setY(1);
		entity_pos.setY(1);
		Vector sub = entity_pos.toVector().subtract(player_pos.toVector());
		Location player_pos_clone = player_pos.clone();
		player_pos_clone.setPitch(0F);
		Vector player_dir = player_pos_clone.getDirection();
		return sub.normalize().dot(player_dir);
	}

	public static double getDirectionDelta(Player paramPlayer, Entity paramEntity)
	{
		Location player_pos = paramPlayer.getLocation();
		Location entity_pos = paramEntity.getLocation();
		double distance = MathUtils.getHorizontalDistance(player_pos, entity_pos);
		Location player_facing_pos = player_pos.add(player_pos.getDirection().multiply(distance));
		return MathUtils.getHorizontalDistance(player_facing_pos, entity_pos);
	}

	public static double getYawBetween(Location from, Location to)
	{
		Location change = from.clone();
		Vector start = change.toVector();
		Vector target = to.toVector();
		change.setDirection(target.subtract(start));
		return change.getYaw();
	}

	public static double getRotationDelta(Player p, LivingEntity le)
	{
		return getRotationDelta_yaw(p, le) + getRotationDelta_pitch(p, le);
	}

	public static double getRotationDelta_yaw(Player p, LivingEntity le)
	{
		Vector pRot = new Vector(p.getLocation().getYaw(), p.getLocation().getPitch(), 0);
		Vector calcRot = MathUtils.getRotation(p.getEyeLocation(), MathUtils.getAABBCenter(le).toLocation(le.getWorld()));
		return getRotationDifference0(pRot, calcRot)[0];
	}

	public static double getRotationDelta_pitch(Player p, LivingEntity le)
	{
		Vector pRot = new Vector(p.getLocation().getYaw(), p.getLocation().getPitch(), 0);
		Vector calcRot = MathUtils.getRotation(p.getEyeLocation(), MathUtils.getAABBCenter(le).toLocation(le.getWorld()));
		return getRotationDifference0(pRot, calcRot)[1];
	}

	public static float[] getRotationDifference0(Vector rotation1, Vector rotation2)
	{
		float yaw = Math.abs(((float) rotation1.getX() + MathUtils.clamp180F((float) rotation2.getX() - (float) rotation1.getX())) - (float) rotation1.getX());
		float pitch = Math.abs(((float) rotation1.getY() + MathUtils.clamp180F((float) rotation2.getY() - (float) rotation1.getY())) - (float) rotation1.getY());
		return new float[]
		{
				yaw, pitch
		};
	}

	public static float getRotationDifference(Vector rotation1, Vector rotation2)
	{
		float yaw = getRotationDifference0(rotation1, rotation2)[0];
		float pitch = getRotationDifference0(rotation1, rotation2)[1];
		return (float) Math.hypot(yaw, pitch);
	}

	/**
	 * Returns the greatest integer less than or equal to the double argument
	 */
	public static int floor_double(double value)
	{
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

	public static float sin(float f)
	{
		return sinTable[(int) (f * 10430.378f) & 65535];
	}

	public static float cos(float f)
	{
		return sinTable[(int) (f * 10430.378f + 16384.0f) & 65535];
	}

	public static boolean isInt(String string)
	{
		try
		{
			Integer.parseInt(string);
			return true;
		}
		catch (Exception localException)
		{
		}
		return false;
	}

	public static boolean isDouble(String string)
	{
		try
		{
			Double.parseDouble(string);
			return true;
		}
		catch (Exception localException)
		{
		}
		return false;
	}

	public static Location getTarget0(GameMode currentGM, Location last, Location current, double eyeHeight)
	{
		float f = 1.0f;
		float pitch = last.getPitch() + (current.getPitch() - last.getPitch()) * f;
		float yaw = last.getYaw() + (current.getYaw() - last.getYaw()) * f;
		double x = last.getX() + (current.getX() - last.getX()) * f;
		double y = last.getY() + (current.getY() - last.getY()) * f + 1.62 - eyeHeight;
		double z = last.getZ() + (current.getZ() - last.getZ()) * f;
		Vector vec1 = new Vector(x, y, z);
		float f3 = MathHelper.cos((-yaw) * 0.017453292f - 3.1415927f);
		float f4 = MathHelper.sin((-yaw) * 0.017453292f - 3.1415927f);
		float f5 = -MathHelper.cos((-pitch) * 0.017453292f);
		float f6 = MathHelper.sin((-pitch) * 0.017453292f);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double maxReach = currentGM == GameMode.CREATIVE ? 5.0 : 4.5;
		Vector vec2 = vec1.add(new Vector(f7 * maxReach, f6 * maxReach, f8 * maxReach));
		MovingObjectPosition obj = NMS.asNMS(current.getWorld()).rayTrace(NMS.vecterToVec3d(vec1), NMS.vecterToVec3d(vec2), false);
		if (obj.type == EnumMovingObjectType.ENTITY)
		{
			return obj.entity.getBukkitEntity().getLocation();
		}
		else if (obj.type == EnumMovingObjectType.BLOCK)
		{
			return new Location(current.getWorld(), obj.b, obj.c, obj.d);
		}
		return null;
	}

	public static Location getTarget(Player p)
	{
		EntityPlayer np = NMS.asNMS(p);
		return getTarget0(p.getGameMode(), new Location(p.getWorld(), np.lastX, np.lastY, np.lastZ, np.lastYaw, np.lastPitch), p.getLocation(), p.getEyeHeight());
	}

	public static double getYaw(Location from, Location to)
	{
		if ((from == null) || (to == null))
		{
			return 0.0D;
		}
		double difX = to.getX() - from.getX();
		double difZ = to.getZ() - from.getZ();
		return clamp180F((float) (Math.atan2(difZ, difX) * 180.0D / Math.PI) - 90.0F);
	}

	public static double getPitch(Location from, Location to)
	{
		if ((from == null) || (to == null))
		{
			return 0.0D;
		}
		double difX = to.getX() - from.getX();
		double difY = to.getY() - from.getY();
		double difZ = to.getZ() - from.getZ();
		double dist = getDistance(difX, difZ);
		return clamp180F((float) (-Math.atan2(difY, dist) * 180.0D / Math.PI));
	}

	public static float angle(Vector src, Vector dir, Vector trg)
	{
		double dirLength = Math.sqrt(dir.getX() * dir.getX() + dir.getY() * dir.getY() + dir.getZ() * dir.getZ());
		if (dirLength == 0.0)
		{
			dirLength = 1.0;
		}
		double dX = trg.getX() - src.getX();
		double dY = trg.getY() - src.getY();
		double dZ = trg.getZ() - src.getZ();
		vec1.setX(dX);
		vec1.setY(dY);
		vec1.setZ(dZ);
		vec2.setX(dir.getX());
		vec2.setY(dir.getY());
		vec2.setZ(dir.getZ());
		return vec2.angle(vec1);
	}

	public static double angle(double x, double z)
	{
		double a;
		if (x > 0.0)
		{
			a = Math.atan(z / x);
		}
		else if (x < 0.0)
		{
			a = Math.atan(z / x) + 3.141592653589793;
		}
		else if (z < 0.0)
		{
			a = 4.71238898038469;
		}
		else if (z > 0.0)
		{
			a = 1.5707963267948966;
		}
		else
		{
			return Double.NaN;
		}
		if (a < 0.0)
		{
			return a + 6.283185307179586;
		}
		return a;
	}

	public static double angleDiff(double a1, double a2)
	{
		if (Double.isNaN(a1) || Double.isNaN(a2))
		{
			return Double.NaN;
		}
		double diff = a2 - a1;
		if (diff < -3.141592653589793)
		{
			return diff + 6.283185307179586;
		}
		if (diff > 3.141592653589793)
		{
			return diff - 6.283185307179586;
		}
		return diff;
	}

	public static float yawDiff(float fromYaw, float toYaw)
	{
		if (fromYaw <= -360.0f)
		{
			fromYaw = -(-fromYaw) % 360.0f;
		}
		else if (fromYaw >= 360.0f)
		{
			fromYaw %= 360.0f;
		}
		if (toYaw <= -360.0f)
		{
			toYaw = -(-toYaw) % 360.0f;
		}
		else if (toYaw >= 360.0f)
		{
			toYaw %= 360.0f;
		}
		float yawDiff = toYaw - fromYaw;
		if (yawDiff < -180.0f)
		{
			yawDiff += 360.0f;
		}
		else if (yawDiff > 180.0f)
		{
			yawDiff -= 360.0f;
		}
		return yawDiff;
	}

	public static double round(double value, int places)
	{
		return round(value, places, RoundingMode.HALF_UP);
	}

	public static double round(double value, int places, RoundingMode rm)
	{
		if (places < 0)
		{
			throw new IllegalArgumentException();
		}
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, rm);
		return bd.doubleValue();
	}

	public static float round_f(double value, int places, RoundingMode rm)
	{
		if (places < 0)
		{
			throw new IllegalArgumentException();
		}
		BigDecimal bd = new BigDecimal(value);
		bd = new BigDecimal(bd.floatValue());
		bd = bd.setScale(places, rm);
		return bd.floatValue();
	}

	public static float convertToFloat(double value)
	{
		BigDecimal bd = new BigDecimal(value);
		return bd.floatValue();
	}

	public static double getHorizontalDistance0(double fromX, double fromZ, double toX, double toZ)
	{
		return (float) Math.hypot(Math.abs(toX - fromX), Math.abs(toZ - fromZ));
	}

	public static double getHorizontalDistance(Location one, Location two)
	{
		return getHorizontalDistance0(one.getX(), one.getZ(), two.getX(), two.getZ());
	}

	public static double getDistance3D(Location one, Location two)
	{
		double toReturn = 0.0f;
		double xSqr = (two.getX() - one.getX()) * (two.getX() - one.getX());
		double ySqr = (two.getY() - one.getY()) * (two.getY() - one.getY());
		double zSqr = (two.getZ() - one.getZ()) * (two.getZ() - one.getZ());
		double sqrt = Math.sqrt(xSqr + ySqr + zSqr);
		toReturn = Math.abs(sqrt);
		return toReturn;
	}

	public static double getDistance(double xDiff, double zDiff)
	{
		return Math.hypot(xDiff, zDiff);
	}

	public static double getFraction(double value)
	{
		return value % 1.0D;
	}

	public static double trim(double d, int degree)
	{
		String format = "#.#";
		for (int i = 1; i < degree; i++)
		{
			format = String.valueOf(format) + "#";
		}
		DecimalFormat twoDForm = new DecimalFormat(format);
		return Double.valueOf(twoDForm.format(d).replaceAll(",", "."));
	}

	public static int r(int i)
	{
		return random.nextInt(i);
	}

	public static double abs(double a)
	{
		return a <= 0.0D ? 0.0D - a : a;
	}

	public static String ArrayToString(String[] list)
	{
		String string = "";
		for (String key : list)
		{
			string = String.valueOf(string) + key + ",";
		}
		if (string.length() != 0)
		{
			return string.substring(0, string.length() - 1);
		}
		return null;
	}

	public static String ArrayToString(List<String> list)
	{
		String string = "";
		for (String key : list)
		{
			string = String.valueOf(string) + key + ",";
		}
		if (string.length() != 0)
		{
			return string.substring(0, string.length() - 1);
		}
		return null;
	}

	public static String[] StringToArray(String string, String split)
	{
		return string.split(split);
	}

	public static String Base64Decode(String strEncrypted)
	{
		String strData = "";
		try
		{
			byte[] decoded = Base64.getDecoder().decode(strEncrypted);
			strData = new String(decoded, "UTF-8") + "\n";
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return strData;
	}

	public static String serializeLocation(Location location)
	{
		int X = (int) location.getX();
		int Y = (int) location.getY();
		int Z = (int) location.getZ();
		int P = (int) location.getPitch();
		int Yaw = (int) location.getYaw();
		return new String(String.valueOf(location.getWorld().getName()) + "," + X + "," + Y + "," + Z + "," + P + "," + Yaw);
	}

	public static Location deserializeLocation(String string)
	{
		String[] parts = string.split(",");
		World world = Bukkit.getServer().getWorld(parts[0]);
		double LX = Double.valueOf(Double.parseDouble(parts[1]));
		double LY = Double.valueOf(Double.parseDouble(parts[2]));
		double LZ = Double.valueOf(Double.parseDouble(parts[3]));
		Float P = Float.valueOf(Float.parseFloat(parts[4]));
		Float Y = Float.valueOf(Float.parseFloat(parts[5]));
		Location result = new Location(world, LX, LY, LZ);
		result.setPitch(P.floatValue());
		result.setYaw(Y.floatValue());
		return result;
	}

	public static long averageLong(List<Long> list)
	{
		long add = 0L;
		for (Long listlist : list)
		{
			add += listlist.longValue();
		}
		return add / list.size();
	}

	public static double averageDouble(List<Double> list)
	{
		double add = Double.valueOf(0.0D);
		for (Double listlist : list)
		{
			add = Double.valueOf(add + listlist);
		}
		return add / list.size();
	}

	public static double getVerticalDistance(Location from, Location to)
	{
		return Math.abs(Math.abs(to.getY()) - Math.abs(from.getY()));
	}

	public static Vector getRotation(Location playerLocation, Location entityLocation)
	{
		double dx = entityLocation.getX() - playerLocation.getX();
		double dy = entityLocation.getY() - playerLocation.getY();
		double dz = entityLocation.getZ() - playerLocation.getZ();
		double dist = Math.hypot(dx, dz);
		float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
		float pitch = (float) Math.toDegrees(-Math.atan2(dy, dist));
		return new Vector(yaw, pitch, 0.0f);
	}

	public static double clamp180D(double theta)
	{
		if ((theta %= 360.0D) >= 180.0D)
		{
			theta -= 360.0D;
		}
		if (theta < -180.0D)
		{
			theta += 360.0D;
		}
		return theta;
	}

	public static float clamp180F(float theta)
	{
		if ((theta %= 360.0F) >= 180.0F)
		{
			theta -= 360.0F;
		}
		if (theta < -180.0F)
		{
			theta += 360.0F;
		}
		return theta;
	}

	public static boolean inRange(double toCompare, double origin, double bound)
	{
		return toCompare >= origin && toCompare <= bound;
	}

	public static Vector getAABBCenter(Entity le)
	{
		AABB aabb = new AABB(le);
		double minX = aabb.getMin().getX();
		double minY = aabb.getMin().getY();
		double minZ = aabb.getMin().getZ();
		double maxX = aabb.getMax().getX();
		double maxY = aabb.getMax().getY();
		double maxZ = aabb.getMax().getZ();
		return new Vector(minX + (maxX - minX) * 0.5D, minY + (maxY - minY) * 0.5D, minZ + (maxZ - minZ) * 0.5D);
	}

	public static double fixXAxis(double x)
	{
		double touchedX = x;
		double rem = touchedX - Math.round(touchedX) + 0.01D;
		if (rem < 0.3D)
		{
			touchedX = NumberConversions.floor(x) - 1;
		}
		return touchedX;
	}

	// public static Vector getAimbotRotationsDelta(Player player, LivingEntity
	// entity)
	// {
	// Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(),
	// 0.0D);
	// Location playerLoc = player.getLocation().add(0.0D, player.getEyeHeight(),
	// 0.0D);
	// Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(),
	// 0.0F);
	// Vector expectedRotation = getRotation(playerLoc, entityLoc);
	// double deltaYaw = clamp180D(playerRotation.getX() - expectedRotation.getX());
	// double deltaPitch = clamp180D(playerRotation.getY() -
	// expectedRotation.getY());
	// double horizontalDistance = getHorizontalDistance(playerLoc, entityLoc);
	// double distance = getDistance3D(playerLoc, entityLoc);
	// double offsetX = deltaYaw * horizontalDistance * distance;
	// double offsetY = deltaPitch * Math.abs(Math.sqrt(entityLoc.getY() -
	// playerLoc.getY())) * distance;
	// return new Vector(offsetX, offsetY, 0);
	// }
	public static long lifeToSeconds(String string)
	{
		if ((string.equals("0")) || (string.equals("")))
		{
			return 0L;
		}
		String[] lifeMatch =
		{
				"d", "h", "m", "s"
		};
		int[] lifeInterval =
		{
				86400, 3600, 60, 1
		};
		long seconds = 0L;
		for (int i = 0; i < lifeMatch.length; i++)
		{
			Matcher matcher = Pattern.compile("([0-9]*)" + lifeMatch[i]).matcher(string);
			while (matcher.find())
			{
				seconds += Integer.parseInt(matcher.group(1)) * lifeInterval[i];
			}
		}
		return seconds;
	}

	public static String listToCSV(List<String> list)
	{
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < list.size(); i++)
		{
			b.append(list.get(i));
			if (i < list.size() - 1)
			{
				b.append(",");
			}
		}
		return b.toString();
	}

	// public static double getAimbotRotationDelta(Player player, LivingEntity
	// entity)
	// {
	// double offset = 0.0D;
	// Vector offsets = getAimbotRotationsDelta(player, entity);
	// offset += offsets.getX();
	// offset += offsets.getY();
	// return offset;
	// }
	static
	{
		for (int i = 0; i < 65536; ++i)
		{
			sinTable[i] = (float) Math.sin(i * 3.141592653589793 * 2.0 / 65536.0);
		}
		// y = new int[] { 0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31,
		// 27, 13, 23, 21, 19, 16, 7, 26, 12,
		// 18, 6, 11, 5, 10, 9 };
	}
}
