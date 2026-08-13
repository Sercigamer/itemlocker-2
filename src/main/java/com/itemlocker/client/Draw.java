package com.itemlocker.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Kleine Brücke für das Zeichnen von Text.
 *
 * <p>Ab 26.x zeichnet man nicht mehr direkt, sondern füllt einen
 * {@link GuiGraphicsExtractor}. Der bietet nur noch {@code text(...)} - eine
 * zentrierte Variante gibt es nicht mehr, die Breite muss man selbst abziehen.
 * Damit das nicht an jeder Aufrufstelle steht, liegt es hier.
 */
public final class Draw {
	private Draw() {
	}

	/** Text mit Schatten, linksbündig ab {@code x}. */
	public static void text(GuiGraphicsExtractor extractor, Font font, Component text, int x, int y, int color) {
		extractor.text(font, text, x, y, color, true);
	}

	/** Text mit Schatten, waagerecht mittig um {@code centerX}. */
	public static void centered(GuiGraphicsExtractor extractor, Font font, Component text, int centerX, int y,
			int color) {
		extractor.text(font, text, centerX - font.width(text) / 2, y, color, true);
	}
}
