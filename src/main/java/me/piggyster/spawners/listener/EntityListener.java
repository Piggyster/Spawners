package me.piggyster.spawners.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.events.AsyncEntityDeathEvent;
import me.piggyster.spawners.stacked.StackedEntity;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class EntityListener implements Listener {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private Cache<Player, Integer> hitCooldown;

    public EntityListener() {
        final int hitCooldown = plugin.getSettingsService().getHitCooldown();
        if(hitCooldown > 1) {
            this.hitCooldown = CacheBuilder.newBuilder().expireAfterWrite(hitCooldown, TimeUnit.MILLISECONDS).build();
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) return;
        if(plugin.getDataService().isStackedEntity((LivingEntity) event.getEntity())) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof LivingEntity livingEntity) || event.getEntity() instanceof Player) return;
        if(!(event.getDamager() instanceof Player player)) return;

        if(plugin.getDataService().isStackedEntity(livingEntity)) {
            event.setCancelled(true);
            if(hitCooldown != null && hitCooldown.asMap().containsKey(player)) return;
            if(hitCooldown != null) hitCooldown.put(player, 1);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                StackedEntity stackedEntity = plugin.getDataService().getStackedEntity(livingEntity);
                if(stackedEntity.getStackAmount() < 1) return;
                AsyncEntityDeathEvent stackedEntityDeathEvent = new AsyncEntityDeathEvent(stackedEntity, player, plugin.getSettingsService().isKillStack() ? stackedEntity.getStackAmount() : 1);
                Bukkit.getPluginManager().callEvent(stackedEntityDeathEvent);
                stackedEntity.decreaseStackAmount(stackedEntityDeathEvent.getKillAmount());
                int lootBonus = player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) + stackedEntityDeathEvent.getLootBonus();
                ItemStack loot = plugin.getLootService().getLootTable(livingEntity).getDrop(stackedEntity, lootBonus, stackedEntityDeathEvent.getKillAmount());
                plugin.getLootService().transform(loot, stackedEntity.getLivingEntity().getLocation(), player);
                if(stackedEntity.getStackAmount() < 1) {
                    stackedEntity.remove();
                    plugin.getDataService().removeStackedEntity(stackedEntity);
                }
            });
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player) return;
        LivingEntity livingEntity = event.getEntity();
        if(plugin.getDataService().isStackedEntity(livingEntity)) {
            event.getDrops().clear();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                StackedEntity stackedEntity = plugin.getDataService().getStackedEntity(livingEntity);
                if(stackedEntity.getStackAmount() < 1) return;
                AsyncEntityDeathEvent stackedEntityDeathEvent = new AsyncEntityDeathEvent(stackedEntity, null, stackedEntity.getStackAmount());
                Bukkit.getPluginManager().callEvent(stackedEntityDeathEvent);
                int lootBonus = stackedEntityDeathEvent.getLootBonus();
                ItemStack loot = plugin.getLootService().getLootTable(livingEntity).getDrop(stackedEntity, lootBonus, stackedEntityDeathEvent.getKillAmount());
                plugin.getLootService().transform(loot, stackedEntity.getLivingEntity().getLocation(), null);
                plugin.getDataService().removeStackedEntity(stackedEntity);
            });
        }
    }

}
