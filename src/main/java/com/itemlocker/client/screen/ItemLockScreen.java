package com.itemlocker.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.lock.DropGuard;
import com.itemlocker.lock.LockManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Item-Auswahl: durchsuchbare Liste aller Items, Klick auf eine Zeile sperrt
 * oder entsperrt den Item-Typ.
 */
public class ItemLockScreen extends Screen {
	private static final int LIST_TOP = 58;
	/** Platz unten fuer Zaehler-Component und die beiden Button-Reihen. */
	private static final int LIST_BOTTOM_MARGIN = 78;
	private static final int ROW_HEIGHT = 26;

	private final Screen parent;

	private EditBox searchField;
	private ItemListWidget itemList;
	private Filter filter = Filter.ALL;

	public ItemLockScreen(Screen parent) {
		super(Component.translatable("itemlocker.config.items.title"));
		this.parent = parent;
	}

	/** Welche Items die Liste zeigt. */
	public enum Filter {
		ALL("itemlocker.config.items.filter.all"),
		LOCKED("itemlocker.config.items.filter.locked"),
		INVENTORY("itemlocker.config.items.filter.inventory");

		private final String translationKey;

		Filter(String translationKey) {
			this.translationKey = translationKey;
		}

		public Component label() {
			return Component.translatable(translationKey);
		}
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;

		this.searchField = new EditBox(this.font, centerX - 155, 30, 200, 20,
				Component.translatable("itemlocker.config.items.search"));
		this.searchField.setHint(
				Component.translatable("itemlocker.config.items.search").withStyle(ChatFormatting.DARK_GRAY));
		this.searchField.setResponder(text -> refreshList());
		addRenderableWidget(this.searchField);

		addRenderableWidget(CycleButton.<Filter>builder(Filter::label, this.filter)
				.withValues(Filter.values())
				.displayOnlyValue()
				.build(centerX + 50, 30, 105, 20, Component.empty(), (button, value) -> {
					this.filter = value;
					refreshList();
				}));

		this.itemList = new ItemListWidget(this.minecraft, this.width,
				this.height - LIST_TOP - LIST_BOTTOM_MARGIN, LIST_TOP, ROW_HEIGHT);
		addRenderableWidget(this.itemList);

		addRenderableWidget(Button
				.builder(Component.translatable("itemlocker.config.items.clear"), button -> {
					ConfigManager.get().lockedItems.clear();
					ConfigManager.save();
					DropGuard.resetCounter();
					refreshList();
				})
				.bounds(centerX - 155, this.height - 56, 150, 20).build());

		addRenderableWidget(Button
				.builder(Component.translatable("itemlocker.config.items.lock_held"), button -> lockHeldItem())
				.bounds(centerX + 5, this.height - 56, 150, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.close())
				.bounds(centerX - 100, this.height - 30, 200, 20).build());

		refreshList();
	}

	private void lockHeldItem() {
		LocalPlayer player = this.minecraft == null ? null : this.minecraft.player;

		if (player == null) {
			return;
		}

		ItemStack stack = player.getInventory().getSelectedItem();

		if (stack.isEmpty()) {
			return;
		}

		LockManager.setItemLocked(LockManager.itemId(stack), true);
		DropGuard.resetCounter();
		refreshList();
	}

	private void refreshList() {
		if (this.itemList == null) {
			return;
		}

		String query = this.searchField == null ? "" : this.searchField.getValue().trim().toLowerCase(Locale.ROOT);
		List<ItemEntry> entries = new ArrayList<>();

		for (Item item : candidates()) {
			if (item == Items.AIR) {
				continue;
			}

			ItemStack stack = new ItemStack(item);
			String id = LockManager.itemId(stack);
			String name = stack.getHoverName().getString();

			if (!query.isEmpty()
					&& !id.toLowerCase(Locale.ROOT).contains(query)
					&& !name.toLowerCase(Locale.ROOT).contains(query)) {
				continue;
			}

			if (filter == Filter.LOCKED && !LockManager.isItemLocked(stack)) {
				continue;
			}

			entries.add(new ItemEntry(stack, id, name));
		}

		// Gesperrte nach oben, sonst alphabetisch.
		entries.sort((a, b) -> {
			boolean lockedA = LockManager.isItemLocked(a.stack);
			boolean lockedB = LockManager.isItemLocked(b.stack);

			if (lockedA != lockedB) {
				return lockedA ? -1 : 1;
			}

			return a.name.compareToIgnoreCase(b.name);
		});

		this.itemList.replaceEntries(entries);
	}

	/** Grundmenge je nach Filter: alle Items oder nur die im Inventar. */
	private Iterable<Item> candidates() {
		if (filter != Filter.INVENTORY) {
			return BuiltInRegistries.ITEM;
		}

		LocalPlayer player = this.minecraft == null ? null : this.minecraft.player;

		if (player == null) {
			return List.of();
		}

		Inventory inventory = player.getInventory();
		List<Item> items = new ArrayList<>();

		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getItem(slot);

			if (!stack.isEmpty() && !items.contains(stack.getItem())) {
				items.add(stack.getItem());
			}
		}

		return items;
	}

	@Override
	public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);

		context.drawCenteredTextWithShadow(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

		Component count = Component.translatable("itemlocker.config.items.count", ConfigManager.get().lockedItems.size())
				.withStyle(ChatFormatting.GRAY);
		context.drawCenteredTextWithShadow(this.font, count, this.width / 2, this.height - 68, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		ConfigManager.save();
		this.minecraft.setScreen(this.parent);
	}

	/** Scrollbare Liste der Items. */
	private class ItemListWidget extends ObjectSelectionList<ItemEntry> {
		ItemListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
		}

		@Override
		public int getRowWidth() {
			return 300;
		}
	}

	/** Eine Zeile: Icon, Name, Registry-ID und Schloss-Status. */
	private class ItemEntry extends ObjectSelectionList.Entry<ItemEntry> {
		private final ItemStack stack;
		private final String id;
		private final String name;

		ItemEntry(ItemStack stack, String id, String name) {
			this.stack = stack;
			this.id = id;
			this.name = name;
		}

		@Override
		public Component getNarration() {
			return Component.literal(name);
		}

		@Override
		public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = getContentX();
			int y = getContentY();
			int width = getContentWidth();
			boolean locked = LockManager.isItemLocked(stack);

			if (locked) {
				extractor.fill(x, y, x + width, y + getContentHeight() - 2, 0x40FF5555);
			} else if (hovered) {
				extractor.fill(x, y, x + width, y + getContentHeight() - 2, 0x30FFFFFF);
			}

			context.item(stack, x + 4, y + 3);

			context.drawTextWithShadow(ItemLockScreen.this.font,
					Component.literal(name).withStyle(locked ? ChatFormatting.RED : ChatFormatting.WHITE),
					x + 26, y + 2, 0xFFFFFFFF);

			context.drawTextWithShadow(ItemLockScreen.this.font,
					Component.literal(id).withStyle(ChatFormatting.DARK_GRAY),
					x + 26, y + 13, 0xFF555555);

			Component state = Component.translatable(locked
					? "itemlocker.config.items.state_locked"
					: "itemlocker.config.items.state_open")
					.withStyle(locked ? ChatFormatting.RED : ChatFormatting.DARK_GRAY);

			int stateWidth = ItemLockScreen.this.font.width(state);
			context.drawTextWithShadow(ItemLockScreen.this.font, state,
					x + width - stateWidth - 6, y + 8, 0xFFFFFFFF);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			LockManager.toggleItem(id);
			DropGuard.resetCounter();
			return true;
		}
	}
}
