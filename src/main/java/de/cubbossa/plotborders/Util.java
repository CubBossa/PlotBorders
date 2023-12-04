package de.cubbossa.plotborders;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageFormat;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@UtilityClass
public class Util {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .hexCharacter('x')
            .useUnusualXRepeatedCharacterHexFormat()
            .build();


    public static String toLegacy(ComponentLike component) {
        return LEGACY_SERIALIZER.serialize(component.asComponent());
    }

    public static ItemStack createItemStack(Material material, ComponentLike name, @Nullable ComponentLike lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (name instanceof Message message) {
            meta.setDisplayName(message.toString(MessageFormat.LEGACY_PARAGRAPH));
        } else {
            meta.setDisplayName(toLegacy(name));
        }
        if (lore != null) {
            if (lore instanceof Message message) {
                meta.setLore(Arrays.asList(message.toString(MessageFormat.LEGACY_PARAGRAPH).split("\n")));
            } else {
                meta.setLore(Arrays.asList(toLegacy(lore).split("\n")));
            }
        }
        stack.setItemMeta(meta);
        return stack;
    }
}
