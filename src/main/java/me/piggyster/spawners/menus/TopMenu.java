package me.piggyster.spawners.menus;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import me.piggyster.api.color.ColorAPI;
import me.piggyster.api.menu.ConsumerMenu;
import me.piggyster.api.menu.item.SimpleItem;
import me.piggyster.api.menu.item.SkullCreator;
import me.piggyster.api.util.NumberUtil;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.top.SpawnerIsland;
import me.piggyster.spawners.utils.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TopMenu extends ConsumerMenu {

    private final static SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private static String title;
    private static int size;
    private static SimpleItem islandFormatItem;
    private static List<Integer> islandSlots;
    private static SimpleItem noIslandItem;
    private static SimpleItem spacerItem;

    private static boolean loaded;

    private static void load() {
        ConfigurationSection config = plugin.getConfigManager().getConfig("menus").getConfigurationSection("island-top-menu");
        title = config.getString("title");
        size = config.getInt("size");
        ConfigurationSection itemSection = config.getConfigurationSection("items");

        ConfigurationSection islandItemSection = itemSection.getConfigurationSection("island");
        islandFormatItem = SimpleItem.fromSection(islandItemSection);
        islandFormatItem.setMaterial(Material.PLAYER_HEAD);
        islandSlots = islandItemSection.getIntegerList("slots");

        ConfigurationSection noIslandItemSection = itemSection.getConfigurationSection("no-island");
        noIslandItem = SimpleItem.fromSection(noIslandItemSection);

        ConfigurationSection spacerSection = itemSection.getConfigurationSection("spacer");
        spacerItem = SimpleItem.fromSection(spacerSection);
    }


    public TopMenu(Player player) {
        super(player);
        if(!loaded) {
            load();
            loaded = true;
        }
    }


    public void draw() {
        for(int i = 0; i < getSize(); i++) {
            setItem(spacerItem, i);
        }
        islandSlots.forEach(slot -> setItem(noIslandItem.clone(), slot));

        final AtomicInteger i = new AtomicInteger(0);

        plugin.getIslandTopService().getTopIslands().forEach(spawnerIsland -> {
            if(islandSlots.size() < i.get() + 1) return;
            SimpleItem item = islandFormatItem.clone();
            item.setPlaceholder("%rank%", i.get() + 1)
                    .setPlaceholder("%leader%", spawnerIsland.getIsland().getOwner().getName())
                    .setPlaceholder("%worth%", NumberUtil.withSuffix(spawnerIsland.getTotalValue()));
            ItemStack itemStack = item.build();
            ItemMeta meta = itemStack.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getLore().forEach(line -> {
                if(line.contains("%member%")) {
                    Stream<String> stream = spawnerIsland.getIsland().getIslandMembers(false).stream().map(SuperiorPlayer::getName);
                    if(stream.findAny().isEmpty()) {
                        lore.add(line.replace("%member%", "No Team Members"));
                    } else {
                        stream.forEach(s -> lore.add(line.replace("%member%", s)));
                    }
                } else {
                    lore.add(line);
                }
            });
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            SkullCreator.itemWithUuid(itemStack, spawnerIsland.getIsland().getOwner().getUniqueId());
            setItem(itemStack, islandSlots.get(i.get()), event -> {
                TopPreviewMenu topPreviewMenu = new TopPreviewMenu(player, spawnerIsland);
                topPreviewMenu.open();
            });
            i.incrementAndGet();
        });
    }

    public int getSize() {
        return size;
    }

    public String getTitle() {
        return ColorAPI.process(title);
    }
}
