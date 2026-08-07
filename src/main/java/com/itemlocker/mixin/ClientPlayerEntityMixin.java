package com.itemlocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.itemlocker.lock.DropGuard;

import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Faengt das Droppen aus der Hand ab (Q und Strg+Q), bevor das Paket zum Server
 * geht und bevor der Client den Stack lokal entfernt.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
	@Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardHotbarDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
		ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

		if (DropGuard.blockHotbarDrop(player)) {
			cir.setReturnValue(false);
		}
	}
}
