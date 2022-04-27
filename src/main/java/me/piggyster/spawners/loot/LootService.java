package me.piggyster.spawners.loot;

import me.piggyster.api.service.PluginService;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.loot.impl.DropLootTransformer;
import me.piggyster.spawners.loot.impl.InventoryLootTransformer;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootService implements PluginService<SpawnerPlugin> {

    private SpawnerPlugin plugin;

    private Map<EntityType, LootTable> tables;
    private List<LootTransformer> transformers;

    public LootService(SpawnerPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        tables = new HashMap<>();
        ConfigurationSection mobSection = plugin.getConfigManager().getConfig("mobs").getConfigurationSection("mobs");
        mobSection.getKeys(false).forEach(key -> {
            ConfigurationSection specificMobSection = mobSection.getConfigurationSection(key);
            EntityType type = EntityType.valueOf(key.toUpperCase());
            LootTable table = new LootTable(specificMobSection);
            tables.put(type, table);
        });

        transformers = new ArrayList<>();
        register(new DropLootTransformer());
        register(new InventoryLootTransformer());
    }

    public void sort() {
        transformers.sort(Comparator.comparingInt(LootTransformer::getPriority));
    }

    public void transform(ItemStack item, Location location, Player player) {
        transformers.stream().sequential().forEach(t -> t.handle(item, location, player));
    }

    public void register(LootTransformer lootTransformer) {
        transformers.add(lootTransformer);
        sort();
    }

    public void unregister(String name) {
        transformers.removeIf(t -> t.getName().equals(name));
    }

    public LootTable getLootTable(LivingEntity livingEntity) {
        return tables.get(livingEntity.getType());
    }

    public LootTable getLootTable(EntityType entityType) {
        return tables.get(entityType);
    }

    public SpawnerPlugin getPlugin() {
        return plugin;
    }
}
