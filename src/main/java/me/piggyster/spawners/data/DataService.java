package me.piggyster.spawners.data;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.common.cache.*;
import me.piggyster.api.service.PluginService;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.stacked.StackedEntity;
import me.piggyster.spawners.stacked.StackedSpawner;
import me.piggyster.spawners.top.SpawnerIsland;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import me.piggyster.spawners.utils.ChunkPosition;
import me.piggyster.spawners.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DataService implements PluginService<SpawnerPlugin> {

    private SpawnerDatabase database;
    private SpawnerPlugin plugin;

    private Map<ChunkPosition, Map<Location, StackedSpawner>> spawners;
    private Map<UUID, StackedEntity> entities;

    public DataService(SpawnerPlugin plugin) {
        this.plugin = plugin;
    }


    public void initialize() {
        database = new SpawnerDatabase(plugin);
        spawners = new HashMap<>();
        entities = new HashMap<>();
    }

    public StackedSpawner getStackedSpawner(CreatureSpawner creatureSpawner) {
        return getStackedSpawner(creatureSpawner.getLocation());
    }

    public boolean isStackedSpawner(Location location) {
        if(!(location.getBlock().getState() instanceof CreatureSpawner)) return false;
        StackedSpawner spawner = getStackedSpawner(location);
        return spawner != null;
    }

    public StackedSpawner getStackedSpawner(Location location) {
        ChunkPosition chunkPos = new ChunkPosition(location);
        Map<Location, StackedSpawner> spawners = this.spawners.getOrDefault(chunkPos, new HashMap<>());
        return spawners.get(location);
    }

    public void addStackedSpawner(StackedSpawner stackedSpawner) {
        ChunkPosition chunkPos = new ChunkPosition(stackedSpawner.getLocation());
        Map<Location, StackedSpawner> map = spawners.getOrDefault(chunkPos, new HashMap<>());
        map.put(stackedSpawner.getLocation(), stackedSpawner);
        spawners.put(chunkPos, map);
        database.insertSpawner(stackedSpawner);
    }

    public void updateStackedSpawner(StackedSpawner stackedSpawner) {
        database.insertSpawner(stackedSpawner);
    }

    public void removeStackedSpawner(StackedSpawner stackedSpawner) {
        ChunkPosition chunkPos = new ChunkPosition(stackedSpawner.getLocation());
        Map<Location, StackedSpawner> map = spawners.getOrDefault(chunkPos, new HashMap<>());
        map.remove(stackedSpawner.getLocation());
        spawners.put(chunkPos, map);
        database.removeSpawner(stackedSpawner);
    }

    public boolean isStackedEntity(LivingEntity livingEntity) {
        return entities.containsKey(livingEntity.getUniqueId());
    }

    public StackedEntity getStackedEntity(LivingEntity livingEntity) {
        return entities.get(livingEntity.getUniqueId());
    }

    public void addStackedEntity(StackedEntity stackedEntity) {
        entities.put(stackedEntity.getLivingEntity().getUniqueId(), stackedEntity);
    }

    public void removeStackedEntity(StackedEntity stackedEntity) {
        entities.remove(stackedEntity.getLivingEntity().getUniqueId());
    }

    public List<StackedSpawner> getSpawnersInChunk(Chunk chunk) {
        Map<Location, StackedSpawner> map = spawners.get(new ChunkPosition(chunk));
        if(map == null) return new ArrayList<>();
        return new ArrayList<>(map.values());
    }

    public void handleChunkLoad(Chunk chunk) {
        if(chunk.getTileEntities().length == 0) return;
        database.loadSpawnersInChunk(chunk, spawners);
    }

    public Stream<StackedEntity> getNearbyStackedEntities(StackedEntity stackedEntity) {
        double searchRange = Math.pow(4, 2);
        Location baseLoc = stackedEntity.getLivingEntity().getLocation();
        return LocationUtils.getNearbyChunks(baseLoc)
                .map(Chunk::getEntities).flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(e -> e.getType() == stackedEntity.getLivingEntity().getType())
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(this::isStackedEntity)
                .map(this::getStackedEntity)
                .filter(e -> e.getUpgrade() == stackedEntity.getUpgrade())
                .filter(e -> e.getStackAmount() < e.getStackLimit())
                .filter(e -> e.getLivingEntity().getLocation().distanceSquared(baseLoc) < searchRange);
    }

    public CompletableFuture<Map<UUID, SpawnerIsland>> loadIslandTop() {
        return database.loadIslandTop();
    }


    public List<StackedEntity> getStackedEntities() {
        return new ArrayList<>(entities.values());
    }

    public void handleChunkUnload(Chunk chunk) {
        for(Entity entity : chunk.getEntities()) {
            if(entity instanceof LivingEntity livingEntity && isStackedEntity(livingEntity)) {
                livingEntity.remove();
                removeStackedEntity(getStackedEntity(livingEntity));
            }
        }
        spawners.remove(new ChunkPosition(chunk));
    }

    public void shutdown() {
        database.close();
    }

    public SpawnerPlugin getPlugin() {
        return plugin;
    }
}
