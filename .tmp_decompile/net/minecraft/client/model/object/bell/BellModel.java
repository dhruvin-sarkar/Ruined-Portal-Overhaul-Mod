/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.model.object.bell;

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
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BellModel
extends Model<State> {
    private static final String BELL_BODY = "bell_body";
    private final ModelPart bellBody;

    public BellModel(ModelPart modelPart) {
        super(modelPart, RenderTypes::entitySolid);
        this.bellBody = modelPart.getChild(BELL_BODY);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(BELL_BODY, CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -6.0f, -3.0f, 6.0f, 7.0f, 6.0f), PartPose.offset(8.0f, 12.0f, 8.0f));
        partDefinition2.addOrReplaceChild("bell_base", CubeListBuilder.create().texOffs(0, 13).addBox(4.0f, 4.0f, 4.0f, 8.0f, 2.0f, 8.0f), PartPose.offset(-8.0f, -12.0f, -8.0f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        float f = 0.0f;
        float g = 0.0f;
        if (state.shakeDirection != null) {
            float h = Mth.sin(state.ticks / (float)Math.PI) / (4.0f + state.ticks / 3.0f);
            switch (state.shakeDirection) {
                case NORTH: {
                    f = -h;
                    break;
                }
                case SOUTH: {
                    f = h;
                    break;
                }
                case EAST: {
                    g = -h;
                    break;
                }
                case WEST: {
                    g = h;
                }
            }
        }
        this.bellBody.xRot = f;
        this.bellBody.zRot = g;
    }

    @Environment(value=EnvType.CLIENT)
    public static final class State
    extends Record {
        final float ticks;
        final @Nullable Direction shakeDirection;

        public State(float f, @Nullable Direction direction) {
            this.ticks = f;
            this.shakeDirection = direction;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{State.class, "ticks;shakeDirection", "ticks", "shakeDirection"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{State.class, "ticks;shakeDirection", "ticks", "shakeDirection"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{State.class, "ticks;shakeDirection", "ticks", "shakeDirection"}, this, object);
        }

        public float ticks() {
            return this.ticks;
        }

        public @Nullable Direction shakeDirection() {
            return this.shakeDirection;
        }
    }
}

