package me.piggyster.spawners.listener;

import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.data.DataService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class ChunkListener implements Listener {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        plugin.getDataService().handleChunkLoad(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.getDataService().handleChunkUnload(event.getChunk());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        for(Chunk chunk : event.getWorld().getLoadedChunks()) {
            plugin.getDataService().handleChunkLoad(chunk);
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        for(Chunk chunk : event.getWorld().getLoadedChunks()) {
            plugin.getDataService().handleChunkUnload(chunk);
        }
    }
}
