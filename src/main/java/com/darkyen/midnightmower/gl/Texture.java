package com.darkyen.midnightmower.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.Disposable;

/**
 * Represents OpenGL texture
 */
@SuppressWarnings("WeakerAccess")
public final class Texture implements Disposable{

    public final int textureTarget;
    public int textureHandle = -1;

    public Pixmap.Format format;
    public int width, height;

    public Texture() {
        this(GL20.GL_TEXTURE_2D);
    }

    public Texture(int textureTarget) {
        this.textureTarget = textureTarget;
        textureHandle = Gdx.gl30.glGenTexture();
    }

    /** Binds the texture! */
    public void load(FileHandle file, Pixmap.Format format) {
        Pixmap pixmap = new Pixmap(file);

        if (pixmap.getFormat() != format) {
            Pixmap formatted = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), format);
            formatted.setBlending(Pixmap.Blending.None);
            formatted.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
            pixmap.dispose();
            pixmap = formatted;
        }

        final GL20 gl = Gdx.gl30;
        this.format = format;
        this.width = pixmap.getWidth();
        this.height = pixmap.getHeight();

        gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
        gl.glBindTexture(textureTarget, textureHandle);
        gl.glTexImage2D(textureTarget, 0, pixmap.getGLInternalFormat(),
                pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());

        setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);

        pixmap.dispose();
    }

    public void bind(int unit) {
        final GL20 gl = Gdx.gl30;
        gl.glActiveTexture(GL20.GL_TEXTURE0 + unit);
        gl.glBindTexture(textureTarget, textureHandle);
    }


    /** Texture must be bound! */
    public void setWrap (TextureWrap u, TextureWrap v) {
        final GL20 gl = Gdx.gl30;
        gl.glTexParameterf(textureTarget, GL20.GL_TEXTURE_WRAP_S, u.getGLEnum());
        gl.glTexParameterf(textureTarget, GL20.GL_TEXTURE_WRAP_T, v.getGLEnum());
    }

    /** Texture must be bound! */
    public void setFilter (TextureFilter minFilter, TextureFilter magFilter) {
        final GL20 gl = Gdx.gl30;
        gl.glTexParameterf(textureTarget, GL20.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
        gl.glTexParameterf(textureTarget, GL20.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
    }

    @Override
    public void dispose() {
        Gdx.gl30.glDeleteTexture(textureHandle);
    }
}
