package com.itemlocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.itemlocker.lock.DropGuard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Faengt Drops im Kreativ-Inventar ab, bevor der Bildschirm den Slot anfasst.
 *
 * <p>Der Kreativ-Bildschirm raeumt beim Wegwerfen erst den Slot leer und
 * schickt danach das Paket. Ein spaeterer Eingriff verhindert zwar das Fallen,
 * laesst das Item aber aus dem Inventar verschwinden - deshalb sitzt der Haken
 * ganz am Anfang.
 */
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {
	@Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardCreativeClick(Slot slot, int slotId, int button, SlotActionType actionType,
			CallbackInfo ci) {
		if (DropGuard.blockCreativeScreenClick(slot, actionType, MinecraftClient.getInstance().player)) {
			ci.cancel();
		}
	}
}
