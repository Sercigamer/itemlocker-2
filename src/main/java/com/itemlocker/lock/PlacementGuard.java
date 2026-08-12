package com.itemlocker.lock;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DecoratedPotBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Schuetzt gesperrte Sachen vor Wegen, auf denen sie ohne Drop verschwinden -
 * und den Spieler vor Oberflaechen, die er im Kampf nicht aufmachen will.
 *
 * <p>Hier wird nicht mitgezaehlt, sondern hart blockiert: Ein einziger
 * Rechtsklick genuegt jeweils, und das Item ist weg bzw. das Fenster offen. Es
 * gibt keinen Grund, das mehrfach zu wollen.
 */
public final class PlacementGuard {
	private PlacementGuard() {
	}

	/**
	 * Rechtsklick auf eine Entity.
	 *
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
		if (stack.isEmpty() || !isLocked(player, stack, hand)) {
			return false;
		}

		Feedback.armorStandBlocked(stack);
		return true;
	}

	/**
	 * Rechtsklick auf einen Block. Deckt zwei Faelle ab: gesperrte Sachen in
	 * einen Deko-Topf stecken und das versehentliche Oeffnen gesperrter
	 * Oberflaechen.
	 *
	 * @return {@code true}, wenn die Interaktion verworfen werden soll.
	 */
	public static boolean blockBlockInteraction(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || player == null || hand == null || hitResult == null) {
			return false;
		}

		World world = player.getEntityWorld();

		if (world == null) {
			return false;
		}

		BlockPos pos = hitResult.getBlockPos();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (config.protectDecoratedPots && block instanceof DecoratedPotBlock) {
			ItemStack stack = player.getStackInHand(hand);

			if (!stack.isEmpty() && isLocked(player, stack, hand)) {
				Feedback.potBlocked(stack);
				return true;
			}
		}

		if (isBlockLocked(block)) {
			// Schleichen ist der bewusste Weg dran - im Kampf drueckt das keiner
			// aus Versehen.
			if (config.blockGuiSneakBypass && isSneaking(player)) {
				return false;
			}

			Feedback.blockGuiBlocked(block.getName(), config.blockGuiSneakBypass);
			return true;
		}

		return false;
	}

	/**
	 * Tausch zwischen Haupt- und Zweithand.
	 *
	 * @return {@code true}, wenn der Tausch verworfen werden soll.
	 */
	public static boolean blockOffhandSwap(PlayerEntity player) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || !config.preventOffhandSwap || player == null) {
			return false;
		}

		PlayerInventory inventory = player.getInventory();
		ItemStack mainHand = inventory.getSelectedStack();
		ItemStack offHand = player.getOffHandStack();

		// Beide Richtungen: rein wie raus.
		boolean locked = LockManager.isItemLocked(mainHand)
				|| LockManager.isItemLocked(offHand)
				|| (!mainHand.isEmpty() && LockManager.isSlotLocked(inventory.getSelectedSlot()));

		if (!locked) {
			return false;
		}

		Feedback.offhandBlocked();
		return true;
	}

	/**
	 * Schleicht der Spieler gerade?
	 *
	 * <p>Neben dem Zustand der Spielfigur wird auch die Taste selbst geprueft:
	 * Beim Rechtsklick zaehlt, was der Spieler in diesem Moment gedrueckt haelt,
	 * und der Zustand der Figur hinkt je nach Umschalt-Einstellung hinterher.
	 */
	private static boolean isSneaking(ClientPlayerEntity player) {
		if (player.isSneaking() || player.shouldCancelInteraction()) {
			return true;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		return client.options != null && client.options.sneakKey.isPressed();
	}

	public static boolean isBlockLocked(Block block) {
		LockerConfig config = ConfigManager.get();

		if (config.lockedBlocks.isEmpty()) {
			return false;
		}

		return config.lockedBlocks.contains(blockId(block));
	}

	public static String blockId(Block block) {
		return Registries.BLOCK.getId(block).toString();
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
