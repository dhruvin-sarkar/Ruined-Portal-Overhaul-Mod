/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class GameTestTicker {
    public static final GameTestTicker SINGLETON = new GameTestTicker();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Collection<GameTestInfo> testInfos = Lists.newCopyOnWriteArrayList();
    private @Nullable GameTestRunner runner;
    private State state = State.IDLE;

    private GameTestTicker() {
    }

    public void add(GameTestInfo gameTestInfo) {
        this.testInfos.add(gameTestInfo);
    }

    public void clear() {
        if (this.state != State.IDLE) {
            this.state = State.HALTING;
            return;
        }
        this.testInfos.clear();
        if (this.runner != null) {
            this.runner.stop();
            this.runner = null;
        }
    }

    public void setRunner(GameTestRunner gameTestRunner) {
        if (this.runner != null) {
            Util.logAndPauseIfInIde("The runner was already set in GameTestTicker");
        }
        this.runner = gameTestRunner;
    }

    public void tick() {
        if (this.runner == null) {
            return;
        }
        this.state = State.RUNNING;
        this.testInfos.forEach(gameTestInfo -> gameTestInfo.tick(this.runner));
        this.testInfos.removeIf(GameTestInfo::isDone);
        State state = this.state;
        this.state = State.IDLE;
        if (state == State.HALTING) {
            this.clear();
        }
    }

    static enum State {
        IDLE,
        RUNNING,
        HALTING;

    }
}

