package com.itemlocker.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.lock.DropGuard;
import com.itemlocker.lock.LockManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Item-Auswahl: durchsuchbare Liste aller Items, Klick auf eine Zeile sperrt
 * oder entsperrt den Item-Typ.
 */
public class ItemLockScreen extends Screen {
	private static final int LIST_TOP = 58;
	/** Platz unten fuer Zaehler-Text und die beiden Button-Reihen. */
	private static final int LIST_BOTTOM_MARGIN = 78;
	private static final int ROW_HEIGHT = 26;

	private final Screen parent;

	private TextFieldWidget searchField;
	private ItemListWidget itemList;
	private Filter filter = Filter.ALL;

	public ItemLockScreen(Screen parent) {
		super(Text.translatable("itemlocker.config.items.title"));
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

		public Text label() {
			return Text.translatable(translationKey);
		}
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;

		this.searchField = new TextFieldWidget(this.textRenderer, centerX - 155, 30, 200, 20,
				Text.translatable("itemlocker.config.items.search"));
		this.searchField.setPlaceholder(
				Text.translatable("itemlocker.config.items.search").formatted(Formatting.DARK_GRAY));
		this.searchField.setChangedListener(text -> refreshList());
		addDrawableChild(this.searchField);

		addDrawableChild(CyclingButtonWidget.<Filter>builder(Filter::label, this.filter)
				.values(Filter.values())
				.omitKeyText()
				.build(centerX + 50, 30, 105, 20, Text.empty(), (button, value) -> {
					this.filter = value;
					refreshList();
				}));

		this.itemList = new ItemListWidget(this.client, this.width,
				this.height - LIST_TOP - LIST_BOTTOM_MARGIN, LIST_TOP, ROW_HEIGHT);
		addDrawableChild(this.itemList);

		addDrawableChild(ButtonWidget
				.builder(Text.translatable("itemlocker.config.items.clear"), button -> {
					ConfigManager.get().lockedItems.clear();
					ConfigManager.save();
					DropGuard.resetCounter();
					refreshList();
				})
				.dimensions(centerX - 155, this.height - 56, 150, 20).build());

		addDrawableChild(ButtonWidget
				.builder(Text.translatable("itemlocker.config.items.lock_held"), button -> lockHeldItem())
				.dimensions(centerX + 5, this.height - 56, 150, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
				.dimensions(centerX - 100, this.height - 30, 200, 20).build());

		refreshList();
	}

	private void lockHeldItem() {
		ClientPlayerEntity player = this.client == null ? null : this.client.player;

		if (player == null) {
			return;
		}

		ItemStack stack = player.getInventory().getSelectedStack();

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

		String query = this.searchField == null ? "" : this.searchField.getText().trim().toLowerCase(Locale.ROOT);
		List<ItemEntry> entries = new ArrayList<>();

		for (Item item : candidates()) {
			if (item == Items.AIR) {
				continue;
			}

			ItemStack stack = new ItemStack(item);
			String id = LockManager.itemId(stack);
			String name = stack.getName().getString();

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
			return Registries.ITEM;
		}

		ClientPlayerEntity player = this.client == null ? null : this.client.player;

		if (player == null) {
			return List.of();
		}

		PlayerInventory inventory = player.getInventory();
		List<Item> items = new ArrayList<>();

		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);

			if (!stack.isEmpty() && !items.contains(stack.getItem())) {
				items.add(stack.getItem());
			}
		}

		return items;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFFFF);

		Text count = Text.translatable("itemlocker.config.items.count", ConfigManager.get().lockedItems.size())
				.formatted(Formatting.GRAY);
		context.drawCenteredTextWithShadow(this.textRenderer, count, this.width / 2, this.height - 68, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		ConfigManager.save();
		this.client.setScreen(this.parent);
	}

	/** Scrollbare Liste der Items. */
	private class ItemListWidget extends AlwaysSelectedEntryListWidget<ItemEntry> {
		ItemListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
		}

		@Override
		public int getRowWidth() {
			return 300;
		}
	}

	/** Eine Zeile: Icon, Name, Registry-ID und Schloss-Status. */
	private class ItemEntry extends AlwaysSelectedEntryListWidget.Entry<ItemEntry> {
		private final ItemStack stack;
		private final String id;
		private final String name;

		ItemEntry(ItemStack stack, String id, String name) {
			this.stack = stack;
			this.id = id;
			this.name = name;
		}

		@Override
		public Text getNarration() {
			return Text.literal(name);
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = getContentX();
			int y = getContentY();
			int width = getContentWidth();
			boolean locked = LockManager.isItemLocked(stack);

			if (locked) {
				context.fill(x, y, x + width, y + getContentHeight() - 2, 0x40FF5555);
			} else if (hovered) {
				context.fill(x, y, x + width, y + getContentHeight() - 2, 0x30FFFFFF);
			}

			context.drawItem(stack, x + 4, y + 3);

			context.drawTextWithShadow(ItemLockScreen.this.textRenderer,
					Text.literal(name).formatted(locked ? Formatting.RED : Formatting.WHITE),
					x + 26, y + 2, 0xFFFFFFFF);

			context.drawTextWithShadow(ItemLockScreen.this.textRenderer,
					Text.literal(id).formatted(Formatting.DARK_GRAY),
					x + 26, y + 13, 0xFF555555);

			Text state = Text.translatable(locked
					? "itemlocker.config.items.state_locked"
					: "itemlocker.config.items.state_open")
					.formatted(locked ? Formatting.RED : Formatting.DARK_GRAY);

			int stateWidth = ItemLockScreen.this.textRenderer.getWidth(state);
			context.drawTextWithShadow(ItemLockScreen.this.textRenderer, state,
					x + width - stateWidth - 6, y + 8, 0xFFFFFFFF);
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			LockManager.toggleItem(id);
			DropGuard.resetCounter();
			return true;
		}
	}
}
