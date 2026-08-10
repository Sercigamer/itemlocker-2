package com.itemlocker.lock;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Schuetzt gesperrte Sachen davor, aus der Hand in die Welt zu wandern, ohne
 * dass ein Drop stattfindet.
 *
 * <p>Ein Ruestungsstaender nimmt das Item beim Rechtsklick sofort entgegen -
 * kein Drop, keine Warnung, weg ist es. Deshalb wird hier hart blockiert statt
 * mitgezaehlt: Es gibt keinen Grund, ein gesperrtes Item mehrfach anlegen zu
 * wollen.
 */
public final class PlacementGuard {
	private PlacementGuard() {
	}

	/**
	 * @return {@code true}, wenn die Interaktion verworfen werden soll.
	 */
	public static boolean blockArmorStandEquip(PlayerEntity player, Entity entity, Hand hand) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || !config.protectArmorStands || player == null || hand == null) {
			return false;
		}

		if (!(entity instanceof ArmorStandEntity)) {
			return false;
		}

		ItemStack stack = player.getStackInHand(hand);

		// Mit leerer Hand nimmt man Sachen vom Staender ab - das bleibt erlaubt.
		if (stack.isEmpty()) {
			return false;
		}

		if (!isLocked(player, stack, hand)) {
			return false;
		}

		Feedback.armorStandBlocked(stack);
		return true;
	}

	private static boolean isLocked(PlayerEntity player, ItemStack stack, Hand hand) {
		if (LockManager.isItemLocked(stack)) {
			return true;
		}

		// Die Slot-Sperre gilt nur fuer die Haupthand - nur die kommt aus der Hotbar.
		return hand == Hand.MAIN_HAND
				&& LockManager.isSlotLocked(player.getInventory().getSelectedSlot());
	}
}
