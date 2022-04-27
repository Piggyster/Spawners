package me.piggyster.spawners.economy;

import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class EconomyProvider {

    public abstract BigDecimal getBalance(UUID uuid);

    public abstract void setBalance(UUID uuid, BigDecimal amount);

    public boolean hasBalance(UUID uuid, BigDecimal amount) {
        BigDecimal balance = getBalance(uuid);
        return balance.compareTo(amount) >= 0;
    }

    public void takeBalance(UUID uuid, BigDecimal amount) {
        setBalance(uuid, getBalance(uuid).subtract(amount));
    }

}
