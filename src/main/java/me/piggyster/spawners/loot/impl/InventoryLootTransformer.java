package me.piggyster.spawners.loot.impl;

import me.piggyster.spawners.loot.LootTransformer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryLootTransformer implements LootTransformer {

    public void handle(ItemStack drop, Location location, Player player) {
        if(player == null || drop == null || drop.getAmount() == 0 || player.getInventory().firstEmpty() == -1) {
            return;
        }
        ItemStack leftover = player.getInventory().addItem(drop).get(0);
        if(leftover == null) {
            drop.setAmount(0);
        } else {
            drop.setAmount(leftover.getAmount());
        }

    }

    public String getName() {
        return "inventory";
    }

    public int getPriority() {
        return 99;
    }
}
