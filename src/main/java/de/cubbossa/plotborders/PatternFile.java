package de.cubbossa.plotborders;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.MenuPresets;
import de.cubbossa.menuframework.inventory.TopMenu;
import de.cubbossa.menuframework.inventory.implementations.ListMenu;
import de.cubbossa.menuframework.util.ItemStackUtils;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
@Getter
public class PatternFile {

	public static final GsonComponentSerializer GSON_SERIALIZER = GsonComponentSerializer.builder().build();

	@RequiredArgsConstructor
	@Getter
	public static class Icon {
		private final String nameFormat;
		private final Material displayMaterial;
		private final String loreFormat;
		private final String loreFormatDenied;
		private final String permission;
		private final String pattern;
	}

	private final PlotBorders plugin;

	private String type;
	private @Nullable String openPermission;
	private int cooldownSeconds;

	private String titleFormat;
	private String successMessage;

	private int rows;
	private boolean autoScale;
	private List<Icon> icons = new ArrayList<>();

	private final HashMap<UUID, Long> cooldowns = new HashMap<>();

	public TopMenu getMenu(Player player, Plot plot) {
		int rows = this.rows;
		if (autoScale) {
			rows = Integer.max(2, Integer.min(6, (icons.size() + 8) / 9));
		}

		ListMenu menu = new ListMenu(TranslationHandler.getInstance().translateLine(titleFormat, player), rows);
		menu.addPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, rows - 1));
		int row = rows - 1;
		menu.addPreset(applier -> {

			ItemStack right = Util.createCustomHead(Util.HEAD_URL_ARROW_NEXT, Messages.NEXT_PAGE, null, player);
			ItemStack left = Util.createCustomHead(Util.HEAD_URL_ARROW_PREV, Messages.PREV_PAGE, null, player);
			ItemStack rightDisabled = Util.createCustomHead(Util.HEAD_URL_ARROW_NEXT_OFF, Messages.NEXT_PAGE, null, player);
			ItemStack leftDisabled = Util.createCustomHead(Util.HEAD_URL_ARROW_PREV_OFF, Messages.PREV_PAGE, null, player);

			int leftSlot = 0;
			int rightSlot = 1;

			boolean leftLimit = applier.getMenu().getCurrentPage() <= applier.getMenu().getMinPage();
			boolean rightLimit = applier.getMenu().getCurrentPage() >= applier.getMenu().getMaxPage();

			if (leftLimit) {
				applier.addItemOnTop(row * 9 + leftSlot, leftDisabled);
			} else {
				applier.addItemOnTop(row * 9 + leftSlot, left);
				applier.addClickHandlerOnTop(row * 9 + leftSlot, Action.LEFT, c -> applier.getMenu().setPreviousPage(c.getPlayer()));
			}
			if (rightLimit) {
				applier.addItemOnTop(row * 9 + rightSlot, rightDisabled);
			} else {
				applier.addItemOnTop(row * 9 + rightSlot, right);
				applier.addClickHandlerOnTop(row * 9 + rightSlot, Action.LEFT, c -> applier.getMenu().setNextPage(c.getPlayer()));
			}
		});
		for (Icon icon : icons) {
			menu.addListEntry(Button.builder()
					.withItemStack(createIconStack(icon, player, icon.permission != null && !player.hasPermission(icon.permission)))
					.withClickHandler(Action.LEFT, c -> {

						long waited = System.currentTimeMillis() - cooldowns.getOrDefault(player.getUniqueId(), 0L);
						waited /= 1000;
						if (waited <= cooldownSeconds && !player.hasPermission(PlotBorders.PERM_BYPASS_COOLDOWN)) {
							plugin.sendMessage(player, Messages.COOLDOWN, TagResolver.resolver("remaining", Tag.inserting(Component.text(cooldownSeconds - waited))));
							return;
						}
						if(icon.permission != null && !player.hasPermission(icon.permission)) {
							plugin.sendMessage(player, Messages.NO_PERMISSION);
							return;
						}

						if (plot == null) {
							plugin.sendMessage(player, Messages.NOT_ON_PLOT);
							return;
						}
						if (plot.getConnectedPlots().size() > 1) {
							for (final Plot plots : plot.getConnectedPlots()) {
								if (!plots.getOwners().contains(player.getUniqueId()) && !player.hasPermission(PlotBorders.PERM_MODIFY_OTHERS)) {
									plugin.sendMessage(player, Messages.NOT_YOUR_PLOT);
									return;
								}
							}
						} else if (plot.getOwner() == null || !plot.getOwner().equals(player.getUniqueId()) && !player.hasPermission(PlotBorders.PERM_MODIFY_OTHERS)) {
							plugin.sendMessage(player, Messages.NOT_YOUR_PLOT);
							return;
						}
						plugin.modifyPlot(plot, icon.pattern, type);
						TranslationHandler.getInstance().sendMessage(successMessage, c.getPlayer());
						cooldowns.put(c.getPlayer().getUniqueId(), System.currentTimeMillis());
						c.getMenu().close(c.getPlayer());
					}));
		}
		return menu;
	}

	public void loadFromFile(File file) throws IOException {
		if (!file.exists()) {
			plugin.saveResource(file.getAbsolutePath().replace(plugin.getDataFolder().getAbsolutePath(), "").substring(1), true);
			file = new File(file.getAbsolutePath());
		}
		if (file == null || !file.exists()) {
			throw new IOException("Could not load file: " + file.getAbsolutePath());
		}

		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

		type = cfg.getString("type", "walls");
		openPermission = cfg.getString("open_permission", null);
		cooldownSeconds = cfg.getInt("cooldown_in_seconds", 30);

		titleFormat = cfg.getString("gui.title_format", "<red>Unnamed</red>");
		successMessage = cfg.getString("gui.success_message", "<message:wall_changed>");

		rows = cfg.getInt("gui.rows", 4);
		autoScale = cfg.getBoolean("gui.auto_scale", false);

		icons = new ArrayList<>();
		ConfigurationSection iconsSection = cfg.getConfigurationSection("icons");
		if (iconsSection == null) {
			return;
		}
		for (String key : iconsSection.getKeys(false)) {
			ConfigurationSection s = iconsSection.getConfigurationSection(key);
			if (s == null) {
				plugin.getLogger().log(Level.SEVERE, "Invalid Configurationsection: " + key);
				continue;
			}
			Material material = null;
			try {
				material = Material.valueOf(s.getString("display_material", "STONE"));
			} catch (IllegalArgumentException ignored) {
			}
			if (material == null) {
				plugin.getLogger().log(Level.SEVERE, "Could not find material: " + s.getString("display_material"));
				continue;
			}
			String displayNameFormat = s.getString("display_name_format", "<gray><lang:block.minecraft.stone>");
			String displayLoreFormat = s.getString("display_lore", "<gray>Click to apply.");
			String displayLoreFormatDenied = s.getString("display_lore_denied", "<red>No permission!");

			icons.add(new Icon(displayNameFormat, material, displayLoreFormat, displayLoreFormatDenied, s.getString("permission", null), s.getString("pattern", "STONE")));
		}
	}

	private ItemStack createIconStack(Icon icon, Player player, boolean denied) {
		return ItemStackUtils.createItemStack(
				icon.getDisplayMaterial(),
				TranslationHandler.getInstance().translateLine(icon.nameFormat, player),
				TranslationHandler.getInstance().translateLines(denied ? icon.loreFormatDenied : icon.loreFormat, player)
		);
	}

	public CommandExecutor getCommand() {
		return (commandSender, command, s, strings) -> {
			if (!(commandSender instanceof Player player)) {
				plugin.sendMessage((ConsoleCommandSender) commandSender, Messages.NO_CONSOLE);
				return false;
			}
			if (openPermission != null && !player.hasPermission(openPermission) && !player.hasPermission(PlotBorders.PERM_MODIFY_OTHERS)) {
				plugin.sendMessage(player, Messages.NO_PERMISSION);
				return false;
			}
			PlotPlayer<?> plotPlayer = new PlotAPI().wrapPlayer(player.getUniqueId());
			if (plotPlayer == null) {
				plugin.getLogger().log(Level.SEVERE, "Player has no corresponding plot player object, please contact an administrator or report the error to the plugin author.");
				return false;
			}
			Plot plot = plotPlayer.getCurrentPlot();

			getMenu(player, plot).open(player);
			return false;
		};
	}
}
