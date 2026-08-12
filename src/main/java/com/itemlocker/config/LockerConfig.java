package com.itemlocker.config;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Alles, was in {@code config/itemlocker.json} landet.
 */
public class LockerConfig {
	/** Aktueller Stand des Config-Formats, siehe ConfigManager#migrate. */
	public static final int CURRENT_VERSION = 1;

	/** Steht in alten Dateien nicht drin und ist dort deshalb 0. */
	public int configVersion;

	/** Master-Schalter. Wenn {@code false}, verhaelt sich alles wie Vanilla. */
	public boolean enabled = true;

	/** Wie oft man droppen muss, damit ein gesperrtes Item wirklich faellt. */
	public int requiredDrops = 5;

	/** Nach dieser Zeit ohne Drop-Versuch faengt der Zaehler wieder bei 0 an. */
	public long resetAfterMillis = 3000L;

	/** Auch in Inventar-/Kisten-GUIs schuetzen (Q auf einem Slot, Item rauswerfen). */
	public boolean guardInventoryScreens = true;

	/**
	 * Haelt den Inhalt eines gesperrten Hotbar-Slots im Inventar fest.
	 *
	 * <p>Ohne das kann man das Item mit der Maus aufnehmen und ausserhalb des
	 * Fensters fallen lassen - dabei ist es nicht mehr im gesperrten Slot, die
	 * Slot-Sperre greift also nicht mehr. Deshalb standardmaessig an.
	 */
	public boolean preventTakingFromLockedSlots = true;

	/**
	 * Verhindert, dass gesperrte Sachen an einen Ruestungsstaender gehen.
	 *
	 * <p>Anders als beim Droppen wird hier nicht gezaehlt, sondern hart
	 * blockiert - ein Rechtsklick genuegt sonst, und das Item ist weg.
	 */
	public boolean protectArmorStands = true;

	/** Verhindert, dass gesperrte Sachen in einen Deko-Topf wandern. */
	public boolean protectDecoratedPots = true;

	/**
	 * Verhindert, dass ein gesperrtes Item in die Zweithand getauscht wird.
	 *
	 * <p>Die Taste dafuer liegt bei vielen direkt neben den Bewegungstasten -
	 * ein Fehlgriff im Kampf schiebt sonst das Totem aus der Hand.
	 */
	public boolean preventOffhandSwap = true;

	/**
	 * Bloecke, deren Oberflaeche sich nicht per Rechtsklick oeffnen laesst -
	 * als Registry-ID, z.B. {@code minecraft:ender_chest}.
	 */
	public Set<String> lockedBlocks = new LinkedHashSet<>();

	/** Schleichen umgeht die Block-Sperre, damit man trotzdem drankommt. */
	public boolean blockGuiSneakBypass = true;

	/** Schloss-Symbole ueber der Hotbar zeichnen. */
	public boolean showHudIcons = true;

	/** Warn-Sound bei blockiertem Drop. */
	public boolean playSound = true;

	/** Warn-Text ueber der Hotbar bei blockiertem Drop. */
	public boolean actionBarMessages = true;

	/** Gesperrte Hotbar-Slots, 0-8 (intern), im Spiel als 1-9 angezeigt. */
	public Set<Integer> lockedSlots = new LinkedHashSet<>();

	/** Gesperrte Item-Typen als Registry-ID, z.B. {@code minecraft:diamond_sword}. */
	public Set<String> lockedItems = new LinkedHashSet<>();

	/**
	 * Repariert kaputte oder von Hand editierte Werte, damit die Mod nie mit
	 * Unsinn aus der JSON-Datei startet.
	 */
	public void sanitize() {
		if (requiredDrops < 1) {
			requiredDrops = 1;
		} else if (requiredDrops > 64) {
			requiredDrops = 64;
		}

		if (resetAfterMillis < 250L) {
			resetAfterMillis = 250L;
		} else if (resetAfterMillis > 60_000L) {
			resetAfterMillis = 60_000L;
		}

		if (lockedSlots == null) {
			lockedSlots = new LinkedHashSet<>();
		}

		if (lockedItems == null) {
			lockedItems = new LinkedHashSet<>();
		}

		if (lockedBlocks == null) {
			lockedBlocks = new LinkedHashSet<>();
		}

		lockedBlocks.removeIf(block -> block == null || block.isBlank());

		lockedSlots.removeIf(slot -> slot == null || slot < 0 || slot > 8);
		lockedItems.removeIf(item -> item == null || item.isBlank());
	}
}
