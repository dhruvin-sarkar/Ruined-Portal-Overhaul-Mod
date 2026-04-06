/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands;

@FunctionalInterface
public interface CommandResultCallback {
    public static final CommandResultCallback EMPTY = new CommandResultCallback(){

        @Override
        public void onResult(boolean bl, int i) {
        }

        public String toString() {
            return "<empty>";
        }
    };

    public void onResult(boolean var1, int var2);

    default public void onSuccess(int i) {
        this.onResult(true, i);
    }

    default public void onFailure() {
        this.onResult(false, 0);
    }

    public static CommandResultCallback chain(CommandResultCallback commandResultCallback, CommandResultCallback commandResultCallback2) {
        if (commandResultCallback == EMPTY) {
            return commandResultCallback2;
        }
        if (commandResultCallback2 == EMPTY) {
            return commandResultCallback;
        }
        return (bl, i) -> {
            commandResultCallback.onResult(bl, i);
            commandResultCallback2.onResult(bl, i);
        };
    }
}

