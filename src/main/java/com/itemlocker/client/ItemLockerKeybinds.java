package com.itemlocker.client;

import org.lwjgl.glfw.GLFW;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;
import com.itemlocker.lock.DropGuard;
import com.itemlocker.lock.Feedback;
import com.itemlocker.lock.LockManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Tastenbelegungen. Standard: L sperrt den aktuellen Hotbar-Slot, K den
 * Item-Typ in der Hand.
 */
public final class ItemLockerKeybinds {
	public static KeyBinding toggleSlot;
	public static KeyBinding toggleItem;
	public static KeyBinding toggleMod;

	private ItemLockerKeybinds() {
	}

	public static void register() {
		toggleSlot = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.itemlocker.toggle_slot", GLFW.GLFW_KEY_L, KeyBinding.Category.INVENTORY));

		toggleItem = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.itemlocker.toggle_item", GLFW.GLFW_KEY_K, KeyBinding.Category.INVENTORY));

		toggleMod = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.itemlocker.toggle_mod", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.Category.INVENTORY));

		ClientTickEvents.END_CLIENT_TICK.register(ItemLockerKeybinds::onTick);
	}

	private static void onTick(MinecraftClient client) {
		ClientPlayerEntity player = client.player;

		if (player == null) {
			return;
		}

		while (toggleSlot.wasPressed()) {
			int slot = player.getInventory().getSelectedSlot();
			boolean locked = LockManager.toggleSlot(slot);
			DropGuard.resetCounter();

			Feedback.info(Text.translatable(locked ? "itemlocker.message.slot_locked" : "itemlocker.message.slot_unlocked",
					slot + 1).formatted(locked ? Formatting.RED : Formatting.GREEN));
		}

		while (toggleItem.wasPressed()) {
			ItemStack stack = player.getInventory().getStack(player.getInventory().getSelectedSlot());

			if (stack.isEmpty()) {
				Feedback.info(Text.translatable("itemlocker.message.no_item").formatted(Formatting.RED));
				continue;
			}

			String id = LockManager.itemId(stack);
			boolean locked = LockManager.toggleItem(id);
			DropGuard.resetCounter();

			Feedback.info(Text.translatable(locked ? "itemlocker.message.item_locked" : "itemlocker.message.item_unlocked",
					stack.getName().copy().formatted(Formatting.WHITE), id)
					.formatted(locked ? Formatting.RED : Formatting.GREEN));
		}

		while (toggleMod.wasPressed()) {
			LockerConfig config = ConfigManager.get();
			config.enabled = !config.enabled;
			ConfigManager.save();
			DropGuard.resetCounter();

			Feedback.info(Text.translatable(config.enabled ? "itemlocker.message.enabled" : "itemlocker.message.disabled")
					.formatted(config.enabled ? Formatting.GREEN : Formatting.GRAY));
		}
	}
}
