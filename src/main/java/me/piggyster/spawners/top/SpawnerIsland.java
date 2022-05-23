package me.piggyster.spawners.top;

import com.bgsoftware.superiorskyblock.api.island.Island;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import org.bukkit.entity.EntityType;

import java.math.BigDecimal;
import java.util.*;

public class SpawnerIsland implements Comparable<SpawnerIsland> {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private Island island;

    private Map<EntityType, Map<SpawnerUpgrade, Integer>> amounts;

    public SpawnerIsland(Island island) {
        this.island = island;
        amounts = new TreeMap<>();
    }

    public void increment(EntityType type, String upgrade, int amount) {
        SpawnerUpgrade spawnerUpgrade = plugin.getUpgradeService().getUpgrade(type, upgrade);
        amounts.computeIfAbsent(type, k -> new HashMap<>()).compute(spawnerUpgrade, (key, value) -> {
            if(value == null) {
                return amount;
            } else {
                return value + amount;
            }
        });
    }

    public BigDecimal getTotalValue() {
        BigDecimal amount = BigDecimal.ZERO;
        for(Map<SpawnerUpgrade, Integer> map : amounts.values()) {
            for(Map.Entry<SpawnerUpgrade, Integer> entry : map.entrySet()) {
                amount = amount.add(entry.getKey().getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }
        return amount;
    }

    public Island getIsland() {
        return island;
    }

    public BigDecimal getValue(EntityType type, String upgrade) {
        if(!amounts.containsKey(type)) return BigDecimal.ZERO;
        SpawnerUpgrade spawnerUpgrade = plugin.getUpgradeService().getUpgrade(type, upgrade);
        Map<SpawnerUpgrade, Integer> map = amounts.get(type);
        if(!map.containsKey(spawnerUpgrade)) return BigDecimal.ZERO;
        return spawnerUpgrade.getPrice().multiply(BigDecimal.valueOf(map.get(spawnerUpgrade)));
    }

    public Map<EntityType, Map<SpawnerUpgrade, Integer>> getAmounts() {
        return amounts;
    }


    public int compareTo(SpawnerIsland o) {
        return getTotalValue().compareTo(o.getTotalValue());
    }
}
