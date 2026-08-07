package com.itemlocker.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itemlocker.ItemLocker;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Laedt und speichert {@link LockerConfig} als JSON.
 */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();

	private static Path configPath;
	private static LockerConfig config = new LockerConfig();

	private ConfigManager() {
	}

	public static LockerConfig get() {
		return config;
	}

	public static Path path() {
		if (configPath == null) {
			configPath = FabricLoader.getInstance().getConfigDir().resolve(ItemLocker.MOD_ID + ".json");
		}

		return configPath;
	}

	public static void load() {
		Path file = path();

		if (Files.exists(file)) {
			try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				LockerConfig loaded = GSON.fromJson(reader, LockerConfig.class);

				if (loaded != null) {
					config = loaded;
				}
			} catch (Exception e) {
				ItemLocker.LOGGER.error("Config konnte nicht gelesen werden, benutze Standardwerte", e);
				config = new LockerConfig();
			}
		}

		migrate();
		config.sanitize();
		save();
	}

	/**
	 * Zieht aeltere Config-Dateien nach.
	 *
	 * <p>Version 1: Gesperrte Slots werden im Inventar festgehalten. Vorher
	 * konnte man den Inhalt aus dem Slot nehmen und ausserhalb des Fensters
	 * fallen lassen - die Slot-Sperre griff dann nicht mehr.
	 */
	private static void migrate() {
		if (config.configVersion < 1) {
			config.preventTakingFromLockedSlots = true;
			ItemLocker.LOGGER.info("Config auf Version 1 gehoben: gesperrte Slots werden jetzt festgehalten");
		}

		config.configVersion = LockerConfig.CURRENT_VERSION;
	}

	public static void save() {
		Path file = path();

		try {
			Path parent = file.getParent();

			if (parent != null) {
				Files.createDirectories(parent);
			}

			try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			ItemLocker.LOGGER.error("Config konnte nicht gespeichert werden", e);
		}
	}
}
