/**
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.propagation;


import ChunkConstants.MAX_SUNLIGHT;
import ChunkConstants.MAX_SUNLIGHT_REGEN;
import Side.BOTTOM;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.propagation.light.InternalLightProcessor;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenWorldView;
import org.terasology.world.propagation.light.SunlightWorldView;


public class BetweenChunkPropagationTest extends TerasologyTestingEnvironment {
    private BlockManagerImpl blockManager;

    private BiomeManager biomeManager;

    private ExtraBlockDataManager extraDataManager;

    private Block solid;

    private SunlightPropagationRules lightRules;

    private SunlightRegenPropagationRules regenRules;

    private BetweenChunkPropagationTest.SelectChunkProvider provider = new BetweenChunkPropagationTest.SelectChunkProvider();

    private SunlightRegenWorldView regenWorldView;

    private SunlightWorldView lightWorldView;

    private BatchPropagator sunlightPropagator;

    private SunlightRegenBatchPropagator propagator;

    @Test
    public void testBetweenChunksSimple() {
        Chunk topChunk = new org.terasology.world.chunks.internal.ChunkImpl(new Vector3i(0, 1, 0), blockManager, biomeManager, extraDataManager);
        Chunk bottomChunk = new org.terasology.world.chunks.internal.ChunkImpl(new Vector3i(0, 0, 0), blockManager, biomeManager, extraDataManager);
        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            topChunk.setSunlight(pos, MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, MAX_SUNLIGHT_REGEN);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);
        propagator.propagateBetween(topChunk, bottomChunk, BOTTOM, true);
        propagator.process();
        sunlightPropagator.process();
        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            Assert.assertEquals(("Incorrect at position " + pos), MAX_SUNLIGHT, bottomChunk.getSunlight(pos));
            Assert.assertEquals(("Incorrect at position " + pos), MAX_SUNLIGHT_REGEN, bottomChunk.getSunlightRegen(pos));
        }
    }

    @Test
    public void testBetweenChunksSimpleSunlightRegenOnly() {
        Chunk topChunk = new org.terasology.world.chunks.internal.ChunkImpl(new Vector3i(0, 1, 0), blockManager, biomeManager, extraDataManager);
        Chunk bottomChunk = new org.terasology.world.chunks.internal.ChunkImpl(new Vector3i(0, 0, 0), blockManager, biomeManager, extraDataManager);
        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            topChunk.setSunlight(pos, MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, MAX_SUNLIGHT_REGEN);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);
        propagator.propagateBetween(topChunk, bottomChunk, BOTTOM, true);
        propagator.process();
        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            Assert.assertEquals(("Incorrect at position " + pos), MAX_SUNLIGHT_REGEN, bottomChunk.getSunlightRegen(pos));
        }
    }

    @Test
    public void testBetweenChunksWithOverhang() {
        Chunk topChunk = new org.terasology.world.chunks.internal.ChunkImpl(new Vector3i(0, 1, 0), blockManager, biomeManager, extraDataManager);
        Chunk bottomChunk = new org.terasology.world.chunks.internal.ChunkImpl(new Vector3i(0, 0, 0), blockManager, biomeManager, extraDataManager);
        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            topChunk.setSunlight(pos, MAX_SUNLIGHT);
            topChunk.setSunlightRegen(pos, MAX_SUNLIGHT_REGEN);
        }
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(16, 48, 0), new Vector3i(31, 48, 31))) {
            bottomChunk.setBlock(pos, solid);
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);
        propagator.propagateBetween(topChunk, bottomChunk, BOTTOM, false);
        propagator.process();
        sunlightPropagator.process();
        for (int z = 0; z < (ChunkConstants.SIZE_Z); ++z) {
            Assert.assertEquals(14, bottomChunk.getSunlight(16, 47, z));
        }
        for (int z = 0; z < (ChunkConstants.SIZE_Z); ++z) {
            Assert.assertEquals(13, bottomChunk.getSunlight(17, 47, z));
        }
    }

    @Test
    public void testPropagateSunlightAppearingMidChunk() {
        Chunk topChunk = new org.terasology.world.chunks.internal.ChunkImpl(new Vector3i(0, 1, 0), blockManager, biomeManager, extraDataManager);
        Chunk bottomChunk = new org.terasology.world.chunks.internal.ChunkImpl(new Vector3i(0, 0, 0), blockManager, biomeManager, extraDataManager);
        provider.addChunk(topChunk);
        provider.addChunk(bottomChunk);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            topChunk.setSunlight(pos, ((byte) (0)));
            topChunk.setSunlightRegen(pos, ((byte) (0)));
        }
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(8, 0, 8), new Vector3i(((ChunkConstants.SIZE_X) - 16), 1, ((ChunkConstants.SIZE_Z) - 16)))) {
            topChunk.setSunlight(pos, ((byte) (0)));
            topChunk.setSunlightRegen(pos, ((byte) (32)));
        }
        InternalLightProcessor.generateInternalLighting(bottomChunk);
        propagator.propagateBetween(topChunk, bottomChunk, BOTTOM, false);
        propagator.process();
        sunlightPropagator.process();
        for (int i = 0; i < 15; ++i) {
            Assert.assertEquals(("Incorrect value at " + (33 + i)), (14 - i), bottomChunk.getSunlight(7, (33 + i), 16));
        }
        for (int i = 2; i < 33; ++i) {
            Assert.assertEquals(("Incorrect value at " + i), 14, bottomChunk.getSunlight(7, i, 16));
        }
    }

    private static class SelectChunkProvider implements ChunkProvider {
        private Map<Vector3i, Chunk> chunks = Maps.newHashMap();

        SelectChunkProvider(Chunk... chunks) {
            for (Chunk chunk : chunks) {
                this.chunks.put(chunk.getPosition(), chunk);
            }
        }

        public void addChunk(Chunk chunk) {
            chunks.put(chunk.getPosition(), chunk);
        }

        @Override
        public ChunkViewCore getLocalView(Vector3i centerChunkPos) {
            return null;
        }

        @Override
        public ChunkViewCore getSubviewAroundBlock(Vector3i blockPos, int extent) {
            return null;
        }

        @Override
        public ChunkViewCore getSubviewAroundChunk(Vector3i chunkPos) {
            return null;
        }

        @Override
        public boolean reloadChunk(Vector3i pos) {
            return false;
        }

        @Override
        public void setWorldEntity(EntityRef entity) {
            // do nothing
        }

        @Override
        public Collection<Chunk> getAllChunks() {
            return this.chunks.values();
        }

        @Override
        public void addRelevanceEntity(EntityRef entity, Vector3i distance) {
            // do nothing
        }

        @Override
        public void addRelevanceEntity(EntityRef entity, Vector3i distance, ChunkRegionListener listener) {
            // do nothing
        }

        @Override
        public void updateRelevanceEntity(EntityRef entity, Vector3i distance) {
            // do nothing
        }

        @Override
        public void removeRelevanceEntity(EntityRef entity) {
            // do nothing
        }

        @Override
        public void completeUpdate() {
            // do nothing
        }

        @Override
        public void beginUpdate() {
            // do nothing
        }

        @Override
        public boolean isChunkReady(Vector3i pos) {
            return false;
        }

        @Override
        public Chunk getChunk(int x, int y, int z) {
            return getChunk(new Vector3i(x, y, z));
        }

        @Override
        public Chunk getChunk(Vector3i chunkPos) {
            return chunks.get(chunkPos);
        }

        @Override
        public void dispose() {
            // do nothing
        }

        @Override
        public void restart() {
            // do nothing
        }

        @Override
        public void shutdown() {
            // do nothing
        }

        @Override
        public void purgeWorld() {
            // do nothing
        }
    }
}
