package me.piggyster.spawners.command;

import me.piggyster.api.command.SubCommand;
import me.piggyster.api.config.ConfigManager;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveSubcommand extends SubCommand {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    public String getName() {
        return "give";
    }

    //spawner give Piggyster ENDERMAN 10000
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
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch(NumberFormatException ex) {
            configManager.getMessage("INVALID-NUMBER").setPlaceholder("%found%", args[2]).send(sender);
            return;
        }
        ItemStack spawner = plugin.getSettingsService().getSpawner(type);
        spawner.setAmount(amount);
        target.getInventory().addItem(spawner);
        plugin.getConfigManager().getMessage("SPAWNER-GIVEN")
                .setPlaceholder("%player%", target.getName())
                .setPlaceholder("%amount%", amount)
                .setPlaceholder("%type%", StringUtils.entityToString(type))
                .send(sender);
        plugin.getConfigManager().getMessage("SPAWNER-RECEIVED")
                .setPlaceholder("%amount%", amount)
                .setPlaceholder("%type%", StringUtils.entityToString(type))
                .send(target);
    }
}
