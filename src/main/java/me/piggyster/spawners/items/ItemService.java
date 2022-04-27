package me.piggyster.spawners.items;

import me.piggyster.api.color.ColorAPI;
import me.piggyster.api.service.PluginService;
import me.piggyster.api.util.NumberUtil;
import me.piggyster.spawners.SpawnerPlugin;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ItemService implements PluginService<SpawnerPlugin> {

    private Map<UUID, Long> itemStacks;
    private ItemHandler itemHandler;
    private String display;

    @Override
    public void initialize() {
        itemHandler = new ItemHandler();
        itemStacks = new ConcurrentHashMap<>();
        display = ColorAPI.process("&3%size%x &b%item%");
    }

    public ItemHandler getItemHandler() {
        return itemHandler;
    }

    public long getStackSize(Item item) {
        if(!itemStacks.containsKey(item.getUniqueId())) {
            setStackSize(item, item.getItemStack().getAmount());
        }
        return itemStacks.get(item.getUniqueId());
    }

    public void setStackSize(Item item, long amount) {
        itemStacks.put(item.getUniqueId(), amount);
        String itemName = WordUtils.capitalizeFully(item.getItemStack().getType().toString().replace("_", " "));
        if(item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta().hasDisplayName()) {
            itemName = ChatColor.stripColor(item.getItemStack().getItemMeta().getDisplayName());
        }
        String display = this.display
                .replaceAll("%size%", NumberUtil.format(amount))
                .replaceAll("%item%", itemName);
        item.setCustomName(display);
        item.setCustomNameVisible(true);
    }

    public boolean isStacked(Item item) {
        return itemStacks.containsKey(item.getUniqueId());
    }

    public void removeStack(Item item) {
        itemStacks.remove(item.getUniqueId());
    }

    public void clear() {
        itemStacks.clear();
    }


    public SpawnerPlugin getPlugin() {
        return SpawnerPlugin.getInstance();
    }
}