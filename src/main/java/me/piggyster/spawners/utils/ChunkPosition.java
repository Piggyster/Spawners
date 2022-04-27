package me.piggyster.spawners.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.nio.file.LinkOption;
import java.util.Objects;

public class ChunkPosition {

    private String world;
    private int x, z;

    public ChunkPosition(Location location) {
        this(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public ChunkPosition(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public ChunkPosition(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x &&
                z == that.z &&
                world.equals(that.world);
    }
}
