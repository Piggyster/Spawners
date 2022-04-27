package me.piggyster.spawners.events;

import me.piggyster.spawners.stacked.StackedEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncEntityDeathEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private StackedEntity stackedEntity;
    private int lootBonus;
    private Player player;
    private boolean cancel;

    public AsyncEntityDeathEvent(StackedEntity stackedEntity, Player player) {
        super(true);
        this.cancel = false;
        this.stackedEntity = stackedEntity;
        this.player = player;
        this.lootBonus = 0;
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

    public HandlerList getHandlers() {
        return handlers;
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
