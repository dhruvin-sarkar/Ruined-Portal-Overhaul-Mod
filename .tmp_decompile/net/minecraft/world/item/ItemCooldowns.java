/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;

public class ItemCooldowns {
    private final Map<Identifier, CooldownInstance> cooldowns = Maps.newHashMap();
    private int tickCount;

    public boolean isOnCooldown(ItemStack itemStack) {
        return this.getCooldownPercent(itemStack, 0.0f) > 0.0f;
    }

    public float getCooldownPercent(ItemStack itemStack, float f) {
        Identifier identifier = this.getCooldownGroup(itemStack);
        CooldownInstance cooldownInstance = this.cooldowns.get(identifier);
        if (cooldownInstance != null) {
            float g = cooldownInstance.endTime - cooldownInstance.startTime;
            float h = (float)cooldownInstance.endTime - ((float)this.tickCount + f);
            return Mth.clamp(h / g, 0.0f, 1.0f);
        }
        return 0.0f;
    }

    public void tick() {
        ++this.tickCount;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Map.Entry<Identifier, CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Identifier, CooldownInstance> entry = iterator.next();
                if (entry.getValue().endTime > this.tickCount) continue;
                iterator.remove();
                this.onCooldownEnded(entry.getKey());
            }
        }
    }

    public Identifier getCooldownGroup(ItemStack itemStack) {
        UseCooldown useCooldown = itemStack.get(DataComponents.USE_COOLDOWN);
        Identifier identifier = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (useCooldown == null) {
            return identifier;
        }
        return useCooldown.cooldownGroup().orElse(identifier);
    }

    public void addCooldown(ItemStack itemStack, int i) {
        this.addCooldown(this.getCooldownGroup(itemStack), i);
    }

    public void addCooldown(Identifier identifier, int i) {
        this.cooldowns.put(identifier, new CooldownInstance(this.tickCount, this.tickCount + i));
        this.onCooldownStarted(identifier, i);
    }

    public void removeCooldown(Identifier identifier) {
        this.cooldowns.remove(identifier);
        this.onCooldownEnded(identifier);
    }

    protected void onCooldownStarted(Identifier identifier, int i) {
    }

    protected void onCooldownEnded(Identifier identifier) {
    }

    static final class CooldownInstance
    extends Record {
        final int startTime;
        final int endTime;

        CooldownInstance(int i, int j) {
            this.startTime = i;
            this.endTime = j;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CooldownInstance.class, "startTime;endTime", "startTime", "endTime"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CooldownInstance.class, "startTime;endTime", "startTime", "endTime"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CooldownInstance.class, "startTime;endTime", "startTime", "endTime"}, this, object);
        }

        public int startTime() {
            return this.startTime;
        }

        public int endTime() {
            return this.endTime;
        }
    }
}

