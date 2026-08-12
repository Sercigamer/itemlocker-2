package com.itemlocker.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.lock.PlacementGuard;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * Bloecke auswaehlen, deren Oberflaeche sich nicht per Rechtsklick oeffnen
 * lassen soll - damit man im Kampf nicht auf der Endertruhe landet.
 */
public class BlockLockScreen extends Screen {
	private static final int LIST_TOP = 58;
	private static final int LIST_BOTTOM_MARGIN = 78;
	private static final int ROW_HEIGHT = 26;

	private final Screen parent;

	private TextFieldWidget searchField;
	private BlockListWidget blockList;
	private Filter filter = Filter.ALL;

	public BlockLockScreen(Screen parent) {
		super(Text.translatable("itemlocker.config.blocks.title"));
		this.parent = parent;
	}

	/** Welche Bloecke die Liste zeigt. */
	public enum Filter {
		ALL("itemlocker.config.blocks.filter.all"),
		LOCKED("itemlocker.config.blocks.filter.locked");

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
				Text.translatable("itemlocker.config.blocks.search"));
		this.searchField.setPlaceholder(
				Text.translatable("itemlocker.config.blocks.search").formatted(Formatting.DARK_GRAY));
		this.searchField.setChangedListener(text -> refreshList());
		addDrawableChild(this.searchField);

		addDrawableChild(CyclingButtonWidget.<Filter>builder(Filter::label, this.filter)
				.values(Filter.values())
				.omitKeyText()
				.build(centerX + 50, 30, 105, 20, Text.empty(), (button, value) -> {
					this.filter = value;
					refreshList();
				}));

		this.blockList = new BlockListWidget(this.client, this.width,
				this.height - LIST_TOP - LIST_BOTTOM_MARGIN, LIST_TOP, ROW_HEIGHT);
		addDrawableChild(this.blockList);

		addDrawableChild(ButtonWidget
				.builder(Text.translatable("itemlocker.config.blocks.clear"), button -> {
					ConfigManager.get().lockedBlocks.clear();
					ConfigManager.save();
					refreshList();
				})
				.dimensions(centerX - 155, this.height - 56, 150, 20).build());

		addDrawableChild(ButtonWidget
				.builder(Text.translatable("itemlocker.config.blocks.lock_looked"), button -> lockLookedAtBlock())
				.dimensions(centerX + 5, this.height - 56, 150, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
				.dimensions(centerX - 100, this.height - 30, 200, 20).build());

		refreshList();
	}

	/** Sperrt den Block, den der Spieler gerade anvisiert. */
	private void lockLookedAtBlock() {
		if (this.client == null || this.client.crosshairTarget == null
				|| this.client.crosshairTarget.getType() != HitResult.Type.BLOCK
				|| this.client.player == null) {
			return;
		}

		BlockHitResult hit = (BlockHitResult) this.client.crosshairTarget;
		Block block = this.client.player.getEntityWorld().getBlockState(hit.getBlockPos()).getBlock();

		if (block == Blocks.AIR) {
			return;
		}

		ConfigManager.get().lockedBlocks.add(PlacementGuard.blockId(block));
		ConfigManager.save();

		if (this.searchField != null) {
			this.searchField.setText("");
		}

		refreshList();
	}

	private void refreshList() {
		if (this.blockList == null) {
			return;
		}

		String query = this.searchField == null ? "" : this.searchField.getText().trim().toLowerCase(Locale.ROOT);
		List<BlockEntry> entries = new ArrayList<>();

		for (Block block : Registries.BLOCK) {
			if (block == Blocks.AIR) {
				continue;
			}

			String id = PlacementGuard.blockId(block);
			String name = block.getName().getString();

			if (!query.isEmpty()
					&& !id.toLowerCase(Locale.ROOT).contains(query)
					&& !name.toLowerCase(Locale.ROOT).contains(query)) {
				continue;
			}

			if (filter == Filter.LOCKED && !PlacementGuard.isBlockLocked(block)) {
				continue;
			}

			entries.add(new BlockEntry(block, id, name));
		}

		entries.sort((a, b) -> {
			boolean lockedA = PlacementGuard.isBlockLocked(a.block);
			boolean lockedB = PlacementGuard.isBlockLocked(b.block);

			if (lockedA != lockedB) {
				return lockedA ? -1 : 1;
			}

			return a.name.compareToIgnoreCase(b.name);
		});

		this.blockList.replaceEntries(entries);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFFFF);

		Text hint = Text.translatable(ConfigManager.get().blockGuiSneakBypass
				? "itemlocker.config.blocks.count_sneak"
				: "itemlocker.config.blocks.count", ConfigManager.get().lockedBlocks.size())
				.formatted(Formatting.GRAY);
		context.drawCenteredTextWithShadow(this.textRenderer, hint, this.width / 2, this.height - 68, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		ConfigManager.save();
		this.client.setScreen(this.parent);
	}

	private class BlockListWidget extends AlwaysSelectedEntryListWidget<BlockEntry> {
		BlockListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
		}

		@Override
		public int getRowWidth() {
			return 300;
		}
	}

	private class BlockEntry extends AlwaysSelectedEntryListWidget.Entry<BlockEntry> {
		private final Block block;
		private final String id;
		private final String name;
		private final ItemStack icon;

		BlockEntry(Block block, String id, String name) {
			this.block = block;
			this.id = id;
			this.name = name;
			this.icon = new ItemStack(block);
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
			boolean locked = PlacementGuard.isBlockLocked(block);

			if (locked) {
				context.fill(x, y, x + width, y + getContentHeight() - 2, 0x40FF5555);
			} else if (hovered) {
				context.fill(x, y, x + width, y + getContentHeight() - 2, 0x30FFFFFF);
			}

			if (!icon.isEmpty()) {
				context.drawItem(icon, x + 4, y + 3);
			}

			context.drawTextWithShadow(BlockLockScreen.this.textRenderer,
					Text.literal(name).formatted(locked ? Formatting.RED : Formatting.WHITE),
					x + 26, y + 2, 0xFFFFFFFF);

			context.drawTextWithShadow(BlockLockScreen.this.textRenderer,
					Text.literal(id).formatted(Formatting.DARK_GRAY),
					x + 26, y + 13, 0xFF555555);

			Text state = Text.translatable(locked
					? "itemlocker.config.blocks.state_locked"
					: "itemlocker.config.blocks.state_open")
					.formatted(locked ? Formatting.RED : Formatting.DARK_GRAY);

			int stateWidth = BlockLockScreen.this.textRenderer.getWidth(state);
			context.drawTextWithShadow(BlockLockScreen.this.textRenderer, state,
					x + width - stateWidth - 6, y + 8, 0xFFFFFFFF);
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			if (PlacementGuard.isBlockLocked(block)) {
				ConfigManager.get().lockedBlocks.remove(id);
			} else {
				ConfigManager.get().lockedBlocks.add(id);
			}

			ConfigManager.save();
			return true;
		}
	}
}
