package com.itemlocker.client;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;
import com.itemlocker.lock.DropGuard;
import com.itemlocker.lock.LockManager;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Zeichnet ein kleines Schloss auf gesperrte Hotbar-Slots und einen
 * Fortschrittsbalken, solange man an einem gesperrten Item "zieht".
 */
public final class LockHudElement implements HudElement {
	/** Breite der Vanilla-Hotbar in GUI-Pixeln. */
	private static final int HOTBAR_WIDTH = 182;
	private static final int SLOT_SIZE = 20;

	private static final int COLOR_SLOT_LOCK = 0xFFFF5555;
	private static final int COLOR_ITEM_LOCK = 0xFF55AAFF;
	private static final int COLOR_OUTLINE = 0xC0000000;
	private static final int COLOR_PROGRESS_BG = 0x90000000;
	private static final int COLOR_PROGRESS_FG = 0xFFFFAA00;

	@Override
	public void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
		LockerConfig config = ConfigManager.get();

		if (!config.enabled || !config.showHudIcons) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;

		if (player == null || player.isSpectator()) {
			return;
		}

		int hotbarLeft = context.guiWidth() / 2 - HOTBAR_WIDTH / 2;
		int hotbarTop = context.guiHeight() - 22;

		for (int slot = 0; slot < LockManager.HOTBAR_SIZE; slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			boolean slotLocked = LockManager.isSlotLocked(slot);
			boolean itemLocked = LockManager.isItemLocked(stack);

			if (!slotLocked && !itemLocked) {
				continue;
			}

			int slotLeft = hotbarLeft + 3 + slot * SLOT_SIZE;
			int color = slotLocked ? LockIcon.COLOR_SLOT : LockIcon.COLOR_ITEM;

			LockIcon.draw(context, slotLeft + 11, hotbarTop + 2, color);

			int attempts = DropGuard.attemptsForHotbar(slot, stack);

			if (attempts > 0) {
				drawProgress(context, slotLeft, hotbarTop + 18, attempts, config.requiredDrops);
			}
		}
	}

	/** Winziges 6x7-Schloss aus Rechtecken - kein Texture-Asset noetig. */
	private void drawPadlock(GuiGraphicsExtractor context, int x, int y, int color) {
		// Schatten/Umriss, damit das Schloss auf hellen Items lesbar bleibt.
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

	private void drawProgress(GuiGraphicsExtractor context, int slotLeft, int y, int attempts, int required) {
		int width = 16;
		int done = Math.min(width, Math.round(width * (attempts / (float) Math.max(1, required))));

		extractor.fill(slotLeft, y, slotLeft + width, y + 2, COLOR_PROGRESS_BG);
		extractor.fill(slotLeft, y, slotLeft + done, y + 2, COLOR_PROGRESS_FG);
	}
}
