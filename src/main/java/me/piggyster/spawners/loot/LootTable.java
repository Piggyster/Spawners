package me.piggyster.spawners.loot;

import com.google.common.collect.Maps;
import me.piggyster.api.menu.item.SimpleItem;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.stacked.StackedEntity;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import me.piggyster.spawners.utils.Pair;
import me.piggyster.spawners.utils.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LootTable {

    private final static SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private Map<String, Pair<Material, String>> drops;

    public LootTable(ConfigurationSection section) {
        drops = new HashMap<>();
        drops.put(null, new Pair<>(Material.valueOf(section.getString("drop")), null));
        ConfigurationSection upgradeSection = section.getConfigurationSection("upgrades");
        if(upgradeSection != null) {
            upgradeSection.getKeys(false).forEach(key -> {
                Material material = Material.valueOf(upgradeSection.getString(key + ".drop"));
                drops.put(key, new Pair<>(material, key));
            });
        }
    }

    public ItemStack getDrop(StackedEntity stackedEntity, int lootBonus, int stackAmount) {
        ItemStack loot = getDrop(stackedEntity, lootBonus);
        loot.setAmount(loot.getAmount() * stackAmount);
        return loot;
    }

    public ItemStack getDrop(StackedEntity stackedEntity, int lootBonus) {
        return getDrop(stackedEntity.getUpgrade(), lootBonus);
    }

    public ItemStack getDrop(SpawnerUpgrade upgrade, int lootBonus) {
        ItemStack drop;
        String upgradeName = upgrade.getName() == null ? null : upgrade.getName();
        Pair<Material, String> pair = drops.get(upgradeName);
        if(upgradeName == null) {
            drop = new ItemStack(pair.getKey());
        } else {
            SimpleItem placeholder = plugin.getSettingsService().getUpgradeDrop();
            placeholder.setMaterial(pair.getKey());
            placeholder.setPlaceholder("&p", upgrade.getColor());
            placeholder.setPlaceholder("%upgrade%", upgrade.getName());
            placeholder.setPlaceholder("%item%", StringUtils.materialToString(pair.getKey()));
            drop = placeholder.build();
        }
        drop.setAmount(lootBonus + 1);
        return drop;
    }
}
