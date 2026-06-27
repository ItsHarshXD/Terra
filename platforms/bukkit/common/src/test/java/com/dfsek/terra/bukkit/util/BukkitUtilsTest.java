/*
 * This file is part of Terra.
 *
 * Terra is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Terra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Terra.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dfsek.terra.bukkit.util;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class BukkitUtilsTest {
    @Test
    void leavesEntityIdentifierUnchanged() {
        assertEquals("minecraft:end_crystal", BukkitUtils.stripEntityData("minecraft:end_crystal"));
    }

    @Test
    void stripsEntityData() {
        assertEquals("minecraft:end_crystal",
            BukkitUtils.stripEntityData("minecraft:end_crystal{ShowBottom:0}"));
    }

    @Test
    void stripsCompoundEntityData() {
        assertEquals("minecraft:zombie",
            BukkitUtils.stripEntityData("minecraft:zombie{Silent:1b,CustomName:'\"Guard\"'}"));
    }

    @Test
    void parsesEntityDataAsNamespacedKey() {
        assertEquals(NamespacedKey.minecraft("end_crystal"),
            BukkitUtils.parseEntityKey("minecraft:end_crystal{ShowBottom:0}"));
    }

    @Test
    void rejectsNonMinecraftEntity() {
        assertThrows(IllegalArgumentException.class, () -> BukkitUtils.parseEntityKey("example:zombie"));
    }
}
