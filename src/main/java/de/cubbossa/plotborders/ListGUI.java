package de.cubbossa.plotborders;

import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder;
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ListGUI<E> extends ChestGui {

    private final int pageContentSize;
    private final OutlinePane controlPane;
    private final OutlinePane pagePane;

    private int page = 0;

    public ListGUI(int rows, @NotNull String title) {
        this(rows, title, JavaPlugin.getProvidingPlugin(ChestGui.class));
    }

    public ListGUI(int rows, @NotNull TextHolder title) {
        this(rows, title, JavaPlugin.getProvidingPlugin(ChestGui.class));
    }

    public ListGUI(int rows, @NotNull String title, @NotNull Plugin plugin) {
        this(rows, StringHolder.of(title), plugin);
    }

    public ListGUI(int rows, @NotNull TextHolder title, @NotNull Plugin plugin) {
        super(rows, title, plugin);

        this.pageContentSize = (rows - 1) * 9;

        pagePane = new OutlinePane(0, 0, 9, rows - 1);
        controlPane = new OutlinePane(0, rows -1, 9, 1);
        addPane(pagePane);
        addPane(controlPane);
        updatePagePane();
        updateControlPane();
        update();
    }

    public abstract List<E> getElementSection(int begin, int end);

    public abstract int getElementCount();

    public abstract GuiItem render(E element);

    private void updateControlPane() {
        controlPane.clear();
        controlPane.setOrientation(Orientable.Orientation.HORIZONTAL);

        controlPane.addItem(pageItem(
                Util.createItemStack(Material.PAPER, Messages.PREV_PAGE, null),
                Util.createItemStack(Material.MAP, Messages.PREV_PAGE, null),
                page - 1
        ));
        controlPane.addItem(pageItem(
                Util.createItemStack(Material.PAPER, Messages.NEXT_PAGE, null),
                Util.createItemStack(Material.MAP, Messages.NEXT_PAGE, null),
                page + 1
        ));

        GuiItem separator = new GuiItem(Util.createItemStack(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), null));
        separator.setAction(e -> e.setCancelled(true));
        for (int i = 0; i < 7; i++) {
            controlPane.addItem(separator);
        }
    }

    private GuiItem pageItem(ItemStack stack, ItemStack disabled, int page) {
        GuiItem item = new GuiItem(canOpenPage(page) ? stack : disabled);
        item.setAction(e -> {
            openPage(page);
            e.setCancelled(true);
        });
        return item;
    }

    private void updatePagePane() {
        pagePane.clear();
        List<E> elements = getElementSection(page * pageContentSize, (page + 1) * pageContentSize);
        for (E element : elements) {
            pagePane.addItem(render(element));
        }
    }

    public int getPage() {
        return page;
    }

    public boolean canOpenPage(int newPage) {
        return newPage >= 0 && getElementCount() / pageContentSize >= newPage;
    }

    public boolean openPage(int newPage) {
        int count = getElementCount();
        if (newPage < 0 || count / pageContentSize < newPage) {
            return false;
        }
        page = newPage;
        updatePagePane();
        updateControlPane();
        update();
        return true;
    }

    public boolean openNextPage() {
        return openPage( page + 1);
    }

    public boolean openPreviousPage() {
        return openPage(page - 1);
    }
}
