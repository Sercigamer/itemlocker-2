package com.itemlocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.itemlocker.lock.PlacementGuard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

/**
 * Faengt den Tausch in die Zweithand ab.
 *
 * <p>Die Taste dafuer laeuft nicht ueber den Interaktions-Manager, sondern
 * schickt direkt ein Paket. Der Client sagt dabei nichts voraus - das Vertauschen
 * macht allein der Server. Ein verworfenes Paket kann also keinen Desync
 * ausloesen.
 */
@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {
	@Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardOffhandSwap(Packet<?> packet, CallbackInfo ci) {
		if (!(packet instanceof PlayerActionC2SPacket action)
				|| action.getAction() != PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
			return;
		}

		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (player != null && PlacementGuard.blockOffhandSwap(player)) {
			ci.cancel();
		}
	}
}
