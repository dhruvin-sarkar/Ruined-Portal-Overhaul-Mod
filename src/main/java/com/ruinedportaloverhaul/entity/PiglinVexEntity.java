package com.ruinedportaloverhaul.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;

public class PiglinVexEntity extends Vex {
    private static final int OWNERLESS_TICKS_LIMIT = 1200;

    private int ownerlessTicks;

    public PiglinVexEntity(EntityType<? extends PiglinVexEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vex.createAttributes()
            .add(Attributes.MAX_HEALTH, 18.0)
            .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public void tick() {
        super.tick();
        Mob owner = this.getOwner();
        if (owner == null || !owner.isAlive()) {
            ownerlessTicks++;
            if (ownerlessTicks >= OWNERLESS_TICKS_LIMIT) {
                this.discard();
            }
        } else {
            ownerlessTicks = 0;
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
    }
}
