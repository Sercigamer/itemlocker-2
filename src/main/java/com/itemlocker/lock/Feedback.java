package com.itemlocker.lock;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Rueckmeldung an den Spieler: Text ueber der Hotbar und ein kurzer Sound.
 */
public final class Feedback {
	private Feedback() {
	}

	public static void dropBlocked(ItemStack stack, int remaining, int required) {
		LockerConfig config = ConfigManager.get();
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (player == null) {
			return;
		}

		if (config.actionBarMessages) {
			player.sendMessage(Text.translatable(
					"itemlocker.message.blocked",
					stack.getName().copy().formatted(Formatting.WHITE),
					Text.literal(String.valueOf(remaining)).formatted(Formatting.YELLOW),
					Text.literal(String.valueOf(required)).formatted(Formatting.GRAY))
					.formatted(Formatting.RED), true);
		}

		if (config.playSound) {
			playSound(0.5F + 0.1F * (required - remaining));
		}
	}

	public static void dropAllowed(ItemStack stack) {
		LockerConfig config = ConfigManager.get();
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (player == null) {
			return;
		}

		if (config.actionBarMessages) {
			player.sendMessage(Text.translatable(
					"itemlocker.message.released",
					stack.getName().copy().formatted(Formatting.WHITE))
					.formatted(Formatting.GREEN), true);
		}

		if (config.playSound) {
			playSound(1.6F);
		}
	}

	public static void slotFrozen(int hotbarSlot) {
		LockerConfig config = ConfigManager.get();
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (player == null) {
			return;
		}

		if (config.actionBarMessages) {
			player.sendMessage(Text.translatable("itemlocker.message.frozen", hotbarSlot + 1)
					.formatted(Formatting.RED), true);
		}

		if (config.playSound) {
			playSound(0.5F);
		}
	}

	public static void info(Text text) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (player != null) {
			player.sendMessage(text, false);
		}
	}

	private static void playSound(float pitch) {
		MinecraftClient client = MinecraftClient.getInstance();
		client.getSoundManager().play(
				PositionedSoundInstance.ui(SoundEvents.BLOCK_NOTE_BLOCK_HAT, Math.min(2.0F, Math.max(0.5F, pitch))));
	}
}
