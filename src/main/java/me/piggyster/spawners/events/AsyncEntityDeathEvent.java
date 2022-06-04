package me.piggyster.spawners.events;

import me.piggyster.spawners.stacked.StackedEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;

public class AsyncEntityDeathEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private StackedEntity stackedEntity;
    private int lootBonus;
    private Player player;

    private int killAmount;

    public AsyncEntityDeathEvent(StackedEntity stackedEntity, Player player, int killAmount) {
        super(true);
        this.stackedEntity = stackedEntity;
        this.player = player;
        this.lootBonus = 0;
        this.killAmount = killAmount;
    }

    public void setLootBonus(int lootBonus) {
        this.lootBonus = lootBonus;
    }

    public int addLootBonus(int lootBonus) {
        return this.lootBonus += lootBonus;
    }

    public int getLootBonus() {
        return lootBonus;
    }

    public StackedEntity getStackedEntity() {
        return stackedEntity;
    }

    public Player getPlayer() {
        return player;
    }

    public int getKillAmount() {
        return killAmount;
    }

    public void setKillAmount(int killAmount) {
        this.killAmount = killAmount;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
