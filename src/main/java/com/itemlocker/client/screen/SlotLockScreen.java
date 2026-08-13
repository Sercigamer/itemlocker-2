package com.itemlocker.client.screen;

import com.itemlocker.client.Draw;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.lock.DropGuard;
import com.itemlocker.lock.LockManager;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Hotbar-Slots per Klick sperren. Zeigt gleich mit an, was gerade drinliegt.
 */
public class SlotLockScreen extends Screen {
	private static final int CELL_SIZE = 26;
	private static final int CELL_GAP = 4;

	private static final int COLOR_CELL = 0x66000000;
	private static final int COLOR_CELL_LOCKED = 0x80FF5555;
	private static final int COLOR_CELL_HOVER = 0x66FFFFFF;
	private static final int COLOR_BORDER_LOCKED = 0xFFFF5555;

	private final Screen parent;

	public SlotLockScreen(Screen parent) {
		super(Component.translatable("itemlocker.config.slots.title"));
		this.parent = parent;
	}

	private int stripLeft() {
		int totalWidth = LockManager.HOTBAR_SIZE * CELL_SIZE + (LockManager.HOTBAR_SIZE - 1) * CELL_GAP;
		return this.width / 2 - totalWidth / 2;
	}

	private int stripTop() {
		return this.height / 2 - CELL_SIZE / 2 - 10;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;

		addRenderableWidget(Button
				.builder(Component.translatable("itemlocker.config.slots.lock_all"), button -> {
					for (int slot = 0; slot < LockManager.HOTBAR_SIZE; slot++) {
						LockManager.setSlotLocked(slot, true);
					}

					DropGuard.resetCounter();
				})
				.bounds(centerX - 155, this.height - 56, 150, 20).build());

		addRenderableWidget(Button
				.builder(Component.translatable("itemlocker.config.slots.unlock_all"), button -> {
					for (int slot = 0; slot < LockManager.HOTBAR_SIZE; slot++) {
						LockManager.setSlotLocked(slot, false);
					}

					DropGuard.resetCounter();
				})
				.bounds(centerX + 5, this.height - 56, 150, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
				.bounds(centerX - 100, this.height - 30, 200, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);

		Draw.centered(context, this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
		Draw.centered(context, this.font,
				Component.translatable("itemlocker.config.slots.hint").withStyle(ChatFormatting.GRAY),
				this.width / 2, 36, 0xFFAAAAAA);

		LocalPlayer player = this.minecraft == null ? null : this.minecraft.player;
		int left = stripLeft();
		int top = stripTop();

		for (int slot = 0; slot < LockManager.HOTBAR_SIZE; slot++) {
			int x = left + slot * (CELL_SIZE + CELL_GAP);
			boolean locked = LockManager.isSlotLocked(slot);
			boolean hovered = mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= top && mouseY < top + CELL_SIZE;

			context.fill(x, top, x + CELL_SIZE, top + CELL_SIZE, locked ? COLOR_CELL_LOCKED : COLOR_CELL);

			if (hovered) {
				context.fill(x, top, x + CELL_SIZE, top + CELL_SIZE, COLOR_CELL_HOVER);
			}

			if (locked) {
				drawBorder(context, x, top, CELL_SIZE, CELL_SIZE, COLOR_BORDER_LOCKED);
			}

			if (player != null) {
				ItemStack stack = player.getInventory().getItem(slot);

				if (!stack.isEmpty()) {
					context.item(stack, x + 5, top + 5);
				}
			}

			Component label = Component.literal(String.valueOf(slot + 1))
					.withStyle(locked ? ChatFormatting.RED : ChatFormatting.GRAY);
			Draw.centered(context, this.font, label, x + CELL_SIZE / 2, top + CELL_SIZE + 5,
					0xFFFFFFFF);
		}
	}

	private void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		int left = stripLeft();
		int top = stripTop();

		if (click.y() >= top && click.y() < top + CELL_SIZE) {
			for (int slot = 0; slot < LockManager.HOTBAR_SIZE; slot++) {
				int x = left + slot * (CELL_SIZE + CELL_GAP);

				if (click.x() >= x && click.x() < x + CELL_SIZE) {
					LockManager.toggleSlot(slot);
					DropGuard.resetCounter();
					return true;
				}
			}
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		this.minecraft.setScreenAndShow(this.parent);
	}
}
