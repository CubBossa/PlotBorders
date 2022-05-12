package de.cubbossa.plotborders;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.sk89q.worldedit.function.pattern.Pattern;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.implementations.ListMenu;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class PlotBorders extends JavaPlugin {

	public static final Message PREFIX = new Message("prefix");
	public static final Message NO_CONSOLE = new Message("error.need_to_be_player");
	public static final Message NO_PERMISSION = new Message("error.no_permission");
	public static final Message COOLDOWN = new Message("error.cooldown");
	public static final Message NOT_ON_PLOT = new Message("error.not_on_plot");
	public static final Message NOT_YOUR_PLOT = new Message("error.not_your_plot");
	public static final Message WALL_CHANGED = new Message("wall_changed");
	public static final Message BORDER_CHANGED = new Message("border_changed");
	public static final Message GUI_WALLS_TITLE = new Message("gui.walls.title");
	public static final Message GUI_BORDER_TITLE = new Message("gui.border.title");

	public static final String PERM_WALLS = "plotborders.walls.open";
	public static final String PERM_BORDER = "plotborders.border.open";
	public static final String PERM_MODIFY_OTHERS = "plotborders.admin.bypass";

	@RequiredArgsConstructor
	@Getter
	public class Icon {
		private final String nameFormat;
		private final Material displayMaterial;
		private final String loreFormat;
		private final String loreFormatDenied;
		private final String permission;
		private final String pattern;
	}

	private BukkitAudiences audiences;
	private Config config = new Config();

	private List<Icon> wallsIcons;
	private List<Icon> borderIcons;

	private Map<UUID, Long> wallsCooldowns;
	private Map<UUID, Long> borderCooldowns;

	@Override
	public void onEnable() {

		this.audiences = BukkitAudiences.create(this);

		TranslationHandler translationHandler = new TranslationHandler(this, BukkitAudiences.create(this), MiniMessage.builder().build(), new File(getDataFolder(), "lang/"), "lang");
		translationHandler.setFallbackLanguage(config.fallbackLocale);
		translationHandler.setUseClientLanguage(config.usePlayerClientLocale);

		new GUIHandler(this).enable();

		config.reload(new File(getDataFolder(), "config.yml"));

		wallsIcons = loadIcons(new File(getDataFolder(), "walls.yml"));
		borderIcons = loadIcons(new File(getDataFolder(), "borders.yml"));
		wallsCooldowns = new HashMap<>();
		borderCooldowns = new HashMap<>();

		getCommand("plotwalls").setExecutor(getWallsCommand());
	}

	@Override
	public void onDisable() {

		GUIHandler.getInstance().disable();
	}

	private void sendMessage(Player player, Message message, TagResolver... resolvers) {
		Audience audience = audiences.player(player);
		audience.sendMessage(message.asComponent(audience, resolvers));
	}

	private void sendMessage(ConsoleCommandSender sender, Message message, TagResolver... resolvers) {
		Audience audience = audiences.sender(sender);
		audience.sendMessage(message.asComponent(audience, resolvers));
	}

	private CommandExecutor getWallsCommand() {
		return (commandSender, command, s, strings) -> {
			if (!(commandSender instanceof Player player)) {
				sendMessage((ConsoleCommandSender) commandSender, NO_CONSOLE);
				return false;
			}
			if (!player.hasPermission(PERM_WALLS) && !player.hasPermission(PERM_MODIFY_OTHERS)) {
				sendMessage(player, NO_PERMISSION);
				return false;
			}
			long waited = System.currentTimeMillis() - wallsCooldowns.getOrDefault(player.getUniqueId(), 0L);
			if (waited <= config.wallsCooldownSeconds) {
				sendMessage(player, COOLDOWN, TagResolver.resolver("seconds", Tag.inserting(Component.text(waited / 1000))));
				return false;
			}
			PlotPlayer<?> plotPlayer = new PlotAPI().wrapPlayer(player.getUniqueId());
			if (plotPlayer != null) {
				getLogger().log(Level.SEVERE, "Player has no corresponding plot player object, please contact an administrator or report the error to the plugin author.");
				return false;
			}
			Plot plot = plotPlayer.getCurrentPlot();
			if (plot == null) {
				sendMessage(player, NOT_ON_PLOT);
				return false;
			}
			if (plot.getConnectedPlots().size() > 1) {
				for (final Plot plots : plot.getConnectedPlots()) {
					if (!plots.getOwners().contains(player.getUniqueId()) && !player.hasPermission(PERM_MODIFY_OTHERS)) {
						sendMessage(player, NOT_YOUR_PLOT);
						return false;
					}
				}
			} else {
				sendMessage(player, NOT_YOUR_PLOT);
				return false;
			}

			ListMenu menu = new ListMenu(GUI_WALLS_TITLE.asComponent(player), config.wallsGUIRows);
			for (Icon icon : wallsIcons) {
				menu.addListEntry(Button.builder()
						.withItemStack(createIconStack(icon, icon.permission != null && !player.hasPermission(icon.permission)))
						.withClickHandler(Action.LEFT, c -> {
							modifyPlot(plot, icon.pattern, "wall");
							sendMessage(c.getPlayer(), WALL_CHANGED);
							c.getMenu().close(c.getPlayer());
						}));
			}
			menu.open(player);
			return false;
		};
	}

	private ItemStack createIconStack(Icon icon, boolean denied) {
		ItemStack stack = new ItemStack(icon.getDisplayMaterial());


		return stack;
	}

	private void modifyPlot(Plot plot, String patternString, String type) {

		final Pattern pattern = ConfigurationUtil.BLOCK_BUCKET.parseString(patternString).toPattern();
		if (plot.getConnectedPlots().size() > 1) {
			for (final Plot plots : plot.getConnectedPlots()) {
				plots.getPlotModificationManager().setComponent(type, pattern, null, null);
			}
		} else {
			plot.getPlotModificationManager().setComponent(type, pattern, null, null);
		}
	}

	private List<Icon> loadIcons(File file) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		List<Icon> icons = new ArrayList<>();
		for (String key : cfg.getKeys(false)) {
			ConfigurationSection s = cfg.getConfigurationSection(key);
			if (s == null) {
				continue;
			}
			Material material = null;
			try {
				material = Material.valueOf(s.getString("display_material", "STONE"));
			} catch (IllegalArgumentException ignored) {
			}
			if (material == null) {
				getLogger().log(Level.SEVERE, "Could not find material: " + s.getString("display_material"));
				continue;
			}
			Icon icon = new Icon(
					s.getString("display_name_format", "<gray><lang:block.minecraft.stone>"),
					material,
					s.getString("display_lore", "<gray>Click to apply"),
					s.getString("display_lore_denied", "<red>No permission!"),
					s.getString("permission", null),
					s.getString("pattern", "STONE")
			);
			icons.add(icon);
		}
		return icons;
	}
}
