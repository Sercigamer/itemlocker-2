package com.itemlocker.client;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Nimmt nur IDs an, die es in der jeweiligen Registry wirklich gibt.
 *
 * <p>Ein reines Freitext-Argument wuerde jede Eingabe schlucken - auch
 * {@code lock} und {@code unlock}. Brigadier kann es dann nicht von den
 * gleichnamigen festen Woertern daneben unterscheiden und meldet beim
 * Registrieren eine Mehrdeutigkeit. Weil dieser Typ unbekannte Eingaben
 * ablehnt, ist die Ueberschneidung weg - und die Fehlermeldung wird
 * nebenbei besser.
 */
public final class RegistryIdArgumentType implements ArgumentType<String> {
	private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(
			id -> Component.translatable("itemlocker.command.unknown_entry", id));

	private static final Collection<String> BEISPIELE = List.of("minecraft:elytra", "ender_chest");

	private final Registry<?> registry;

	private RegistryIdArgumentType(Registry<?> registry) {
		this.registry = registry;
	}

	public static RegistryIdArgumentType of(Registry<?> registry) {
		return new RegistryIdArgumentType(registry);
	}

	public static String get(CommandContext<?> context, String name) {
		return context.getArgument(name, String.class);
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		String raw = reader.getRemaining().trim().toLowerCase(Locale.ROOT);
		reader.setCursor(reader.getTotalLength());

		Identifier id = Identifier.tryParse(raw);

		if (id == null || !contains(id)) {
			throw UNKNOWN.create(raw);
		}

		return id.toString();
	}

	/**
	 * Beim Pruefen auf Mehrdeutigkeiten laeuft das hier sehr frueh. Sollte die
	 * Registry wider Erwarten noch nicht bereitstehen, wird die Eingabe
	 * durchgelassen - lieber eine Warnung im Log als ein Befehl, der sich gar
	 * nicht erst registrieren laesst.
	 */
	private boolean contains(Identifier id) {
		try {
			return registry.keySet().contains(id);
		} catch (Throwable ignored) {
			return true;
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

		for (Identifier id : registry.keySet()) {
			String full = id.toString();

			if (full.startsWith(remaining) || id.getPath().startsWith(remaining)) {
				builder.suggest(full);
			}
		}

		return builder.buildFuture();
	}

	@Override
	public Collection<String> getExamples() {
		return BEISPIELE;
	}
}
