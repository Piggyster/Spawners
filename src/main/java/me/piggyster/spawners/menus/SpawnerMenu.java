package me.piggyster.spawners.menus;

import me.piggyster.api.color.ColorAPI;
import me.piggyster.api.config.ConfigManager;
import me.piggyster.api.menu.ConsumerMenu;
import me.piggyster.api.menu.item.SimpleItem;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.stacked.StackedSpawner;
import me.piggyster.spawners.utils.Pair;
import me.piggyster.spawners.utils.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SpawnerMenu extends ConsumerMenu {

    private static SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private static String title;
    private static int size;
    private static Map<Integer, Pair<SimpleItem, Integer>> addItems;
    private static Map<Integer, Pair<SimpleItem, Integer>> takeItems;
    private static SimpleItem spawnerItem;
    private static int spawnerItemSlot;
    private static SimpleItem spacerItem;

    private static SimpleItem noSpawnersFoundTempItem;
    private static long noSpawnersFoundTempTime;
    private static SimpleItem stackFullTempItem;
    private static long stackFullTempTime;

    private static boolean loaded;

    private static void load() {
        ConfigurationSection config = plugin.getConfigManager().getConfig("menus").getConfigurationSection("spawner-menu");
        title = config.getString("title");
        size = config.getInt("size");
        ConfigurationSection itemSection = config.getConfigurationSection("items");

        addItems = new HashMap<>();
        ConfigurationSection addSection = itemSection.getConfigurationSection("add");
        SimpleItem addItem = SimpleItem.fromSection(addSection);
        ConfigurationSection addSlotSection = addSection.getConfigurationSection("slots");
        addSlotSection.getKeys(false).forEach(key -> {
            int slot = Integer.parseInt(key);
            int amount = addSlotSection.getInt(key);
            SimpleItem item = addItem.clone().setPlaceholder("%amount%", amount == -1 ? "ALL" : "+" + amount);
            if(amount > 1) {
                item.setAmount(amount);
            }
            addItems.put(slot, new Pair<>(item, amount));
        });

        takeItems = new HashMap<>();
        ConfigurationSection takeSection = itemSection.getConfigurationSection("take");
        SimpleItem takeItem = SimpleItem.fromSection(takeSection);
        ConfigurationSection takeSlotSection = takeSection.getConfigurationSection("slots");
        takeSlotSection.getKeys(false).forEach(key -> {
            int slot = Integer.parseInt(key);
            int amount = takeSlotSection.getInt(key);
            SimpleItem item = takeItem.clone().setPlaceholder("%amount%", amount == -1 ? "ALL" : "-" + amount);
            if(amount > 1) {
                item.setAmount(amount);
            }
            takeItems.put(slot, new Pair<>(item, amount));
        });

        ConfigurationSection spawnerSection = itemSection.getConfigurationSection("spawner");
        spawnerItemSlot = spawnerSection.getInt("slot");
        spawnerItem = SimpleItem.fromSection(spawnerSection);

        ConfigurationSection spacerSection = itemSection.getConfigurationSection("spacer");
        spacerItem = SimpleItem.fromSection(spacerSection);

        ConfigurationSection tempItemSection = config.getConfigurationSection("temp-items");

        ConfigurationSection noSpawnersFoundSection = tempItemSection.getConfigurationSection("no-spawners-found");
        noSpawnersFoundTempItem = SimpleItem.fromSection(noSpawnersFoundSection);
        noSpawnersFoundTempTime = noSpawnersFoundSection.getLong("time");

        ConfigurationSection stackFullSection = tempItemSection.getConfigurationSection("stack-full");
        stackFullTempItem = SimpleItem.fromSection(stackFullSection);
        stackFullTempTime = stackFullSection.getLong("time");
    }


    private StackedSpawner stackedSpawner;

    public SpawnerMenu(Player player, StackedSpawner stackedSpawner) {
        super(player);
        this.stackedSpawner = stackedSpawner;
        if(!loaded) {
            load();
            loaded = true;
        }
    }

    public void draw() {
        for(int i = 0; i < getSize(); i++) {
            setItem(spacerItem, i);
        }
        setItem(spawnerItem.clone()
                        .setPlaceholder("%type%", StringUtils.entityToString(stackedSpawner.getEntityType()))
                        .setPlaceholder("%upgrade%", stackedSpawner.getUpgradeName() == null ? "None" : stackedSpawner.getUpgradeName())
                        .setPlaceholder("%size%", stackedSpawner.getStackAmount()).setAmount(stackedSpawner.getStackAmount()),
                spawnerItemSlot, (event) -> {
            UpgradeMenu upgradeMenu = new UpgradeMenu(player, stackedSpawner);
            upgradeMenu.open();
        });

        addItems.forEach((slot, pair) -> {
            setItem(pair.getKey(), slot, (event) -> {
                ItemStack spawner = plugin.getSettingsService().getSpawner(stackedSpawner);
                int target = stackedSpawner.getStackLimit() - stackedSpawner.getStackAmount(); //amount that can fit
                if(pair.getValue() < target && pair.getValue() != -1) { //if we want less than the fit, decrease to value & if it's an ALL deposit
                    target = pair.getValue();
                }
                if(target == 0) {
                    setTempItem(stackFullTempItem, slot, stackFullTempTime);
                    return;
                }

                boolean hasInserted = false;

                for(ItemStack itemStack : player.getInventory().getContents()) {
                    if(itemStack == null || itemStack.getType() == Material.AIR || !spawner.isSimilar(itemStack)) continue;
                    if(itemStack.getAmount() >= target) { //if there is more spawners than fit
                        stackedSpawner.increaseStackAmount(target); //increase stack by fit
                        itemStack.setAmount(itemStack.getAmount() - target); //remove from item
                        hasInserted = true;
                        break;
                    }
                    if(target >= itemStack.getAmount()) { //if there are less spawners than fit
                        stackedSpawner.increaseStackAmount(itemStack.getAmount()); //increase by spawners
                        target = target - itemStack.getAmount(); //subtract spawners from fit
                        itemStack.setAmount(0); //remove item
                        hasInserted = true;
                    }
                    if(target < 1) {
                        break;
                    }
                }
                if(hasInserted) {
                    plugin.getDataService().updateStackedSpawner(stackedSpawner);
                    draw();
                } else {
                    setTempItem(noSpawnersFoundTempItem, slot, noSpawnersFoundTempTime);
                }

            });
        });

        takeItems.forEach((slot, pair) -> {
            setItem(pair.getKey(), slot, (event) -> {
                if(stackedSpawner.getStackAmount() < 1) return;
                ItemStack spawner = plugin.getSettingsService().getSpawner(stackedSpawner);
                int target = stackedSpawner.getStackAmount(); //amount that can be taken
                if(pair.getValue() < target && pair.getValue() != -1) { //if we want less than the fit, decrease to value & if it's an ALL deposit
                    target = pair.getValue();
                }
                if(target == 0) return;
                spawner.setAmount(target);
                player.getInventory().addItem(spawner);
                stackedSpawner.setStackAmount(stackedSpawner.getStackAmount() - target);
                if(stackedSpawner.getStackAmount() < 1) {
                    plugin.getDataService().removeStackedSpawner(stackedSpawner);
                    stackedSpawner.remove();
                    player.closeInventory();
                } else {
                    plugin.getDataService().updateStackedSpawner(stackedSpawner);
                    draw();
                }

            });
        });
    }

    public int getSize() {
        return size;
    }

    public String getTitle() {
        return ColorAPI.process(title.replace("%type%", StringUtils.entityToString(stackedSpawner.getEntityType())));
    }
}
