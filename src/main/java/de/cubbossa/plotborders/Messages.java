package de.cubbossa.plotborders;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageFile;
import de.cubbossa.translations.MessageGroupMeta;
import de.cubbossa.translations.MessageMeta;

@MessageFile(
		author = "CubBossa",
		header = """
				MESSAGE FILE
				----------------------------------------
				Styling:
				To style your messages, you must use the MiniMessage formatting.
				It is a tag based styling method like html or xml and allows you to
				create hover messages and click actions within your language file!

				Example: <green>I am a green Text. <hover:show_text:"Hello there">Hover me</hover><green>

				All information on how to use the formatting can be found here:
				https://docs.adventure.kyori.net/minimessage/format.html

				Additionally to the default MiniMessage, you have the following possibilities:
				<prefix> will always be replaced with the prefix message. (general.prefix)
				<msg:[message-key]> or <message:[message-key]> will replaced with another already interpreted messages
				    Using <msg:color_red> and referring to a translation `color_red: <#ff0000>` will not work.
				    The color will be interpreted as component before being inserted.
				<col:[message-key]> will insert a message as raw MiniMessage.
				    So instead of <msg:color_red>, you can use
				    <col:color_red> to insert the value of color_red ("<#ff0000>") as simple string.
				    With this functionality, you can add a style table at the top of your language file and change colors for the
				    whole file easily.
				----------------------------------------
				"""
)
public class Messages {

	@MessageMeta(value = "<gold>Plotborders</gold><gray> » </gray>")
	public static final Message PREFIX = new Message("prefix");
	@MessageMeta(value = "<red>You have to be a player to use this command!</red>")
	public static final Message NO_CONSOLE = new Message("error.need_to_be_player");
	@MessageMeta(value = "<red>Please use <syntax>.</red>")
	public static final Message WRONG_SYNTAX = new Message("error.syntax");
	@MessageMeta(value = "<red>You don't have enough permission!</red>")
	public static final Message NO_PERMISSION = new Message("error.no_permission");
	@MessageMeta(value = "<red>You have to wait <remaining> more seconds.")
	public static final Message COOLDOWN = new Message("error.cooldown");
	@MessageMeta(value = "<red>You have to be on a plot for this function.")
	public static final Message NOT_ON_PLOT = new Message("error.not_on_plot");
	@MessageMeta(value = "<red>This is not your plot. You can only modify your own plot.</red>")
	public static final Message NOT_YOUR_PLOT = new Message("error.not_your_plot");
	@MessageMeta(value = "<message:prefix>Your plot wall has been changed.")
	public static final Message WALL_CHANGED = new Message("wall_changed");
	@MessageMeta(value = "<message:prefix>Your plot border has been changed.")
	public static final Message BORDER_CHANGED = new Message("border_changed");
	@MessageMeta(value = "<gold>Next Page</gold>")
	public static final Message NEXT_PAGE = new Message("gui.next_page");
	@MessageMeta(value = "<gold>Previous Page</gold>")
	public static final Message PREV_PAGE = new Message("gui.prev_page");
	@MessageMeta(value = "<message:prefix>Successfully reloaded files.")
	public static final Message RELOAD_SUCCESS = new Message("reload.success");
	@MessageMeta(value = "<red>Error while reloading PlotBorders. See console for more information.</red>")
	public static final Message RELOAD_FAILED = new Message("reload.failed");

	@MessageGroupMeta(path = "walls", comment = {
			"Custom one's to simplify example walls.yml and borders.yml.",
			"Refered to with <message:walls.no_perm>"})
	@MessageMeta(value = "<red>No permission!</red>")
	public static final Message WALLS_NO_PERM = new Message("walls.no_perm");
	@MessageMeta(value = "<gray>Group: </gray>")
	public static final Message WALLS_GROUP = new Message("walls.group");
	@MessageMeta(value = "<gray>Click to apply</gray>")
	public static final Message WALLS_CLICK = new Message("walls.click");

	@MessageMeta(value = "<red>No permission!</red>")
	public static final Message BORDER_NO_PERM = new Message("border.no_perm");
	@MessageMeta(value = "<gray>Group: </gray>")
	public static final Message BORDER_GROUP = new Message("border.group");
	@MessageMeta(value = "<gray>Click to apply</gray>")
	public static final Message BORDER_CLICK = new Message("border.click");
}
