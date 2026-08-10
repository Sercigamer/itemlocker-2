package com.itemlocker.client.screen;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;
import com.itemlocker.lock.DropGuard;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Hauptmenue der Mod. Erreichbar ueber Mod Menu, {@code /itemlocker config}
 * oder die Taste aus den Steuerungs-Einstellungen.
 */
public class ItemLockerConfigScreen extends Screen {
	private static final int ROW_HEIGHT = 24;
	private static final int COLUMN_WIDTH = 150;

	private final Screen parent;

	public ItemLockerConfigScreen(Screen parent) {
		super(Text.translatable("itemlocker.config.title"));
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
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.enabled)
				.build(leftX, top, COLUMN_WIDTH, 20, Text.translatable("itemlocker.config.enabled"),
						(button, value) -> {
							config.enabled = value;
							DropGuard.resetCounter();
							ConfigManager.save();
						}));

		addDrawableChild(new IntSliderWidget(leftX, top + ROW_HEIGHT, COLUMN_WIDTH, 20,
				"itemlocker.config.required_drops", 1, 64, config.requiredDrops, value -> {
					config.requiredDrops = value;
					DropGuard.resetCounter();
					ConfigManager.save();
				}));

		addDrawableChild(new IntSliderWidget(leftX, top + ROW_HEIGHT * 2, COLUMN_WIDTH, 20,
				"itemlocker.config.timeout", 1, 60, (int) (config.resetAfterMillis / 1000L), value -> {
					config.resetAfterMillis = value * 1000L;
					ConfigManager.save();
				}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.guardInventoryScreens)
				.build(leftX, top + ROW_HEIGHT * 3, COLUMN_WIDTH, 20,
						Text.translatable("itemlocker.config.guard_inventory"), (button, value) -> {
							config.guardInventoryScreens = value;
							ConfigManager.save();
						}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.preventTakingFromLockedSlots)
				.build(leftX, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20,
						Text.translatable("itemlocker.config.freeze_slots"), (button, value) -> {
							config.preventTakingFromLockedSlots = value;
							ConfigManager.save();
						}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.protectArmorStands)
				.build(leftX, top + ROW_HEIGHT * 5, COLUMN_WIDTH, 20,
						Text.translatable("itemlocker.config.armor_stands"), (button, value) -> {
							config.protectArmorStands = value;
							ConfigManager.save();
						}));

		// Rechte Spalte
		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.showHudIcons)
				.build(rightX, top, COLUMN_WIDTH, 20, Text.translatable("itemlocker.config.hud"),
						(button, value) -> {
							config.showHudIcons = value;
							ConfigManager.save();
						}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.playSound)
				.build(rightX, top + ROW_HEIGHT, COLUMN_WIDTH, 20, Text.translatable("itemlocker.config.sound"),
						(button, value) -> {
							config.playSound = value;
							ConfigManager.save();
						}));

		addDrawableChild(CyclingButtonWidget.onOffBuilder(config.actionBarMessages)
				.build(rightX, top + ROW_HEIGHT * 2, COLUMN_WIDTH, 20,
						Text.translatable("itemlocker.config.messages"), (button, value) -> {
							config.actionBarMessages = value;
							ConfigManager.save();
						}));

		addDrawableChild(ButtonWidget
				.builder(Text.translatable("itemlocker.config.open_slots", config.lockedSlots.size()),
						button -> this.client.setScreen(new SlotLockScreen(this)))
				.dimensions(rightX, top + ROW_HEIGHT * 3, COLUMN_WIDTH, 20).build());

		addDrawableChild(ButtonWidget
				.builder(Text.translatable("itemlocker.config.open_items", config.lockedItems.size()),
						button -> this.client.setScreen(new ItemLockScreen(this)))
				.dimensions(rightX, top + ROW_HEIGHT * 4, COLUMN_WIDTH, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
				.dimensions(centerX - 100, this.height - 30, 200, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFFFF);

		LockerConfig config = ConfigManager.get();
		Text hint = Text.translatable("itemlocker.config.hint", config.requiredDrops).formatted(Formatting.GRAY);
		context.drawCenteredTextWithShadow(this.textRenderer, hint, this.width / 2, this.height - 44, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		ConfigManager.save();
		this.client.setScreen(this.parent);
	}
}
