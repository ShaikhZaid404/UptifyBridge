package site.uptify.bridge.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Translates standard '&' color codes and '&#RRGGBB' hex color codes.
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        // Support modern hex colors if on 1.16+
        try {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                String hexCode = matcher.group(1);
                matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
            }
            matcher.appendTail(buffer);
            message = buffer.toString();
        } catch (Throwable ignored) {
            // Older Bukkit fallback
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Sends a colorized message to a CommandSender.
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(colorize(message));
        }
    }

    /**
     * Creates a clickable URL text component with hover tooltip.
     */
    public static TextComponent createClickableUrl(String buttonText, String url, String hoverText) {
        TextComponent component = new TextComponent(colorize(buttonText));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        if (hoverText != null && !hoverText.isEmpty()) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(colorize(hoverText))));
        }
        return component;
    }

    /**
     * Creates a clickable command text component with hover tooltip.
     */
    public static TextComponent createClickableCommand(String buttonText, String command, String hoverText) {
        TextComponent component = new TextComponent(colorize(buttonText));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        if (hoverText != null && !hoverText.isEmpty()) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(colorize(hoverText))));
        }
        return component;
    }
}
