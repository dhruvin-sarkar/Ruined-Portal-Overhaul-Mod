/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class VertexMultiConsumer {
    public static VertexConsumer create() {
        throw new IllegalArgumentException();
    }

    public static VertexConsumer create(VertexConsumer vertexConsumer) {
        return vertexConsumer;
    }

    public static VertexConsumer create(VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2) {
        return new Double(vertexConsumer, vertexConsumer2);
    }

    public static VertexConsumer create(VertexConsumer ... vertexConsumers) {
        return new Multiple(vertexConsumers);
    }

    @Environment(value=EnvType.CLIENT)
    static class Double
    implements VertexConsumer {
        private final VertexConsumer first;
        private final VertexConsumer second;

        public Double(VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2) {
            if (vertexConsumer == vertexConsumer2) {
                throw new IllegalArgumentException("Duplicate delegates");
            }
            this.first = vertexConsumer;
            this.second = vertexConsumer2;
        }

        @Override
        public VertexConsumer addVertex(float f, float g, float h) {
            this.first.addVertex(f, g, h);
            this.second.addVertex(f, g, h);
            return this;
        }

        @Override
        public VertexConsumer setColor(int i, int j, int k, int l) {
            this.first.setColor(i, j, k, l);
            this.second.setColor(i, j, k, l);
            return this;
        }

        @Override
        public VertexConsumer setColor(int i) {
            this.first.setColor(i);
            this.second.setColor(i);
            return this;
        }

        @Override
        public VertexConsumer setUv(float f, float g) {
            this.first.setUv(f, g);
            this.second.setUv(f, g);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int i, int j) {
            this.first.setUv1(i, j);
            this.second.setUv1(i, j);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int i, int j) {
            this.first.setUv2(i, j);
            this.second.setUv2(i, j);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float f, float g, float h) {
            this.first.setNormal(f, g, h);
            this.second.setNormal(f, g, h);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float f) {
            this.first.setLineWidth(f);
            this.second.setLineWidth(f);
            return this;
        }

        @Override
        public void addVertex(float f, float g, float h, int i, float j, float k, int l, int m, float n, float o, float p) {
            this.first.addVertex(f, g, h, i, j, k, l, m, n, o, p);
            this.second.addVertex(f, g, h, i, j, k, l, m, n, o, p);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Multiple(VertexConsumer[] delegates) implements VertexConsumer
    {
        Multiple {
            for (int i = 0; i < vertexConsumers.length; ++i) {
                for (int j = i + 1; j < vertexConsumers.length; ++j) {
                    if (vertexConsumers[i] != vertexConsumers[j]) continue;
                    throw new IllegalArgumentException("Duplicate delegates");
                }
            }
        }

        private void forEach(Consumer<VertexConsumer> consumer) {
            for (VertexConsumer vertexConsumer : this.delegates) {
                consumer.accept(vertexConsumer);
            }
        }

        @Override
        public VertexConsumer addVertex(float f, float g, float h) {
            this.forEach(vertexConsumer -> vertexConsumer.addVertex(f, g, h));
            return this;
        }

        @Override
        public VertexConsumer setColor(int i, int j, int k, int l) {
            this.forEach(vertexConsumer -> vertexConsumer.setColor(i, j, k, l));
            return this;
        }

        @Override
        public VertexConsumer setColor(int i) {
            this.forEach(vertexConsumer -> vertexConsumer.setColor(i));
            return this;
        }

        @Override
        public VertexConsumer setUv(float f, float g) {
            this.forEach(vertexConsumer -> vertexConsumer.setUv(f, g));
            return this;
        }

        @Override
        public VertexConsumer setUv1(int i, int j) {
            this.forEach(vertexConsumer -> vertexConsumer.setUv1(i, j));
            return this;
        }

        @Override
        public VertexConsumer setUv2(int i, int j) {
            this.forEach(vertexConsumer -> vertexConsumer.setUv2(i, j));
            return this;
        }

        @Override
        public VertexConsumer setNormal(float f, float g, float h) {
            this.forEach(vertexConsumer -> vertexConsumer.setNormal(f, g, h));
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float f) {
            this.forEach(vertexConsumer -> vertexConsumer.setLineWidth(f));
            return this;
        }

        @Override
        public void addVertex(float f, float g, float h, int i, float j, float k, int l, int m, float n, float o, float p) {
            this.forEach(vertexConsumer -> vertexConsumer.addVertex(f, g, h, i, j, k, l, m, n, o, p));
        }
    }
}

