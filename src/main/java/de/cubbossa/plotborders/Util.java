package de.cubbossa.plotborders;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.cubbossa.menuframework.util.ItemStackUtils;
import de.cubbossa.translations.Message;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class Util {

	public static String HEAD_URL_ARROW_NEXT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
	public static String HEAD_URL_ARROW_NEXT_OFF = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFhMTg3ZmVkZTg4ZGUwMDJjYmQ5MzA1NzVlYjdiYTQ4ZDNiMWEwNmQ5NjFiZGM1MzU4MDA3NTBhZjc2NDkyNiJ9fX0=";
	public static String HEAD_URL_ARROW_PREV = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
	public static String HEAD_URL_ARROW_PREV_OFF = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjZkYWI3MjcxZjRmZjA0ZDU0NDAyMTkwNjdhMTA5YjVjMGMxZDFlMDFlYzYwMmMwMDIwNDc2ZjdlYjYxMjE4MCJ9fX0=";


	public ItemStack createCustomHead(String url) {
		return createCustomHead(new ItemStack(Material.PLAYER_HEAD, 1), url);
	}

	public ItemStack createCustomHead(String url, Message name, Message lore, Player player) {
		return createCustomHead(ItemStackUtils.createItemStack(Material.PLAYER_HEAD, name.asComponent(player), lore == null ? new ArrayList<>() : lore.asComponents(player)), url);
	}

	public ItemStack createCustomHead(ItemStack itemStack, String url) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta instanceof SkullMeta meta) {
			GameProfile profile = new GameProfile(UUID.randomUUID(), null);
			profile.getProperties().put("textures", new Property("textures", url));

			try {
				Field profileField = meta.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);
				profileField.set(meta, profile);

			} catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
				error.printStackTrace();
			}
			itemStack.setItemMeta(meta);
		} else {
			throw new UnsupportedOperationException("Trying to add a skull texture to a non-playerhead item");
		}
		return itemStack;
	}
}
