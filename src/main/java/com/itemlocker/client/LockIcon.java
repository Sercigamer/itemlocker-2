package com.itemlocker.client;

import com.itemlocker.lock.LockManager;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * Das kleine Schloss, das gesperrte Sachen markiert - ueber der Hotbar wie im
 * Inventar. Bewusst aus Rechtecken gezeichnet statt aus einer Textur, damit es
 * in jeder Ressourcenpaket-Kombination gleich aussieht.
 */
public final class LockIcon {
	/** Roter Ton: der Slot selbst ist gesperrt. */
	public static final int COLOR_SLOT = 0xFFFF5555;

	/** Blauer Ton: der Item-Typ ist gesperrt, egal wo er liegt. */
	public static final int COLOR_ITEM = 0xFF55AAFF;

	private static final int COLOR_OUTLINE = 0xC0000000;

	private LockIcon() {
	}

	/**
	 * Welche Farbe passt zu diesem Slot - oder {@code 0}, wenn nichts gesperrt ist.
	 *
	 * @param hotbarSlot Hotbar-Index 0-8, sonst ein negativer Wert.
	 */
	public static int colorFor(int hotbarSlot, ItemStack stack) {
		if (LockManager.isSlotLocked(hotbarSlot)) {
			return COLOR_SLOT;
		}

		if (LockManager.isItemLocked(stack)) {
			return COLOR_ITEM;
		}

		return 0;
	}

	/** Zeichnet ein 8x9-Schloss mit der oberen linken Ecke bei (x, y). */
	public static void draw(GuiGraphicsExtractor context, int x, int y, int color) {
		// Umriss, damit das Schloss auch auf hellen Items lesbar bleibt.
		extractor.fill(x - 1, y - 1, x + 7, y + 8, COLOR_OUTLINE);

		// Buegel
		extractor.fill(x + 1, y, x + 5, y + 1, color);
		extractor.fill(x, y + 1, x + 1, y + 3, color);
		extractor.fill(x + 5, y + 1, x + 6, y + 3, color);

		// Koerper
		extractor.fill(x, y + 3, x + 6, y + 7, color);

		// Schluesselloch
		extractor.fill(x + 2, y + 4, x + 4, y + 6, COLOR_OUTLINE);
	}
}
