package com.itemlocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.itemlocker.lock.DropGuard;
import com.itemlocker.lock.PlacementGuard;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Faengt Drops aus einem offenen Screen ab: Q auf einem Slot und das
 * Fallenlassen des Stacks am Cursor ausserhalb des Fensters.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {
	@Inject(method = "handleContainerInput", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardSlotClick(int syncId, int slotId, int button, ContainerInput actionType,
			Player player, CallbackInfo ci) {
		if (DropGuard.blockSlotClick(slotId, button, actionType, player)) {
			ci.cancel();
		}
	}

	/**
	 * Rechtsklick auf einen Ruestungsstaender. Minecraft probiert erst die
	 * Variante mit Trefferpunkt und faellt dann auf die einfache zurueck -
	 * deshalb muessen beide abgesichert sein.
	 */
	@Inject(method = "interactEntityAtLocation", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardEntityUseAtLocation(Player player, Entity entity, EntityHitResult hitResult,
			InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (PlacementGuard.blockArmorStandEquip(player, entity, hand)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardEntityUse(Player player, Entity entity, InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (PlacementGuard.blockArmorStandEquip(player, entity, hand)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	/** Deko-Toepfe und gesperrte Block-Oberflaechen. */
	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardBlockUse(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (PlacementGuard.blockBlockInteraction(player, hand, hitResult)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}
