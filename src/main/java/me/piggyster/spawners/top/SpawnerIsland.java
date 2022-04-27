package me.piggyster.spawners.top;

import com.bgsoftware.superiorskyblock.api.island.Island;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import org.bukkit.entity.EntityType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class SpawnerIsland implements Comparable<SpawnerIsland> {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private Island island;

    private Map<EntityType, Map<SpawnerUpgrade, Integer>> amounts;

    public SpawnerIsland(Map<EntityType, Map<SpawnerUpgrade, Integer>> amounts) {
        this.amounts = amounts;
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
