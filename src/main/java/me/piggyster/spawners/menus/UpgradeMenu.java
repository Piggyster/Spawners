package me.piggyster.spawners.menus;

import me.piggyster.api.color.ColorAPI;
import me.piggyster.api.menu.ConsumerMenu;
import me.piggyster.api.menu.item.SimpleItem;
import me.piggyster.api.util.NumberUtil;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.stacked.StackedSpawner;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import me.piggyster.spawners.utils.Pair;
import me.piggyster.spawners.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UpgradeMenu extends ConsumerMenu {

    private static SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private static String title;
    private static int size;
    private static List<Integer> upgradeSlots;
    private static SimpleItem upgradeFormatItem;
    private static SimpleItem resetItem;
    private static int resetItemSlot;
    private static SimpleItem spacerItem;

    private static SimpleItem notEnoughMoneyTempItem;
    private static long notEnoughMoneyTempTime;
    private static SimpleItem upgradePurchasedTempItem;
    private static long upgradePurchasedTempTime;
    private static SimpleItem alreadyUpgradedTempItem;
    private static long alreadyUpgradedTempTime;
    private static SimpleItem upgradeResetTempItem;
    private static long upgradeResetTempTime;

    private static boolean loaded;

    private static void load() {
        ConfigurationSection config = plugin.getConfigManager().getConfig("menus").getConfigurationSection("upgrade-menu");
        title = config.getString("title");
        size = config.getInt("size");


        ConfigurationSection itemSection = config.getConfigurationSection("items");

        ConfigurationSection upgradeSection = itemSection.getConfigurationSection("upgrade");
        upgradeFormatItem = SimpleItem.fromSection(upgradeSection);
        upgradeSlots = upgradeSection.getIntegerList("slots");

        ConfigurationSection resetSection = itemSection.getConfigurationSection("reset");
        resetItem = SimpleItem.fromSection(resetSection);
        resetItemSlot = resetSection.getInt("slot");

        ConfigurationSection spacerSection = itemSection.getConfigurationSection("spacer");
        spacerItem = SimpleItem.fromSection(spacerSection);

        ConfigurationSection tempItemSection = config.getConfigurationSection("temp-items");

        ConfigurationSection notEnoughMoneySection = tempItemSection.getConfigurationSection("not-enough-money");
        notEnoughMoneyTempItem = SimpleItem.fromSection(notEnoughMoneySection);
        notEnoughMoneyTempTime = notEnoughMoneySection.getLong("time");

        ConfigurationSection upgradePurchasedSection = tempItemSection.getConfigurationSection("upgrade-purchased");
        upgradePurchasedTempItem = SimpleItem.fromSection(upgradePurchasedSection);
        upgradePurchasedTempTime = upgradePurchasedSection.getLong("time");

        ConfigurationSection alreadyUpgradedSection = tempItemSection.getConfigurationSection("already-upgraded");
        alreadyUpgradedTempItem = SimpleItem.fromSection(alreadyUpgradedSection);
        alreadyUpgradedTempTime = alreadyUpgradedSection.getLong("time");

        ConfigurationSection upgradeResetSection = tempItemSection.getConfigurationSection("upgrade-reset");
        upgradeResetTempItem = SimpleItem.fromSection(upgradeResetSection);
        upgradeResetTempTime = upgradeResetSection.getLong("time");

    }


    private StackedSpawner stackedSpawner;

    public UpgradeMenu(Player player, StackedSpawner stackedSpawner) {
        super(player);
        this.stackedSpawner = stackedSpawner;
        if(!loaded) {
            load();
            loaded = true;
        }
    }

    public void draw() {
        for(int i = 0; i < getSize(); i++) {
            if(upgradeSlots.contains(i)) continue;
            setItem(spacerItem, i);
        }

        int i = 0;
        for(SpawnerUpgrade upgrade : plugin.getUpgradeService().getUpgrades(stackedSpawner.getEntityType()).values()) {
            if(upgrade.getName() == null) continue;
            int slot = upgradeSlots.get(i);
            SimpleItem item = upgradeFormatItem.clone();
            Material drop = plugin.getLootService().getLootTable(stackedSpawner.getEntityType()).getDrop(upgrade, 1).getType();
            item.setPlaceholder("%name%", upgrade.getName())
                    .setPlaceholder("%cost%", NumberUtil.withSuffix(upgrade.getPrice().multiply(BigDecimal.valueOf(stackedSpawner.getStackAmount()))))
                    .setPlaceholder("%drop%", upgrade.getColor() + upgrade.getName() + " " + StringUtils.materialToString(drop))
                    .setPlaceholder("%size%", stackedSpawner.getStackAmount());
            item.setMaterial(drop);
            setItem(item, slot, (event) -> {
                BigDecimal price = upgrade.getPrice().multiply(BigDecimal.valueOf(stackedSpawner.getStackAmount()));
                if(!plugin.getEconomyProvider().hasBalance(player.getUniqueId(), price)) {
                    setTempItem(notEnoughMoneyTempItem, slot, notEnoughMoneyTempTime);
                    return;
                }
                if(Objects.equals(stackedSpawner.getUpgrade(), upgrade)) {
                    setTempItem(alreadyUpgradedTempItem, slot, alreadyUpgradedTempTime);
                    return;
                }
                plugin.getEconomyProvider().takeBalance(player.getUniqueId(), price);
                stackedSpawner.setUpgrade(upgrade.getName());
                plugin.getDataService().updateStackedSpawner(stackedSpawner);
                SimpleItem tempItem = upgradePurchasedTempItem.clone();
                setTempItem(tempItem, slot, upgradePurchasedTempTime);
            });

            i++;
        }

        setItem(resetItem, resetItemSlot, (event) -> {
            stackedSpawner.setUpgrade(null);
            plugin.getDataService().updateStackedSpawner(stackedSpawner);
            setTempItem(upgradeResetTempItem, resetItemSlot, upgradeResetTempTime);
        });
    }

    public int getSize() {
        return size;
    }

    public String getTitle() {
        return ColorAPI.process(title.replace("%type%", StringUtils.entityToString(stackedSpawner.getEntityType())));
    }
}
