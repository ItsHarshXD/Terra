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

package com.dfsek.terra.bukkit.handles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class BukkitWorldHandleTest {
    @Test
    void leavesBlockDataUnchanged() {
        assertEquals("minecraft:oak_stairs[facing=north,half=bottom]",
            BukkitWorldHandle.stripBlockEntityData("minecraft:oak_stairs[facing=north,half=bottom]"));
    }

    @Test
    void stripsBlockEntityData() {
        assertEquals("minecraft:chest",
            BukkitWorldHandle.stripBlockEntityData("minecraft:chest{LootTable:'chests/simple_dungeon'}"));
    }

    @Test
    void preservesBlockPropertiesBeforeBlockEntityData() {
        assertEquals("minecraft:chest[facing=east,type=single,waterlogged=false]",
            BukkitWorldHandle.stripBlockEntityData(
                "minecraft:chest[facing=east,type=single,waterlogged=false]{LootTable:'chests/end_city_treasure'}"));
    }
}
