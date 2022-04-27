package me.piggyster.spawners.economy.impl;

import me.piggyster.spawners.SpawnerPlugin;
import me.piggyster.spawners.economy.EconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.UUID;

public class DefaultEconomyProvider extends EconomyProvider {


    private static SpawnerPlugin plugin = SpawnerPlugin.getInstance();

    public static DefaultEconomyProvider get() {
        try {
            return new DefaultEconomyProvider();
        } catch(Exception ignore) {}
        return null;
    }

    private Economy economy;

    private DefaultEconomyProvider() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            throw new InstantiationError();
        }
        economy = rsp.getProvider();
    }

    public BigDecimal getBalance(UUID uuid) {
        return BigDecimal.valueOf(economy.getBalance(Bukkit.getOfflinePlayer(uuid)));
    }

    public void setBalance(UUID uuid, BigDecimal amount) {
        economy.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount.doubleValue());
    }
}
