package me.piggyster.spawners.listener;

import me.piggyster.api.service.Service;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.items.ItemHandler;
import me.piggyster.spawners.items.ItemService;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private final ItemHandler itemHandler;
    private final ItemService itemService;

    public ItemListener() {
        itemService = Service.grab(ItemService.class);
        itemHandler = itemService.getItemHandler();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent e) {
        Item item = e.getItem();
        if (itemService.isStacked(item)) {
            e.setCancelled(true);

            long stackSize = itemService.getStackSize(item);

            int playerInv = itemHandler.computeSpace(e.getPlayer().getInventory(), item.getItemStack());
            int stacksToGive = (int) Math.min(stackSize, playerInv); // Can safely cast this
            long leftOver = stackSize - stacksToGive;

            item.getItemStack().setAmount(1);
            itemHandler.addToInventory(e.getPlayer().getInventory(), e.getItem().getItemStack(), stacksToGive);

            if (leftOver <= 0) {// If this is less than 0, there's a dupe somewhere
                itemService.removeStack(e.getItem());
                e.getItem().remove();
            } else
                itemService.setStackSize(e.getItem(), leftOver);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHopperPickup(InventoryPickupItemEvent e) {
        Item item = e.getItem();
        if (itemService.isStacked(item)) {
            e.setCancelled(true);

            long stackSize = itemService.getStackSize(item);
            int invMax = itemHandler.computeSpace(e.getInventory(), item.getItemStack());
            int stacksToGive = (int) Math.min(stackSize, invMax);
            long leftOver = stackSize - stacksToGive;

            ItemStack stack = item.getItemStack();
            itemHandler.addToInventory(e.getInventory(), stack, stacksToGive);

            if (leftOver <= 0) {// If this is less than 0, there's a dupe somewhere
                itemService.removeStack(item);
                item.remove();
            } else
                itemService.setStackSize(item, leftOver);
        }
    }

    @EventHandler
    public void onItemMerge(ItemMergeEvent e) {
        Item entity = e.getEntity();
        Item target = e.getTarget();

        if (itemService.isStacked(entity) || itemService.isStacked(target)) {
            e.setCancelled(true);
            long entitySize = itemService.getStackSize(entity);
            long targetSize = itemService.getStackSize(target);

            long stackLimit = 9999999999L;
            if (stackLimit > 0 && (entitySize >= stackLimit || targetSize >= stackLimit))
                return;

            long total = entitySize + targetSize;
            long newAmount = stackLimit > 0 ? Math.min(total, stackLimit) : total;
            itemService.setStackSize(e.getTarget(), newAmount);

            targetSize = newAmount;
            entitySize = total - targetSize;

            if (entitySize <= 0) {
                itemService.removeStack(e.getEntity());
                e.getEntity().remove();
            } else {
                itemService.setStackSize(e.getEntity(), entitySize);
            }
        }
    }
}