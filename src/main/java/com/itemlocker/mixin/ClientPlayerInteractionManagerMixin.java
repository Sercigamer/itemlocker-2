package com.itemlocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.itemlocker.lock.DropGuard;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Faengt Drops aus einem offenen Screen ab: Q auf einem Slot und das
 * Fallenlassen des Stacks am Cursor ausserhalb des Fensters.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
	@Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardSlotClick(int syncId, int slotId, int button, SlotActionType actionType,
			PlayerEntity player, CallbackInfo ci) {
		if (DropGuard.blockSlotClick(slotId, button, actionType, player)) {
			ci.cancel();
		}
	}

	/**
	 * Das Kreativ-Inventar dropped nicht ueber {@code clickSlot}, sondern hier.
	 * Ohne diesen Haken fiel ein gesperrtes Item im Kreativmodus sofort.
	 */
	@Inject(method = "dropCreativeStack", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardCreativeDrop(ItemStack stack, CallbackInfo ci) {
		if (DropGuard.blockCreativeDrop(stack)) {
			ci.cancel();
		}
	}
}
