package me.piggyster.spawners.utils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LocationUtils {

    public static String chunkToString(Chunk chunk) {
        return chunk.getX() + "," + chunk.getZ();
    }

    public static String locationToString(Location location, boolean world) {
        String string = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
        if(world) {
            string = string + "," + location.getWorld().getName();
        }
        return string;
    }

    public static String locationToString(Location location) {
        return locationToString(location, true);
    }

    public static Location locationFromString(String string) {
        String[] split = string.split(",");
        if(split.length < 4) return null;
        World world = Bukkit.getWorld(split[3]);
        return new Location(
                world,
                Integer.parseInt(split[0]),
                Integer.parseInt(split[1]),
                Integer.parseInt(split[2])
        );
    }

    public static Location locationFromString(String string, World world) {
        return locationFromString(string + "," + world.getName());
    }


    public static Stream<Chunk> getNearbyChunks(Location location) {
        World world = location.getWorld();
        Chunk chunk = location.getChunk();

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
}
