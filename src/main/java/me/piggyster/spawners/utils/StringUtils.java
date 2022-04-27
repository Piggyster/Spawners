package me.piggyster.spawners.utils;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityTargetEvent;

public class StringUtils {

    public static String entityToString(EntityType type) {
        return WordUtils.capitalizeFully(type.toString().replace("_", " "));
    }

    public static String materialToString(Material material) {
        return WordUtils.capitalizeFully(material.toString().replace("_", " "));
    }
}
