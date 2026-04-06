/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface RemoteSlot {
    public static final RemoteSlot PLACEHOLDER = new RemoteSlot(){

        @Override
        public void receive(HashedStack hashedStack) {
        }

        @Override
        public void force(ItemStack itemStack) {
        }

        @Override
        public boolean matches(ItemStack itemStack) {
            return true;
        }
    };

    public void force(ItemStack var1);

    public void receive(HashedStack var1);

    public boolean matches(ItemStack var1);

    public static class Synchronized
    implements RemoteSlot {
        private final HashedPatchMap.HashGenerator hasher;
        private @Nullable ItemStack remoteStack = null;
        private @Nullable HashedStack remoteHash = null;

        public Synchronized(HashedPatchMap.HashGenerator hashGenerator) {
            this.hasher = hashGenerator;
        }

        @Override
        public void force(ItemStack itemStack) {
            this.remoteStack = itemStack.copy();
            this.remoteHash = null;
        }

        @Override
        public void receive(HashedStack hashedStack) {
            this.remoteStack = null;
            this.remoteHash = hashedStack;
        }

        @Override
        public boolean matches(ItemStack itemStack) {
            if (this.remoteStack != null) {
                return ItemStack.matches(this.remoteStack, itemStack);
            }
            if (this.remoteHash != null && this.remoteHash.matches(itemStack, this.hasher)) {
                this.remoteStack = itemStack.copy();
                return true;
            }
            return false;
        }

        public void copyFrom(Synchronized synchronized_) {
            this.remoteStack = synchronized_.remoteStack;
            this.remoteHash = synchronized_.remoteHash;
        }
    }
}

