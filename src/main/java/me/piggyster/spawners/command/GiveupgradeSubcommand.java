package me.piggyster.spawners.command;

import me.piggyster.api.command.SubCommand;
import me.piggyster.api.config.ConfigManager;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveupgradeSubcommand extends SubCommand {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    public String getName() {
        return "giveupgrade";
    }
    //spawner giveupgrade Piggyster ENDERMAN STAR 10000
    public void run(CommandSender sender, String[] args) {
        ConfigManager configManager = plugin.getConfigManager();
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) {
            configManager.getMessage("INVALID-PLAYER").setPlaceholder("%found%", args[0]).send(sender);
            return;
        }
        EntityType type;
        try {
            type = EntityType.valueOf(args[1].toUpperCase());
        } catch(IllegalArgumentException ex) {
            configManager.getMessage("INVALID-ENTITY-TYPE").setPlaceholder("%found%", args[1]).send(sender);
            return;
        }
        SpawnerUpgrade upgrade = plugin.getUpgradeService().getUpgrade(type, args[2]);
        if(upgrade == null) {
            configManager.getMessage("INVALID-UPGRADE").setPlaceholder("%found%", args[2]).send(sender);
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch(NumberFormatException ex) {
            configManager.getMessage("INVALID-NUMBER").setPlaceholder("%found%", args[3]).send(sender);
            return;
        }
        ItemStack spawner = plugin.getSettingsService().getSpawner(type, upgrade);
        spawner.setAmount(amount);
        target.getInventory().addItem(spawner);
    }
}