package me.piggyster.spawners.top;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import me.piggyster.api.service.PluginService;
import me.piggyster.spawners.SpawnerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class IslandTopService implements PluginService<SpawnerPlugin> {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private List<SpawnerIsland> topIslands;
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
        }, 50, 12000);
    }

    public CompletableFuture<Long> update() {
        topIslands = null;
        CompletableFuture<Long> future = new CompletableFuture<>();
        long ms = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            topIslands = new ArrayList<>();
            plugin.getDataService().loadIslandTop().thenAccept(map -> {
                map.forEach((uuid, spawnerIsland) -> {
                    topIslands.add(spawnerIsland);
                });
                Collections.sort(topIslands);
                topIslands = topIslands.stream().limit(10).collect(Collectors.toList());
            });
            future.complete(System.currentTimeMillis() - ms);
        });
        return future;
    }

    public List<SpawnerIsland> getTopIslands() {
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
