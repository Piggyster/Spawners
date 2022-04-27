package me.piggyster.spawners.stacked;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import me.piggyster.api.color.ColorAPI;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import me.piggyster.spawners.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.Optional;
import java.util.stream.Stream;

public class StackedSpawner {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private Location location;
    private EntityType type;
    private String upgrade;
    private int stackAmount;

    public StackedSpawner(Location location, EntityType type, int stackAmount, String upgrade) {
        this.location = location;
        this.type = type;
        this.stackAmount = stackAmount;
        this.upgrade = upgrade;
    }

    public int getStackAmount() {
        return stackAmount;
    }

    public int increaseStackAmount(int stackAmount) {
        return this.stackAmount += stackAmount;
    }

    public int decreaseStackAmount(int stackAmount) {
        return increaseStackAmount(-stackAmount);
    }

    public void setStackAmount(int stackAmount) {
        this.stackAmount = stackAmount;
    }

    public String getUpgradeName() {
        return upgrade;
    }

    public SpawnerUpgrade getUpgrade() {
        return plugin.getUpgradeService().getUpgrade(type, upgrade);
    }

    public void setUpgrade(String upgrade) {
        this.upgrade = upgrade;
    }


    public Location getLocation() {
        return location;
    }

    public EntityType getEntityType() {
        return type;
    }

    public int getStackLimit() {
        return plugin.getSettingsService().getSpawnerMaxStack();
    }

    public void remove() {
        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::remove);
            return;
        }
        plugin.getDataService().removeStackedSpawner(this);
        location.getBlock().setType(Material.AIR);
    }
}
