package com.itemlocker.lock;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Die eigentliche Entscheidung: darf dieser Drop durch oder nicht?
 *
 * <p>Wird von den Mixins aufgerufen. Alles hier laeuft rein clientseitig - der
 * Server sieht einen geblockten Drop nie, weil das Paket gar nicht erst
 * rausgeht.
 */
public final class DropGuard {
	private static final DropAttemptTracker TRACKER = new DropAttemptTracker();

	private static final String CONTEXT_HOTBAR = "hotbar";
	private static final String CONTEXT_SCREEN = "screen";
	private static final String CONTEXT_CURSOR = "cursor";
	private static final String CONTEXT_CREATIVE = "creative";

	private DropGuard() {
	}

	/**
	 * Q / Strg+Q auf das Item in der Hand.
	 *
	 * @return {@code true}, wenn der Drop geschluckt werden soll.
	 */
	public static boolean blockHotbarDrop(ClientPlayerEntity player) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || player == null) {
			return false;
		}

		PlayerInventory inventory = player.getInventory();

		return blockDrop(CONTEXT_HOTBAR, inventory.getSelectedSlot(), inventory.getSelectedStack());
	}

	/**
	 * Klicks in einem offenen Inventar-/Kisten-Screen.
	 *
	 * @return {@code true}, wenn der Klick verworfen werden soll.
	 */
	public static boolean blockSlotClick(int slotId, int button, SlotActionType actionType, PlayerEntity player) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || !config.guardInventoryScreens || player == null) {
			return false;
		}

		ScreenHandler handler = player.currentScreenHandler;

		if (handler == null) {
			return false;
		}

		// Stack am Cursor ausserhalb des Fensters fallen lassen.
		if (slotId == ScreenHandler.EMPTY_SPACE_SLOT_INDEX
				&& (actionType == SlotActionType.PICKUP || actionType == SlotActionType.THROW)) {
			return blockDrop(CONTEXT_CURSOR, -1, handler.getCursorStack());
		}

		// Zahlentaste: der gesperrte Hotbar-Slot steckt in button, nicht in
		// slotId - der zeigt auf den Slot unter dem Mauszeiger.
		// Einen leeren gesperrten Slot darf man befuellen, nur leeren nicht.
		if (actionType == SlotActionType.SWAP
				&& config.preventTakingFromLockedSlots
				&& LockManager.isSlotLocked(button)
				&& !player.getInventory().getStack(button).isEmpty()) {
			Feedback.slotFrozen(button);
			return true;
		}

		if (slotId < 0 || slotId >= handler.slots.size()) {
			return false;
		}

		Slot slot = handler.getSlot(slotId);
		ItemStack stack = slot.getStack();

		if (stack.isEmpty()) {
			return false;
		}

		int hotbarSlot = hotbarIndexOf(slot, player);

		// Q auf einen Slot im offenen Inventar.
		if (actionType == SlotActionType.THROW) {
			return blockDrop(CONTEXT_SCREEN, hotbarSlot, stack);
		}

		// Inhalt eines gesperrten Slots festhalten. Ohne das koennte man ihn
		// aufnehmen und dann ausserhalb des Fensters fallen lassen - dort ist
		// er nicht mehr im gesperrten Slot und waere ungeschuetzt.
		if (config.preventTakingFromLockedSlots
				&& LockManager.isSlotLocked(hotbarSlot)
				&& isTakeAction(actionType)) {
			Feedback.slotFrozen(hotbarSlot);
			return true;
		}

		return false;
	}

	/**
	 * Droppen aus dem Kreativ-Inventar.
	 *
	 * <p>Das Kreativ-Inventar geht nicht ueber {@code clickSlot}, sondern ruft
	 * {@code dropCreativeStack} auf. Ohne diesen Weg fiel ein gesperrtes Item im
	 * Kreativmodus beim ersten Versuch.
	 *
	 * <p>Welcher Slot dahintersteckt, ist hier nicht mehr bekannt - deshalb kann
	 * nur die Item-Sperre greifen, nicht die Slot-Sperre.
	 */
	public static boolean blockCreativeDrop(ItemStack stack) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || !config.guardInventoryScreens) {
			return false;
		}

		return blockDrop(CONTEXT_CREATIVE, -1, stack);
	}

	/**
	 * Wie viele Drop-Versuche fuer diesen Hotbar-Slot schon gezaehlt sind.
	 * Nur fuer die HUD-Anzeige.
	 */
	public static int attemptsForHotbar(int hotbarSlot, ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}

		LockerConfig config = ConfigManager.get();
		return TRACKER.attemptsFor(key(CONTEXT_HOTBAR, hotbarSlot, stack), config.resetAfterMillis);
	}

	public static void resetCounter() {
		TRACKER.reset();
	}

	private static boolean blockDrop(String context, int hotbarSlot, ItemStack stack) {
		if (stack == null || stack.isEmpty() || !LockManager.isProtected(hotbarSlot, stack)) {
			return false;
		}

		LockerConfig config = ConfigManager.get();
		int remaining = TRACKER.attempt(key(context, hotbarSlot, stack), config.requiredDrops, config.resetAfterMillis);

		if (remaining <= 0) {
			Feedback.dropAllowed(stack);
			return false;
		}

		Feedback.dropBlocked(stack, remaining, config.requiredDrops);
		return true;
	}

	private static String key(String context, int hotbarSlot, ItemStack stack) {
		return context + '#' + hotbarSlot + '#' + LockManager.itemId(stack);
	}

	private static int hotbarIndexOf(Slot slot, PlayerEntity player) {
		PlayerInventory inventory = player.getInventory();

		// Normalfall: der Slot zeigt direkt auf das Spieler-Inventar.
		if (slot.inventory == inventory && LockManager.isValidHotbarSlot(slot.getIndex())) {
			return slot.getIndex();
		}

		// Manche Screens (z.B. das Kreativ-Inventar) schieben eigene Slot-Typen
		// dazwischen. Dann hilft die Objektidentitaet des Stacks weiter.
		ItemStack stack = slot.getStack();

		if (!stack.isEmpty()) {
			for (int index = 0; index < LockManager.HOTBAR_SIZE; index++) {
				if (inventory.getStack(index) == stack) {
					return index;
				}
			}
		}

		return -1;
	}

	private static boolean isTakeAction(SlotActionType actionType) {
		return actionType == SlotActionType.PICKUP
				|| actionType == SlotActionType.QUICK_MOVE
				|| actionType == SlotActionType.SWAP
				|| actionType == SlotActionType.PICKUP_ALL;
	}
}
