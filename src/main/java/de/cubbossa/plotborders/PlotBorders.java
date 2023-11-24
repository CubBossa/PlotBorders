package de.cubbossa.plotborders;

import com.google.common.collect.Lists;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.plot.Plot;
import com.sk89q.worldedit.function.pattern.Pattern;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.Translations;
import de.cubbossa.translations.TranslationsFramework;
import de.cubbossa.translations.persistent.PropertiesMessageStorage;
import de.cubbossa.translations.persistent.PropertiesStyleStorage;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;

@Getter
public class PlotBorders extends JavaPlugin {


	public static final String PERM_RELOAD = "plotborders.admin.reload";
	public static final String PERM_MODIFY_OTHERS = "plotborders.admin.bypass.modify";
	public static final String PERM_BYPASS_COOLDOWN = "plotborders.admin.bypass.cooldown";

	private Metrics metrics;

	private BukkitAudiences audiences;
	private final Config fileConfig = new Config();

	private PatternFile wallsFile;
	private PatternFile borderFile;
	
	private Translations translations;

	@SneakyThrows
	@Override
	public void onEnable() {

		this.metrics = new Metrics(this, 15187);

		this.audiences = BukkitAudiences.create(this);

		fileConfig.reload(this, new File(getDataFolder(), "config.yml"));

		TranslationsFramework.enable(new File(getDataFolder(), "/.."));
		translations = TranslationsFramework.application("PlotBorders");
		translations.setLocaleProvider(audience -> {
			Locale fallback;
			try {
				fallback = Locale.forLanguageTag(fileConfig.fallbackLocale);
			} catch (Throwable t) {
				getLogger().log(Level.WARNING, "Could not parse locale tag '" + fileConfig.fallbackLocale + "'. Using 'en' instead.");
				fallback = Locale.ENGLISH;
			}

			if (audience == null) {
				return fallback;
			}
			if (!fileConfig.usePlayerClientLocale) {
				return fallback;
			}
			return audience.getOrDefault(Identity.LOCALE, fallback);
		});

		translations.addMessages(TranslationsFramework.messageFieldsFromClass(Messages.class));
		translations.setMessageStorage(new PropertiesMessageStorage(getLogger(), new File(getDataFolder(), "lang")));
		translations.setStyleStorage(new PropertiesStyleStorage(new File(getDataFolder(), "lang/styles.properties")));
		translations.loadStyles();
		translations.saveLocale(Locale.ENGLISH);
		translations.saveLocale(Locale.GERMAN);
		translations.loadLocales();

		wallsFile = new PatternFile(this);
		borderFile = new PatternFile(this);

		try {
			wallsFile.loadFromFile(new File(getDataFolder(), "commands/walls.yml"));
			borderFile.loadFromFile(new File(getDataFolder(), "commands/borders.yml"));
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, e.getMessage(), e);
		}

		getCommand("plotwalls").setExecutor(wallsFile.getCommand());
		getCommand("plotborders").setExecutor(borderFile.getCommand());
		getCommand("plotbordersadmin").setExecutor((commandSender, command, s, strings) -> {
			if (!commandSender.hasPermission(PERM_RELOAD)) {
				sendMessage(commandSender, Messages.NO_PERMISSION);
				return false;
			}
			if(strings.length != 1 && !strings[0].equalsIgnoreCase("reload")) {
				sendMessage(commandSender, Messages.WRONG_SYNTAX.formatted(TagResolver.resolver("syntax", Tag.inserting(Component.text("/plotbordersadmin reload")))));
				return false;
			}
			try {
				fileConfig.reload(this, new File(getDataFolder(), "config.yml"));

				translations.saveLocale(Locale.ENGLISH);
				translations.saveLocale(Locale.GERMAN);
				translations.loadLocales();

				wallsFile.loadFromFile(new File(getDataFolder(), "commands/walls.yml"));
				borderFile.loadFromFile(new File(getDataFolder(), "commands/borders.yml"));
				sendMessage(commandSender, Messages.RELOAD_SUCCESS);
			} catch (Throwable t) {
				sendMessage(commandSender, Messages.RELOAD_FAILED);
				getLogger().log(Level.SEVERE, "Could not reload PlotBorders:", t);
			}
			return false;
		});
		getCommand("plotbordersadmin").setTabCompleter((commandSender, command, s, strings) -> Lists.newArrayList("reload"));
	}

	@Override
	public void onDisable() {
	}

	public void sendMessage(CommandSender sender, ComponentLike message) {
		Audience audience = audiences.sender(sender);
		ComponentLike resolved = message;
		if (message instanceof Message msg) {
			resolved = msg.asComponent(audience);
		}
		audience.sendMessage(resolved);
	}

	public void sendMessage(Player player, Component component) {
		audiences.player(player).sendMessage(component);
	}

	public void modifyPlot(Plot plot, String patternString, String type) {
		try {
			final Pattern pattern = ConfigurationUtil.BLOCK_BUCKET.parseString(patternString).toPattern();
			if (plot.getConnectedPlots().size() > 1) {
				for (final Plot plots : plot.getConnectedPlots()) {
					plots.getPlotModificationManager().setComponent(type, pattern, null, null);
				}
			} else {
				plot.getPlotModificationManager().setComponent(type, pattern, null, null);
			}
		} catch (Throwable t) {
			throw new RuntimeException("Error while modifying plot with pattern '" + patternString + "'.", t);
		}
	}
}
