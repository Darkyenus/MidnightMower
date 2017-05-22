package com.darkyen.midnightmower.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Shader based skybox
 */
public final class Skybox implements Disposable {

    private final Shader shader;
    private final Mesh screenMesh;
    private final Texture moon = new Texture();

    public Skybox() {
        this.shader = new Shader(Gdx.files.internal("shaders/skybox-vert.glsl"), Gdx.files.internal("shaders/skybox-frag.glsl"));
        screenMesh = new Mesh(new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position")), true, 4, true, 4);
        screenMesh.setVertices(new float[] {
                -1f, -1f,
                1f, -1f,
                1f, 1f,
                -1f, 1f
        }, 0, 2*4);
        screenMesh.setIndices(new short[]{
                0, 1, 2, 3
        }, 0, 4);

        moon.load(Gdx.files.internal("textures/dione.png"), Pixmap.Format.RGBA8888);
    }

    public void draw(Viewport viewport, Light moonLight) {
        final PerspectiveCamera camera = ((PerspectiveCamera) viewport.getCamera());
        final float fovFactor = (float)(1f / (Math.tan(Math.toRadians(camera.fieldOfView / 2f))));

        final GL20 gl = Gdx.gl30;
        gl.glDepthMask(false);

        gl.glDisable(GL20.GL_CULL_FACE);//TODO

        moon.bind(0);

        shader.bind();
        shader.uniform("fovFactor").set(fovFactor);
        shader.uniform("cameraDirection").set(viewport.getCamera().direction);
        shader.uniform("screenDimensions").set(viewport.getScreenWidth(), viewport.getScreenHeight());

        shader.uniform("moonPos").set(moonLight.position);
        shader.uniform("moon").set(0);

        screenMesh.bind(shader);
        screenMesh.render(GL20.GL_TRIANGLE_FAN, 0, 4);
        screenMesh.unbind();
        shader.unbind();

        gl.glDepthMask(true);
    }

    public void dispose() {
        shader.dispose();
        screenMesh.dispose();
    }
}
