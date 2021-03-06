package me.piggyster.spawners.data;

import de.tr7zw.nbtapi.NBTItem;
import me.piggyster.api.config.ConfigManager;
import me.piggyster.api.menu.item.SimpleItem;
import me.piggyster.api.service.PluginService;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.stacked.StackedSpawner;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class SettingsService implements PluginService<SpawnerPlugin> {

    private SpawnerPlugin plugin;

    private SimpleItem upgradeDrop;
    private SimpleItem spawner;

    private int mobMaxStack;
    private int spawnerMaxStack;

    private boolean killStack;
    private boolean chunkSpawners;

    private String entityName;
    private String entityNameUpgraded;
    private String itemName;

    private int hitCooldown;

    public SettingsService(SpawnerPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        ConfigManager configManager = plugin.getConfigManager();
        FileConfiguration mainConfig = configManager.getConfig("config");
        FileConfiguration mobsConfig = configManager.getConfig("mobs");

        upgradeDrop = SimpleItem.fromSection(mobsConfig.getConfigurationSection("upgrade-drop"));
        spawner = SimpleItem.fromSection(mainConfig.getConfigurationSection("spawner-item"));
        spawner.setMaterial(Material.SPAWNER);

        ConfigurationSection settingsSection = mainConfig.getConfigurationSection("settings");
        spawnerMaxStack = settingsSection.getInt("max-spawner-stack-size", 500);
        mobMaxStack = settingsSection.getInt("max-mob-stack-size", 500);
        killStack = settingsSection.getBoolean("kill-entire-mob-stack", false);
        chunkSpawners = settingsSection.getBoolean("limit-spawners-per-chunk", false);
        entityName = settingsSection.getString("entity-name", "&6&l%amount%x &e&l%mob%");
        entityNameUpgraded = settingsSection.getString("entity-name-upgraded", "&6&l%amount%x &e&l%mob% &7(%upgrade%)");
        itemName = settingsSection.getString("item-name", "&6%amount%x &e%item%");
        hitCooldown = settingsSection.getInt("hit-cooldown", 100);
    }

    public SimpleItem getUpgradeDrop() {
        return upgradeDrop.clone();
    }

    public ItemStack getSpawner(StackedSpawner stackedSpawner) {
        return getSpawner(stackedSpawner.getEntityType(), stackedSpawner.getUpgradeName());
    }

    public ItemStack getSpawner(EntityType type, String upgrade) {
        SpawnerUpgrade spawnerUpgrade = plugin.getUpgradeService().getUpgrade(type, upgrade);
        SimpleItem item = spawner.clone();
        item.setPlaceholder("%name%", WordUtils.capitalizeFully(type.toString().replace("_", " ")));
        item.setPlaceholder("%upgrade%", spawnerUpgrade.getName() == null ? "None" : spawnerUpgrade.getName());
        if(spawnerUpgrade.getName() != null) {
            item.addNBT("spawner-upgrade", spawnerUpgrade.getName());
        }
        ItemStack itemStack = item.build();
        ItemMeta meta = itemStack.getItemMeta();
        BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
        creatureSpawner.setSpawnedType(type);
        creatureSpawner.setDelay(creatureSpawner.getMinSpawnDelay());
        blockStateMeta.setBlockState(creatureSpawner);
        itemStack.setItemMeta(blockStateMeta);
        return itemStack;
    }

    public ItemStack getSpawner(EntityType type) {
        return getSpawner(type, null);
    }

    public int getMobMaxStack() {
        return mobMaxStack;
    }

    public int getSpawnerMaxStack() {
        return spawnerMaxStack;
    }

    public boolean isKillStack() {
        return killStack;
    }

    public boolean isChunkSpawners() {
        return chunkSpawners;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityNameUpgraded() {
        return entityNameUpgraded;
    }
    public String getItemName() {
        return itemName;
    }

    public int getHitCooldown() {
        return hitCooldown;
    }


    public SpawnerPlugin getPlugin() {
        return plugin;
    }
}
