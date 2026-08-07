package com.itemlocker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;

/**
 * Gemeinsame Konstanten der Mod.
 */
public final class ItemLocker {
	public static final String MOD_ID = "itemlocker";
	public static final String MOD_NAME = "ItemLocker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	private ItemLocker() {
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
