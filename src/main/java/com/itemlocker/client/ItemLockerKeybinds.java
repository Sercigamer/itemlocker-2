package com.itemlocker.client;

import org.lwjgl.glfw.GLFW;

import com.itemlocker.client.screen.ItemLockerConfigScreen;
import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;
import com.itemlocker.lock.DropGuard;
import com.itemlocker.lock.Feedback;
import com.itemlocker.lock.LockManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Tastenbelegungen. Standard: L sperrt den aktuellen Hotbar-Slot, K den
 * Item-Typ in der Hand.
 */
public final class ItemLockerKeybinds {
	public static KeyMapping toggleSlot;
	public static KeyMapping toggleItem;
	public static KeyMapping toggleMod;
	public static KeyMapping openConfig;

	/**
	 * Ein Screen darf nicht mitten aus einem Command heraus geoeffnet werden -
	 * der schliessende Chat wuerde ihn sofort wieder wegraeumen. Also merken und
	 * im naechsten Tick oeffnen, wenn kein anderer Screen offen ist.
	 */
	private static boolean configScreenRequested;

	private ItemLockerKeybinds() {
	}

	public static void requestConfigScreen() {
		configScreenRequested = true;
	}

	public static void register() {
		toggleSlot = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.itemlocker.toggle_slot", GLFW.GLFW_KEY_L, KeyMapping.Category.INVENTORY));

		toggleItem = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.itemlocker.toggle_item", GLFW.GLFW_KEY_K, KeyMapping.Category.INVENTORY));

		toggleMod = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.itemlocker.toggle_mod", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.INVENTORY));

		openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.itemlocker.open_config", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.INVENTORY));

		ClientTickEvents.END_CLIENT_TICK.register(ItemLockerKeybinds::onTick);
	}

	private static void onTick(Minecraft client) {
		while (openConfig.consumeClick()) {
			requestConfigScreen();
		}

		if (configScreenRequested && client.screen == null) {
			configScreenRequested = false;
			client.setScreen(new ItemLockerConfigScreen(null));
		}

		LocalPlayer player = client.player;

		if (player == null) {
			return;
		}

		while (toggleSlot.consumeClick()) {
			int slot = player.getInventory().getSelectedSlot();
			boolean locked = LockManager.toggleSlot(slot);
			DropGuard.resetCounter();

			Feedback.info(Component.translatable(locked ? "itemlocker.message.slot_locked" : "itemlocker.message.slot_unlocked",
					slot + 1).withStyle(locked ? ChatFormatting.RED : ChatFormatting.GREEN));
		}

		while (toggleItem.consumeClick()) {
			ItemStack stack = player.getInventory().getItem(player.getInventory().getSelectedSlot());

			if (stack.isEmpty()) {
				Feedback.info(Component.translatable("itemlocker.message.no_item").withStyle(ChatFormatting.RED));
				continue;
			}

			String id = LockManager.itemId(stack);
			boolean locked = LockManager.toggleItem(id);
			DropGuard.resetCounter();

			Feedback.info(Component.translatable(locked ? "itemlocker.message.item_locked" : "itemlocker.message.item_unlocked",
					stack.getHoverName().copy().withStyle(ChatFormatting.WHITE), id)
					.withStyle(locked ? ChatFormatting.RED : ChatFormatting.GREEN));
		}

		while (toggleMod.consumeClick()) {
			LockerConfig config = ConfigManager.get();
			config.enabled = !config.enabled;
			ConfigManager.save();
			DropGuard.resetCounter();

			Feedback.info(Component.translatable(config.enabled ? "itemlocker.message.enabled" : "itemlocker.message.disabled")
					.withStyle(config.enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY));
		}
	}
}
