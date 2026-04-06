/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.jspecify.annotations.Nullable;

public class ItemSpawnEggFix
extends DataFix {
    private static final @Nullable String[] ID_TO_ENTITY = (String[])DataFixUtils.make((Object)new String[256], strings -> {
        strings[1] = "Item";
        strings[2] = "XPOrb";
        strings[7] = "ThrownEgg";
        strings[8] = "LeashKnot";
        strings[9] = "Painting";
        strings[10] = "Arrow";
        strings[11] = "Snowball";
        strings[12] = "Fireball";
        strings[13] = "SmallFireball";
        strings[14] = "ThrownEnderpearl";
        strings[15] = "EyeOfEnderSignal";
        strings[16] = "ThrownPotion";
        strings[17] = "ThrownExpBottle";
        strings[18] = "ItemFrame";
        strings[19] = "WitherSkull";
        strings[20] = "PrimedTnt";
        strings[21] = "FallingSand";
        strings[22] = "FireworksRocketEntity";
        strings[23] = "TippedArrow";
        strings[24] = "SpectralArrow";
        strings[25] = "ShulkerBullet";
        strings[26] = "DragonFireball";
        strings[30] = "ArmorStand";
        strings[41] = "Boat";
        strings[42] = "MinecartRideable";
        strings[43] = "MinecartChest";
        strings[44] = "MinecartFurnace";
        strings[45] = "MinecartTNT";
        strings[46] = "MinecartHopper";
        strings[47] = "MinecartSpawner";
        strings[40] = "MinecartCommandBlock";
        strings[50] = "Creeper";
        strings[51] = "Skeleton";
        strings[52] = "Spider";
        strings[53] = "Giant";
        strings[54] = "Zombie";
        strings[55] = "Slime";
        strings[56] = "Ghast";
        strings[57] = "PigZombie";
        strings[58] = "Enderman";
        strings[59] = "CaveSpider";
        strings[60] = "Silverfish";
        strings[61] = "Blaze";
        strings[62] = "LavaSlime";
        strings[63] = "EnderDragon";
        strings[64] = "WitherBoss";
        strings[65] = "Bat";
        strings[66] = "Witch";
        strings[67] = "Endermite";
        strings[68] = "Guardian";
        strings[69] = "Shulker";
        strings[90] = "Pig";
        strings[91] = "Sheep";
        strings[92] = "Cow";
        strings[93] = "Chicken";
        strings[94] = "Squid";
        strings[95] = "Wolf";
        strings[96] = "MushroomCow";
        strings[97] = "SnowMan";
        strings[98] = "Ozelot";
        strings[99] = "VillagerGolem";
        strings[100] = "EntityHorse";
        strings[101] = "Rabbit";
        strings[120] = "Villager";
        strings[200] = "EnderCrystal";
    });

    public ItemSpawnEggFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type type = schema.getType(References.ITEM_STACK);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder opticFinder2 = DSL.fieldFinder((String)"id", (Type)DSL.string());
        OpticFinder opticFinder3 = type.findField("tag");
        OpticFinder opticFinder4 = opticFinder3.type().findField("EntityTag");
        OpticFinder opticFinder5 = DSL.typeFinder((Type)schema.getTypeRaw(References.ENTITY));
        return this.fixTypeEverywhereTyped("ItemSpawnEggFix", type, typed2 -> {
            Optional optional = typed2.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:spawn_egg")) {
                Dynamic dynamic = (Dynamic)typed2.get(DSL.remainderFinder());
                short s = dynamic.get("Damage").asShort((short)0);
                Optional optional2 = typed2.getOptionalTyped(opticFinder3);
                Optional optional3 = optional2.flatMap(typed -> typed.getOptionalTyped(opticFinder4));
                Optional optional4 = optional3.flatMap(typed -> typed.getOptionalTyped(opticFinder5));
                Optional optional5 = optional4.flatMap(typed -> typed.getOptional(opticFinder2));
                Typed typed22 = typed2;
                String string = ID_TO_ENTITY[s & 0xFF];
                if (string != null && (optional5.isEmpty() || !Objects.equals(optional5.get(), string))) {
                    Typed typed3 = typed2.getOrCreateTyped(opticFinder3);
                    Dynamic dynamic2 = (Dynamic)DataFixUtils.orElse(typed3.getOptionalTyped(opticFinder4).map(typed -> (Dynamic)typed.write().getOrThrow()), (Object)dynamic.emptyMap());
                    dynamic2 = dynamic2.set("id", dynamic2.createString(string));
                    typed22 = typed22.set(opticFinder3, ExtraDataFixUtils.readAndSet(typed3, opticFinder4, dynamic2));
                }
                if (s != 0) {
                    dynamic = dynamic.set("Damage", dynamic.createShort((short)0));
                    typed22 = typed22.set(DSL.remainderFinder(), (Object)dynamic);
                }
                return typed22;
            }
            return typed2;
        });
    }
}

