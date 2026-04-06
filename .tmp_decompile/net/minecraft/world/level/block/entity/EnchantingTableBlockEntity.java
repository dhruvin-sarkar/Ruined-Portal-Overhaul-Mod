/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class EnchantingTableBlockEntity
extends BlockEntity
implements Nameable {
    private static final Component DEFAULT_NAME = Component.translatable("container.enchant");
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final RandomSource RANDOM = RandomSource.create();
    private @Nullable Component name;

    public EnchantingTableBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.ENCHANTING_TABLE, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.storeNullable("CustomName", ComponentSerialization.CODEC, this.name);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.name = EnchantingTableBlockEntity.parseCustomNameSafe(valueInput, "CustomName");
    }

    public static void bookAnimationTick(Level level, BlockPos blockPos, BlockState blockState, EnchantingTableBlockEntity enchantingTableBlockEntity) {
        float g;
        enchantingTableBlockEntity.oOpen = enchantingTableBlockEntity.open;
        enchantingTableBlockEntity.oRot = enchantingTableBlockEntity.rot;
        Player player = level.getNearestPlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 3.0, false);
        if (player != null) {
            double d = player.getX() - ((double)blockPos.getX() + 0.5);
            double e = player.getZ() - ((double)blockPos.getZ() + 0.5);
            enchantingTableBlockEntity.tRot = (float)Mth.atan2(e, d);
            enchantingTableBlockEntity.open += 0.1f;
            if (enchantingTableBlockEntity.open < 0.5f || RANDOM.nextInt(40) == 0) {
                float f = enchantingTableBlockEntity.flipT;
                do {
                    enchantingTableBlockEntity.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f == enchantingTableBlockEntity.flipT);
            }
        } else {
            enchantingTableBlockEntity.tRot += 0.02f;
            enchantingTableBlockEntity.open -= 0.1f;
        }
        while (enchantingTableBlockEntity.rot >= (float)Math.PI) {
            enchantingTableBlockEntity.rot -= (float)Math.PI * 2;
        }
        while (enchantingTableBlockEntity.rot < (float)(-Math.PI)) {
            enchantingTableBlockEntity.rot += (float)Math.PI * 2;
        }
        while (enchantingTableBlockEntity.tRot >= (float)Math.PI) {
            enchantingTableBlockEntity.tRot -= (float)Math.PI * 2;
        }
        while (enchantingTableBlockEntity.tRot < (float)(-Math.PI)) {
            enchantingTableBlockEntity.tRot += (float)Math.PI * 2;
        }
        for (g = enchantingTableBlockEntity.tRot - enchantingTableBlockEntity.rot; g >= (float)Math.PI; g -= (float)Math.PI * 2) {
        }
        while (g < (float)(-Math.PI)) {
            g += (float)Math.PI * 2;
        }
        enchantingTableBlockEntity.rot += g * 0.4f;
        enchantingTableBlockEntity.open = Mth.clamp(enchantingTableBlockEntity.open, 0.0f, 1.0f);
        ++enchantingTableBlockEntity.time;
        enchantingTableBlockEntity.oFlip = enchantingTableBlockEntity.flip;
        float h = (enchantingTableBlockEntity.flipT - enchantingTableBlockEntity.flip) * 0.4f;
        float i = 0.2f;
        h = Mth.clamp(h, -0.2f, 0.2f);
        enchantingTableBlockEntity.flipA += (h - enchantingTableBlockEntity.flipA) * 0.9f;
        enchantingTableBlockEntity.flip += enchantingTableBlockEntity.flipA;
    }

    @Override
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return DEFAULT_NAME;
    }

    public void setCustomName(@Nullable Component component) {
        this.name = component;
    }

    @Override
    public @Nullable Component getCustomName() {
        return this.name;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        super.applyImplicitComponents(dataComponentGetter);
        this.name = dataComponentGetter.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput valueOutput) {
        valueOutput.discard("CustomName");
    }
}

