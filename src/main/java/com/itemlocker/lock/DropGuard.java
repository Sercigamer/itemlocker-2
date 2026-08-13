package com.itemlocker.lock;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;

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
	public static boolean blockHotbarDrop(LocalPlayer player) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || player == null) {
			return false;
		}

		Inventory inventory = player.getInventory();

		return blockDrop(CONTEXT_HOTBAR, inventory.getSelectedSlot(), inventory.getSelectedItem());
	}

	/**
	 * Klicks in einem offenen Inventar-/Kisten-Screen.
	 *
	 * @return {@code true}, wenn der Klick verworfen werden soll.
	 */
	public static boolean blockSlotClick(int slotId, int button, ContainerInput actionType, Player player) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || !config.guardInventoryScreens || player == null) {
			return false;
		}

		AbstractContainerMenu handler = player.containerMenu;

		if (handler == null) {
			return false;
		}

		// Stack am Cursor ausserhalb des Fensters fallen lassen.
		if (slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE
				&& (actionType == ContainerInput.PICKUP || actionType == ContainerInput.THROW)) {
			return blockDrop(CONTEXT_CURSOR, -1, handler.getCarried());
		}

		// Zahlentaste: der gesperrte Hotbar-Slot steckt in button, nicht in
		// slotId - der zeigt auf den Slot unter dem Mauszeiger.
		// Einen leeren gesperrten Slot darf man befuellen, nur leeren nicht.
		if (actionType == ContainerInput.SWAP
				&& config.preventTakingFromLockedSlots
				&& LockManager.isSlotLocked(button)
				&& !player.getInventory().getItem(button).isEmpty()) {
			Feedback.slotFrozen(button);
			return true;
		}

		if (slotId < 0 || slotId >= handler.slots.size()) {
			return false;
		}

		Slot slot = handler.getSlot(slotId);
		ItemStack stack = slot.getItem();

		if (stack.isEmpty()) {
			return false;
		}

		int hotbarSlot = hotbarIndexOf(slot, player);

		// Q auf einen Slot im offenen Inventar.
		if (actionType == ContainerInput.THROW) {
			return blockDrop(CONTEXT_SCREEN, hotbarSlot, stack);
		}

		// Der zweite Weg in die Zweithand: Zweithand-Taste im offenen Inventar.
		if (actionType == ContainerInput.SWAP
				&& button == Inventory.SLOT_OFFHAND
				&& config.preventOffhandSwap
				&& (LockManager.isItemLocked(stack) || LockManager.isItemLocked(player.getOffhandItem()))) {
			Feedback.offhandBlocked();
			return true;
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
	 * Klick im Kreativ-Inventar.
	 *
	 * <p>Muss ganz am Anfang von {@code onMouseClick} greifen: Der Bildschirm
	 * raeumt den Slot leer und ruft <em>erst danach</em> {@code dropCreativeStack}.
	 * Wer nur den Drop abfaengt, verhindert zwar das Fallen - das Item ist aber
	 * lokal schon aus dem Slot verschwunden.
	 *
	 * <p>Welcher Hotbar-Slot dahintersteckt, laesst sich hier nicht zuordnen -
	 * es greift also nur die Item-Sperre.
	 */
	public static boolean blockCreativeScreenClick(Slot slot, ContainerInput actionType, Player player) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || !config.guardInventoryScreens || player == null) {
			return false;
		}

		// slot == null heisst: Klick neben das Fenster, der Stack am Cursor faellt.
		if (slot == null) {
			AbstractContainerMenu handler = player.containerMenu;
			return handler != null && blockDrop(CONTEXT_CREATIVE, -1, handler.getCarried());
		}

		if (actionType == ContainerInput.THROW) {
			return blockDrop(CONTEXT_CREATIVE, -1, slot.getItem());
		}

		return false;
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

	private static int hotbarIndexOf(Slot slot, Player player) {
		Inventory inventory = player.getInventory();

		// Normalfall: der Slot zeigt direkt auf das Spieler-Inventar.
		if (slot.container == inventory && LockManager.isValidHotbarSlot(slot.getContainerSlot())) {
			return slot.getContainerSlot();
		}

		// Manche Screens (z.B. das Kreativ-Inventar) schieben eigene Slot-Typen
		// dazwischen. Dann hilft die Objektidentitaet des Stacks weiter.
		ItemStack stack = slot.getItem();

		if (!stack.isEmpty()) {
			for (int index = 0; index < LockManager.HOTBAR_SIZE; index++) {
				if (inventory.getItem(index) == stack) {
					return index;
				}
			}
		}

		return -1;
	}

	private static boolean isTakeAction(ContainerInput actionType) {
		return actionType == ContainerInput.PICKUP
				|| actionType == ContainerInput.QUICK_MOVE
				|| actionType == ContainerInput.SWAP
				|| actionType == ContainerInput.PICKUP_ALL;
	}
}
