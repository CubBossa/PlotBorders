package de.cubbossa.plotborders;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageBuilder;

import java.util.Locale;

public class Messages {

    public static final Message PREFIX = new MessageBuilder("prefix")
            .withDefault("<gold>Plotborders</gold><gray> » </gray>")
            .build();
    public static final Message NO_CONSOLE = new MessageBuilder("error.need_to_be_player")
            .withDefault("<red>You have to be a player to use this command!</red>")
            .withTranslation(Locale.GERMAN, "<red>Du musst ein Spieler sein, um diesen Befehl auszuführen!</red>")
            .build();
    public static final Message WRONG_SYNTAX = new MessageBuilder("error.syntax")
            .withDefault("<red>Please use <syntax>.</red>")
            .withTranslation(Locale.GERMAN, "<red>Benutze <syntax>.</red>")
            .build();
    public static final Message NO_PERMISSION = new MessageBuilder("error.no_permission")
            .withDefault("<red>You don't have enough permission!</red>")
            .withTranslation(Locale.GERMAN, "<red>Dafür hast du keine Berechtigung!</red>")
            .build();
    public static final Message COOLDOWN = new MessageBuilder("error.cooldown")
            .withDefault("<red>You have to wait <remaining> more seconds.")
            .withTranslation(Locale.GERMAN, "<red>Warte <remaining> Sekunden.</red>")
            .build();
    public static final Message NOT_ON_PLOT = new MessageBuilder("error.not_on_plot")
            .withDefault("<red>You have to be on a plot for this function.")
            .withTranslation(Locale.GERMAN, "<red>Du kannst diese Funktion nur auf einem Plot ausführen.</red>")
            .build();
    public static final Message NOT_YOUR_PLOT = new MessageBuilder("error.not_your_plot")
            .withDefault("<red>This is not your plot. You can only modify your own plot.</red>")
            .withTranslation(Locale.GERMAN, "<red>Du kannst Wände und Ränder nur von deinem eigenen Plot ändern.</red>")
            .build();
    public static final Message WALL_CHANGED = new MessageBuilder("wall_changed")
            .withDefault("<msg:prefix>Your plot wall has been changed.")
            .withTranslation(Locale.GERMAN, "<msg:prefix>Deine Plotwand wurde geändert.")
            .build();
    public static final Message BORDER_CHANGED = new MessageBuilder("border_changed")
            .withDefault("<msg:prefix>Your plot border has been changed.")
            .withTranslation(Locale.GERMAN, "<msg:prefix>Dein Plotrand wurde geändert.")
            .build();
    public static final Message NEXT_PAGE = new MessageBuilder("gui.next_page")
            .withDefault("<gold>Next Page</gold>")
            .withTranslation(Locale.GERMAN, "<gold>Nächste Seite</gold>")
            .build();
    public static final Message PREV_PAGE = new MessageBuilder("gui.prev_page")
            .withDefault("<gold>Previous Page</gold>")
            .withTranslation(Locale.GERMAN, "<gold>Vorherige Seite</gold>")
            .build();
    public static final Message RELOAD_SUCCESS = new MessageBuilder("reload.success")
            .withDefault("<msg:prefix>Successfully reloaded files.")
            .withTranslation(Locale.GERMAN, "<msg:prefix>Dateien erfolgreich neu geladen.")
            .build();
    public static final Message RELOAD_FAILED = new MessageBuilder("reload.failed")
            .withDefault("<red>Error while reloading PlotBorders. See console for more information.</red>")
            .withTranslation(Locale.GERMAN, "<red>Fehler beim Laden von PlotBorders. Mehr Informationen in der Konsole.</red>")
            .build();

    public static final Message WALLS_NO_PERM = new MessageBuilder("walls.no_perm")
            .withDefault("<red>No permission!</red>")
            .withTranslation(Locale.GERMAN, "<red>Keine Berechtigung!</red>")
            .build();
    public static final Message WALLS_GROUP = new MessageBuilder("walls.group")
            .withDefault("<gray>Group: </gray>")
            .withTranslation(Locale.GERMAN, "<gray>Gruppe: </gray>")
            .build();
    public static final Message WALLS_CLICK = new MessageBuilder("walls.click")
            .withDefault("<gray>Click to apply</gray>")
            .withTranslation(Locale.GERMAN, "<gray>Klicke zum Anwenden</gray>")
            .build();

    public static final Message BORDER_NO_PERM = new MessageBuilder("border.no_perm")
            .withDefault("<red>No permission!</red>")
            .withTranslation(Locale.GERMAN, "<red>Keine Berechtigung!</red>")
            .build();
    public static final Message BORDER_GROUP = new MessageBuilder("border.group")
            .withDefault("<gray>Group: </gray>")
            .withTranslation(Locale.GERMAN, "<gray>Gruppe: </gray>")
            .build();
    public static final Message BORDER_CLICK = new MessageBuilder("border.click")
            .withDefault("<gray>Click to apply</gray>")
            .withTranslation(Locale.GERMAN, "<gray>Klicke zum Anwenden</gray>")
            .build();
}
