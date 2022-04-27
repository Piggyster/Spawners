package me.piggyster.spawners.command;

import me.piggyster.api.command.Command;
import me.piggyster.spawners.SpawnerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnerCommand extends Command {


    public SpawnerCommand() {
        super(SpawnerPlugin.getInstance(), "spawner");
        addSubCommand(new GiveSubcommand());
        addSubCommand(new GiveupgradeSubcommand());
        if(Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") != null) {
            addSubCommand(new TopSubcommand());
        }
    }


    public void run(CommandSender sender, String[] args) {

    }
}
