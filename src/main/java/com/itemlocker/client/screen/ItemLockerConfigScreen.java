package com.itemlocker.client.screen;

import com.itemlocker.client.Draw;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;
import com.itemlocker.lock.DropGuard;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Hauptmenue der Mod. Erreichbar ueber Mod Menu, {@code /itemlocker config}
 * oder die Taste aus den Steuerungs-Einstellungen.
 */
public class ItemLockerConfigScreen extends Screen {
	private static final int ROW_HEIGHT = 24;
	private static final int COLUMN_WIDTH = 150;

	private final Screen parent;

	public ItemLockerConfigScreen(Screen parent) {
		super(Component.translatable("itemlocker.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		LockerConfig config = ConfigManager.get();

		int centerX = this.width / 2;
		int leftX = centerX - COLUMN_WIDTH - 5;
		int rightX = centerX + 5;
		int top = 36;

		// Linke Spalte
		addRenderableWidget(CycleButton.onOffBuilder(config.enabled)
				.create(leftX, top, COLUMN_WIDTH, 20, Component.translatable("itemlocker.config.enabled"),
						(button, value) -> {
							config.enabled = value;
							DropGuard.resetCounter();
							ConfigManager.save();
						}));

		addRenderableWidget(new IntSliderWidget(leftX, top + ROW_HEIGHT, COLUMN_WIDTH, 20,
				"itemlocker.config.required_drops", 1, 64, config.requiredDrops, value -> {
					config.requiredDrops = value;
					DropGuard.resetCounter();
					ConfigManager.save();
				}));

		addRenderableWidget(new IntSliderWidget(leftX, top + ROW_HEIGHT * 2, COLUMN_WIDTH, 20,
				"itemlocker.config.timeout", 1, 60, (int) (config.resetAfterMillis / 1000L), value -> {
					config.resetAfterMillis = value * 1000L;
					ConfigManager.save();
				}));

		addRenderableWidget(CycleButton.onOffBuilder(config.guardInventoryScreens)
				.create(leftX, top + ROW_HEIGHT * 3, COLUMN_WIDTH, 20,
						Component.translatable("itemlocker.config.guard_inventory"), (button, value) -> {
							config.guardInventoryScreens = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.preventTakingFromLockedSlots)
				.create(leftX, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20,
						Component.translatable("itemlocker.config.freeze_slots"), (button, value) -> {
							config.preventTakingFromLockedSlots = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.protectArmorStands)
				.create(leftX, top + ROW_HEIGHT * 5, COLUMN_WIDTH, 20,
						Component.translatable("itemlocker.config.armor_stands"), (button, value) -> {
							config.protectArmorStands = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.protectDecoratedPots)
				.create(leftX, top + ROW_HEIGHT * 6, COLUMN_WIDTH, 20,
						Component.translatable("itemlocker.config.pots"), (button, value) -> {
							config.protectDecoratedPots = value;
							ConfigManager.save();
						}));

		// Rechte Spalte
		addRenderableWidget(CycleButton.onOffBuilder(config.preventOffhandSwap)
				.create(rightX, top, COLUMN_WIDTH, 20, Component.translatable("itemlocker.config.offhand"),
						(button, value) -> {
							config.preventOffhandSwap = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.showHudIcons)
				.create(rightX, top + ROW_HEIGHT, COLUMN_WIDTH, 20, Component.translatable("itemlocker.config.hud"),
						(button, value) -> {
							config.showHudIcons = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.playSound)
				.create(rightX, top + ROW_HEIGHT * 2, COLUMN_WIDTH, 20, Component.translatable("itemlocker.config.sound"),
						(button, value) -> {
							config.playSound = value;
							ConfigManager.save();
						}));

		addRenderableWidget(CycleButton.onOffBuilder(config.actionBarMessages)
				.create(rightX, top + ROW_HEIGHT * 3, COLUMN_WIDTH, 20,
						Component.translatable("itemlocker.config.messages"), (button, value) -> {
							config.actionBarMessages = value;
							ConfigManager.save();
						}));

		addRenderableWidget(Button
				.builder(Component.translatable("itemlocker.config.open_slots", config.lockedSlots.size()),
						button -> this.minecraft.setScreenAndShow(new SlotLockScreen(this)))
				.bounds(rightX, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20).build());

		addRenderableWidget(Button
				.builder(Component.translatable("itemlocker.config.open_items", config.lockedItems.size()),
						button -> this.minecraft.setScreenAndShow(new ItemLockScreen(this)))
				.bounds(rightX, top + ROW_HEIGHT * 5, COLUMN_WIDTH, 20).build());

		addRenderableWidget(Button
				.builder(Component.translatable("itemlocker.config.open_blocks", config.lockedBlocks.size()),
						button -> this.minecraft.setScreenAndShow(new BlockLockScreen(this)))
				.bounds(rightX, top + ROW_HEIGHT * 6, COLUMN_WIDTH, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
				.bounds(centerX - 100, this.height - 30, 200, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);

		Draw.centered(context, this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

		LockerConfig config = ConfigManager.get();
		Component hint = Component.translatable("itemlocker.config.hint", config.requiredDrops).withStyle(ChatFormatting.GRAY);
		Draw.centered(context, this.font, hint, this.width / 2, this.height - 44, 0xFFAAAAAA);
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		this.minecraft.setScreenAndShow(this.parent);
	}
}
