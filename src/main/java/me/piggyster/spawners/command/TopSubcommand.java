package me.piggyster.spawners.command;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import me.piggyster.api.command.SubCommand;
import me.piggyster.api.util.NumberUtil;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.menus.TopMenu;
import me.piggyster.spawners.stacked.StackedSpawner;
import me.piggyster.spawners.top.IslandTopService;
import me.piggyster.spawners.top.SpawnerIsland;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class TopSubcommand extends SubCommand {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    public String getName() {
        return "top";
    }

    public void run(CommandSender sender, String[] args) {
        IslandTopService service = plugin.getIslandTopService();
        if(args.length == 1 && args[0].equalsIgnoreCase("update")) {
            plugin.getConfigManager().getMessage("STARTED-ISLAND-TOP-UPDATE").send(sender);
            service.update().thenAccept(ms -> plugin.getConfigManager().getMessage("FINISHED-ISLAND-TOP-UPDATE").setPlaceholder("%ms%", ms));
            return;
        }

        if(!(sender instanceof Player player)) return;

        if(service.isLoaded()) {
            plugin.getConfigManager().getMessage("ERROR-ISLAND-TOP-UPDATING").send(player);
            return;
        }
        TopMenu topMenu = new TopMenu(player);
        topMenu.open();
    }



}
