package de.cubbossa.plotborders;

import com.google.common.collect.Lists;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.plot.Plot;
import com.sk89q.worldedit.function.pattern.Pattern;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.MenuPresets;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
	private final MiniMessage miniMessage = MiniMessage.miniMessage();
	private final Config fileConfig = new Config();

	private PatternFile wallsFile;
	private PatternFile borderFile;

	@SneakyThrows
	@Override
	public void onEnable() {

		this.metrics = new Metrics(this, 15187);

		this.audiences = BukkitAudiences.create(this);

		fileConfig.reload(this, new File(getDataFolder(), "config.yml"));

		saveResource(getDataFolder().getPath() + "lang/en_US.yml", false);
		saveResource(getDataFolder().getPath() + "lang/de_DE.yml", false);

		TranslationHandler translationHandler = new TranslationHandler(this, audiences, miniMessage, new File(getDataFolder(), "lang/"), "lang");
		translationHandler.registerAnnotatedLanguageClass(Messages.class);
		translationHandler.setFallbackLanguage(fileConfig.fallbackLocale);
		translationHandler.setUseClientLanguage(fileConfig.usePlayerClientLocale);
		translationHandler.loadLanguages();

		new GUIHandler(this).enable();

		MenuPresets.FILLER_DARK = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemMeta meta = MenuPresets.FILLER_DARK.getItemMeta();
		meta.setDisplayName(" ");
		MenuPresets.FILLER_DARK.setItemMeta(meta);

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
				sendMessage(commandSender, Messages.WRONG_SYNTAX, TagResolver.resolver("syntax", Tag.inserting(Component.text("/plotbordersadmin reload"))));
				return false;
			}
			try {
				fileConfig.reload(this, new File(getDataFolder(), "config.yml"));
				translationHandler.loadLanguages(Locale.US, Locale.GERMANY);
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

		GUIHandler.getInstance().disable();
	}

	public void sendMessage(Player player, Message message, TagResolver... resolvers) {
		Audience audience = audiences.player(player);
		audience.sendMessage(message.asComponent(audience, resolvers));
	}

	public void sendMessage(CommandSender sender, Message message, TagResolver... resolvers) {
		Audience audience = audiences.sender(sender);
		audience.sendMessage(message.asComponent(audience, resolvers));
	}

	public void sendMessage(Player player, Component component) {
		Audience audience = audiences.player(player);
		audience.sendMessage(component);
	}

	public void modifyPlot(Plot plot, String patternString, String type) {

		final Pattern pattern = ConfigurationUtil.BLOCK_BUCKET.parseString(patternString).toPattern();
		if (plot.getConnectedPlots().size() > 1) {
			for (final Plot plots : plot.getConnectedPlots()) {
				plots.getPlotModificationManager().setComponent(type, pattern, null, null);
			}
		} else {
			plot.getPlotModificationManager().setComponent(type, pattern, null, null);
		}
	}
}
