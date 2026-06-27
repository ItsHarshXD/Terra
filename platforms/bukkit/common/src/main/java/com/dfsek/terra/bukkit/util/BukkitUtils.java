package com.dfsek.terra.bukkit.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;

import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.bukkit.world.entity.BukkitEntityType;


public class BukkitUtils {
    public static boolean isLiquid(BlockData blockState) {
        Material material = blockState.getMaterial();
        return material == Material.WATER || material == Material.LAVA;
    }

    public static EntityType getEntityType(String data) {
        NamespacedKey key = parseEntityKey(data);
        org.bukkit.entity.EntityType entityType = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.ENTITY_TYPE)
            .get(key);
        if(entityType == null) throw new IllegalArgumentException("Invalid entity identifier " + data);

        return new BukkitEntityType(entityType);
    }

    static NamespacedKey parseEntityKey(String data) {
        NamespacedKey key = NamespacedKey.fromString(stripEntityData(data));
        if(key == null || !NamespacedKey.MINECRAFT.equals(key.namespace())) {
            throw new IllegalArgumentException("Invalid entity identifier " + data);
        }
        return key;
    }

    /**
     * Bukkit entity types are registry keys and cannot represent the optional entity data accepted
     * by TerraScript.
     */
    static String stripEntityData(String data) {
        int entityDataStart = data.indexOf('{');
        return entityDataStart < 0 ? data : data.substring(0, entityDataStart);
    }
}
