package me.piggyster.spawners.upgrades;

import com.google.common.collect.Maps;
import me.piggyster.api.service.PluginService;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.math.BigDecimal;
import java.util.*;

public class UpgradeService implements PluginService<SpawnerPlugin> {

    private SpawnerPlugin plugin;

    private Map<EntityType, Map<String, SpawnerUpgrade>> upgrades;

    public UpgradeService(SpawnerPlugin plugin) {
        this.plugin = plugin;
    }


    public SpawnerUpgrade getUpgrade(EntityType entityType, String name) {
        if(entityType == null || !upgrades.containsKey(entityType)) return null;
        if(name == null) {
            return upgrades.get(entityType).get(null);
        }
        return upgrades.get(entityType).get(name.toUpperCase());
    }

    public Map<String, SpawnerUpgrade> getUpgrades(EntityType entityType) {
        return upgrades.get(entityType);
    }

    public void initialize() {
        upgrades = new HashMap<>();
        ConfigurationSection section = plugin.getConfigManager().getConfig("mobs").getConfigurationSection("mobs");
        section.getKeys(false).forEach(key -> {
            try {
                EntityType type = EntityType.valueOf(key);
                ConfigurationSection entitySection = section.getConfigurationSection(key);
                Map<String, SpawnerUpgrade> map = new HashMap<>();
                SpawnerUpgrade defaultUpgrade = new SpawnerUpgrade(null, type, new BigDecimal(entitySection.getString("price")), null);
                map.put(null, defaultUpgrade);
                ConfigurationSection upgradeSection = entitySection.getConfigurationSection("upgrades");
                upgradeSection.getKeys(false).forEach(key2 -> {
                    SpawnerUpgrade upgrade = new SpawnerUpgrade(key2, type, new BigDecimal(upgradeSection.getString(key2 + ".price")), upgradeSection.getString(key2 + ".color"));
                    map.put(upgrade.getName().toUpperCase(), upgrade);
                });
                upgrades.put(type, map);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });

    }

    public SpawnerPlugin getPlugin() {
        return plugin;
    }
}
