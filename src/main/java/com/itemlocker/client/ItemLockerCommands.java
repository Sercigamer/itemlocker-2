package com.itemlocker.client;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import java.util.Locale;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.itemlocker.config.ConfigManager;
import com.itemlocker.config.LockerConfig;
import com.itemlocker.lock.DropGuard;
import com.itemlocker.lock.LockManager;

import com.itemlocker.lock.PlacementGuard;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

/**
 * {@code /itemlocker} (kurz: {@code /il}) - laeuft komplett clientseitig, der
 * Server sieht davon nichts.
 */
public final class ItemLockerCommands {
	private ItemLockerCommands() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal("itemlocker")
					.executes(ItemLockerCommands::status)
					.then(literal("config").executes(ItemLockerCommands::openConfig))
					.then(literal("help").executes(ItemLockerCommands::help))
					.then(literal("status").executes(ItemLockerCommands::status))
					.then(literal("list").executes(ItemLockerCommands::list))
					.then(literal("clear").executes(ItemLockerCommands::clear))
					.then(literal("on").executes(ctx -> setEnabled(ctx, true)))
					.then(literal("off").executes(ctx -> setEnabled(ctx, false)))
					.then(literal("count")
							.then(argument("amount", IntegerArgumentType.integer(1, 64))
									.executes(ItemLockerCommands::setCount)))
					.then(literal("timeout")
							.then(argument("seconds", IntegerArgumentType.integer(1, 60))
									.executes(ItemLockerCommands::setTimeout)))
					.then(literal("slot")
							.then(argument("slot", IntegerArgumentType.integer(1, 9))
									.executes(ctx -> toggleSlot(ctx, null))
									.then(literal("lock").executes(ctx -> toggleSlot(ctx, Boolean.TRUE)))
									.then(literal("unlock").executes(ctx -> toggleSlot(ctx, Boolean.FALSE)))))
					.then(literal("item")
							.executes(ctx -> toggleHeldItem(ctx, null))
							.then(literal("lock").executes(ctx -> toggleHeldItem(ctx, Boolean.TRUE)))
							.then(literal("unlock").executes(ctx -> toggleHeldItem(ctx, Boolean.FALSE)))
							.then(argument("id", RegistryIdArgumentType.of(BuiltInRegistries.ITEM))
									.executes(ItemLockerCommands::toggleItemById)))
					.then(literal("hud")
							.then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
								ConfigManager.get().showHudIcons = BoolArgumentType.getBool(ctx, "value");
								return saveAndReport(ctx, "itemlocker.command.hud", ConfigManager.get().showHudIcons);
							})))
					.then(literal("sound")
							.then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
								ConfigManager.get().playSound = BoolArgumentType.getBool(ctx, "value");
								return saveAndReport(ctx, "itemlocker.command.sound", ConfigManager.get().playSound);
							})))
					.then(literal("messages")
							.then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
								ConfigManager.get().actionBarMessages = BoolArgumentType.getBool(ctx, "value");
								return saveAndReport(ctx, "itemlocker.command.messages",
										ConfigManager.get().actionBarMessages);
							})))
					.then(literal("inventory")
							.then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
								ConfigManager.get().guardInventoryScreens = BoolArgumentType.getBool(ctx, "value");
								return saveAndReport(ctx, "itemlocker.command.inventory",
										ConfigManager.get().guardInventoryScreens);
							})))
					.then(literal("pots")
							.then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
								ConfigManager.get().protectDecoratedPots = BoolArgumentType.getBool(ctx, "value");
								return saveAndReport(ctx, "itemlocker.command.pots",
										ConfigManager.get().protectDecoratedPots);
							})))
					.then(literal("offhand")
							.then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
								ConfigManager.get().preventOffhandSwap = BoolArgumentType.getBool(ctx, "value");
								return saveAndReport(ctx, "itemlocker.command.offhand",
										ConfigManager.get().preventOffhandSwap);
							})))
					.then(literal("sneakbypass")
							.then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
								ConfigManager.get().blockGuiSneakBypass = BoolArgumentType.getBool(ctx, "value");
								return saveAndReport(ctx, "itemlocker.command.sneakbypass",
										ConfigManager.get().blockGuiSneakBypass);
							})))
					.then(literal("block")
							.executes(ItemLockerCommands::toggleLookedAtBlock)
							.then(argument("id", RegistryIdArgumentType.of(BuiltInRegistries.BLOCK))
									.executes(ItemLockerCommands::toggleBlockById)))
					.then(literal("armorstands")
							.then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
								ConfigManager.get().protectArmorStands = BoolArgumentType.getBool(ctx, "value");
								return saveAndReport(ctx, "itemlocker.command.armor_stands",
										ConfigManager.get().protectArmorStands);
							})))
					.then(literal("freeze")
							.then(argument("value", BoolArgumentType.bool()).executes(ctx -> {
								ConfigManager.get().preventTakingFromLockedSlots = BoolArgumentType.getBool(ctx, "value");
								return saveAndReport(ctx, "itemlocker.command.freeze",
										ConfigManager.get().preventTakingFromLockedSlots);
							}))));

			dispatcher.register(literal("il").executes(ItemLockerCommands::status)
					.redirect(dispatcher.getRoot().getChild("itemlocker")));
		});
	}

	private static int openConfig(CommandContext<FabricClientCommandSource> ctx) {
		// Nicht sofort oeffnen - der Chat-Screen wird gerade erst geschlossen.
		ItemLockerKeybinds.requestConfigScreen();
		return 1;
	}

	private static int help(CommandContext<FabricClientCommandSource> ctx) {
		FabricClientCommandSource source = ctx.getSource();
		source.sendFeedback(header());

		for (String line : new String[] {
				"itemlocker.help.config",
				"itemlocker.help.block",
				"itemlocker.help.slot",
				"itemlocker.help.item",
				"itemlocker.help.item_id",
				"itemlocker.help.count",
				"itemlocker.help.list",
				"itemlocker.help.clear",
				"itemlocker.help.toggle",
				"itemlocker.help.hud",
				"itemlocker.help.keys" }) {
			source.sendFeedback(Component.translatable(line).withStyle(ChatFormatting.GRAY));
		}

		return 1;
	}

	private static int status(CommandContext<FabricClientCommandSource> ctx) {
		LockerConfig config = ConfigManager.get();
		FabricClientCommandSource source = ctx.getSource();

		source.sendFeedback(header());
		source.sendFeedback(Component.translatable("itemlocker.command.status.enabled",
				onOff(config.enabled)).withStyle(ChatFormatting.GRAY));
		source.sendFeedback(Component.translatable("itemlocker.command.status.count",
				Component.literal(String.valueOf(config.requiredDrops)).withStyle(ChatFormatting.YELLOW))
				.withStyle(ChatFormatting.GRAY));
		source.sendFeedback(Component.translatable("itemlocker.command.status.locked",
				Component.literal(String.valueOf(config.lockedSlots.size())).withStyle(ChatFormatting.YELLOW),
				Component.literal(String.valueOf(config.lockedItems.size())).withStyle(ChatFormatting.YELLOW))
				.withStyle(ChatFormatting.GRAY));

		return 1;
	}

	private static int list(CommandContext<FabricClientCommandSource> ctx) {
		LockerConfig config = ConfigManager.get();
		FabricClientCommandSource source = ctx.getSource();

		source.sendFeedback(header());

		if (config.lockedSlots.isEmpty() && config.lockedItems.isEmpty()) {
			source.sendFeedback(Component.translatable("itemlocker.command.list.empty").withStyle(ChatFormatting.GRAY));
			return 1;
		}

		if (!config.lockedSlots.isEmpty()) {
			StringBuilder slots = new StringBuilder();

			for (int slot : config.lockedSlots) {
				if (slots.length() > 0) {
					slots.append(", ");
				}

				slots.append(slot + 1);
			}

			source.sendFeedback(Component.translatable("itemlocker.command.list.slots",
					Component.literal(slots.toString()).withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.GRAY));
		}

		for (String item : config.lockedItems) {
			source.sendFeedback(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(item).withStyle(ChatFormatting.AQUA)));
		}

		return 1;
	}

	private static int clear(CommandContext<FabricClientCommandSource> ctx) {
		LockManager.clearAll();
		DropGuard.resetCounter();
		ctx.getSource().sendFeedback(Component.translatable("itemlocker.command.cleared").withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static int setEnabled(CommandContext<FabricClientCommandSource> ctx, boolean enabled) {
		ConfigManager.get().enabled = enabled;
		ConfigManager.save();
		DropGuard.resetCounter();

		ctx.getSource().sendFeedback(Component.translatable(enabled
				? "itemlocker.message.enabled"
				: "itemlocker.message.disabled").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY));
		return 1;
	}

	private static int setCount(CommandContext<FabricClientCommandSource> ctx) {
		int amount = IntegerArgumentType.getInteger(ctx, "amount");
		ConfigManager.get().requiredDrops = amount;
		ConfigManager.save();
		DropGuard.resetCounter();

		ctx.getSource().sendFeedback(Component.translatable("itemlocker.command.count_set",
				Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static int setTimeout(CommandContext<FabricClientCommandSource> ctx) {
		int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
		ConfigManager.get().resetAfterMillis = seconds * 1000L;
		ConfigManager.save();

		ctx.getSource().sendFeedback(Component.translatable("itemlocker.command.timeout_set",
				Component.literal(String.valueOf(seconds)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static int toggleSlot(CommandContext<FabricClientCommandSource> ctx, Boolean forced) {
		int slot = IntegerArgumentType.getInteger(ctx, "slot") - 1;
		boolean locked;

		if (forced == null) {
			locked = LockManager.toggleSlot(slot);
		} else {
			locked = forced;
			LockManager.setSlotLocked(slot, locked);
		}

		DropGuard.resetCounter();
		ctx.getSource().sendFeedback(Component.translatable(locked
				? "itemlocker.message.slot_locked"
				: "itemlocker.message.slot_unlocked", slot + 1)
				.withStyle(locked ? ChatFormatting.RED : ChatFormatting.GREEN));
		return 1;
	}

	private static int toggleHeldItem(CommandContext<FabricClientCommandSource> ctx, Boolean forced) {
		LocalPlayer player = ctx.getSource().getPlayer();
		ItemStack stack = player.getInventory().getItem(player.getInventory().getSelectedSlot());

		if (stack.isEmpty()) {
			ctx.getSource().sendError(Component.translatable("itemlocker.message.no_item"));
			return 0;
		}

		return applyItemLock(ctx, LockManager.itemId(stack), forced);
	}

	private static int toggleItemById(CommandContext<FabricClientCommandSource> ctx) {
		return applyItemLock(ctx, RegistryIdArgumentType.get(ctx, "id"), null);
	}

	private static int applyItemLock(CommandContext<FabricClientCommandSource> ctx, String itemId, Boolean forced) {
		boolean locked;

		if (forced == null) {
			locked = LockManager.toggleItem(itemId);
		} else {
			locked = forced;
			LockManager.setItemLocked(itemId, locked);
		}

		DropGuard.resetCounter();
		ctx.getSource().sendFeedback(Component.translatable(locked
				? "itemlocker.message.item_locked"
				: "itemlocker.message.item_unlocked", itemId, itemId)
				.withStyle(locked ? ChatFormatting.RED : ChatFormatting.GREEN));
		return 1;
	}

	/** Sperrt oder entsperrt den Block, den der Spieler gerade anvisiert. */
	private static int toggleLookedAtBlock(CommandContext<FabricClientCommandSource> ctx) {
		Minecraft client = Minecraft.getInstance();

		if (client.hitResult == null || client.hitResult.getType() != HitResult.Type.BLOCK
				|| client.player == null) {
			ctx.getSource().sendError(Component.translatable("itemlocker.command.no_block"));
			return 0;
		}

		BlockHitResult hit = (BlockHitResult) client.hitResult;
		Block block = client.player.level().getBlockState(hit.getBlockPos()).getBlock();

		if (block == Blocks.AIR) {
			ctx.getSource().sendError(Component.translatable("itemlocker.command.no_block"));
			return 0;
		}

		return applyBlockLock(ctx, PlacementGuard.blockId(block), block.getName());
	}

	private static int toggleBlockById(CommandContext<FabricClientCommandSource> ctx) {
		String id = RegistryIdArgumentType.get(ctx, "id");
		return applyBlockLock(ctx, id, Component.literal(id));
	}

	private static int applyBlockLock(CommandContext<FabricClientCommandSource> ctx, String blockId, Component name) {
		LockerConfig config = ConfigManager.get();
		boolean locked;

		if (config.lockedBlocks.contains(blockId)) {
			config.lockedBlocks.remove(blockId);
			locked = false;
		} else {
			config.lockedBlocks.add(blockId);
			locked = true;
		}

		ConfigManager.save();
		ctx.getSource().sendFeedback(Component.translatable(locked
				? "itemlocker.message.block_locked"
				: "itemlocker.message.block_unlocked", name.copy().withStyle(ChatFormatting.WHITE), blockId)
				.withStyle(locked ? ChatFormatting.RED : ChatFormatting.GREEN));
		return 1;
	}

	private static int saveAndReport(CommandContext<FabricClientCommandSource> ctx, String key, boolean value) {
		ConfigManager.save();
		ctx.getSource().sendFeedback(Component.translatable(key, onOff(value)).withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static Component onOff(boolean value) {
		return Component.translatable(value ? "itemlocker.generic.on" : "itemlocker.generic.off")
				.withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
	}

	private static Component header() {
		return Component.literal("ItemLocker").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
	}
}
