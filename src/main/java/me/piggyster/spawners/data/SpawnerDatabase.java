package me.piggyster.spawners.data;

import com.bgsoftware.superiorskyblock.api.island.Island;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.stacked.StackedSpawner;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import me.piggyster.spawners.utils.ChunkPosition;
import me.piggyster.spawners.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpawnerDatabase {

    private String type;
    private Connection connection;
    private SpawnerPlugin plugin;
    private ExecutorService executorService;

    public SpawnerDatabase(SpawnerPlugin plugin) {
        this.plugin = plugin;
        try {
            initialize();
        } catch(SQLException | NullPointerException | ClassNotFoundException ex) {
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public CompletableFuture<Map<EntityType, Map<SpawnerUpgrade, Integer>>> loadSpawnersInIsland(Island island) {
        CompletableFuture<Map<EntityType, Map<SpawnerUpgrade, Integer>>> future = new CompletableFuture<>();
        executorService.submit(() -> {
            Map<EntityType, Map<SpawnerUpgrade, Integer>> map = new HashMap<>();
            try {
                for(Chunk chunk : island.getAllChunks()) {
                    PreparedStatement ps = connection.prepareStatement("SELECT * FROM spawners WHERE chunk = ? AND world = ?;");
                    ps.setString(1, LocationUtils.chunkToString(chunk));
                    ps.setString(2, chunk.getWorld().getName());
                    ResultSet set = ps.executeQuery();
                    while(set.next()) {
                        EntityType type = EntityType.valueOf(set.getString("type"));
                        String upgrade = set.getString("upgrade");
                        int amount = set.getInt("amount");
                        SpawnerUpgrade spawnerUpgrade = plugin.getUpgradeService().getUpgrade(type, upgrade);
                        Map<SpawnerUpgrade, Integer> map2 = map.computeIfAbsent(type, k -> new HashMap<>());
                        map2.put(spawnerUpgrade, map2.getOrDefault(spawnerUpgrade, 0) + amount);
                    }
                }
                future.complete(map);
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        });
        return future;
    }

    public void loadSpawnersInChunk(Chunk chunk, Map<ChunkPosition, Map<Location, StackedSpawner>> spawners) {
        executorService.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM spawners WHERE chunk = ? AND world = ?;");
                ps.setString(1, LocationUtils.chunkToString(chunk));
                ps.setString(2, chunk.getWorld().getName());
                ResultSet set = ps.executeQuery();
                while(set.next()) {
                    System.out.println("looping");
                    Location location = LocationUtils.locationFromString(set.getString("location"), chunk.getWorld());
                    int amount = set.getInt("amount");
                    EntityType entityType = EntityType.valueOf(set.getString("type"));
                    String upgrade = set.getString("upgrade");
                    StackedSpawner spawner = new StackedSpawner(location, entityType, amount, upgrade);
                    spawners.computeIfAbsent(new ChunkPosition(chunk), k -> new HashMap<>()).put(location, spawner);
                }
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    public void insertSpawner(StackedSpawner stackedSpawner) {
        executorService.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO spawners(type, amount, location, chunk, world, upgrade) VALUES(?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE amount = VALUES(amount), upgrade = VALUES(upgrade);");
                ps.setString(1, stackedSpawner.getEntityType().toString());
                ps.setInt(2, stackedSpawner.getStackAmount());
                ps.setString(3, LocationUtils.locationToString(stackedSpawner.getLocation(), false));
                ps.setString(4, LocationUtils.chunkToString(stackedSpawner.getLocation().getChunk()));
                ps.setString(5, stackedSpawner.getLocation().getWorld().getName());
                ps.setString(6, stackedSpawner.getUpgrade().getName() == null ? null : stackedSpawner.getUpgradeName());
                ps.execute();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    public void removeSpawner(StackedSpawner stackedSpawner) {
        executorService.submit(() -> {
            try {
                PreparedStatement ps = connection.prepareStatement("DELETE FROM spawners WHERE location = ? AND chunk = ? AND world = ?;");
                ps.setString(1, LocationUtils.locationToString(stackedSpawner.getLocation(), false));
                ps.setString(2, LocationUtils.chunkToString(stackedSpawner.getLocation().getChunk()));
                ps.setString(3, stackedSpawner.getLocation().getWorld().getName());
                ps.execute();
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    public void close() {
        try {
            executorService.shutdown();
            connection.close();
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void initialize() throws SQLException, NullPointerException, ClassNotFoundException {
        ConfigurationSection section = plugin.getConfigManager().getConfig("config").getConfigurationSection("database");
        type = section.getString("type");
        executorService = Executors.newSingleThreadExecutor();
        if(type.equalsIgnoreCase("mysql")) {
            String host = section.getString("host");
            int port = section.getInt("port");
            String database = section.getString("database");
            String username = section.getString("username");
            String password = section.getString("password");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
        } else if(type.equalsIgnoreCase("h2")) {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:" + plugin.getDataFolder().getAbsolutePath() + "/storage;MODE=MYSQL");
        }
        if(connection == null) {
            throw new SQLException("Cannot create connection.");
        }
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS spawners(type VARCHAR(24), amount INTEGER NOT NULL, location VARCHAR(32), chunk VARCHAR(16), world VARCHAR(16), upgrade VARCHAR(32), UNIQUE(chunk, world, location));").execute();
    }
}
