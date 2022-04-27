package me.piggyster.spawners.loot.impl;

import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.loot.LootTransformer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

public class DropLootTransformer implements LootTransformer {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();


    public void handle(ItemStack drop, Location location, Player player) {
        if(drop == null || drop.getAmount() == 0) {
            return;
        }
        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> handle(drop, location, player));
            return;
        }
        int size = drop.getAmount();
        Stream<Item> stream = plugin.getItemService().getItemHandler().getNearbyItems(location, drop);
        stream.findAny().ifPresentOrElse((item) -> {
            plugin.getItemService().setStackSize(item, plugin.getItemService().getStackSize(item) + drop.getAmount());
        }, () -> {
            Item item = location.getWorld().dropItemNaturally(location, drop);
            plugin.getItemService().setStackSize(item, size);
        });
        drop.setAmount(0);
    }

    public String getName() {
        return "drop";
    }

    public int getPriority() {
        return 100;
    }
}
