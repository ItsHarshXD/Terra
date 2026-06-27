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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.api.world.chunk.generation.util.GeneratorWrapper;
import com.dfsek.terra.api.world.info.WorldProperties;
import com.dfsek.terra.bukkit.world.BukkitWorldProperties;


public class BukkitChunkGeneratorWrapper extends org.bukkit.generator.ChunkGenerator implements GeneratorWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(BukkitChunkGeneratorWrapper.class);
    private static final int SPAWN_SEARCH_ATTEMPTS = 64;
    private static final int SPAWN_SEARCH_RADIUS = 256;

    private final BlockState air;
    private final BukkitBlockPopulator blockPopulator;
    private ChunkGenerator delegate;
    private ConfigPack pack;


    public BukkitChunkGeneratorWrapper(ChunkGenerator delegate, ConfigPack pack, BlockState air) {
        this.delegate = delegate;
        this.pack = pack;
        this.air = air;
        this.blockPopulator = new BukkitBlockPopulator(pack, air);
    }

    public void setDelegate(ChunkGenerator delegate) {
        this.delegate = delegate;
    }

    @Override
    public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return new BukkitBiomeProvider(pack.getBiomeProvider());
    }

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkData chunkData) {
        BukkitWorldProperties properties = new BukkitWorldProperties(worldInfo);
        delegate.generateChunkData(new BukkitProtoChunk(chunkData), properties, pack.getBiomeProvider(), x, z);
    }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return List.of(blockPopulator);
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
        //return pack.vanillaCaves();
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public @Nullable Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        WorldProperties properties = new BukkitWorldProperties(world);
        com.dfsek.terra.api.world.biome.generation.BiomeProvider biomeProvider = pack.getBiomeProvider();

        for(int attempt = 0; attempt < SPAWN_SEARCH_ATTEMPTS; attempt++) {
            int x = attempt == 0 ? 0 : random.nextInt(-SPAWN_SEARCH_RADIUS, SPAWN_SEARCH_RADIUS + 1);
            int z = attempt == 0 ? 0 : random.nextInt(-SPAWN_SEARCH_RADIUS, SPAWN_SEARCH_RADIUS + 1);
            OptionalInt spawnY = findSpawnY(properties.getMinHeight(), properties.getMaxHeight(),
                y -> delegate.getBlock(properties, x, y, z, biomeProvider),
                BlockState::isAir,
                state -> state.getBlockType().isSolid());

            if(spawnY.isPresent()) return new Location(world, x, spawnY.getAsInt(), z);
        }

        LOGGER.warn("No safe fixed spawn found for world '{}'; falling back to the server spawn search", world.getName());
        return null;
    }

    static <T> OptionalInt findSpawnY(int minHeight, int maxHeight, IntFunction<T> blockAt,
                                     Predicate<T> isAir, Predicate<T> isSolid) {
        if(maxHeight - minHeight < 3) return OptionalInt.empty();

        T head = blockAt.apply(maxHeight - 1);
        T feet = blockAt.apply(maxHeight - 2);

        for(int groundY = maxHeight - 3; groundY >= minHeight; groundY--) {
            T ground = blockAt.apply(groundY);
            if(isSolid.test(ground) && isAir.test(feet) && isAir.test(head)) {
                return OptionalInt.of(groundY + 1);
            }
            head = feet;
            feet = ground;
        }

        return OptionalInt.empty();
    }

    @Override
    public boolean shouldGenerateStructures() {
        return true;
    }

    public ConfigPack getPack() {
        return pack;
    }

    public void setPack(ConfigPack pack) {
        this.pack = pack;
        setDelegate(pack.getGeneratorProvider().newInstance(pack));
    }

    @Override
    public ChunkGenerator getHandle() {
        return delegate;
    }


    private record SeededVector(int x, int z, WorldProperties worldProperties) {
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof SeededVector that) {
                return this.z == that.z && this.x == that.x && this.worldProperties.equals(that.worldProperties);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int code = x;
            code = 31 * code + z;
            return 31 * code + worldProperties.hashCode();
        }
    }
}
