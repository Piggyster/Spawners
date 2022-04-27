package me.piggyster.spawners.upgrades;

import me.piggyster.spawners.loot.LootTable;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.math.BigDecimal;
import java.util.Objects;

public class SpawnerUpgrade {

    private BigDecimal price;
    private String name;
    private EntityType entityType;
    private String color;

    public SpawnerUpgrade(String name, EntityType entityType, BigDecimal price, String color) {
        this.name = name;
        this.entityType = entityType;
        this.price = price;
        this.color = color;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getName() {
        return name;
    }

    public SpawnerUpgrade getNextUpgrade() {
        return null;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getColor() {
        return color;
    }
}
