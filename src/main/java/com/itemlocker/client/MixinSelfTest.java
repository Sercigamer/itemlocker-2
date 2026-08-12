package com.itemlocker.client;

import java.lang.reflect.Method;

import com.itemlocker.ItemLocker;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

/**
 * Prueft, ob die Mixins wirklich in den Zielklassen gelandet sind.
 *
 * <p>Mixins werden erst angewendet, wenn die Zielklasse geladen wird - also
 * normalerweise erst beim Betreten einer Welt. Bei Problemen (z.B. eine
 * Launcher-Umgebung wie Lunar Client, die Mixins anders behandelt) merkt man
 * sonst erst im Spiel, dass gar nichts passiert.
 *
 * <p>Aktivieren mit {@code -Ditemlocker.selftest=true}. Im Dev-Client ist der
 * Test immer an.
 */
public final class MixinSelfTest {
	private MixinSelfTest() {
	}

	public static void runIfRequested() {
		if (!Boolean.getBoolean("itemlocker.selftest")) {
			return;
		}

		check("net.minecraft.class_746", "itemlocker$guardHotbarDrop");
		check("net.minecraft.class_636", "itemlocker$guardSlotClick");
		check("net.minecraft.class_481", "itemlocker$guardCreativeClick");
		check("net.minecraft.class_465", "itemlocker$drawLockIcon");
		check("net.minecraft.class_636", "itemlocker$guardEntityUseAtLocation");
		check("net.minecraft.class_636", "itemlocker$guardEntityUse");
		check("net.minecraft.class_636", "itemlocker$guardBlockUse");
		check("net.minecraft.class_8673", "itemlocker$guardOffhandSwap");
	}

	private static void check(String intermediaryName, String injectedMethod) {
		MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
		String runtimeName = resolver.mapClassName("intermediary", intermediaryName);

		try {
			// initialize=false: die Klasse wird geladen und transformiert (dabei
			// greifen die Mixins), aber ihre statischen Initializer laufen noch nicht.
			Class<?> target = Class.forName(runtimeName, false, MixinSelfTest.class.getClassLoader());

			for (Method method : target.getDeclaredMethods()) {
				// Mixin haengt nur ein Praefix davor (handler$abc000$...), der
				// eigene Name steht am Ende. Deshalb endsWith statt contains -
				// sonst wuerde "guardEntityUse" faelschlich auf
				// "guardEntityUseAtLocation" passen.
				if (method.getName().endsWith(injectedMethod)) {
					ItemLocker.LOGGER.info("Selbsttest OK: {} enthaelt {}", runtimeName, method.getName());
					return;
				}
			}

			StringBuilder synthetic = new StringBuilder();

			for (Method method : target.getDeclaredMethods()) {
				if (method.getName().contains("itemlocker")) {
					synthetic.append(method.getName()).append(' ');
				}
			}

			ItemLocker.LOGGER.error("Selbsttest FEHLGESCHLAGEN: {} enthaelt kein {} - das Mixin wurde nicht angewendet."
					+ " Eingefuegte Fremd-Methoden: [{}]", runtimeName, injectedMethod, synthetic.toString().trim());
		} catch (Throwable t) {
			ItemLocker.LOGGER.error("Selbsttest FEHLGESCHLAGEN: {} konnte nicht geladen werden", runtimeName, t);
		}
	}
}
