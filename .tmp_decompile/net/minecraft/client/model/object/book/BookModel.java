/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.object.book;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class BookModel
extends Model<State> {
    private static final String LEFT_PAGES = "left_pages";
    private static final String RIGHT_PAGES = "right_pages";
    private static final String FLIP_PAGE_1 = "flip_page1";
    private static final String FLIP_PAGE_2 = "flip_page2";
    private final ModelPart leftLid;
    private final ModelPart rightLid;
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart flipPage1;
    private final ModelPart flipPage2;

    public BookModel(ModelPart modelPart) {
        super(modelPart, RenderTypes::entitySolid);
        this.leftLid = modelPart.getChild("left_lid");
        this.rightLid = modelPart.getChild("right_lid");
        this.leftPages = modelPart.getChild(LEFT_PAGES);
        this.rightPages = modelPart.getChild(RIGHT_PAGES);
        this.flipPage1 = modelPart.getChild(FLIP_PAGE_1);
        this.flipPage2 = modelPart.getChild(FLIP_PAGE_2);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), PartPose.offset(0.0f, 0.0f, -1.0f));
        partDefinition.addOrReplaceChild("right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), PartPose.offset(0.0f, 0.0f, 1.0f));
        partDefinition.addOrReplaceChild("seam", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0f, -5.0f, 0.0f, 2.0f, 10.0f, 0.005f), PartPose.rotation(0.0f, 1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild(LEFT_PAGES, CubeListBuilder.create().texOffs(0, 10).addBox(0.0f, -4.0f, -0.99f, 5.0f, 8.0f, 1.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(RIGHT_PAGES, CubeListBuilder.create().texOffs(12, 10).addBox(0.0f, -4.0f, -0.01f, 5.0f, 8.0f, 1.0f), PartPose.ZERO);
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(24, 10).addBox(0.0f, -4.0f, 0.0f, 5.0f, 8.0f, 0.005f);
        partDefinition.addOrReplaceChild(FLIP_PAGE_1, cubeListBuilder, PartPose.ZERO);
        partDefinition.addOrReplaceChild(FLIP_PAGE_2, cubeListBuilder, PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        float f = (Mth.sin(state.animationPos * 0.02f) * 0.1f + 1.25f) * state.open;
        this.leftLid.yRot = (float)Math.PI + f;
        this.rightLid.yRot = -f;
        this.leftPages.yRot = f;
        this.rightPages.yRot = -f;
        this.flipPage1.yRot = f - f * 2.0f * state.pageFlip1;
        this.flipPage2.yRot = f - f * 2.0f * state.pageFlip2;
        this.leftPages.x = Mth.sin(f);
        this.rightPages.x = Mth.sin(f);
        this.flipPage1.x = Mth.sin(f);
        this.flipPage2.x = Mth.sin(f);
    }

    @Environment(value=EnvType.CLIENT)
    public static final class State
    extends Record {
        final float animationPos;
        final float pageFlip1;
        final float pageFlip2;
        final float open;

        public State(float f, float g, float h, float i) {
            this.animationPos = f;
            this.pageFlip1 = g;
            this.pageFlip2 = h;
            this.open = i;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{State.class, "animationPos;pageFlip1;pageFlip2;open", "animationPos", "pageFlip1", "pageFlip2", "open"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{State.class, "animationPos;pageFlip1;pageFlip2;open", "animationPos", "pageFlip1", "pageFlip2", "open"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{State.class, "animationPos;pageFlip1;pageFlip2;open", "animationPos", "pageFlip1", "pageFlip2", "open"}, this, object);
        }

        public float animationPos() {
            return this.animationPos;
        }

        public float pageFlip1() {
            return this.pageFlip1;
        }

        public float pageFlip2() {
            return this.pageFlip2;
        }

        public float open() {
            return this.open;
        }
    }
}

