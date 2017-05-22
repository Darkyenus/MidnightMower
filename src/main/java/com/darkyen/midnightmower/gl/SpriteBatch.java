package com.darkyen.midnightmower.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

/**
 *
 */
public final class SpriteBatch implements Disposable {

    public static final float WHITE = Color.WHITE.toFloatBits();

    private Mesh mesh;
    private Shader shader;

    private int spriteCount = 0;
    private final int maxSpriteCount;

    private final float[] draw_dataCache;
    private int lastBoundTexture = -1;

    public SpriteBatch() {
        this(new Shader(Gdx.files.internal("shaders/batch2d-vert.glsl"),
                Gdx.files.internal("shaders/batch2d-frag.glsl")), 2048);
    }

    public SpriteBatch(Shader shader, int maxSprites) {
        this.maxSpriteCount = maxSprites;
        this.shader = shader;
        this.mesh = new Mesh(new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                VertexAttribute.ColorPacked(),
                VertexAttribute.TexCoords(0)
        ), false, maxSprites * 4, true, maxSprites * 6);
        final short[] indices = new short[maxSprites * 6];
        {
            short vertex = 0;
            for (int base = 0; base < indices.length; base += 6) {
                indices[base] = vertex;
                indices[base+1] = (short) (vertex + 1);
                indices[base+2] = (short) (vertex + 2);

                indices[base+3] = (short) (vertex + 1);
                indices[base+4] = (short) (vertex + 3);
                indices[base+5] = (short) (vertex + 2);

                vertex += 4;
            }
        }
        this.mesh.setIndices(indices, 0, indices.length);
        draw_dataCache = new float[(mesh.attributes.vertexSize / 4) * 4];
    }

    public void begin(Camera camera, boolean blending) {
        if (blending) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        shader.bind();
        shader.uniform("projectionMat").set(camera.combined);
        shader.uniform("texture0").set(0);
    }

    public void useTexture(Texture texture) {
        if (lastBoundTexture != texture.textureHandle) {
            flush();
            texture.bind(0);
            lastBoundTexture = texture.textureHandle;
        }
    }

    public void flush() {
        if (spriteCount != 0) {
            mesh.bind(shader);
            mesh.render(GL20.GL_TRIANGLES, 0, spriteCount * 6);
            spriteCount = 0;
        }
    }

    public void end() {
        flush();
        mesh.unbind();
        shader.unbind();
        lastBoundTexture = -1;
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void draw(float x, float y, float width, float height, float u, float v, float u2, float v2) {
        draw(x, y, width, height, u, v, u2, v2, WHITE, WHITE, WHITE, WHITE);
    }

    public void draw(float x, float y, float width, float height, float u, float v, float u2, float v2, float color) {
        draw(x, y, width, height, u, v, u2, v2, color, color, color, color);
    }

    public void draw(float x, float y, float width, float height, float u, float v, float u2, float v2,
                     float colorTL, float colorTR, float colorBL, float colorBR) {
        final float[] data = this.draw_dataCache;
        int i = -1;
        data[++i] = x;
        data[++i] = y;
        data[++i] = colorTL;
        data[++i] = u;
        data[++i] = v;

        data[++i] = x + width;
        data[++i] = y;
        data[++i] = colorTR;
        data[++i] = u2;
        data[++i] = v;

        data[++i] = x;
        data[++i] = y + height;
        data[++i] = colorBL;
        data[++i] = u;
        data[++i] = v2;

        data[++i] = x + width;
        data[++i] = y + height;
        data[++i] = colorBR;
        data[++i] = u2;
        data[++i] = v2;

        draw(data, 1);
    }

    public void draw(float[] data, int sprites) {
        if (spriteCount + sprites > maxSpriteCount) {
            flush();
            assert spriteCount + sprites <= maxSpriteCount;
        }

        final int totalSpriteCount = spriteCount + sprites;
        final int vertexFloats = mesh.attributes.vertexSize / 4;
        mesh.setVertexCount(totalSpriteCount * vertexFloats * 4);
        mesh.putVertices(spriteCount * vertexFloats * 4, data, 0, sprites * vertexFloats * 4);
        spriteCount = totalSpriteCount;
    }

    @Override
    public void dispose() {
        this.mesh.dispose();
        this.shader.dispose();
    }
}
