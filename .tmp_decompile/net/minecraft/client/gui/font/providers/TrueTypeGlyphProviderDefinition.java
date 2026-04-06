/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.freetype.FT_Face
 *  org.lwjgl.util.freetype.FreeType
 */
package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

@Environment(value=EnvType.CLIENT)
public record TrueTypeGlyphProviderDefinition(Identifier location, float size, float oversample, Shift shift, String skip) implements GlyphProviderDefinition
{
    private static final Codec<String> SKIP_LIST_CODEC = Codec.withAlternative((Codec)Codec.STRING, (Codec)Codec.STRING.listOf(), list -> String.join((CharSequence)"", list));
    public static final MapCodec<TrueTypeGlyphProviderDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("file").forGetter(TrueTypeGlyphProviderDefinition::location), (App)Codec.FLOAT.optionalFieldOf("size", (Object)Float.valueOf(11.0f)).forGetter(TrueTypeGlyphProviderDefinition::size), (App)Codec.FLOAT.optionalFieldOf("oversample", (Object)Float.valueOf(1.0f)).forGetter(TrueTypeGlyphProviderDefinition::oversample), (App)Shift.CODEC.optionalFieldOf("shift", (Object)Shift.NONE).forGetter(TrueTypeGlyphProviderDefinition::shift), (App)SKIP_LIST_CODEC.optionalFieldOf("skip", (Object)"").forGetter(TrueTypeGlyphProviderDefinition::skip)).apply((Applicative)instance, TrueTypeGlyphProviderDefinition::new));

    @Override
    public GlyphProviderType type() {
        return GlyphProviderType.TTF;
    }

    @Override
    public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
        return Either.left(this::load);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private GlyphProvider load(ResourceManager resourceManager) throws IOException {
        FT_Face fT_Face = null;
        ByteBuffer byteBuffer = null;
        try (InputStream inputStream = resourceManager.open(this.location.withPrefix("font/"));){
            byteBuffer = TextureUtil.readResource(inputStream);
            Object object = FreeTypeUtil.LIBRARY_LOCK;
            synchronized (object) {
                try (MemoryStack memoryStack = MemoryStack.stackPush();){
                    PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                    FreeTypeUtil.assertError(FreeType.FT_New_Memory_Face((long)FreeTypeUtil.getLibrary(), (ByteBuffer)byteBuffer, (long)0L, (PointerBuffer)pointerBuffer), "Initializing font face");
                    fT_Face = FT_Face.create((long)pointerBuffer.get());
                }
                String string = FreeType.FT_Get_Font_Format((FT_Face)fT_Face);
                if (!"TrueType".equals(string)) {
                    throw new IOException("Font is not in TTF format, was " + string);
                }
                FreeTypeUtil.assertError(FreeType.FT_Select_Charmap((FT_Face)fT_Face, (int)FreeType.FT_ENCODING_UNICODE), "Find unicode charmap");
                TrueTypeGlyphProvider trueTypeGlyphProvider = new TrueTypeGlyphProvider(byteBuffer, fT_Face, this.size, this.oversample, this.shift.x, this.shift.y, this.skip);
                return trueTypeGlyphProvider;
            }
        }
        catch (Exception exception) {
            Object object = FreeTypeUtil.LIBRARY_LOCK;
            synchronized (object) {
                if (fT_Face != null) {
                    FreeType.FT_Done_Face(fT_Face);
                }
            }
            MemoryUtil.memFree((Buffer)byteBuffer);
            throw exception;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Shift
    extends Record {
        final float x;
        final float y;
        public static final Shift NONE = new Shift(0.0f, 0.0f);
        public static final Codec<Shift> CODEC = Codec.floatRange((float)-512.0f, (float)512.0f).listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 2).map(list -> new Shift(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue())), shift -> List.of((Object)Float.valueOf(shift.x), (Object)Float.valueOf(shift.y)));

        public Shift(float f, float g) {
            this.x = f;
            this.y = g;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Shift.class, "x;y", "x", "y"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Shift.class, "x;y", "x", "y"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Shift.class, "x;y", "x", "y"}, this, object);
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }
    }
}

