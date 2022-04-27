package me.piggyster.spawners.loot;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface LootTransformer {

    void handle(ItemStack drop, Location location, Player player);

    String getName();

    int getPriority();
}
