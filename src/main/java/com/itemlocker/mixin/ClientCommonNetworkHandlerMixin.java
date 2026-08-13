package com.itemlocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.itemlocker.lock.PlacementGuard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

/**
 * Faengt den Tausch in die Zweithand ab.
 *
 * <p>Die Taste dafuer laeuft nicht ueber den Interaktions-Manager, sondern
 * schickt direkt ein Paket. Der Client sagt dabei nichts voraus - das Vertauschen
 * macht allein der Server. Ein verworfenes Paket kann also keinen Desync
 * ausloesen.
 */
@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonNetworkHandlerMixin {
	@Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
	private void itemlocker$guardOffhandSwap(Packet<?> packet, CallbackInfo ci) {
		if (!(packet instanceof ServerboundPlayerActionPacket action)
				|| action.getAction() != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
			return;
		}

		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null && PlacementGuard.blockOffhandSwap(player)) {
			ci.cancel();
		}
	}
}
