/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level.progress;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public interface LevelLoadListener {
    public static LevelLoadListener compose(final LevelLoadListener levelLoadListener, final LevelLoadListener levelLoadListener2) {
        return new LevelLoadListener(){

            @Override
            public void start(Stage stage, int i) {
                levelLoadListener.start(stage, i);
                levelLoadListener2.start(stage, i);
            }

            @Override
            public void update(Stage stage, int i, int j) {
                levelLoadListener.update(stage, i, j);
                levelLoadListener2.update(stage, i, j);
            }

            @Override
            public void finish(Stage stage) {
                levelLoadListener.finish(stage);
                levelLoadListener2.finish(stage);
            }

            @Override
            public void updateFocus(ResourceKey<Level> resourceKey, ChunkPos chunkPos) {
                levelLoadListener.updateFocus(resourceKey, chunkPos);
                levelLoadListener2.updateFocus(resourceKey, chunkPos);
            }
        };
    }

    public void start(Stage var1, int var2);

    public void update(Stage var1, int var2, int var3);

    public void finish(Stage var1);

    public void updateFocus(ResourceKey<Level> var1, ChunkPos var2);

    public static enum Stage {
        START_SERVER,
        PREPARE_GLOBAL_SPAWN,
        LOAD_INITIAL_CHUNKS,
        LOAD_PLAYER_CHUNKS;

    }
}

