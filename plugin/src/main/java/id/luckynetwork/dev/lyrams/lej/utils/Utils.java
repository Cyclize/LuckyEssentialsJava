package id.luckynetwork.dev.lyrams.lej.utils;

import id.luckynetwork.dev.lyrams.lej.config.Config;
import id.luckynetwork.dev.lyrams.lej.enums.TrueFalseType;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class Utils {

    public String colorize(String toColor) {
        return ChatColor.translateAlternateColorCodes('&', toColor);
    }

    /**
     * see {@link Utils#checkPermission(CommandSender, String, boolean, boolean, String)}
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkPermission(CommandSender sender, String permission) {
        return Utils.checkPermission(sender, permission, false, false, null);
    }

    /**
     * see {@link Utils#checkPermission(CommandSender, String, boolean, boolean, String)}
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkPermission(CommandSender sender, boolean others, String permission) {
        return Utils.checkPermission(sender, permission, others, false, null);
    }

    /**
     * checks if the command sender has a certain permission
     *
     * @param sender         the sender
     * @param permission     the permission
     * @param others         is the sender executing this to other player(s)?
     * @param showPermission should the permission deny message show the required permission?
     * @param command        the command, if set to null then the permission deny message will show the executed command.
     * @return see {@link CommandSender#hasPermission(String)}
     */
    public boolean checkPermission(CommandSender sender, String permission, boolean others, boolean showPermission, @Nullable String command) {
        permission = "luckyessentials." + permission.toLowerCase();
        if (others) {
            permission += ".others";
        }

        if (sender.hasPermission(permission)) {
            return true;
        }

        if (showPermission) {
            if (command != null) {
                sender.sendMessage(Config.PREFIX + "§cYou need to have the required permission §l" + permission + " to do §l" + command + "§c!");
            } else {
                sender.sendMessage(Config.PREFIX + "§cYou need to have the required permission §l" + permission + " to do that!");
            }
        } else {
            if (command != null) {
                sender.sendMessage(Config.PREFIX + "§cYou don't have the required permission to do §l" + command + "§c!");
            } else {
                sender.sendMessage(Config.PREFIX + "§cYou don't have the required permission to do that!");
            }
        }
        return false;
    }

    /**
     * colorizes a string from {@link TrueFalseType} to green if true and to red if false
     *
     * @param state         the state
     * @param trueFalseType see {@link TrueFalseType}
     * @return a green {@link TrueFalseType#getIfTrue()} if true and a red {@link TrueFalseType#getIfFalse()} if false
     */
    public String colorizeTrueFalse(boolean state, TrueFalseType trueFalseType) {
        if (state) {
            return "§a" + trueFalseType.getIfTrue();
        } else {
            return "§c" + trueFalseType.getIfFalse();
        }
    }

    /**
     * Replaces all characters in {@link Config#PREFIX} with a blank space
     */
    public String getPrefixSpacePlaceholder() {
        String prefix = ChatColor.stripColor(Config.PREFIX);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < prefix.length(); i++) {
            stringBuilder.append(" ");
        }

        return stringBuilder.toString();
    }
}
