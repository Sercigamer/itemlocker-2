package com.itemlocker.client;

import com.itemlocker.ItemLocker;
import com.itemlocker.config.ConfigManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public final class ItemLockerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ConfigManager.load();
		ItemLockerKeybinds.register();
		ItemLockerCommands.register();

		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ItemLocker.id("lock_overlay"),
				new LockHudElement());

		MixinSelfTest.runIfRequested();

		ItemLocker.LOGGER.info("ItemLocker bereit - {} gesperrte Slots, {} gesperrte Items, {} Drops noetig",
				ConfigManager.get().lockedSlots.size(),
				ConfigManager.get().lockedItems.size(),
				ConfigManager.get().requiredDrops);
	}
}
