package com.itemlocker.lock;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Beantwortet die Frage "ist das hier gesperrt?" und verwaltet die Sperrliste.
 */
public final class LockManager {
	public static final int HOTBAR_SIZE = 9;

	private LockManager() {
	}

	public static String itemId(ItemStack stack) {
		Identifier id = Registries.ITEM.getId(stack.getItem());
		return id.toString();
	}

	public static boolean isValidHotbarSlot(int slot) {
		return slot >= 0 && slot < HOTBAR_SIZE;
	}

	public static boolean isSlotLocked(int hotbarSlot) {
		return isValidHotbarSlot(hotbarSlot) && ConfigManager.get().lockedSlots.contains(hotbarSlot);
	}

	public static boolean isItemLocked(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}

		return ConfigManager.get().lockedItems.contains(itemId(stack));
	}

	/**
	 * @param hotbarSlot Hotbar-Index 0-8, oder ein negativer Wert wenn der Stack
	 *                   nicht in der Hotbar liegt.
	 */
	public static boolean isProtected(int hotbarSlot, ItemStack stack) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || stack == null || stack.isEmpty()) {
			return false;
		}

		return isSlotLocked(hotbarSlot) || isItemLocked(stack);
	}

	/** @return {@code true}, wenn der Slot jetzt gesperrt ist. */
	public static boolean toggleSlot(int hotbarSlot) {
		LockerConfig config = ConfigManager.get();
		boolean nowLocked;

		if (config.lockedSlots.contains(hotbarSlot)) {
			config.lockedSlots.remove(hotbarSlot);
			nowLocked = false;
		} else {
			config.lockedSlots.add(hotbarSlot);
			nowLocked = true;
		}

		ConfigManager.save();
		return nowLocked;
	}

	public static boolean setSlotLocked(int hotbarSlot, boolean locked) {
		LockerConfig config = ConfigManager.get();
		boolean changed = locked ? config.lockedSlots.add(hotbarSlot) : config.lockedSlots.remove(hotbarSlot);

		if (changed) {
			ConfigManager.save();
		}

		return changed;
	}

	/** @return {@code true}, wenn der Item-Typ jetzt gesperrt ist. */
	public static boolean toggleItem(String itemId) {
		LockerConfig config = ConfigManager.get();
		boolean nowLocked;

		if (config.lockedItems.contains(itemId)) {
			config.lockedItems.remove(itemId);
			nowLocked = false;
		} else {
			config.lockedItems.add(itemId);
			nowLocked = true;
		}

		ConfigManager.save();
		return nowLocked;
	}

	public static boolean setItemLocked(String itemId, boolean locked) {
		LockerConfig config = ConfigManager.get();
		boolean changed = locked ? config.lockedItems.add(itemId) : config.lockedItems.remove(itemId);

		if (changed) {
			ConfigManager.save();
		}

		return changed;
	}

	public static void clearAll() {
		LockerConfig config = ConfigManager.get();
		config.lockedSlots.clear();
		config.lockedItems.clear();
		ConfigManager.save();
	}
}
