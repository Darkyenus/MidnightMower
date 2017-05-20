package com.darkyen.pv112game.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;

/**
 *
 */
public final class Environment implements Disposable {

    private final Camera camera;
    private final Shader shader;
    private final UniformBuffer environmentUniforms;
    private final Shader.Uniform environmentBlock;

    private final Color ambientLight = new Color(0.9f, 0.9f, 0.9f, 1f);
    private final Array<PointLight> pointLights = new Array<>();
    private boolean dirty = true;

    public Environment(Camera camera) {
        this.camera = camera;
        this.shader = new Shader(Gdx.files.internal("shaders/world-vert.glsl"), Gdx.files.internal("shaders/world-frag.glsl"));

        environmentUniforms = new UniformBuffer();
        environmentBlock = shader.uniformBlock("Environment");
    }

    public void setAmbientLight(Color color) {
        this.ambientLight.set(color);
        dirty = true;
    }

    public Array<PointLight> getPointLights() {
        dirty = true;
        return pointLights;
    }

    private static final String[] environmentUniformNames = {"pointLightCount",  "ambientLight",
            "pointLightPosition[0]", "pointLightColor[0]", "pointLightAttenuation[0]", "pointLightDirection[0]"};
    private final int[] environmentUniformOffsets = new int[environmentUniformNames.length];

    public void begin() {
        shader.bind();
        shader.uniform("eye_position").set(camera.position);
        shader.uniform("projectionMat").set(camera.combined);

        if (dirty) {
            dirty = false;
            final int[] offsets = this.environmentUniformOffsets;
            final ByteBuffer data = environmentUniforms.prepareData(environmentBlock, environmentUniformNames, offsets, null);

            final int pointLightCount = Math.min(pointLights.size, 8);
            data.putInt(offsets[0], pointLightCount);
            data.putFloat(offsets[1], ambientLight.r);
            data.putFloat(offsets[1]+4, ambientLight.g);
            data.putFloat(offsets[1]+8, ambientLight.b);

            final int stride = 4*4;
            for (int i = 0; i < pointLightCount; i++) {
                final PointLight pointLight = pointLights.get(i);
                data.putFloat(offsets[2] + i*stride, pointLight.position.x);
                data.putFloat(offsets[2] + i*stride+4, pointLight.position.y);
                data.putFloat(offsets[2] + i*stride+8, pointLight.position.z);

                data.putFloat(offsets[3] + i*stride, pointLight.color.r);
                data.putFloat(offsets[3] + i*stride+4, pointLight.color.g);
                data.putFloat(offsets[3] + i*stride+8, pointLight.color.b);

                data.putFloat(offsets[4] + i*stride, pointLight.attenuation.x);
                data.putFloat(offsets[4] + i*stride+4, pointLight.attenuation.y);
                data.putFloat(offsets[4] + i*stride+8, pointLight.attenuation.z);

                data.putFloat(offsets[5] + i*stride, pointLight.direction.x);
                data.putFloat(offsets[5] + i*stride+4, pointLight.direction.y);
                data.putFloat(offsets[5] + i*stride+8, pointLight.direction.z);
                data.putFloat(offsets[5] + i*stride+12, pointLight.directionCutoff);
            }

            environmentUniforms.uploadData(data, true);
        }

        environmentUniforms.bind(environmentBlock, 0);
    }

    private final Matrix4 draw_transform = new Matrix4();

    public void draw(Model model, Vector3 position) {
        final Matrix4 transform = draw_transform.setToTranslation(position);
        model.draw(shader, transform);
    }

    public void draw(Model model, Vector3 position, float yDegrees) {
        final Matrix4 transform = draw_transform.idt();
        transform.translate(position);
        transform.rotate(Vector3.Y, yDegrees);
        model.draw(shader, transform);
    }

    public void draw(Model model, Vector3 position, float yDegrees, float scale) {
        final Matrix4 transform = draw_transform.idt();
        transform.translate(position);
        transform.rotate(Vector3.Y, yDegrees);
        transform.scale(scale, scale, scale);
        model.draw(shader, transform);
    }

    public void end() {
        shader.unbind();
    }

    public void dispose() {
        end();
        shader.dispose();
    }

    public static final class PointLight {
        public final Color color = new Color(1f, 1f, 1f, 1f);
        public final Vector3 position = new Vector3();
        public final Vector3 attenuation = new Vector3(0.1f, 0.3f, 0.02f);
        public final Vector3 direction = new Vector3(0f, 0f, 0f);
        public float directionCutoff = -5f;
    }
}
