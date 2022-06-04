package me.piggyster.spawners;

import me.piggyster.api.config.ConfigManager;
import me.piggyster.api.service.Service;
import me.piggyster.spawners.command.SpawnerCommand;
import me.piggyster.spawners.data.DataService;
import me.piggyster.spawners.data.SettingsService;
import me.piggyster.spawners.economy.EconomyProvider;
import me.piggyster.spawners.economy.impl.DefaultEconomyProvider;
import me.piggyster.spawners.items.ItemService;
import me.piggyster.spawners.listener.ChunkListener;
import me.piggyster.spawners.listener.EntityListener;
import me.piggyster.spawners.listener.ItemListener;
import me.piggyster.spawners.listener.SpawnerListener;
import me.piggyster.spawners.loot.LootService;
import me.piggyster.spawners.stacked.StackedEntity;
import me.piggyster.spawners.top.IslandTopService;
import me.piggyster.spawners.upgrades.UpgradeService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SpawnerPlugin extends JavaPlugin {
    private static SpawnerPlugin instance;
    private ConfigManager configManager;
    private DataService dataService;
    private UpgradeService upgradeService;
    private LootService lootService;
    private SettingsService settingsService;
    private ItemService itemService;
    private IslandTopService islandTopService;

    private EconomyProvider economyProvider;

    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this, new File(getDataFolder(), "mobs"));

        configManager = new ConfigManager(this);
        configManager.createConfig("config");
        configManager.createConfig("mobs");
        configManager.createConfig("menus");
        configManager.setMessageSection("messages");

        settingsService = Service.provide(SettingsService.class, new SettingsService(this));
        dataService = Service.provide(DataService.class, new DataService(this));
        upgradeService = Service.provide(UpgradeService.class, new UpgradeService(this));
        lootService = Service.provide(LootService.class, new LootService(this));
        itemService = Service.provide(ItemService.class, new ItemService());
        islandTopService = Service.provide(IslandTopService.class, new IslandTopService());

        economyProvider = DefaultEconomyProvider.get();

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new SpawnerListener(), this);
        pluginManager.registerEvents(new ChunkListener(), this);
        pluginManager.registerEvents(new EntityListener(), this);
        pluginManager.registerEvents(new ItemListener(), this);
        new SpawnerCommand();

        for(World world : Bukkit.getWorlds()) {
            for(Chunk chunk : world.getLoadedChunks()) {
                dataService.handleChunkLoad(chunk);
            }
        }
    }

    public void onDisable() {
        instance = null;
        dataService.getStackedEntities().forEach(StackedEntity::remove);
        dataService.shutdown();
    }

    public static SpawnerPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public IslandTopService getIslandTopService() {
        return islandTopService;
    }

    public EconomyProvider getEconomyProvider() {
        return economyProvider;
    }

    public void setEconomyProvider(EconomyProvider economyProvider) {
        this.economyProvider = economyProvider;
    }

    public DataService getDataService() {
        return dataService;
    }

    public LootService getLootService() {
        return lootService;
    }

    public UpgradeService getUpgradeService() {
        return upgradeService;
    }

    public ItemService getItemService() {
        return itemService;
    }
}
