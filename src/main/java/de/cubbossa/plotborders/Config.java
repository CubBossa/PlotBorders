package de.cubbossa.plotborders;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Locale;

public class Config {

	public boolean usePlayerClientLocale;
	public String fallbackLocale;

	public void reload(Plugin pl, File file) {

		if (!file.exists()) {
			pl.saveResource(file.getName(), true);
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

		usePlayerClientLocale = cfg.getBoolean("general.use_client_locales", false);
		fallbackLocale = cfg.getString("general.fallback_locale", Locale.ENGLISH.getLanguage());
	}
}
