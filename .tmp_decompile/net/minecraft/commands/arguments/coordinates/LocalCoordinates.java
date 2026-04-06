/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record LocalCoordinates(double left, double up, double forwards) implements Coordinates
{
    public static final char PREFIX_LOCAL_COORDINATE = '^';

    @Override
    public Vec3 getPosition(CommandSourceStack commandSourceStack) {
        Vec3 vec3 = commandSourceStack.getAnchor().apply(commandSourceStack);
        return Vec3.applyLocalCoordinatesToRotation(commandSourceStack.getRotation(), new Vec3(this.left, this.up, this.forwards)).add(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public Vec2 getRotation(CommandSourceStack commandSourceStack) {
        return Vec2.ZERO;
    }

    @Override
    public boolean isXRelative() {
        return true;
    }

    @Override
    public boolean isYRelative() {
        return true;
    }

    @Override
    public boolean isZRelative() {
        return true;
    }

    public static LocalCoordinates parse(StringReader stringReader) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        double d = LocalCoordinates.readDouble(stringReader, i);
        if (!stringReader.canRead() || stringReader.peek() != ' ') {
            stringReader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)stringReader);
        }
        stringReader.skip();
        double e = LocalCoordinates.readDouble(stringReader, i);
        if (!stringReader.canRead() || stringReader.peek() != ' ') {
            stringReader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)stringReader);
        }
        stringReader.skip();
        double f = LocalCoordinates.readDouble(stringReader, i);
        return new LocalCoordinates(d, e, f);
    }

    private static double readDouble(StringReader stringReader, int i) throws CommandSyntaxException {
        if (!stringReader.canRead()) {
            throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext((ImmutableStringReader)stringReader);
        }
        if (stringReader.peek() != '^') {
            stringReader.setCursor(i);
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext((ImmutableStringReader)stringReader);
        }
        stringReader.skip();
        return stringReader.canRead() && stringReader.peek() != ' ' ? stringReader.readDouble() : 0.0;
    }
}

