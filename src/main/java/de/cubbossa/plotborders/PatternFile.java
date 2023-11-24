package de.cubbossa.plotborders;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

	private final PlotBorders plotBorders;

	private String type;
	private @Nullable String openPermission;
	private int cooldownSeconds;

	private String titleFormat;
	private String successMessage;

	private int rows;
	private boolean autoScale;
	private List<Icon> icons = new ArrayList<>();

	private final HashMap<UUID, Long> cooldowns = new HashMap<>();

	public Gui getMenu(Player player, Plot plot) {
		int rows = this.rows;
		if (autoScale) {
			rows = Integer.max(2, Integer.min(6, (icons.size() + 8) / 9));
		}

		return new ListGUI<Icon>(rows, ComponentHolder.of(plotBorders.getTranslations().process(titleFormat))) {
			@Override
			public List<Icon> getElementSection(int begin, int end) {
				return icons.subList(Math.max(0, begin), Math.min(icons.size(), end));
			}

			@Override
			public int getElementCount() {
				return icons.size();
			}

			@Override
			public GuiItem render(Icon icon) {
				GuiItem item = new GuiItem(createIconStack(icon, icon.permission != null && !player.hasPermission(icon.permission)));
				item.setAction(e -> {
					e.setCancelled(true);
					if (!e.isLeftClick()) {
						return;
					}
					long waited = System.currentTimeMillis() - cooldowns.getOrDefault(player.getUniqueId(), 0L);
					waited /= 1000;
					if (waited <= cooldownSeconds && !player.hasPermission(PlotBorders.PERM_BYPASS_COOLDOWN)) {
						plotBorders.sendMessage(player, Messages.COOLDOWN.formatted(TagResolver.resolver("remaining", Tag.inserting(Component.text(cooldownSeconds - waited)))));
						return;
					}
					if(icon.permission != null && !player.hasPermission(icon.permission)) {
						plotBorders.sendMessage(player, Messages.NO_PERMISSION);
						return;
					}

					if (plot == null) {
						plotBorders.sendMessage(player, Messages.NOT_ON_PLOT);
						return;
					}
					if (plot.getConnectedPlots().size() > 1) {
						for (final Plot plots : plot.getConnectedPlots()) {
							if (!plots.getOwners().contains(player.getUniqueId()) && !player.hasPermission(PlotBorders.PERM_MODIFY_OTHERS)) {
								plotBorders.sendMessage(player, Messages.NOT_YOUR_PLOT);
								return;
							}
						}
					} else if (plot.getOwner() == null || !plot.getOwner().equals(player.getUniqueId()) && !player.hasPermission(PlotBorders.PERM_MODIFY_OTHERS)) {
						plotBorders.sendMessage(player, Messages.NOT_YOUR_PLOT);
						return;
					}
					plotBorders.modifyPlot(plot, icon.pattern, type);
					plotBorders.sendMessage(player, plotBorders.getTranslations().process(successMessage));
					cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
					player.closeInventory();
				});
				return item;
			}
		};
	}

	public void loadFromFile(File file) throws IOException {
		if (!file.exists()) {
			plotBorders.saveResource(file.getAbsolutePath().replace(plotBorders.getDataFolder().getAbsolutePath(), "").substring(1), true);
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
		successMessage = cfg.getString("gui.success_message", "<msg:wall_changed>");

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
				plotBorders.getLogger().log(Level.SEVERE, "Invalid Configurationsection: " + key);
				continue;
			}
			Material material = null;
			try {
				material = Material.valueOf(s.getString("display_material", "STONE").toUpperCase());
			} catch (IllegalArgumentException ignored) {
			}
			if (material == null) {
				plotBorders.getLogger().log(Level.SEVERE, "Could not find material: " + s.getString("display_material"));
				continue;
			}
			String displayNameFormat = s.getString("display_name_format", "<gray><lang:block.minecraft.stone>");
			String displayLoreFormat = s.getString("display_lore", "<gray>Click to apply.");
			String displayLoreFormatDenied = s.getString("display_lore_denied", "<red>No permission!");

			icons.add(new Icon(displayNameFormat, material, displayLoreFormat, displayLoreFormatDenied, s.getString("permission", null), s.getString("pattern", "STONE")));
		}
	}

	private ItemStack createIconStack(Icon icon, boolean denied) {
		return Util.createItemStack(
				icon.getDisplayMaterial(),
				plotBorders.getTranslations().process(icon.nameFormat),
				Arrays.stream((denied ? icon.loreFormatDenied : icon.loreFormat).split("\n"))
						.map(s -> plotBorders.getTranslations().process(s))
						.collect(Collectors.toList())
		);
	}

	public CommandExecutor getCommand() {
		return (commandSender, command, s, strings) -> {
			if (!(commandSender instanceof Player player)) {
				plotBorders.sendMessage(commandSender, Messages.NO_CONSOLE);
				return false;
			}
			if (openPermission != null && !player.hasPermission(openPermission) && !player.hasPermission(PlotBorders.PERM_MODIFY_OTHERS)) {
				plotBorders.sendMessage(player, Messages.NO_PERMISSION);
				return false;
			}
			PlotPlayer<?> plotPlayer = new PlotAPI().wrapPlayer(player.getUniqueId());
			if (plotPlayer == null) {
				plotBorders.getLogger().log(Level.SEVERE, "Player has no corresponding plot player object, please contact an administrator or report the error to the plugin author.");
				return false;
			}
			Plot plot = plotPlayer.getCurrentPlot();

			var gui = getMenu(player, plot);
			gui.show(player);
			gui.update();
			return false;
		};
	}
}
