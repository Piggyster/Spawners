package me.piggyster.spawners.menus;

import me.piggyster.api.color.ColorAPI;
import me.piggyster.api.menu.ConsumerMenu;
import me.piggyster.api.menu.item.SimpleItem;
import me.piggyster.api.util.NumberUtil;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.top.SpawnerIsland;
import me.piggyster.spawners.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

public class TopPreviewMenu extends ConsumerMenu {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private static String title;
    private static SimpleItem spawnerFormatItem;
    private static SimpleItem spacerItem;


    private static boolean loaded;

    private SpawnerIsland spawnerIsland;

    public static void load() {
        ConfigurationSection config = plugin.getConfigManager().getConfig("menus").getConfigurationSection("island-top-preview-menu");
        title = config.getString("title");
        ConfigurationSection itemSection = config.getConfigurationSection("items");


        spawnerFormatItem = SimpleItem.fromSection(itemSection.getConfigurationSection("spawner"));
        spawnerFormatItem.setMaterial(Material.PLAYER_HEAD);
        spawnerFormatItem.setURL("http://textures.minecraft.net/texture/badc048a7ce78f7dad72a07da27d85c0916881e5522eeed1e3daf217a38c1a");

        ConfigurationSection spacerSection = itemSection.getConfigurationSection("spacer");
        spacerItem = SimpleItem.fromSection(spacerSection);
    }


    public TopPreviewMenu(Player player, SpawnerIsland spawnerIsland) {
        super(player);
        this.spawnerIsland = spawnerIsland;
        if(!loaded) {
            load();
            loaded = true;
        }
    }

    public void draw() {
        for(int i = 0; i < getSize(); i++) {
            setItem(spacerItem, i);
        }

        AtomicInteger i = new AtomicInteger(0);


        spawnerIsland.getAmounts().forEach((entityType, map) -> {
            map.forEach((upgrade, amount) -> {
                SimpleItem item = spawnerFormatItem.clone();
                item.setPlaceholder("%type%", StringUtils.entityToString(entityType))
                        .setPlaceholder("%price%", NumberUtil.format(upgrade.getPrice()))
                        .setPlaceholder("%worth%", NumberUtil.withSuffix(upgrade.getPrice().multiply(BigDecimal.valueOf(amount))))
                        .setPlaceholder("%amount%", NumberUtil.format(amount))
                                .setPlaceholder("%upgrade%", upgrade.getName() == null ? "None" : upgrade.getName());
                String skull = plugin.getIslandTopService().getSkullURL(entityType);
                if(skull != null) item.setURL(skull);
                setItem(item, i.get());
                i.incrementAndGet();
            });
        });

    }

    public int getSize() {
        AtomicInteger i = new AtomicInteger(0);
        spawnerIsland.getAmounts().forEach((e, m) -> m.forEach((u, a) -> i.incrementAndGet()));
        int size = (int) (Math.ceil(i.get() / 9f) * 9);
        return size == 0 ? 9 : size;
    }

    public String getTitle() {
        return ColorAPI.process(title);
    }
}
