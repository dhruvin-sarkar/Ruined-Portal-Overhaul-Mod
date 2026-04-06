/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix3x2fc
 *  org.joml.Vector2f
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ScreenRectangle(ScreenPosition position, int width, int height) {
    private static final ScreenRectangle EMPTY = new ScreenRectangle(0, 0, 0, 0);

    public ScreenRectangle(int i, int j, int k, int l) {
        this(new ScreenPosition(i, j), k, l);
    }

    public static ScreenRectangle empty() {
        return EMPTY;
    }

    public static ScreenRectangle of(ScreenAxis screenAxis, int i, int j, int k, int l) {
        return switch (screenAxis) {
            default -> throw new MatchException(null, null);
            case ScreenAxis.HORIZONTAL -> new ScreenRectangle(i, j, k, l);
            case ScreenAxis.VERTICAL -> new ScreenRectangle(j, i, l, k);
        };
    }

    public ScreenRectangle step(ScreenDirection screenDirection) {
        return new ScreenRectangle(this.position.step(screenDirection), this.width, this.height);
    }

    public int getLength(ScreenAxis screenAxis) {
        return switch (screenAxis) {
            default -> throw new MatchException(null, null);
            case ScreenAxis.HORIZONTAL -> this.width;
            case ScreenAxis.VERTICAL -> this.height;
        };
    }

    public int getBoundInDirection(ScreenDirection screenDirection) {
        ScreenAxis screenAxis = screenDirection.getAxis();
        if (screenDirection.isPositive()) {
            return this.position.getCoordinate(screenAxis) + this.getLength(screenAxis) - 1;
        }
        return this.position.getCoordinate(screenAxis);
    }

    public ScreenRectangle getBorder(ScreenDirection screenDirection) {
        int i = this.getBoundInDirection(screenDirection);
        ScreenAxis screenAxis = screenDirection.getAxis().orthogonal();
        int j = this.getBoundInDirection(screenAxis.getNegative());
        int k = this.getLength(screenAxis);
        return ScreenRectangle.of(screenDirection.getAxis(), i, j, 1, k).step(screenDirection);
    }

    public boolean overlaps(ScreenRectangle screenRectangle) {
        return this.overlapsInAxis(screenRectangle, ScreenAxis.HORIZONTAL) && this.overlapsInAxis(screenRectangle, ScreenAxis.VERTICAL);
    }

    public boolean overlapsInAxis(ScreenRectangle screenRectangle, ScreenAxis screenAxis) {
        int i = this.getBoundInDirection(screenAxis.getNegative());
        int j = screenRectangle.getBoundInDirection(screenAxis.getNegative());
        int k = this.getBoundInDirection(screenAxis.getPositive());
        int l = screenRectangle.getBoundInDirection(screenAxis.getPositive());
        return Math.max(i, j) <= Math.min(k, l);
    }

    public int getCenterInAxis(ScreenAxis screenAxis) {
        return (this.getBoundInDirection(screenAxis.getPositive()) + this.getBoundInDirection(screenAxis.getNegative())) / 2;
    }

    public @Nullable ScreenRectangle intersection(ScreenRectangle screenRectangle) {
        int i = Math.max(this.left(), screenRectangle.left());
        int j = Math.max(this.top(), screenRectangle.top());
        int k = Math.min(this.right(), screenRectangle.right());
        int l = Math.min(this.bottom(), screenRectangle.bottom());
        if (i >= k || j >= l) {
            return null;
        }
        return new ScreenRectangle(i, j, k - i, l - j);
    }

    public boolean intersects(ScreenRectangle screenRectangle) {
        return this.left() < screenRectangle.right() && this.right() > screenRectangle.left() && this.top() < screenRectangle.bottom() && this.bottom() > screenRectangle.top();
    }

    public boolean encompasses(ScreenRectangle screenRectangle) {
        return screenRectangle.left() >= this.left() && screenRectangle.top() >= this.top() && screenRectangle.right() <= this.right() && screenRectangle.bottom() <= this.bottom();
    }

    public int top() {
        return this.position.y();
    }

    public int bottom() {
        return this.position.y() + this.height;
    }

    public int left() {
        return this.position.x();
    }

    public int right() {
        return this.position.x() + this.width;
    }

    public boolean containsPoint(int i, int j) {
        return i >= this.left() && i < this.right() && j >= this.top() && j < this.bottom();
    }

    public ScreenRectangle transformAxisAligned(Matrix3x2fc matrix3x2fc) {
        Vector2f vector2f = matrix3x2fc.transformPosition((float)this.left(), (float)this.top(), new Vector2f());
        Vector2f vector2f2 = matrix3x2fc.transformPosition((float)this.right(), (float)this.bottom(), new Vector2f());
        return new ScreenRectangle(Mth.floor(vector2f.x), Mth.floor(vector2f.y), Mth.floor(vector2f2.x - vector2f.x), Mth.floor(vector2f2.y - vector2f.y));
    }

    public ScreenRectangle transformMaxBounds(Matrix3x2fc matrix3x2fc) {
        Vector2f vector2f = matrix3x2fc.transformPosition((float)this.left(), (float)this.top(), new Vector2f());
        Vector2f vector2f2 = matrix3x2fc.transformPosition((float)this.right(), (float)this.top(), new Vector2f());
        Vector2f vector2f3 = matrix3x2fc.transformPosition((float)this.left(), (float)this.bottom(), new Vector2f());
        Vector2f vector2f4 = matrix3x2fc.transformPosition((float)this.right(), (float)this.bottom(), new Vector2f());
        float f = Math.min(Math.min(vector2f.x(), vector2f3.x()), Math.min(vector2f2.x(), vector2f4.x()));
        float g = Math.max(Math.max(vector2f.x(), vector2f3.x()), Math.max(vector2f2.x(), vector2f4.x()));
        float h = Math.min(Math.min(vector2f.y(), vector2f3.y()), Math.min(vector2f2.y(), vector2f4.y()));
        float i = Math.max(Math.max(vector2f.y(), vector2f3.y()), Math.max(vector2f2.y(), vector2f4.y()));
        return new ScreenRectangle(Mth.floor(f), Mth.floor(h), Mth.ceil(g - f), Mth.ceil(i - h));
    }
}

