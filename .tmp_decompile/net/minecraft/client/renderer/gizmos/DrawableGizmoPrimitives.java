/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package net.minecraft.client.renderer.gizmos;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

@Environment(value=EnvType.CLIENT)
public class DrawableGizmoPrimitives
implements GizmoPrimitives {
    private final Group opaque = new Group(true);
    private final Group translucent = new Group(false);
    private boolean isEmpty = true;

    private Group getGroup(int i) {
        if (ARGB.alpha(i) < 255) {
            return this.translucent;
        }
        return this.opaque;
    }

    @Override
    public void addPoint(Vec3 vec3, int i, float f) {
        this.getGroup((int)i).points.add(new Point(vec3, i, f));
        this.isEmpty = false;
    }

    @Override
    public void addLine(Vec3 vec3, Vec3 vec32, int i, float f) {
        this.getGroup((int)i).lines.add(new Line(vec3, vec32, i, f));
        this.isEmpty = false;
    }

    @Override
    public void addTriangleFan(Vec3[] vec3s, int i) {
        this.getGroup((int)i).triangleFans.add(new TriangleFan(vec3s, i));
        this.isEmpty = false;
    }

    @Override
    public void addQuad(Vec3 vec3, Vec3 vec32, Vec3 vec33, Vec3 vec34, int i) {
        this.getGroup((int)i).quads.add(new Quad(vec3, vec32, vec33, vec34, i));
        this.isEmpty = false;
    }

    @Override
    public void addText(Vec3 vec3, String string, TextGizmo.Style style) {
        this.getGroup((int)style.color()).texts.add(new Text(vec3, string, style));
        this.isEmpty = false;
    }

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, CameraRenderState cameraRenderState, Matrix4f matrix4f) {
        this.opaque.render(poseStack, multiBufferSource, cameraRenderState, matrix4f);
        this.translucent.render(poseStack, multiBufferSource, cameraRenderState, matrix4f);
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    @Environment(value=EnvType.CLIENT)
    static final class Group
    extends Record {
        private final boolean opaque;
        final List<Line> lines;
        final List<Quad> quads;
        final List<TriangleFan> triangleFans;
        final List<Text> texts;
        final List<Point> points;

        Group(boolean bl) {
            this(bl, new ArrayList<Line>(), new ArrayList<Quad>(), new ArrayList<TriangleFan>(), new ArrayList<Text>(), new ArrayList<Point>());
        }

        private Group(boolean bl, List<Line> list, List<Quad> list2, List<TriangleFan> list3, List<Text> list4, List<Point> list5) {
            this.opaque = bl;
            this.lines = list;
            this.quads = list2;
            this.triangleFans = list3;
            this.texts = list4;
            this.points = list5;
        }

        public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, CameraRenderState cameraRenderState, Matrix4f matrix4f) {
            this.renderQuads(poseStack, multiBufferSource, cameraRenderState);
            this.renderTriangleFans(poseStack, multiBufferSource, cameraRenderState);
            this.renderLines(poseStack, multiBufferSource, cameraRenderState, matrix4f);
            this.renderTexts(poseStack, multiBufferSource, cameraRenderState);
            this.renderPoints(poseStack, multiBufferSource, cameraRenderState);
        }

        private void renderTexts(PoseStack poseStack, MultiBufferSource multiBufferSource, CameraRenderState cameraRenderState) {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            if (!cameraRenderState.initialized) {
                return;
            }
            double d = cameraRenderState.pos.x();
            double e = cameraRenderState.pos.y();
            double f = cameraRenderState.pos.z();
            for (Text text : this.texts) {
                poseStack.pushPose();
                poseStack.translate((float)(text.pos().x() - d), (float)(text.pos().y() - e), (float)(text.pos().z() - f));
                poseStack.mulPose((Quaternionfc)cameraRenderState.orientation);
                poseStack.scale(text.style.scale() / 16.0f, -text.style.scale() / 16.0f, text.style.scale() / 16.0f);
                float g = text.style.adjustLeft().isEmpty() ? (float)(-font.width(text.text)) / 2.0f : (float)(-text.style.adjustLeft().getAsDouble()) / text.style.scale();
                font.drawInBatch(text.text, g, 0.0f, text.style.color(), false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                poseStack.popPose();
            }
        }

        private void renderLines(PoseStack poseStack, MultiBufferSource multiBufferSource, CameraRenderState cameraRenderState, Matrix4f matrix4f) {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.opaque ? RenderTypes.lines() : RenderTypes.linesTranslucent());
            PoseStack.Pose pose = poseStack.last();
            Vector4f vector4f = new Vector4f();
            Vector4f vector4f2 = new Vector4f();
            Vector4f vector4f3 = new Vector4f();
            Vector4f vector4f4 = new Vector4f();
            Vector4f vector4f5 = new Vector4f();
            double d = cameraRenderState.pos.x();
            double e = cameraRenderState.pos.y();
            double f = cameraRenderState.pos.z();
            for (Line line : this.lines) {
                boolean bl2;
                vector4f.set(line.start().x() - d, line.start().y() - e, line.start().z() - f, 1.0);
                vector4f2.set(line.end().x() - d, line.end().y() - e, line.end().z() - f, 1.0);
                vector4f.mul((Matrix4fc)matrix4f, vector4f3);
                vector4f2.mul((Matrix4fc)matrix4f, vector4f4);
                boolean bl = vector4f3.z > -0.05f;
                boolean bl3 = bl2 = vector4f4.z > -0.05f;
                if (bl && bl2) continue;
                if (bl || bl2) {
                    float g = vector4f4.z - vector4f3.z;
                    if (Math.abs(g) < 1.0E-9f) continue;
                    float h = Mth.clamp((-0.05f - vector4f3.z) / g, 0.0f, 1.0f);
                    vector4f.lerp((Vector4fc)vector4f2, h, vector4f5);
                    if (bl) {
                        vector4f.set((Vector4fc)vector4f5);
                    } else {
                        vector4f2.set((Vector4fc)vector4f5);
                    }
                }
                vertexConsumer.addVertex(pose, vector4f.x, vector4f.y, vector4f.z).setNormal(pose, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z).setColor(line.color()).setLineWidth(line.width());
                vertexConsumer.addVertex(pose, vector4f2.x, vector4f2.y, vector4f2.z).setNormal(pose, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z).setColor(line.color()).setLineWidth(line.width());
            }
        }

        private void renderTriangleFans(PoseStack poseStack, MultiBufferSource multiBufferSource, CameraRenderState cameraRenderState) {
            PoseStack.Pose pose = poseStack.last();
            double d = cameraRenderState.pos.x();
            double e = cameraRenderState.pos.y();
            double f = cameraRenderState.pos.z();
            for (TriangleFan triangleFan : this.triangleFans) {
                VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.debugTriangleFan());
                for (Vec3 vec3 : triangleFan.points()) {
                    vertexConsumer.addVertex(pose, (float)(vec3.x() - d), (float)(vec3.y() - e), (float)(vec3.z() - f)).setColor(triangleFan.color());
                }
            }
        }

        private void renderQuads(PoseStack poseStack, MultiBufferSource multiBufferSource, CameraRenderState cameraRenderState) {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.debugFilledBox());
            PoseStack.Pose pose = poseStack.last();
            double d = cameraRenderState.pos.x();
            double e = cameraRenderState.pos.y();
            double f = cameraRenderState.pos.z();
            for (Quad quad : this.quads) {
                vertexConsumer.addVertex(pose, (float)(quad.a().x() - d), (float)(quad.a().y() - e), (float)(quad.a().z() - f)).setColor(quad.color());
                vertexConsumer.addVertex(pose, (float)(quad.b().x() - d), (float)(quad.b().y() - e), (float)(quad.b().z() - f)).setColor(quad.color());
                vertexConsumer.addVertex(pose, (float)(quad.c().x() - d), (float)(quad.c().y() - e), (float)(quad.c().z() - f)).setColor(quad.color());
                vertexConsumer.addVertex(pose, (float)(quad.d().x() - d), (float)(quad.d().y() - e), (float)(quad.d().z() - f)).setColor(quad.color());
            }
        }

        private void renderPoints(PoseStack poseStack, MultiBufferSource multiBufferSource, CameraRenderState cameraRenderState) {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.debugPoint());
            PoseStack.Pose pose = poseStack.last();
            double d = cameraRenderState.pos.x();
            double e = cameraRenderState.pos.y();
            double f = cameraRenderState.pos.z();
            for (Point point : this.points) {
                vertexConsumer.addVertex(pose, (float)(point.pos.x() - d), (float)(point.pos.y() - e), (float)(point.pos.z() - f)).setColor(point.color()).setLineWidth(point.size());
            }
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Group.class, "opaque;lines;quads;triangleFans;texts;points", "opaque", "lines", "quads", "triangleFans", "texts", "points"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Group.class, "opaque;lines;quads;triangleFans;texts;points", "opaque", "lines", "quads", "triangleFans", "texts", "points"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Group.class, "opaque;lines;quads;triangleFans;texts;points", "opaque", "lines", "quads", "triangleFans", "texts", "points"}, this, object);
        }

        public boolean opaque() {
            return this.opaque;
        }

        public List<Line> lines() {
            return this.lines;
        }

        public List<Quad> quads() {
            return this.quads;
        }

        public List<TriangleFan> triangleFans() {
            return this.triangleFans;
        }

        public List<Text> texts() {
            return this.texts;
        }

        public List<Point> points() {
            return this.points;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class Point
    extends Record {
        final Vec3 pos;
        private final int color;
        private final float size;

        Point(Vec3 vec3, int i, float f) {
            this.pos = vec3;
            this.color = i;
            this.size = f;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Point.class, "pos;color;size", "pos", "color", "size"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Point.class, "pos;color;size", "pos", "color", "size"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Point.class, "pos;color;size", "pos", "color", "size"}, this, object);
        }

        public Vec3 pos() {
            return this.pos;
        }

        public int color() {
            return this.color;
        }

        public float size() {
            return this.size;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Line(Vec3 start, Vec3 end, int color, float width) {
    }

    @Environment(value=EnvType.CLIENT)
    record TriangleFan(Vec3[] points, int color) {
    }

    @Environment(value=EnvType.CLIENT)
    record Quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color) {
    }

    @Environment(value=EnvType.CLIENT)
    static final class Text
    extends Record {
        private final Vec3 pos;
        final String text;
        final TextGizmo.Style style;

        Text(Vec3 vec3, String string, TextGizmo.Style style) {
            this.pos = vec3;
            this.text = string;
            this.style = style;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Text.class, "pos;text;style", "pos", "text", "style"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Text.class, "pos;text;style", "pos", "text", "style"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Text.class, "pos;text;style", "pos", "text", "style"}, this, object);
        }

        public Vec3 pos() {
            return this.pos;
        }

        public String text() {
            return this.text;
        }

        public TextGizmo.Style style() {
            return this.style;
        }
    }
}

