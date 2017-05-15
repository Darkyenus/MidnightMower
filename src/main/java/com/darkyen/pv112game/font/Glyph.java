package com.darkyen.pv112game.font;

/**
 *
 */
@SuppressWarnings("WeakerAccess")
public final class Glyph {
    public final int codePoint;
    public final int pageIndex;
    public final int pageX, pageY, pageWidth, pageHeight;
    public final float u, v, u2, v2;

    public final int xOffset, yOffset;
    public final int xAdvance, leftSideBearing;

    public Glyph(int codePoint,
                 int pageIndex, int pageX, int pageY, int pageWidth, int pageHeight,
                 float u, float v, float u2, float v2,
                 int xOffset, int yOffset, int xAdvance, int leftSideBearing) {
        this.codePoint = codePoint;
        this.pageIndex = pageIndex;
        this.pageX = pageX;
        this.pageY = pageY;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.u = u;
        this.v = v;
        this.u2 = u2;
        this.v2 = v2;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xAdvance = xAdvance;
        this.leftSideBearing = leftSideBearing;
    }

    public String toString() {
        return new String(Character.toChars(codePoint));
    }
}
