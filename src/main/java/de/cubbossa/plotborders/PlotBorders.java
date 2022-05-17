package de.cubbossa.plotborders;

import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.plot.Plot;
import com.sk89q.worldedit.function.pattern.Pattern;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.MenuPresets;
import de.cubbossa.translations.LanguageFileException;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
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

	public static final Message PREFIX = new Message("prefix");
	public static final Message NO_CONSOLE = new Message("error.need_to_be_player");
	public static final Message WRONG_SYNTAX = new Message("error.syntax");
	public static final Message NO_PERMISSION = new Message("error.no_permission");
	public static final Message COOLDOWN = new Message("error.cooldown");
	public static final Message NOT_ON_PLOT = new Message("error.not_on_plot");
	public static final Message NOT_YOUR_PLOT = new Message("error.not_your_plot");
	public static final Message WALL_CHANGED = new Message("wall_changed");
	public static final Message BORDER_CHANGED = new Message("border_changed");
	public static final Message NEXT_PAGE = new Message("gui.next_page");
	public static final Message PREV_PAGE = new Message("gui.prev_page");
	public static final Message RELOAD_SUCCESS = new Message("reload.success");
	public static final Message RELOAD_FAILED = new Message("reload.failed");

	public static final String PERM_RELOAD = "plotborders.admin.reload";
	public static final String PERM_MODIFY_OTHERS = "plotborders.admin.bypass.modify";
	public static final String PERM_BYPASS_COOLDOWN = "plotborders.admin.bypass.cooldown";

	private Metrics metrics;

	private BukkitAudiences audiences;
	private final MiniMessage miniMessage = MiniMessage.miniMessage();
	private final Config fileConfig = new Config();

	private PatternFile wallsFile;
	private PatternFile borderFile;

	@Override
	public void onEnable() {

		this.metrics = new Metrics(this, 15187);

		this.audiences = BukkitAudiences.create(this);

		fileConfig.reload(this, new File(getDataFolder(), "config.yml"));

		TranslationHandler translationHandler = new TranslationHandler(this, audiences, miniMessage, new File(getDataFolder(), "lang/"), "lang");
		translationHandler.setFallbackLanguage(fileConfig.fallbackLocale);
		translationHandler.setUseClientLanguage(fileConfig.usePlayerClientLocale);
		try {
			translationHandler.loadLanguages(Locale.US, Locale.GERMANY);
		} catch (LanguageFileException e) {
			getLogger().log(Level.SEVERE, "Could not load languages:", e);
		}

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
				sendMessage(commandSender, NO_PERMISSION);
				return false;
			}
			if(strings.length != 1 && !strings[0].equalsIgnoreCase("reload")) {
				sendMessage(commandSender, WRONG_SYNTAX, TagResolver.resolver("syntax", Tag.inserting(Component.text("/plotbordersadmin reload"))));
				return false;
			}
			try {
				fileConfig.reload(this, new File(getDataFolder(), "config.yml"));
				translationHandler.loadLanguages(Locale.US, Locale.GERMANY);
				wallsFile.loadFromFile(new File(getDataFolder(), "commands/walls.yml"));
				borderFile.loadFromFile(new File(getDataFolder(), "commands/borders.yml"));
				sendMessage(commandSender, RELOAD_SUCCESS);
			} catch (Throwable t) {
				sendMessage(commandSender, RELOAD_FAILED);
				getLogger().log(Level.SEVERE, "Could not reload PlotBorders:", t);
			}
			return false;
		});
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
