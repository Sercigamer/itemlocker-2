package com.itemlocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.itemlocker.client.LockIcon;
import com.itemlocker.config.ConfigManager;
import com.itemlocker.lock.LockManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Zeichnet das Schloss auch im offenen Inventar auf gesperrte Slots und Items -
 * damit man dort auf einen Blick sieht, was geschuetzt ist, und nicht erst beim
 * Drop-Versuch.
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
	// Achtung: die beiden int-Parameter sind die Mausposition, nicht die des
	// Slots. Vanilla positioniert ueber slot.x / slot.y - hier genauso.
	@Inject(method = "drawSlot", at = @At("TAIL"))
	private void itemlocker$drawLockIcon(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
		if (!ConfigManager.get().enabled || !ConfigManager.get().showHudIcons) {
			return;
		}

		ItemStack stack = slot.getStack();

		if (stack.isEmpty()) {
			return;
		}

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		int color = LockIcon.colorFor(player == null ? -1 : hotbarIndexOf(slot, player), stack);

		if (color != 0) {
			// Obere linke Ecke des 16x16-Feldes, damit die Stapelzahl unten
			// rechts frei bleibt.
			LockIcon.draw(context, slot.x + 1, slot.y + 1, color);
		}
	}

	/** Wie in DropGuard: erst ueber den Slot-Index, sonst ueber Objektidentitaet. */
	private static int hotbarIndexOf(Slot slot, ClientPlayerEntity player) {
		PlayerInventory inventory = player.getInventory();

		if (slot.inventory == inventory && LockManager.isValidHotbarSlot(slot.getIndex())) {
			return slot.getIndex();
		}

		ItemStack stack = slot.getStack();

		for (int index = 0; index < LockManager.HOTBAR_SIZE; index++) {
			if (inventory.getStack(index) == stack) {
				return index;
			}
		}

		return -1;
	}
}
