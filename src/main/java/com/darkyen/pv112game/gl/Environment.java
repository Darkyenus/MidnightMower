package com.darkyen.pv112game.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

import java.nio.ByteBuffer;

/**
 *
 */
public final class Environment implements Disposable {

    private final Camera camera;

    private final Shader shader;
    private final Shader.Uniform environmentBlock;
    private final Shader shaderInstanced;
    private final Shader.Uniform environmentBlockInstanced;

    private final UniformBuffer environmentUniforms;

    private final Color ambientLight = new Color(0.9f, 0.9f, 0.9f, 1f);
    private final Array<Light> pointLights = new Array<>();
    private boolean dirty = true;

    private final ObjectMap<Model, Array<Matrix4>> batchedModels = new ObjectMap<>();

    public Environment(Camera camera) {
        this.camera = camera;
        environmentUniforms = new UniformBuffer();

        this.shader = new Shader(Gdx.files.internal("shaders/world-vert.glsl"), Gdx.files.internal("shaders/world-frag.glsl"));
        environmentBlock = shader.uniformBlock("Environment");

        this.shaderInstanced = new Shader(Gdx.files.internal("shaders/world-vert-inst.glsl"), Gdx.files.internal("shaders/world-frag.glsl"));
        environmentBlockInstanced = shaderInstanced.uniformBlock("Environment");
    }

    public void setAmbientLight(Color color) {
        this.ambientLight.set(color);
        dirty = true;
    }

    /** @return ambient light of the scene, assumes that it is not modified. */
    public Color getAmbientLight() {
        return this.ambientLight;
    }

    public Array<Light> getPointLights() {
        dirty = true;
        return pointLights;
    }

    private static final String[] environmentUniformNames = {"pointLightCount",  "ambientLight",
            "pointLightPosition[0]", "pointLightColor[0]", "pointLightAttenuation[0]", "pointLightDirection[0]"};
    private final int[] environmentUniformOffsets = new int[environmentUniformNames.length];

    private void bindShader(Shader shader, Shader.Uniform environmentBlock, boolean dirty) {
        shader.bind();
        shader.uniform("eye_position").set(camera.position);
        shader.uniform("projectionMat").set(camera.combined);

        if (dirty) {
            final int[] offsets = this.environmentUniformOffsets;
            final ByteBuffer data = environmentUniforms.prepareData(environmentBlock, environmentUniformNames, offsets, null);

            final int pointLightCount = Math.min(pointLights.size, 8);
            data.putInt(offsets[0], pointLightCount);
            data.putFloat(offsets[1], ambientLight.r);
            data.putFloat(offsets[1]+4, ambientLight.g);
            data.putFloat(offsets[1]+8, ambientLight.b);

            final int stride = 4*4;
            for (int i = 0; i < pointLightCount; i++) {
                final Light light = pointLights.get(i);
                data.putFloat(offsets[2] + i*stride, light.position.x);
                data.putFloat(offsets[2] + i*stride+4, light.position.y);
                data.putFloat(offsets[2] + i*stride+8, light.position.z);

                data.putFloat(offsets[3] + i*stride, light.color.r);
                data.putFloat(offsets[3] + i*stride+4, light.color.g);
                data.putFloat(offsets[3] + i*stride+8, light.color.b);

                data.putFloat(offsets[4] + i*stride, light.attenuation.x);
                data.putFloat(offsets[4] + i*stride+4, light.attenuation.y);
                data.putFloat(offsets[4] + i*stride+8, light.attenuation.z);

                data.putFloat(offsets[5] + i*stride, light.direction.x);
                data.putFloat(offsets[5] + i*stride+4, light.direction.y);
                data.putFloat(offsets[5] + i*stride+8, light.direction.z);
                data.putFloat(offsets[5] + i*stride+12, light.directionCutoff);
            }

            environmentUniforms.uploadData(data, true);
        }

        environmentUniforms.bind(environmentBlock, 0);
    }

    public void begin() {
        bindShader(shader, environmentBlock, dirty);
        dirty = false;
    }

    public void draw(Model model, Vector3 position) {
        final Matrix4 transform = MATRIX_4_POOL.obtain().setToTranslation(position);
        draw(model, transform);
    }

    public void draw(Model model, Vector3 position, float yDegrees) {
        final Matrix4 transform = MATRIX_4_POOL.obtain().idt();
        transform.translate(position);
        transform.rotate(Vector3.Y, yDegrees);
        draw(model, transform);
    }

    public void draw(Model model, Vector3 position, float yDegrees, float scale) {
        final Matrix4 transform = MATRIX_4_POOL.obtain().idt();
        transform.translate(position);
        transform.rotate(Vector3.Y, yDegrees);
        transform.scale(scale, scale, scale);
        draw(model, transform);
    }

    private void draw(Model model, Matrix4 pooledTransform) {
        if (model.instanced) {
            Array<Matrix4> models = batchedModels.get(model);
            if (models == null) {
                models = ARRAY_MATRIX_4_POOL.obtain();
                batchedModels.put(model, models);
            }
            models.add(pooledTransform);
        } else {
            model.draw(shader, pooledTransform);
            MATRIX_4_POOL.free(pooledTransform);
        }
    }

    public void end() {
        shader.unbind();

        if (batchedModels.size != 0) {
            bindShader(shaderInstanced, environmentBlockInstanced, false);

            for (ObjectMap.Entry<Model, Array<Matrix4>> entry : batchedModels.entries()) {
                entry.key.draw(shaderInstanced, entry.value);
                MATRIX_4_POOL.freeAll(entry.value);
                entry.value.size = 0;
                ARRAY_MATRIX_4_POOL.free(entry.value);
            }
            batchedModels.clear();
            shaderInstanced.unbind();
        }
    }

    public void dispose() {
        end();
        shader.dispose();
    }

    private static final Pool<Matrix4> MATRIX_4_POOL = new Pool<Matrix4>() {
        @Override
        protected Matrix4 newObject() {
            return new Matrix4();
        }
    };
    private static final Pool<Array<Matrix4>> ARRAY_MATRIX_4_POOL = new Pool<Array<Matrix4>>() {
        @Override
        protected Array<Matrix4> newObject() {
            return new Array<>(false, 64, Matrix4.class);
        }
    };
}
