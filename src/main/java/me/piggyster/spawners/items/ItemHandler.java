package me.piggyster.spawners.items;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ItemHandler {

    private final double searchRange;

    public ItemHandler() {
        searchRange = Math.pow(3, 3);
    }

    public int computeSpace(Inventory inv, ItemStack stack) {
        if (inv == null || stack == null) return 0;

        int totalSpace = 0;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);

            if (item == null) {
                totalSpace += stack.getMaxStackSize();
            } else if (checkSimilarity(item, stack)) {
                totalSpace += Math.max(item.getMaxStackSize() - item.getAmount(), 0);
            }
        }

        return totalSpace;
    }

    private boolean checkSimilarity(ItemStack i1, ItemStack i2) {
        if (i1 == null || i2 == null)
            return false;

        return i1.getType() == i2.getType() && i1.getDurability() == i2.getDurability();
    }

    public void addToInventory(Inventory inventory, ItemStack stack, int amount) {
        final int stackMax = stack.getType().getMaxStackSize();

        while (amount > 0) {
            int stackSize = Math.min(amount, stackMax);
            amount -= stackSize;

            ItemStack clone = stack.clone();
            clone.setAmount(stackSize);

            inventory.addItem(clone);
        }
    }

    private Stream<Chunk> getNearbyChunks(Entity entity) {
        World world = entity.getWorld();
        Chunk chunk = entity.getLocation().getChunk();

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        Set<Chunk> chunks = new HashSet<>();
        chunks.add(chunk);
        IntStream.range(-1, 2).forEach(x -> IntStream.range(-1, 2).forEach(z -> {
            int nearX = chunkX + x;
            int nearZ = chunkZ + z;
            if (world.isChunkLoaded(nearX, nearZ)) chunks.add(world.getChunkAt(nearX, nearZ));
        }));

        return chunks.stream();
    }

    public Stream<Item> getNearbyItems(Location loc, ItemStack item) {
        if (loc.getWorld() != null) {
            return loc.getWorld().getNearbyEntities(loc, 2, 2, 2).stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.getType() == EntityType.DROPPED_ITEM)
                    .map(e -> (Item) e)
                    .filter(e -> e.getItemStack().isSimilar(item));
        }

        return Stream.empty();
    }

    public Stream<Item> getNearbyItems(Entity entity, ItemStack item) {
        Location baseLoc = entity.getLocation();
        return getNearbyChunks(entity)
                .map(Chunk::getEntities).flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(e -> e.getType() == EntityType.DROPPED_ITEM)
                .filter(e -> e.getLocation().distanceSquared(baseLoc) < searchRange)
                .map(e -> (Item) e)
                .filter(e -> e.getItemStack().isSimilar(item));
    }
}
