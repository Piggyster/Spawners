package me.piggyster.spawners.top;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import me.piggyster.api.service.PluginService;
import me.piggyster.spawners.SpawnerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class IslandTopService implements PluginService<SpawnerPlugin> {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private TreeMap<SpawnerIsland, Island> topIslands;
    private Map<EntityType, String> skullURLs;

    public void initialize() {
        initializeSchedulers();
        skullURLs = new HashMap<>();
        ConfigurationSection section = plugin.getConfigManager().getConfig("mobs").getConfigurationSection("mobs");
        section.getKeys(false).forEach(key -> {
            ConfigurationSection mobSection = section.getConfigurationSection(key);
            if(mobSection.isString("skull")) {
                skullURLs.put(EntityType.valueOf(key), mobSection.getString("skull"));
            }
        });
    }


    public void initializeSchedulers() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> plugin.getConfigManager().getMessage("ISLAND-TOP-UPDATING").send(player));
            update().thenAccept((ms) -> {
                Bukkit.getOnlinePlayers().forEach(player -> plugin.getConfigManager().getMessage("ISLAND-TOP-UPDATED").send(player));
            });
        }, 400, 12000);
    }

    public CompletableFuture<Long> update() {
        CompletableFuture<Long> future = new CompletableFuture<>();
        long ms = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            topIslands = new TreeMap<>();
            for(Island island : SuperiorSkyblockAPI.getGrid().getIslands()) {
                plugin.getDataService().loadSpawnersInIsland(island).thenAccept(map -> {
                    SpawnerIsland spawnerIsland = new SpawnerIsland(map);
                    topIslands.put(spawnerIsland, island);
                });
            }
            future.complete(System.currentTimeMillis() - ms);
        });
        return future;
    }

    public TreeMap<SpawnerIsland, Island> getTopIslands() {
        return topIslands;
    }

    public String getSkullURL(EntityType entityType) {
        return skullURLs.get(entityType);
    }

    public boolean isLoaded() {
        return topIslands != null;
    }

    public SpawnerPlugin getPlugin() {
        return plugin;
    }
}
