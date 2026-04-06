/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.minecraft.util.Ease;
import net.minecraft.util.ExtraCodecs;

public interface EasingType {
    public static final ExtraCodecs.LateBoundIdMapper<String, EasingType> SIMPLE_REGISTRY = new ExtraCodecs.LateBoundIdMapper();
    public static final Codec<EasingType> CODEC = Codec.either(SIMPLE_REGISTRY.codec((Codec<String>)Codec.STRING), CubicBezier.CODEC).xmap(Either::unwrap, easingType -> {
        Either either;
        if (easingType instanceof CubicBezier) {
            CubicBezier cubicBezier = (CubicBezier)easingType;
            either = Either.right((Object)cubicBezier);
        } else {
            either = Either.left((Object)easingType);
        }
        return either;
    });
    public static final EasingType CONSTANT = EasingType.registerSimple("constant", f -> 0.0f);
    public static final EasingType LINEAR = EasingType.registerSimple("linear", f -> f);
    public static final EasingType IN_BACK = EasingType.registerSimple("in_back", Ease::inBack);
    public static final EasingType IN_BOUNCE = EasingType.registerSimple("in_bounce", Ease::inBounce);
    public static final EasingType IN_CIRC = EasingType.registerSimple("in_circ", Ease::inCirc);
    public static final EasingType IN_CUBIC = EasingType.registerSimple("in_cubic", Ease::inCubic);
    public static final EasingType IN_ELASTIC = EasingType.registerSimple("in_elastic", Ease::inElastic);
    public static final EasingType IN_EXPO = EasingType.registerSimple("in_expo", Ease::inExpo);
    public static final EasingType IN_QUAD = EasingType.registerSimple("in_quad", Ease::inQuad);
    public static final EasingType IN_QUART = EasingType.registerSimple("in_quart", Ease::inQuart);
    public static final EasingType IN_QUINT = EasingType.registerSimple("in_quint", Ease::inQuint);
    public static final EasingType IN_SINE = EasingType.registerSimple("in_sine", Ease::inSine);
    public static final EasingType IN_OUT_BACK = EasingType.registerSimple("in_out_back", Ease::inOutBack);
    public static final EasingType IN_OUT_BOUNCE = EasingType.registerSimple("in_out_bounce", Ease::inOutBounce);
    public static final EasingType IN_OUT_CIRC = EasingType.registerSimple("in_out_circ", Ease::inOutCirc);
    public static final EasingType IN_OUT_CUBIC = EasingType.registerSimple("in_out_cubic", Ease::inOutCubic);
    public static final EasingType IN_OUT_ELASTIC = EasingType.registerSimple("in_out_elastic", Ease::inOutElastic);
    public static final EasingType IN_OUT_EXPO = EasingType.registerSimple("in_out_expo", Ease::inOutExpo);
    public static final EasingType IN_OUT_QUAD = EasingType.registerSimple("in_out_quad", Ease::inOutQuad);
    public static final EasingType IN_OUT_QUART = EasingType.registerSimple("in_out_quart", Ease::inOutQuart);
    public static final EasingType IN_OUT_QUINT = EasingType.registerSimple("in_out_quint", Ease::inOutQuint);
    public static final EasingType IN_OUT_SINE = EasingType.registerSimple("in_out_sine", Ease::inOutSine);
    public static final EasingType OUT_BACK = EasingType.registerSimple("out_back", Ease::outBack);
    public static final EasingType OUT_BOUNCE = EasingType.registerSimple("out_bounce", Ease::outBounce);
    public static final EasingType OUT_CIRC = EasingType.registerSimple("out_circ", Ease::outCirc);
    public static final EasingType OUT_CUBIC = EasingType.registerSimple("out_cubic", Ease::outCubic);
    public static final EasingType OUT_ELASTIC = EasingType.registerSimple("out_elastic", Ease::outElastic);
    public static final EasingType OUT_EXPO = EasingType.registerSimple("out_expo", Ease::outExpo);
    public static final EasingType OUT_QUAD = EasingType.registerSimple("out_quad", Ease::outQuad);
    public static final EasingType OUT_QUART = EasingType.registerSimple("out_quart", Ease::outQuart);
    public static final EasingType OUT_QUINT = EasingType.registerSimple("out_quint", Ease::outQuint);
    public static final EasingType OUT_SINE = EasingType.registerSimple("out_sine", Ease::outSine);

    public static EasingType registerSimple(String string, EasingType easingType) {
        SIMPLE_REGISTRY.put(string, easingType);
        return easingType;
    }

    public static EasingType cubicBezier(float f, float g, float h, float i) {
        return new CubicBezier(new CubicBezierControls(f, g, h, i));
    }

    public static EasingType symmetricCubicBezier(float f, float g) {
        return EasingType.cubicBezier(f, g, 1.0f - f, 1.0f - g);
    }

    public float apply(float var1);

    public static final class CubicBezier
    implements EasingType {
        public static final Codec<CubicBezier> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CubicBezierControls.CODEC.fieldOf("cubic_bezier").forGetter(cubicBezier -> cubicBezier.controls)).apply((Applicative)instance, CubicBezier::new));
        private static final int NEWTON_RAPHSON_ITERATIONS = 4;
        private final CubicBezierControls controls;
        private final CubicCurve xCurve;
        private final CubicCurve yCurve;

        public CubicBezier(CubicBezierControls cubicBezierControls) {
            this.controls = cubicBezierControls;
            this.xCurve = CubicBezier.curveFromControls(cubicBezierControls.x1, cubicBezierControls.x2);
            this.yCurve = CubicBezier.curveFromControls(cubicBezierControls.y1, cubicBezierControls.y2);
        }

        private static CubicCurve curveFromControls(float f, float g) {
            return new CubicCurve(3.0f * f - 3.0f * g + 1.0f, -6.0f * f + 3.0f * g, 3.0f * f);
        }

        @Override
        public float apply(float f) {
            float h;
            float g = f;
            for (int i = 0; i < 4 && !((h = this.xCurve.sampleGradient(g)) < 1.0E-5f); ++i) {
                float j = this.xCurve.sample(g) - f;
                g -= j / h;
            }
            return this.yCurve.sample(g);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (!(object instanceof CubicBezier)) return false;
            CubicBezier cubicBezier = (CubicBezier)object;
            if (!this.controls.equals((Object)cubicBezier.controls)) return false;
            return true;
        }

        public int hashCode() {
            return this.controls.hashCode();
        }

        public String toString() {
            return "CubicBezier(" + this.controls.x1 + ", " + this.controls.y1 + ", " + this.controls.x2 + ", " + this.controls.y2 + ")";
        }

        record CubicCurve(float a, float b, float c) {
            public float sample(float f) {
                return ((this.a * f + this.b) * f + this.c) * f;
            }

            public float sampleGradient(float f) {
                return (3.0f * this.a * f + 2.0f * this.b) * f + this.c;
            }
        }
    }

    public static final class CubicBezierControls
    extends Record {
        final float x1;
        final float y1;
        final float x2;
        final float y2;
        public static final Codec<CubicBezierControls> CODEC = Codec.FLOAT.listOf(4, 4).xmap(list -> new CubicBezierControls(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue(), ((Float)list.get(2)).floatValue(), ((Float)list.get(3)).floatValue()), cubicBezierControls -> List.of((Object)Float.valueOf(cubicBezierControls.x1), (Object)Float.valueOf(cubicBezierControls.y1), (Object)Float.valueOf(cubicBezierControls.x2), (Object)Float.valueOf(cubicBezierControls.y2))).validate(CubicBezierControls::validate);

        public CubicBezierControls(float f, float g, float h, float i) {
            this.x1 = f;
            this.y1 = g;
            this.x2 = h;
            this.y2 = i;
        }

        private DataResult<CubicBezierControls> validate() {
            if (this.x1 < 0.0f || this.x1 > 1.0f) {
                return DataResult.error(() -> "x1 must be in range [0; 1]");
            }
            if (this.x2 < 0.0f || this.x2 > 1.0f) {
                return DataResult.error(() -> "x2 must be in range [0; 1]");
            }
            return DataResult.success((Object)((Object)this));
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CubicBezierControls.class, "x1;y1;x2;y2", "x1", "y1", "x2", "y2"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CubicBezierControls.class, "x1;y1;x2;y2", "x1", "y1", "x2", "y2"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CubicBezierControls.class, "x1;y1;x2;y2", "x1", "y1", "x2", "y2"}, this, object);
        }

        public float x1() {
            return this.x1;
        }

        public float y1() {
            return this.y1;
        }

        public float x2() {
            return this.x2;
        }

        public float y2() {
            return this.y2;
        }
    }
}

