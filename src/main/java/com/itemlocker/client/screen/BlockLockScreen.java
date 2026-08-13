package com.itemlocker.client.screen;

import com.itemlocker.client.Draw;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.itemlocker.config.ConfigManager;
import com.itemlocker.lock.PlacementGuard;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Bloecke auswaehlen, deren Oberflaeche sich nicht per Rechtsklick oeffnen
 * lassen soll - damit man im Kampf nicht auf der Endertruhe landet.
 */
public class BlockLockScreen extends Screen {
	private static final int LIST_TOP = 58;
	private static final int LIST_BOTTOM_MARGIN = 78;
	private static final int ROW_HEIGHT = 26;

	private final Screen parent;

	private EditBox searchField;
	private BlockListWidget blockList;
	private Filter filter = Filter.ALL;

	public BlockLockScreen(Screen parent) {
		super(Component.translatable("itemlocker.config.blocks.title"));
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

		public Component label() {
			return Component.translatable(translationKey);
		}
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;

		this.searchField = new EditBox(this.font, centerX - 155, 30, 200, 20,
				Component.translatable("itemlocker.config.blocks.search"));
		this.searchField.setHint(
				Component.translatable("itemlocker.config.blocks.search").withStyle(ChatFormatting.DARK_GRAY));
		this.searchField.setResponder(text -> refreshList());
		addRenderableWidget(this.searchField);

		addRenderableWidget(CycleButton.<Filter>builder(Filter::label, this.filter)
				.withValues(Filter.values())
				.displayOnlyValue()
				.create(centerX + 50, 30, 105, 20, Component.empty(), (button, value) -> {
					this.filter = value;
					refreshList();
				}));

		this.blockList = new BlockListWidget(this.minecraft, this.width,
				this.height - LIST_TOP - LIST_BOTTOM_MARGIN, LIST_TOP, ROW_HEIGHT);
		addRenderableWidget(this.blockList);

		addRenderableWidget(Button
				.builder(Component.translatable("itemlocker.config.blocks.clear"), button -> {
					ConfigManager.get().lockedBlocks.clear();
					ConfigManager.save();
					refreshList();
				})
				.bounds(centerX - 155, this.height - 56, 150, 20).build());

		addRenderableWidget(Button
				.builder(Component.translatable("itemlocker.config.blocks.lock_looked"), button -> lockLookedAtBlock())
				.bounds(centerX + 5, this.height - 56, 150, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
				.bounds(centerX - 100, this.height - 30, 200, 20).build());

		refreshList();
	}

	/** Sperrt den Block, den der Spieler gerade anvisiert. */
	private void lockLookedAtBlock() {
		if (this.minecraft == null || this.minecraft.hitResult == null
				|| this.minecraft.hitResult.getType() != HitResult.Type.BLOCK
				|| this.minecraft.player == null) {
			return;
		}

		BlockHitResult hit = (BlockHitResult) this.minecraft.hitResult;
		Block block = this.minecraft.player.level().getBlockState(hit.getBlockPos()).getBlock();

		if (block == Blocks.AIR) {
			return;
		}

		ConfigManager.get().lockedBlocks.add(PlacementGuard.blockId(block));
		ConfigManager.save();

		if (this.searchField != null) {
			this.searchField.setValue("");
		}

		refreshList();
	}

	/** Anzeigename ohne Stapel - ausserhalb einer Welt bleibt nur die ID. */
	private static String displayName(Block block, String id) {
		try {
			return Component.translatable(block.getDescriptionId()).getString();
		} catch (Throwable ignored) {
			int colon = id.indexOf(':');
			return colon < 0 ? id : id.substring(colon + 1);
		}
	}

	private static ItemStack safeStack(Block block) {
		try {
			return new ItemStack(block);
		} catch (Throwable ignored) {
			return null;
		}
	}

	private void refreshList() {
		if (this.blockList == null) {
			return;
		}

		String query = this.searchField == null ? "" : this.searchField.getValue().trim().toLowerCase(Locale.ROOT);
		List<BlockEntry> entries = new ArrayList<>();

		for (Block block : BuiltInRegistries.BLOCK) {
			if (block == Blocks.AIR) {
				continue;
			}

			String id = PlacementGuard.blockId(block);
			String name = displayName(block, id);

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
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);

		Draw.centered(context, this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

		Component hint = Component.translatable(ConfigManager.get().blockGuiSneakBypass
				? "itemlocker.config.blocks.count_sneak"
				: "itemlocker.config.blocks.count", ConfigManager.get().lockedBlocks.size())
				.withStyle(ChatFormatting.GRAY);
		Draw.centered(context, this.font, hint, this.width / 2, this.height - 68, 0xFFAAAAAA);
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		this.minecraft.setScreenAndShow(this.parent);
	}

	private class BlockListWidget extends ObjectSelectionList<BlockEntry> {
		BlockListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
		}

		@Override
		public int getRowWidth() {
			return 300;
		}
	}

	private class BlockEntry extends ObjectSelectionList.Entry<BlockEntry> {
		private final Block block;
		private final String id;
		private final String name;
		private final ItemStack icon;

		BlockEntry(Block block, String id, String name) {
			this.block = block;
			this.id = id;
			this.name = name;
			this.icon = safeStack(block);
		}

		@Override
		public Component getNarration() {
			return Component.literal(name);
		}

		@Override
		public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = getContentX();
			int y = getContentY();
			int width = getContentWidth();
			boolean locked = PlacementGuard.isBlockLocked(block);

			if (locked) {
				context.fill(x, y, x + width, y + getContentHeight() - 2, 0x40FF5555);
			} else if (hovered) {
				context.fill(x, y, x + width, y + getContentHeight() - 2, 0x30FFFFFF);
			}

			if (icon != null && !icon.isEmpty()) {
				context.item(icon, x + 4, y + 3);
			}

			Draw.text(context, BlockLockScreen.this.font,
					Component.literal(name).withStyle(locked ? ChatFormatting.RED : ChatFormatting.WHITE),
					x + 26, y + 2, 0xFFFFFFFF);

			Draw.text(context, BlockLockScreen.this.font,
					Component.literal(id).withStyle(ChatFormatting.DARK_GRAY),
					x + 26, y + 13, 0xFF555555);

			Component state = Component.translatable(locked
					? "itemlocker.config.blocks.state_locked"
					: "itemlocker.config.blocks.state_open")
					.withStyle(locked ? ChatFormatting.RED : ChatFormatting.DARK_GRAY);

			int stateWidth = BlockLockScreen.this.font.width(state);
			Draw.text(context, BlockLockScreen.this.font, state,
					x + width - stateWidth - 6, y + 8, 0xFFFFFFFF);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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
