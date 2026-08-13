package com.itemlocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.itemlocker.client.LockIcon;
import com.itemlocker.config.ConfigManager;
import com.itemlocker.lock.LockManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Zeichnet das Schloss auch im offenen Inventar auf gesperrte Slots und Items.
 *
 * <p>Ab 26.x gibt es kein {@code drawSlot} mehr, an das man sich pro Slot
 * haengen koennte. Stattdessen wird einmal am Ende von {@code extractSlots}
 * ueber alle Slots gelaufen - {@code Slot.x}/{@code Slot.y} liefern weiterhin
 * die Position.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {
	@Inject(method = "extractSlots", at = @At("TAIL"))
	private void itemlocker$drawLockIcon(GuiGraphicsExtractor extractor, int mouseX, int mouseY, CallbackInfo ci) {
		if (!ConfigManager.get().enabled || !ConfigManager.get().showHudIcons) {
			return;
		}

		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null) {
			return;
		}

		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

		for (Slot slot : screen.getMenu().slots) {
			ItemStack stack = slot.getItem();

			if (stack.isEmpty()) {
				continue;
			}

			int color = LockIcon.colorFor(hotbarIndexOf(slot, player), stack);

			if (color != 0) {
				// Obere linke Ecke des 16x16-Feldes, damit die Stapelzahl unten
				// rechts frei bleibt.
				LockIcon.draw(extractor, slot.x + 1, slot.y + 1, color);
			}
		}
	}

	/** Wie in DropGuard: erst ueber den Slot-Index, sonst ueber Objektidentitaet. */
	private static int hotbarIndexOf(Slot slot, LocalPlayer player) {
		Inventory inventory = player.getInventory();

		if (slot.container == inventory && LockManager.isValidHotbarSlot(slot.getContainerSlot())) {
			return slot.getContainerSlot();
		}

		ItemStack stack = slot.getItem();

		for (int index = 0; index < LockManager.HOTBAR_SIZE; index++) {
			if (inventory.getItem(index) == stack) {
				return index;
			}
		}

		return -1;
	}
}
