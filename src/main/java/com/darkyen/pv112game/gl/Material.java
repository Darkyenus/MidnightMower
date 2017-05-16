package com.darkyen.pv112game.gl;

import com.badlogic.gdx.graphics.Color;

import java.nio.ByteBuffer;

/**
 *
 */
public class Material {
    public final String id;

    private final Color ambient;
    private final Color diffuse;
    private final Color specular;
    private float shininess;
    private final Texture texture;//TODO

    private final UniformBuffer uniformBuffer = new UniformBuffer();

    private boolean dirty = true;

    public Material(String id, Color ambient, Color diffuse, Color specular, float shininess, Texture texture) {
        this.id = id;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
        this.texture = texture;
    }

    public void setAmbient(Color ambient) {
        this.ambient.set(ambient);
        this.dirty = true;
    }

    public void setDiffuse(Color diffuse) {
        this.diffuse.set(diffuse);
        this.dirty = true;
    }

    public void setSpecular(Color specular) {
        this.specular.set(specular);
        this.dirty = true;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
        this.dirty = true;
    }

    private static final String[] uniformBlockNames = {"material_ambientColor", "material_diffuseColor", "material_specularColor", "material_shininess"};
    private static final int[] offsets = new int[uniformBlockNames.length];

    public void bind(Shader.Uniform uniform, int unit) {
        if (dirty) {
            dirty = false;

            final int[] offsets = Material.offsets;
            final ByteBuffer data = uniformBuffer.prepareData(uniform, uniformBlockNames, offsets, null);
            data.putFloat(offsets[0], ambient.r);
            data.putFloat(offsets[0]+4, ambient.g);
            data.putFloat(offsets[0]+8, ambient.b);

            data.putFloat(offsets[1], diffuse.r);
            data.putFloat(offsets[1]+4, diffuse.g);
            data.putFloat(offsets[1]+8, diffuse.b);

            data.putFloat(offsets[2], specular.r);
            data.putFloat(offsets[2]+4, specular.g);
            data.putFloat(offsets[2]+8, specular.b);

            data.putFloat(offsets[3], shininess);
            uniformBuffer.uploadData(data, true);
        }
        uniformBuffer.bind(uniform, unit);
    }
}
