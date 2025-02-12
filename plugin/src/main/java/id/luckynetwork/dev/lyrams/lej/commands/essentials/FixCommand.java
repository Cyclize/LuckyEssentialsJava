package id.luckynetwork.dev.lyrams.lej.commands.essentials;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.Flag;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import id.luckynetwork.dev.lyrams.lej.commands.api.CommandClass;
import id.luckynetwork.dev.lyrams.lej.enums.InventoryScope;
import id.luckynetwork.dev.lyrams.lej.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FixCommand extends CommandClass {

    @CommandMethod("fix|repair [target] [type]")
    @CommandDescription("Repair items")
    public void fixCommand(
            final @NonNull CommandSender sender,
            final @NonNull @Argument(value = "target", description = "The target player", defaultValue = "self", suggestions = "players") String targetName,
            final @NonNull @Argument(value = "type", description = "hand/all/armor", defaultValue = "hand", suggestions = "inventoryScopes") String type,
            final @Nullable @Flag(value = "silent", aliases = "s", description = "Should the target not be notified?") Boolean silent
    ) {
        if (!Utils.checkPermission(sender, "fix")) {
            return;
        }

        TargetsCallback targets;
        InventoryScope inventoryScope;
        if (!InventoryScope.getType(targetName).equals(InventoryScope.UNKNOWN) && sender instanceof Player) {
            // the sender wants to fix their own items
            targets = this.getTargets(sender, "self");
            inventoryScope = InventoryScope.getType(targetName);
        } else {
            targets = this.getTargets(sender, targetName);
            inventoryScope = InventoryScope.getType(type);
        }

        if (targets.notifyIfEmpty()) {
            sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cNo targets found!");
            return;
        }

        if (inventoryScope.equals(InventoryScope.UNKNOWN)) {
            sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cUnknown inventory scope §l" + type + "§c!");
            return;
        }

        boolean others = targets.size() > 1 || (sender instanceof Player && targets.doesNotContain((Player) sender));
        if (others && !Utils.checkPermission(sender, true, "fix")) {
            return;
        }

        plugin.getConfirmationManager().requestConfirmation(() -> {
            targets.forEach(target -> {
                switch (inventoryScope) {
                    case ALL: {
                        for (ItemStack content : target.getInventory().getContents()) {
                            if (content == null || content.getType().isBlock() || content.getDurability() == 0 || content.getType().getMaxDurability() < 1) {
                                continue;
                            }

                            content.setDurability((short) 0);
                        }
                        for (ItemStack content : target.getInventory().getArmorContents()) {
                            if (content == null || content.getType().isBlock() || content.getDurability() == 0 || content.getType().getMaxDurability() < 1) {
                                continue;
                            }

                            content.setDurability((short) 0);
                        }
                        target.updateInventory();

                        if (silent == null || !silent) {
                            target.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eYour items have been repaired.");
                        }
                        break;
                    }
                    case HAND: {
                        ItemStack itemInHand = plugin.getVersionSupport().getItemInHand(target);
                        if (itemInHand == null || itemInHand.getType().isBlock() || itemInHand.getDurability() == 0 || itemInHand.getType().getMaxDurability() < 1) {
                            return;
                        }

                        itemInHand.setDurability((short) 0);
                        target.updateInventory();

                        if (silent == null || !silent) {
                            target.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eYour item in hand has been repaired.");
                        }
                        break;
                    }
                    case ARMOR: {
                        for (ItemStack content : target.getInventory().getArmorContents()) {
                            if (content == null || content.getType().isBlock() || content.getDurability() == 0 || content.getType().getMaxDurability() < 1) {
                                continue;
                            }

                            content.setDurability((short) 0);
                        }
                        target.updateInventory();

                        if (silent == null || !silent) {
                            target.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eYour armor has been repaired.");
                        }
                        break;
                    }
                    case SPECIFIC: {
                        for (ItemStack content : target.getInventory().getContents()) {
                            if (content == null || content.getType().isBlock() || content.getDurability() == 0 || content.getType().getMaxDurability() < 1) {
                                continue;
                            }

                            if (content.getType() != inventoryScope.getItemStack().getType()) {
                                continue;
                            }

                            content.setDurability((short) 0);
                        }

                        for (ItemStack content : target.getInventory().getArmorContents()) {
                            if (content == null || content.getType().isBlock() || content.getDurability() == 0 || content.getType().getMaxDurability() < 1) {
                                continue;
                            }

                            if (content.getType() != inventoryScope.getItemStack().getType()) {
                                continue;
                            }

                            content.setDurability((short) 0);
                        }
                        target.updateInventory();

                        if (silent == null || !silent) {
                            target.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eRepaired all §d" + inventoryScope.getItemStack().getType() + " §ein your inventory.");
                        }
                        break;
                    }
                }
            });

            if (others) {
                if (targets.size() == 1) {
                    targets.stream().findFirst().ifPresent(target -> sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eRepaired §6" + inventoryScope.getDisplay() + " §efor §d" + target.getName() + "§e."));
                } else {
                    sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eRepaired §6" + inventoryScope.getDisplay() + " §efor §d" + targets.size() + " §eplayers.");
                }
            } else if ((!(sender instanceof Player)) || (targets.doesNotContain((Player) sender) && !targetName.equals("self"))) {
                targets.stream().findFirst().ifPresent(target -> sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eRepaired §6" + inventoryScope.getDisplay() + " §efor §d" + target.getName() + "§e."));
            }
        }, this.canSkip("repair", targets, sender));
    }

    @Suggestions("inventoryScopes")
    public List<String> inventoryScopes(CommandContext<CommandSender> context, String current) {
        return Stream.of("all", "hand", "armor")
                .filter(it -> it.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

}
