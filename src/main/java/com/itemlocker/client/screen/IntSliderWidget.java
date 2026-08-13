package com.itemlocker.client.screen;

import java.util.function.IntConsumer;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * Schieberegler fuer ganze Zahlen - Vanilla liefert nur die 0..1-Variante.
 */
public class IntSliderWidget extends AbstractSliderButton {
	private String translationKey;
	private int min;
	private int max;
	private IntConsumer setter;

	public IntSliderWidget(int x, int y, int width, int height, String translationKey, int min, int max, int current,
			IntConsumer setter) {
		super(x, y, width, height, Component.empty(), toSliderValue(current, min, max));

		this.translationKey = translationKey;
		this.min = min;
		this.max = max;
		this.setter = setter;

		updateMessage();
	}

	private static double toSliderValue(int current, int min, int max) {
		if (max <= min) {
			return 0.0D;
		}

		double normalized = (current - min) / (double) (max - min);
		return Math.min(1.0D, Math.max(0.0D, normalized));
	}

	public int getIntValue() {
		return min + (int) Math.round(this.value * (max - min));
	}

	@Override
	protected void updateMessage() {
		// Wird schon aus dem Super-Konstruktor heraus aufgerufen, da sind die
		// eigenen Felder noch nicht gesetzt.
		if (translationKey == null) {
			return;
		}

		setMessage(Component.translatable(translationKey, getIntValue()));
	}

	@Override
	protected void applyValue() {
		if (setter != null) {
			setter.accept(getIntValue());
		}
	}
}
