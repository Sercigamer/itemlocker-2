package com.itemlocker.lock;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
	public static boolean blockArmorStandEquip(Player player, Entity entity, InteractionHand hand) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || !config.protectArmorStands || player == null || hand == null) {
			return false;
		}

		if (!(entity instanceof ArmorStand)) {
			return false;
		}

		ItemStack stack = player.getItemInHand(hand);

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
	public static boolean blockBlockInteraction(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || player == null || hand == null || hitResult == null) {
			return false;
		}

		Level world = player.level();

		if (world == null) {
			return false;
		}

		BlockPos pos = hitResult.getBlockPos();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (config.protectDecoratedPots && block instanceof DecoratedPotBlock) {
			ItemStack stack = player.getItemInHand(hand);

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
	public static boolean blockOffhandSwap(Player player) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || !config.preventOffhandSwap || player == null) {
			return false;
		}

		Inventory inventory = player.getInventory();
		ItemStack mainHand = inventory.getSelectedItem();
		ItemStack offHand = player.getOffhandItem();

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
	private static boolean isSneaking(LocalPlayer player) {
		if (player.isShiftKeyDown() || player.isSecondaryUseActive()) {
			return true;
		}

		Minecraft client = Minecraft.getInstance();
		return client.options != null && client.options.keyShift.isDown();
	}

	public static boolean isBlockLocked(Block block) {
		LockerConfig config = ConfigManager.get();

		if (config.lockedBlocks.isEmpty()) {
			return false;
		}

		return config.lockedBlocks.contains(blockId(block));
	}

	public static String blockId(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block).toString();
	}

	private static boolean isLocked(Player player, ItemStack stack, InteractionHand hand) {
		if (LockManager.isItemLocked(stack)) {
			return true;
		}

		// Die Slot-Sperre gilt nur fuer die Haupthand - nur die kommt aus der Hotbar.
		return hand == InteractionHand.MAIN_HAND
				&& LockManager.isSlotLocked(player.getInventory().getSelectedSlot());
	}
}
