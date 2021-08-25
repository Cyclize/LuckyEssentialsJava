package id.luckynetwork.dev.lyrams.lej.commands.essentials;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.Flag;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import id.luckynetwork.dev.lyrams.lej.commands.api.CommandClass;
import id.luckynetwork.dev.lyrams.lej.config.Config;
import id.luckynetwork.dev.lyrams.lej.enums.InventoryScope;
import id.luckynetwork.dev.lyrams.lej.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClearCommand extends CommandClass {

    @CommandMethod("clearinventory|clear|ci [target] [type]")
    @CommandDescription("Clear items")
    public void clearCommand(
            final @NonNull CommandSender sender,
            final @NonNull @Argument(value = "target", description = "The target player", defaultValue = "self", suggestions = "players") String targetName,
            final @NonNull @Argument(value = "type", description = "hand/all/armor", defaultValue = "all", suggestions = "inventoryScopes") String type,
            final @Nullable @Flag(value = "silent", aliases = "s", description = "Should it not notify the target of their inventory changes?") Boolean silent
    ) {
        if (!Utils.checkPermission(sender, "clear")) {
            return;
        }

        TargetsCallback targets;
        InventoryScope inventoryScope;
        if (!InventoryScope.getType(targetName).equals(InventoryScope.UNKNOWN) && sender instanceof Player) {
            // the sender wants to clear their own inventory
            targets = this.getTargets(sender, "self");
            inventoryScope = InventoryScope.getType(targetName);
        } else {
            targets = this.getTargets(sender, targetName);
            inventoryScope = InventoryScope.getType(type);
        }

        if (targets.notifyIfEmpty()) {
            sender.sendMessage(Config.PREFIX + "§cNo targets found!");
            return;
        }

        if (inventoryScope.equals(InventoryScope.UNKNOWN)) {
            sender.sendMessage(Config.PREFIX + "§cUnknown fix type §l" + type + "§c!");
            return;
        }

        boolean others = targets.size() > 1;
        if (others && !Utils.checkPermission(sender, true, "fix")) {
            return;
        }

        targets.forEach(target -> {
            switch (inventoryScope) {
                case ALL: {
                    target.getInventory().clear();
                    target.getInventory().setArmorContents(null);
                    target.updateInventory();

                    if (silent == null || !silent) {
                        target.sendMessage(Config.PREFIX + "§eYour inventory has been cleared!");
                    }
                    break;
                }
                case HAND: {
                    target.getInventory().clear(target.getInventory().getHeldItemSlot());
                    target.updateInventory();

                    if (silent == null || !silent) {
                        target.sendMessage(Config.PREFIX + "§eYour item in hand has been cleared!");
                    }
                    break;
                }
                case ARMOR: {
                    target.getInventory().setArmorContents(null);
                    target.updateInventory();

                    if (silent == null || !silent) {
                        target.sendMessage(Config.PREFIX + "§eYour armor has been cleared!");
                    }
                    break;
                }
                case SPECIFIC: {
                    target.getInventory().remove(inventoryScope.getItemStack().getType());
                    target.updateInventory();

                    if (silent == null || !silent) {
                        target.sendMessage(Config.PREFIX + "§eCleared all §d" + inventoryScope.getItemStack().getType() + " §efrom your inventory!");
                    }
                    break;
                }
            }
        });

        if (others) {
            sender.sendMessage(Config.PREFIX + "§eCleared §6" + inventoryScope.getDisplay() + " §for §d" + targets.size() + " players!");
        } else if ((!(sender instanceof Player)) || (targets.doesNotContain((Player) sender) && !targetName.equals("self"))) {
            targets.stream().findFirst().ifPresent(target -> sender.sendMessage(Config.PREFIX + "§eCleared §6" + inventoryScope.getDisplay() + " §efor §d" + target.getName() + "§e!"));
        }
    }

    @Suggestions("inventoryScopes")
    public List<String> inventoryScopes(CommandContext<CommandSender> context, String current) {
        return Stream.of("all", "hand", "armor")
                .filter(it -> it.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

}
