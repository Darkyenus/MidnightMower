package com.darkyen.midnightmower.font;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.darkyen.midnightmower.gl.Texture;

import java.io.DataInputStream;

/**
 * Represents data for simple bitmap Font.
 */
@SuppressWarnings("WeakerAccess")
public final class Font implements Disposable {

    /** Textures of font's pages. */
    public final Texture[] pages;
    /** Typographic line height */
    public final int lineHeight;
    public final int lineGap;
    /** Distance from top to baseline  */
    public final int ascent;
    /** The distance from baseline to bottom */
    public final int descent;
    /** Loaded glyphs */
    public final IntMap<Glyph> glyphs = new IntMap<>();
    /** The glyph to display for characters not in the font. May be null. */
    public final Glyph missingGlyph;

    /**
     * Loads font from file with special binary format for fast serialization of fonts. (Supported by ResourcePacker)
     *
     * Format specification:
     *
     * <code>
     * unsigned byte pages;
     * [pages]{
     *     UTF pagePath; //Relative to fontFile
     * };
     * short lineGap; //Gap between two lines
     * short ascent; //Offset from baseline down to lower part of glyph (negative)
     * short descent; //From baseline down
     *
     * int amountOfGlyphs;
     * [amountOfGlyphs] {
     *     int codePoint;
     *     unsigned byte page;
     *     unsigned short pageX, pageY, pageWidth, pageHeight;
     *     short offsetX, offsetY;
     *     short xAdvance, leftSideBearing;
     * };
     *
     * // pageX/Y/Width/Height are pixel coordinates on the bitmap page
     * // offsetX/offsetY are offsets in pixel space from the glyph origin to the top-left of the bitmap
     * // leftSideBearing is the offset from the current horizontal position to the left edge of the character
     * // advanceWidth is the offset from the current horizontal position to the next horizontal position
     * </code>
     */
    public Font(final FileHandle fontFile) {
        try(final DataInputStream in = new DataInputStream(fontFile.read(512))){
            Glyph missingGlyph = null;
            final int pageCount = in.readUnsignedByte();
            this.pages = new Texture[pageCount];
            for (int i = 0; i < pageCount; i++) {
                pages[i] = new Texture();
                pages[i].load(fontFile.sibling(in.readUTF()), Pixmap.Format.RGBA8888);
            }
            this.lineGap = in.readShort();
            this.ascent = in.readShort();
            this.descent = -Math.abs(in.readShort());//Just to make sure it is negative
            this.lineHeight = ascent - descent + lineGap;

            final int amountOfGlyphs = in.readInt();
            for (int i = 0; i < amountOfGlyphs; i++) {
                final int codePoint = in.readInt();
                final int pageIndex = in.readUnsignedByte();

                final int pX = in.readUnsignedShort();
                final int pY = in.readUnsignedShort();
                final int pWidth = in.readUnsignedShort();
                final int pHeight = in.readUnsignedShort();

                final int xOffset = in.readShort();
                final int yOffset = in.readShort();
                final int xAdvance = in.readShort();
                final int leftSideBearing = in.readShort();

                final Texture page = pages[pageIndex];

                final Glyph glyph = new Glyph(codePoint,
                        pageIndex, pX, pY, pWidth, pHeight,
                        pX / (float) page.width, pY / (float) page.height,
                        (pX + pWidth) / (float) page.width, (pY + pHeight) / (float) page.height,
                        xOffset, yOffset, xAdvance, leftSideBearing);

                glyphs.put(codePoint, glyph);

                if (codePoint == 0){
                    missingGlyph = glyph;
                }
            }
            this.missingGlyph = missingGlyph;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load font "+fontFile, e);
        }
    }

    public boolean isWhitespace(int codepoint) {
        if (codepoint <= 0) return false;
        final Glyph glyph = glyphs.get(codepoint, missingGlyph);
        if (glyph == null) {
            return Character.isWhitespace(codepoint);
        } else {
            return glyph.pageWidth == 0 || glyph.pageHeight == 0;
        }
    }

    @Override
    public void dispose() {
        for (int i = 0; i < pages.length; i++) {
            pages[i].dispose();
            pages[i] = null;
        }
    }
}
