package com.itemlocker.compat;

import com.itemlocker.client.screen.ItemLockerConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Haengt das Config-Menue an den Zahnrad-Knopf in Mod Menu.
 *
 * <p>Diese Klasse wird nur geladen, wenn Mod Menu installiert ist - ohne Mod
 * Menu passiert hier gar nichts, die Mod laeuft trotzdem.
 */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ItemLockerConfigScreen::new;
	}
}
