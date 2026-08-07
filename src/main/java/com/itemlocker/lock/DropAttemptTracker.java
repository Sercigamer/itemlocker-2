package com.itemlocker.lock;

/**
 * Zaehlt, wie oft hintereinander versucht wurde, dasselbe gesperrte Item zu droppen.
 *
 * <p>Der Zaehler haengt an einem Schluessel (Slot + Item). Wechselt der Schluessel
 * oder vergeht zu viel Zeit, faengt das Zaehlen wieder von vorne an - damit ein
 * einzelner Fehldruck von vor fuenf Minuten nicht spaeter zu einem echten Drop
 * beitraegt.
 */
public final class DropAttemptTracker {
	private String key = "";
	private int attempts;
	private long lastAttemptAt;

	/**
	 * Registriert einen Drop-Versuch.
	 *
	 * @return wie viele weitere Versuche noch noetig sind; {@code 0} heisst
	 *         "durchlassen".
	 */
	public int attempt(String key, int required, long resetAfterMillis) {
		long now = System.currentTimeMillis();

		if (!this.key.equals(key) || now - this.lastAttemptAt > resetAfterMillis) {
			this.key = key;
			this.attempts = 0;
		}

		this.lastAttemptAt = now;
		this.attempts++;

		if (this.attempts >= required) {
			reset();
			return 0;
		}

		return required - this.attempts;
	}

	/** Bereits gezaehlte Versuche fuer diesen Schluessel, 0 wenn abgelaufen. */
	public int attemptsFor(String key, long resetAfterMillis) {
		if (!this.key.equals(key)) {
			return 0;
		}

		if (System.currentTimeMillis() - this.lastAttemptAt > resetAfterMillis) {
			return 0;
		}

		return this.attempts;
	}

	public void reset() {
		this.key = "";
		this.attempts = 0;
		this.lastAttemptAt = 0L;
	}
}
