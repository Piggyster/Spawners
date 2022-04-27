package me.piggyster.spawners.listener;

import de.tr7zw.nbtapi.NBTItem;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.menus.SpawnerMenu;
import me.piggyster.spawners.stacked.StackedEntity;
import me.piggyster.spawners.stacked.StackedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpawnerListener implements Listener {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        StackedSpawner stackedSpawner = plugin.getDataService().getStackedSpawner(event.getSpawner());
        if(stackedSpawner == null) {
            event.setCancelled(true);
            return;
        }
        StackedEntity stackedEntity = new StackedEntity((LivingEntity) event.getEntity(), stackedSpawner.getStackAmount(), stackedSpawner.getUpgrade().getName() == null ? null : stackedSpawner.getUpgradeName());
        Stream<StackedEntity> stream = plugin.getDataService().getNearbyStackedEntities(stackedEntity);
        for(StackedEntity streamEntity : stream.toList()) {
            int fit = streamEntity.getStackLimit() - streamEntity.getStackAmount();
            if(fit >= stackedEntity.getStackAmount()) {
                streamEntity.increaseStackAmount(stackedEntity.getStackAmount());
                stackedEntity.decreaseStackAmount(stackedEntity.getStackAmount());
                event.setCancelled(true);
                return;
            }
            if(stackedEntity.getStackAmount() >= fit) {
                streamEntity.increaseStackAmount(fit);
                stackedEntity.decreaseStackAmount(fit);
            }
            if(stackedEntity.getStackAmount() < 1) {
                event.setCancelled(true);
                return;
            }
        }
        plugin.getDataService().addStackedEntity(stackedEntity);
        stackedEntity.updateName();

        /*
        if(stackedSpawner.getLinkedEntity() == null || !stackedSpawner.getLinkedEntity().isAlive()) {
            plugin.getDataService().addStackedEntity(stackedEntity);
            stackedSpawner.setLinkedEntity(stackedEntity);
            stackedEntity.updateName();
        } else {
            StackedEntity linkedEntity = stackedSpawner.getLinkedEntity();
            linkedEntity.increaseStackAmount(stackedEntity.getStackAmount());
            event.setCancelled(true);
        }*/
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        if(event.getBlockPlaced().getType() != Material.SPAWNER) {
            return;
        }
        try {
            ItemStack item = event.getItemInHand();
            NBTItem nbtItem = new NBTItem(item);
            String upgrade = nbtItem.getObject("spawner-upgrade", String.class);
            EntityType entityType = getSpawnerType(item);
            int amountToPlace = event.getPlayer().isSneaking() ? item.getAmount() : 1;
            item.setAmount(item.getAmount() - amountToPlace);
            StackedSpawner stackedSpawner = new StackedSpawner(event.getBlockPlaced().getLocation(), entityType, amountToPlace, upgrade);
            plugin.getConfigManager().getMessage("SPAWNER-PLACED").setPlaceholder("%type%", stackedSpawner.getEntityType()).setPlaceholder("%amount%", amountToPlace).send(event.getPlayer());
            List<StackedSpawner> spawnersInChunk = plugin.getDataService().getSpawnersInChunk(stackedSpawner.getLocation().getChunk());
            if(spawnersInChunk == null || spawnersInChunk.isEmpty()) {
                Bukkit.getLogger().warning("starting new stack, none found in chunk");
                plugin.getDataService().addStackedSpawner(stackedSpawner);
                return;
            }
            List<StackedSpawner> spawners = spawnersInChunk.stream().filter(s -> s.getStackAmount() < s.getStackLimit() && s.getEntityType() == entityType && s.getUpgradeName().equals(stackedSpawner.getUpgradeName())).toList();
            for(StackedSpawner streamSpawner : spawners) {
                int fit = stackedSpawner.getStackLimit() - streamSpawner.getStackAmount();
                if(fit >= stackedSpawner.getStackAmount()) {
                    streamSpawner.increaseStackAmount(stackedSpawner.getStackAmount());
                    stackedSpawner.decreaseStackAmount(stackedSpawner.getStackAmount());
                    plugin.getDataService().updateStackedSpawner(streamSpawner);
                    event.setCancelled(true);
                    return;
                }
                if(stackedSpawner.getStackAmount() >= fit) {
                    streamSpawner.increaseStackAmount(fit);
                    stackedSpawner.decreaseStackAmount(fit);
                    plugin.getDataService().updateStackedSpawner(streamSpawner);
                }

                if(stackedSpawner.getStackAmount() < 1) {
                    event.setCancelled(true);
                    return;
                }
            }
            plugin.getDataService().addStackedSpawner(stackedSpawner);
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() != Material.SPAWNER || !plugin.getDataService().isStackedSpawner(event.getBlock().getLocation())) {
            return;
        }
        try {
            StackedSpawner stackedSpawner = plugin.getDataService().getStackedSpawner(event.getBlock().getLocation());
            stackedSpawner.remove();
            ItemStack spawnerItem = plugin.getSettingsService().getSpawner(stackedSpawner.getEntityType(), stackedSpawner.getUpgrade());
            spawnerItem.setAmount(stackedSpawner.getStackAmount());
            Map<Integer, ItemStack> map = event.getPlayer().getInventory().addItem(spawnerItem);
            map.forEach((i, item) -> {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
            });
            event.setCancelled(true);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onSpawnerClick(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || !plugin.getDataService().isStackedSpawner(event.getClickedBlock().getLocation()) || event.getHand() != EquipmentSlot.HAND) return;
        event.setCancelled(true);
        StackedSpawner stackedSpawner = plugin.getDataService().getStackedSpawner(event.getClickedBlock().getLocation());
        SpawnerMenu spawnerMenu = new SpawnerMenu(event.getPlayer(), stackedSpawner);
        spawnerMenu.open();

    }


    private EntityType getSpawnerType(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        EntityType spawnType = EntityType.PIG;
        try {
            BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
            spawnType = ((CreatureSpawner) blockStateMeta.getBlockState()).getSpawnedType();
        } catch(Throwable ignore) {}

        return spawnType;
    }
}
