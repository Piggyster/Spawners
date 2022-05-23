package me.piggyster.spawners.stacked;

import me.piggyster.api.color.ColorAPI;
import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.upgrades.SpawnerUpgrade;
import me.piggyster.spawners.upgrades.UpgradeService;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class StackedEntity {

    private static final SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    private LivingEntity entity;
    private int stackAmount;
    private String upgrade;

    public StackedEntity(LivingEntity entity, int stackAmount, String upgrade) {
        this.entity = entity;
        this.stackAmount = stackAmount;
        this.upgrade = upgrade;
    }

    public int getStackAmount() {
        return stackAmount;
    }

    public int getStackLimit() {
        return plugin.getSettingsService().getMobMaxStack();
    }

    public LivingEntity getLivingEntity() {
        return entity;
    }

    public void setStackAmount(int stackAmount) {
        this.stackAmount = stackAmount;
        updateName();
    }

    public boolean isAlive() {
        if(entity.isDead()) {
            return false;
        }
        if(plugin.getDataService().getStackedEntity(entity) == null) {
            return false;
        }
        return true;
    }

    public int decreaseStackAmount(int stackAmount) {
        int newAmount = increaseStackAmount(-stackAmount);
        return newAmount;
    }

    public int increaseStackAmount(int stackAmount) {
        int newAmount = this.stackAmount += stackAmount;
        if(newAmount > 0) updateName();
        return newAmount;
    }

    public void updateName() {
        entity.setCustomNameVisible(true);
        String newName = upgrade == null ? plugin.getSettingsService().getEntityName() : plugin.getSettingsService().getEntityNameUpgraded();
        newName = newName.replace("%amount%", stackAmount + "").replace("%mob%", WordUtils.capitalizeFully(entity.getType().toString().replace("_", " ")));
        if(upgrade != null) {
            newName = newName.replace("%upgrade%", upgrade);
        }
        entity.setCustomName(ColorAPI.process(newName));
    }

    public String getUpgradeName() {
        return upgrade == null ? "None" : upgrade;
    }

    public SpawnerUpgrade getUpgrade() {
        return plugin.getUpgradeService().getUpgrade(entity.getType(), upgrade);
    }

    public void setUpgrade(String upgrade) {
        this.upgrade = upgrade;
    }


    public void remove() {
        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::remove);
            return;
        }
        entity.remove();
    }
}
