package de.cubbossa.plotborders;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Locale;

public class Config {

	public boolean usePlayerClientLocale;
	public String fallbackLocale;

	public int wallsGUIRows;
	public boolean wallsGUIAutoScale;
	public int wallsCooldownSeconds;
	public int borderGUIRows;
	public boolean borderGUIAutoScale;
	public int borderCooldownSeconds;


	public void reload(File file) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

		usePlayerClientLocale = cfg.getBoolean("general.use_client_locales", false);
		fallbackLocale = cfg.getString("general.fallback_locale", Locale.ENGLISH.getLanguage());

		wallsGUIRows = cfg.getInt("gui.walls.rows", 4);
		wallsGUIAutoScale = cfg.getBoolean("gui.walls.auto_scale", false);
		wallsCooldownSeconds = cfg.getInt("gui.walls.cooldown_in_seconds", 30);

		borderGUIRows = cfg.getInt("gui.border.rows", 4);
		borderGUIAutoScale = cfg.getBoolean("gui.border.auto_scale", false);
		borderCooldownSeconds = cfg.getInt("gui.border.cooldown_in_seconds", 30);
	}
}
