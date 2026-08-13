package com.itemlocker.lock;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Rueckmeldung an den Spieler: Text ueber der Hotbar und ein kurzer Sound.
 */
public final class Feedback {
	private Feedback() {
	}

	public static void dropBlocked(ItemStack stack, int remaining, int required) {
		LockerConfig config = ConfigManager.get();
		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null) {
			return;
		}

		if (config.actionBarMessages) {
			player.displayClientMessage(Component.translatable(
					"itemlocker.message.blocked",
					stack.getHoverName().copy().withStyle(ChatFormatting.WHITE),
					Component.literal(String.valueOf(remaining)).withStyle(ChatFormatting.YELLOW),
					Component.literal(String.valueOf(required)).withStyle(ChatFormatting.GRAY))
					.withStyle(ChatFormatting.RED), true);
		}

		if (config.playSound) {
			playSound(0.5F + 0.1F * (required - remaining));
		}
	}

	public static void dropAllowed(ItemStack stack) {
		LockerConfig config = ConfigManager.get();
		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null) {
			return;
		}

		if (config.actionBarMessages) {
			player.displayClientMessage(Component.translatable(
					"itemlocker.message.released",
					stack.getHoverName().copy().withStyle(ChatFormatting.WHITE))
					.withStyle(ChatFormatting.GREEN), true);
		}

		if (config.playSound) {
			playSound(1.6F);
		}
	}

	public static void armorStandBlocked(ItemStack stack) {
		LockerConfig config = ConfigManager.get();
		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null) {
			return;
		}

		if (config.actionBarMessages) {
			player.displayClientMessage(Component.translatable("itemlocker.message.armor_stand",
					stack.getHoverName().copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.RED), true);
		}

		if (config.playSound) {
			playSound(0.5F);
		}
	}

	public static void potBlocked(ItemStack stack) {
		warn(Component.translatable("itemlocker.message.pot",
				stack.getHoverName().copy().withStyle(ChatFormatting.WHITE)));
	}

	public static void blockGuiBlocked(Component blockName, boolean sneakBypass) {
		warn(Component.translatable(sneakBypass
				? "itemlocker.message.block_gui_sneak"
				: "itemlocker.message.block_gui",
				blockName.copy().withStyle(ChatFormatting.WHITE)));
	}

	public static void offhandBlocked() {
		warn(Component.translatable("itemlocker.message.offhand"));
	}

	private static void warn(Component message) {
		LockerConfig config = ConfigManager.get();
		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null) {
			return;
		}

		if (config.actionBarMessages) {
			player.displayClientMessage(message.copy().withStyle(ChatFormatting.RED), true);
		}

		if (config.playSound) {
			playSound(0.5F);
		}
	}

	public static void slotFrozen(int hotbarSlot) {
		LockerConfig config = ConfigManager.get();
		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null) {
			return;
		}

		if (config.actionBarMessages) {
			player.displayClientMessage(Component.translatable("itemlocker.message.frozen", hotbarSlot + 1)
					.withStyle(ChatFormatting.RED), true);
		}

		if (config.playSound) {
			playSound(0.5F);
		}
	}

	public static void info(Component text) {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null) {
			player.displayClientMessage(text, false);
		}
	}

	private static void playSound(float pitch) {
		Minecraft client = Minecraft.getInstance();
		client.getSoundManager().play(
				SimpleSoundInstance.ui(SoundEvents.BLOCK_NOTE_BLOCK_HAT, Math.min(2.0F, Math.max(0.5F, pitch))));
	}
}
