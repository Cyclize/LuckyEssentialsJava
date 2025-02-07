package id.luckynetwork.dev.lyrams.lej.commands.essentials;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.Flag;
import id.luckynetwork.dev.lyrams.lej.commands.api.CommandClass;
import id.luckynetwork.dev.lyrams.lej.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SmiteCommand extends CommandClass {

    @CommandMethod("smite [target]")
    @CommandDescription("Summons lighting where you're looking at or at other player")
    public void smiteCommand(
            final @NonNull CommandSender sender,
            final @NonNull @Argument(value = "target", description = "The target player", defaultValue = "self", suggestions = "players") String targetName,
            final @Nullable @Flag(value = "damage", aliases = "d", description = "Should the lighting not do damage?") Boolean damage,
            final @Nullable @Flag(value = "silent", aliases = "s", description = "Should the target not be notified?") Boolean silent
    ) {
        if (!Utils.checkPermission(sender, "smite")) {
            return;
        }

        Set<Location> locations = new HashSet<>();
        TargetsCallback targets = new TargetsCallback();
        if (targetName.equals("self") && sender instanceof Player) {
            locations.add(((Player) sender).getTargetBlock(null, 120).getLocation());
        } else {
            targets = this.getTargets(sender, targetName);
        }

        TargetsCallback finalTargets = targets;
        plugin.getConfirmationManager().requestConfirmation(() -> {
                    if (!finalTargets.isEmpty()) {
                        locations.addAll(finalTargets.stream().map(Player::getLocation).collect(Collectors.toList()));
                        if (silent == null || !silent) {
                            finalTargets.forEach(target -> target.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eYou have been smitten!"));
                        }
                    }

                    if (damage == null || !damage) {
                        locations.forEach(location -> location.getWorld().strikeLightning(location));
                    } else {
                        locations.forEach(location -> location.getWorld().strikeLightningEffect(location));
                    }

                    boolean others = !finalTargets.isEmpty() && finalTargets.size() > 1;
                    if (others) {
                        sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eSmitten §d" + finalTargets.size() + " §eplayers.");
                    } else if ((!(sender instanceof Player)) || (finalTargets.doesNotContain((Player) sender) && !targetName.equals("self"))) {
                        finalTargets.stream().findFirst().ifPresent(target -> sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eSmitten §d" + target.getName() + "§e."));
                    }
                }, this.canSkip("smite", targets, sender));
    }

}
