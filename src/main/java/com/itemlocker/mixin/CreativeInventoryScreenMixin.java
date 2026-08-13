package com.itemlocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.itemlocker.lock.DropGuard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;

/**
 * Faengt Drops im Kreativ-Inventar ab, bevor der Bildschirm den Slot anfasst.
 *
 * <p>Der Kreativ-Bildschirm raeumt beim Wegwerfen erst den Slot leer und
 * schickt danach das Paket. Ein spaeterer Eingriff verhindert zwar das Fallen,
 * laesst das Item aber aus dem Inventar verschwinden - deshalb sitzt der Haken
 * ganz am Anfang.
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {
	@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardCreativeClick(Slot slot, int slotId, int button, ContainerInput actionType,
			CallbackInfo ci) {
		if (DropGuard.blockCreativeScreenClick(slot, actionType, Minecraft.getInstance().player)) {
			ci.cancel();
		}
	}
}
