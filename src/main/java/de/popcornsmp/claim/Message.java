package de.popcornsmp.claim;

import org.bukkit.command.CommandSender;

public final class Message {
    public static final String PREFIX = "§6§lPopcornSMP§r§8 » §r";
    public static final String INFO = PREFIX + "§7";
    public static final String HIGHLIGHT = "§6";
    public static final String ERROR = PREFIX + "§c";

    private Message() {
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(INFO + message);
    }

    public static void sendHighlight(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + "§7" + message);
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(ERROR + message);
    }

    public static String highlight(String text) {
        return HIGHLIGHT + text + "§7";
    }
}
