/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  org.apache.commons.lang3.exception.ExceptionUtils
 */
package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.ExhaustedAttemptsException;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener
implements GameTestListener {
    private int attempts = 0;
    private int successes = 0;

    @Override
    public void testStructureLoaded(GameTestInfo gameTestInfo) {
        ++this.attempts;
    }

    private void handleRetry(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner, boolean bl) {
        RetryOptions retryOptions = gameTestInfo.retryOptions();
        Object string = String.format(Locale.ROOT, "[Run: %4d, Ok: %4d, Fail: %4d", this.attempts, this.successes, this.attempts - this.successes);
        if (!retryOptions.unlimitedTries()) {
            string = (String)string + String.format(Locale.ROOT, ", Left: %4d", retryOptions.numberOfTries() - this.attempts);
        }
        string = (String)string + "]";
        String string2 = String.valueOf(gameTestInfo.id()) + " " + (bl ? "passed" : "failed") + "! " + gameTestInfo.getRunTime() + "ms";
        String string3 = String.format(Locale.ROOT, "%-53s%s", string, string2);
        if (bl) {
            ReportGameListener.reportPassed(gameTestInfo, string3);
        } else {
            ReportGameListener.say(gameTestInfo.getLevel(), ChatFormatting.RED, string3);
        }
        if (retryOptions.hasTriesLeft(this.attempts, this.successes)) {
            gameTestRunner.rerunTest(gameTestInfo);
        }
    }

    @Override
    public void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
        ++this.successes;
        if (gameTestInfo.retryOptions().hasRetries()) {
            this.handleRetry(gameTestInfo, gameTestRunner, true);
            return;
        }
        if (!gameTestInfo.isFlaky()) {
            ReportGameListener.reportPassed(gameTestInfo, String.valueOf(gameTestInfo.id()) + " passed! (" + gameTestInfo.getRunTime() + "ms / " + gameTestInfo.getTick() + "gameticks)");
            return;
        }
        if (this.successes >= gameTestInfo.requiredSuccesses()) {
            ReportGameListener.reportPassed(gameTestInfo, String.valueOf(gameTestInfo) + " passed " + this.successes + " times of " + this.attempts + " attempts.");
        } else {
            ReportGameListener.say(gameTestInfo.getLevel(), ChatFormatting.GREEN, "Flaky test " + String.valueOf(gameTestInfo) + " succeeded, attempt: " + this.attempts + " successes: " + this.successes);
            gameTestRunner.rerunTest(gameTestInfo);
        }
    }

    @Override
    public void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
        if (!gameTestInfo.isFlaky()) {
            ReportGameListener.reportFailure(gameTestInfo, gameTestInfo.getError());
            if (gameTestInfo.retryOptions().hasRetries()) {
                this.handleRetry(gameTestInfo, gameTestRunner, false);
            }
            return;
        }
        GameTestInstance gameTestInstance = gameTestInfo.getTest();
        String string = "Flaky test " + String.valueOf(gameTestInfo) + " failed, attempt: " + this.attempts + "/" + gameTestInstance.maxAttempts();
        if (gameTestInstance.requiredSuccesses() > 1) {
            string = string + ", successes: " + this.successes + " (" + gameTestInstance.requiredSuccesses() + " required)";
        }
        ReportGameListener.say(gameTestInfo.getLevel(), ChatFormatting.YELLOW, string);
        if (gameTestInfo.maxAttempts() - this.attempts + this.successes >= gameTestInfo.requiredSuccesses()) {
            gameTestRunner.rerunTest(gameTestInfo);
        } else {
            ReportGameListener.reportFailure(gameTestInfo, new ExhaustedAttemptsException(this.attempts, this.successes, gameTestInfo));
        }
    }

    @Override
    public void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner) {
        gameTestInfo2.addListener(this);
    }

    public static void reportPassed(GameTestInfo gameTestInfo, String string) {
        ReportGameListener.getTestInstanceBlockEntity(gameTestInfo).ifPresent(testInstanceBlockEntity -> testInstanceBlockEntity.setSuccess());
        ReportGameListener.visualizePassedTest(gameTestInfo, string);
    }

    private static void visualizePassedTest(GameTestInfo gameTestInfo, String string) {
        ReportGameListener.say(gameTestInfo.getLevel(), ChatFormatting.GREEN, string);
        GlobalTestReporter.onTestSuccess(gameTestInfo);
    }

    protected static void reportFailure(GameTestInfo gameTestInfo, Throwable throwable) {
        Component component;
        if (throwable instanceof GameTestAssertException) {
            GameTestAssertException gameTestAssertException = (GameTestAssertException)throwable;
            component = gameTestAssertException.getDescription();
        } else {
            component = Component.literal(Util.describeError(throwable));
        }
        ReportGameListener.getTestInstanceBlockEntity(gameTestInfo).ifPresent(testInstanceBlockEntity -> testInstanceBlockEntity.setErrorMessage(component));
        ReportGameListener.visualizeFailedTest(gameTestInfo, throwable);
    }

    protected static void visualizeFailedTest(GameTestInfo gameTestInfo, Throwable throwable) {
        String string = throwable.getMessage() + (String)(throwable.getCause() == null ? "" : " cause: " + Util.describeError(throwable.getCause()));
        String string2 = (gameTestInfo.isRequired() ? "" : "(optional) ") + String.valueOf(gameTestInfo.id()) + " failed! " + string;
        ReportGameListener.say(gameTestInfo.getLevel(), gameTestInfo.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, string2);
        Throwable throwable2 = (Throwable)MoreObjects.firstNonNull((Object)ExceptionUtils.getRootCause((Throwable)throwable), (Object)throwable);
        if (throwable2 instanceof GameTestAssertPosException) {
            GameTestAssertPosException gameTestAssertPosException = (GameTestAssertPosException)throwable2;
            gameTestInfo.getTestInstanceBlockEntity().markError(gameTestAssertPosException.getAbsolutePos(), gameTestAssertPosException.getMessageToShowAtBlock());
        }
        GlobalTestReporter.onTestFailed(gameTestInfo);
    }

    private static Optional<TestInstanceBlockEntity> getTestInstanceBlockEntity(GameTestInfo gameTestInfo) {
        ServerLevel serverLevel = gameTestInfo.getLevel();
        Optional<BlockPos> optional = Optional.ofNullable(gameTestInfo.getTestBlockPos());
        Optional<TestInstanceBlockEntity> optional2 = optional.flatMap(blockPos -> serverLevel.getBlockEntity((BlockPos)blockPos, BlockEntityType.TEST_INSTANCE_BLOCK));
        return optional2;
    }

    protected static void say(ServerLevel serverLevel, ChatFormatting chatFormatting, String string) {
        serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal(string).withStyle(chatFormatting)));
    }
}

