package pl.eventowki;

import java.util.HashMap;
import java.util.Map;

/** Zaznaczone sloty ekwipunku (0-8 hotbar, 9-35 reszta). Wartosc: true = PPM (uzyj), false = LPM (uderz). */
public final class Selection {
	public static final Map<Integer, Boolean> SLOTS = new HashMap<>();

	private Selection() {}
}
