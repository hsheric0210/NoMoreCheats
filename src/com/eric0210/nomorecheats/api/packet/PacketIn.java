package com.eric0210.nomorecheats.api.packet;

import com.eric0210.nomorecheats.api.packet.packets.in.AbilitiesPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.ArmAnimationPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockDigPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.BlockPlacePacket;
import com.eric0210.nomorecheats.api.packet.packets.in.ChatPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.ClientCommandPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.CloseWindowPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.CustomPayloadPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.EnchantItemPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.EntityActionPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.FlyingPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.HeldItemSlotPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.KeepAlivePacket;
import com.eric0210.nomorecheats.api.packet.packets.in.SetCreativeSlotPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.SettingsPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.SteerVehiclePacket;
import com.eric0210.nomorecheats.api.packet.packets.in.TabCompletePacket;
import com.eric0210.nomorecheats.api.packet.packets.in.TransactionPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.UpdateSignPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.UseEntityPacket;
import com.eric0210.nomorecheats.api.packet.packets.in.WindowClickPacket;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayInAbilities;
import net.minecraft.server.v1_7_R4.PacketPlayInArmAnimation;
import net.minecraft.server.v1_7_R4.PacketPlayInBlockDig;
import net.minecraft.server.v1_7_R4.PacketPlayInBlockPlace;
import net.minecraft.server.v1_7_R4.PacketPlayInChat;
import net.minecraft.server.v1_7_R4.PacketPlayInClientCommand;
import net.minecraft.server.v1_7_R4.PacketPlayInCloseWindow;
import net.minecraft.server.v1_7_R4.PacketPlayInCustomPayload;
import net.minecraft.server.v1_7_R4.PacketPlayInEnchantItem;
import net.minecraft.server.v1_7_R4.PacketPlayInEntityAction;
import net.minecraft.server.v1_7_R4.PacketPlayInFlying;
import net.minecraft.server.v1_7_R4.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_7_R4.PacketPlayInKeepAlive;
import net.minecraft.server.v1_7_R4.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_7_R4.PacketPlayInSettings;
import net.minecraft.server.v1_7_R4.PacketPlayInSteerVehicle;
import net.minecraft.server.v1_7_R4.PacketPlayInTabComplete;
import net.minecraft.server.v1_7_R4.PacketPlayInTransaction;
import net.minecraft.server.v1_7_R4.PacketPlayInUpdateSign;
import net.minecraft.server.v1_7_R4.PacketPlayInUseEntity;
import net.minecraft.server.v1_7_R4.PacketPlayInWindowClick;

public abstract class PacketIn extends Packet
{
	public PacketIn(EntityPlayer owner,PacketTypeIn type, net.minecraft.server.v1_7_R4.Packet packet)
	{
		super(owner, packet);
		this.type = type;
	}

	private PacketTypeIn type;

	public final PacketTypeIn getType()
	{
		return this.type;
	}

	public static <T extends net.minecraft.server.v1_7_R4.Packet> PacketIn byNMS(EntityPlayer owner, T p)
	{
		PacketTypeIn type = PacketTypeIn.getTypeByPacket(p);
		switch (type)
		{
		case ABILITIES:
			return new AbilitiesPacket(owner, (PacketPlayInAbilities) p);
		case ARM_ANIMATION:
			return new ArmAnimationPacket(owner, (PacketPlayInArmAnimation) p);
		case BLOCK_DIG:
			return new BlockDigPacket(owner, (PacketPlayInBlockDig) p);
		case BLOCK_PLACE:
			return new BlockPlacePacket(owner, (PacketPlayInBlockPlace) p);
		case CHAT:
			return new ChatPacket(owner, (PacketPlayInChat) p);
		case CLIENT_COMMAND:
			return new ClientCommandPacket(owner, (PacketPlayInClientCommand) p);
		case CLOSE_WINDOW:
			return new CloseWindowPacket(owner, (PacketPlayInCloseWindow) p);
		case CUSTOM_PAYLOAD:
			return new CustomPayloadPacket(owner, (PacketPlayInCustomPayload) p);
		case ENCHANT_ITEM:
			return new EnchantItemPacket(owner, (PacketPlayInEnchantItem) p);
		case ENTITY_ACTION:
			return new EntityActionPacket(owner, (PacketPlayInEntityAction) p);
		case FLYING:
			return new FlyingPacket(owner, (PacketPlayInFlying) p);
		case HELD_ITEM_SLOT:
			return new HeldItemSlotPacket(owner, (PacketPlayInHeldItemSlot) p);
		case KEEP_ALIVE:
			return new KeepAlivePacket(owner, (PacketPlayInKeepAlive) p);
		case SETTINGS:
			return new SettingsPacket(owner, (PacketPlayInSettings) p);
		case SET_CREATIVE_SLOT:
			return new SetCreativeSlotPacket(owner, (PacketPlayInSetCreativeSlot) p);
		case STEER_VEHICLE:
			return new SteerVehiclePacket(owner, (PacketPlayInSteerVehicle) p);
		case TAB_COMPLETE:
			return new TabCompletePacket(owner, (PacketPlayInTabComplete) p);
		case TRANSACTION:
			return new TransactionPacket(owner, (PacketPlayInTransaction) p);
		case UNKNOWN:
			return new UnknownPacket(owner, p);
		case UPDATE_SIGN:
			return new UpdateSignPacket(owner, (PacketPlayInUpdateSign) p);
		case USE_ENTITY:
			return new UseEntityPacket(owner, (PacketPlayInUseEntity) p);
		case WINDOW_CLICK:
			return new WindowClickPacket(owner, (PacketPlayInWindowClick) p);
		}
		return new UnknownPacket(owner, p);
	}

	private static class UnknownPacket extends PacketIn
	{
		public UnknownPacket(EntityPlayer owner, net.minecraft.server.v1_7_R4.Packet p)
		{
			super(owner, PacketTypeIn.UNKNOWN, p);
		}

		@Override
		public net.minecraft.server.v1_7_R4.Packet toNMS()
		{
			return getNMSPacket();
		}
	}
}
