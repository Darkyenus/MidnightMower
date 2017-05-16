package com.darkyen.pv112game.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 *
 */
public final class Environment {

    private final Camera camera;
    private final Shader shader;

    public final Color ambientLight = new Color(0.9f, 0.9f, 0.9f, 1f);
    public final Array<PointLight> pointLights = new Array<>();

    public Environment(Camera camera, Shader shader) {
        this.camera = camera;
        this.shader = shader;
    }

    public void begin() {
        final GL30 gl = Gdx.gl30;
        shader.bind();
        shader.uniform("eye_position").set(camera.position);
        shader.uniform("projectionMat").set(camera.combined);

        final int pointLightCount = Math.min(pointLights.size, 8);
        shader.uniform("pointLightCount").set(pointLightCount);
        final int pointLightLocation = shader.uniform("pointLights[0].position").getLocation();
        if (pointLightLocation >= 0) {
            final int pointLightSize = 3;
            for (int i = 0; i < pointLightCount; i++) {
                final PointLight pointLight = pointLights.get(i);
                gl.glUniform3f(pointLightLocation + i*pointLightSize, pointLight.position.x, pointLight.position.y, pointLight.position.z);
                gl.glUniform3f(pointLightLocation + i*pointLightSize+1, pointLight.color.r, pointLight.color.g, pointLight.color.b);
                gl.glUniform3f(pointLightLocation + i*pointLightSize+2, pointLight.attenuation.x, pointLight.attenuation.y, pointLight.attenuation.z);
            }
        }
    }

    private final Matrix4 draw_transform = new Matrix4();

    public void draw(Model model, Vector3 position) {
        final Matrix4 transform = draw_transform.setToTranslation(position);
        model.draw(shader, transform, ambientLight);
    }

    public void end() {
        shader.unbind();
    }

    public static final class PointLight {
        public final Vector3 position = new Vector3();
        public final Color color = new Color(1f, 1f, 1f, 1f);
        public final Vector3 attenuation = new Vector3(0.1f, 0.3f, 0.02f);
    }
}
