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

package com.dfsek.terra.bukkit.generator;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class BukkitChunkGeneratorWrapperTest {
    private enum TestBlock {
        AIR,
        SOLID,
        NON_SOLID
    }

    @Test
    void findsSurfaceWithoutLoadingAChunk() {
        OptionalInt result = BukkitChunkGeneratorWrapper.findSpawnY(-64, 320,
            y -> y <= 63 ? TestBlock.SOLID : TestBlock.AIR,
            block -> block == TestBlock.AIR,
            block -> block == TestBlock.SOLID);

        assertEquals(OptionalInt.of(64), result);
    }

    @Test
    void requiresTwoBlocksOfHeadroom() {
        OptionalInt result = BukkitChunkGeneratorWrapper.findSpawnY(0, 10,
            y -> y <= 4 || y >= 6 ? TestBlock.SOLID : TestBlock.AIR,
            block -> block == TestBlock.AIR,
            block -> block == TestBlock.SOLID);

        assertTrue(result.isEmpty());
    }

    @Test
    void rejectsNonSolidGround() {
        OptionalInt result = BukkitChunkGeneratorWrapper.findSpawnY(0, 10,
            y -> y == 4 ? TestBlock.NON_SOLID : TestBlock.AIR,
            block -> block == TestBlock.AIR,
            block -> block == TestBlock.SOLID);

        assertTrue(result.isEmpty());
    }

    @Test
    void acceptsGroundAtMinimumBuildHeight() {
        OptionalInt result = BukkitChunkGeneratorWrapper.findSpawnY(-64, -60,
            y -> y == -64 ? TestBlock.SOLID : TestBlock.AIR,
            block -> block == TestBlock.AIR,
            block -> block == TestBlock.SOLID);

        assertEquals(OptionalInt.of(-63), result);
    }
}
