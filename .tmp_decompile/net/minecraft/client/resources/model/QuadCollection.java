/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ArrayListMultimap
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Multimap
 *  java.lang.MatchException
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.resources.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.lang.runtime.SwitchBootstraps;
import java.util.Collection;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class QuadCollection {
    public static final QuadCollection EMPTY = new QuadCollection(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    private final List<BakedQuad> all;
    private final List<BakedQuad> unculled;
    private final List<BakedQuad> north;
    private final List<BakedQuad> south;
    private final List<BakedQuad> east;
    private final List<BakedQuad> west;
    private final List<BakedQuad> up;
    private final List<BakedQuad> down;

    QuadCollection(List<BakedQuad> list, List<BakedQuad> list2, List<BakedQuad> list3, List<BakedQuad> list4, List<BakedQuad> list5, List<BakedQuad> list6, List<BakedQuad> list7, List<BakedQuad> list8) {
        this.all = list;
        this.unculled = list2;
        this.north = list3;
        this.south = list4;
        this.east = list5;
        this.west = list6;
        this.up = list7;
        this.down = list8;
    }

    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        Direction direction2 = direction;
        int n = 0;
        return switch (SwitchBootstraps.enumSwitch("enumSwitch", new Object[]{"NORTH", "SOUTH", "EAST", "WEST", "UP", "DOWN"}, (Direction)direction2, (int)n)) {
            default -> throw new MatchException(null, null);
            case -1 -> this.unculled;
            case 0 -> this.north;
            case 1 -> this.south;
            case 2 -> this.east;
            case 3 -> this.west;
            case 4 -> this.up;
            case 5 -> this.down;
        };
    }

    public List<BakedQuad> getAll() {
        return this.all;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final ImmutableList.Builder<BakedQuad> unculledFaces = ImmutableList.builder();
        private final Multimap<Direction, BakedQuad> culledFaces = ArrayListMultimap.create();

        public Builder addCulledFace(Direction direction, BakedQuad bakedQuad) {
            this.culledFaces.put((Object)direction, (Object)bakedQuad);
            return this;
        }

        public Builder addUnculledFace(BakedQuad bakedQuad) {
            this.unculledFaces.add((Object)bakedQuad);
            return this;
        }

        private static QuadCollection createFromSublists(List<BakedQuad> list, int i, int j, int k, int l, int m, int n, int o) {
            int p = 0;
            List<BakedQuad> list2 = list.subList(p, p += i);
            List<BakedQuad> list3 = list.subList(p, p += j);
            List<BakedQuad> list4 = list.subList(p, p += k);
            List<BakedQuad> list5 = list.subList(p, p += l);
            List<BakedQuad> list6 = list.subList(p, p += m);
            List<BakedQuad> list7 = list.subList(p, p += n);
            List<BakedQuad> list8 = list.subList(p, p + o);
            return new QuadCollection(list, list2, list3, list4, list5, list6, list7, list8);
        }

        public QuadCollection build() {
            ImmutableList immutableList = this.unculledFaces.build();
            if (this.culledFaces.isEmpty()) {
                if (immutableList.isEmpty()) {
                    return EMPTY;
                }
                return new QuadCollection((List<BakedQuad>)immutableList, (List<BakedQuad>)immutableList, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            builder.addAll((Iterable)immutableList);
            Collection collection = this.culledFaces.get((Object)Direction.NORTH);
            builder.addAll((Iterable)collection);
            Collection collection2 = this.culledFaces.get((Object)Direction.SOUTH);
            builder.addAll((Iterable)collection2);
            Collection collection3 = this.culledFaces.get((Object)Direction.EAST);
            builder.addAll((Iterable)collection3);
            Collection collection4 = this.culledFaces.get((Object)Direction.WEST);
            builder.addAll((Iterable)collection4);
            Collection collection5 = this.culledFaces.get((Object)Direction.UP);
            builder.addAll((Iterable)collection5);
            Collection collection6 = this.culledFaces.get((Object)Direction.DOWN);
            builder.addAll((Iterable)collection6);
            return Builder.createFromSublists((List<BakedQuad>)builder.build(), immutableList.size(), collection.size(), collection2.size(), collection3.size(), collection4.size(), collection5.size(), collection6.size());
        }
    }
}

