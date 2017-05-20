package com.darkyen.pv112game.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

import static com.badlogic.gdx.graphics.GL20.GL_CULL_FACE;

/**
 * Particle effect made out of triangles
 */
public final class ParticleEffect <P extends ParticleEffect.Particle> {

    private final float SQRT3 = (float) Math.sqrt(3f);

    private final Shader shader;
    private final Mesh mesh;
    private final float[] vertices;
    private boolean verticesDirty = true;

    private P[] particles;
    private int particleCount = 0;
    private final int maxParticles;

    private final ParticleController<P> controller;

    public ParticleEffect(int maxParticles, ParticleController<P> controller) {
        this.shader = new Shader(Gdx.files.internal("shaders/particle-vert.glsl"), Gdx.files.internal("shaders/particle-frag.glsl"));
        this.mesh = new Mesh(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorPacked()), false, 3 * maxParticles, true,0);
        this.vertices = new float[mesh.attributes.vertexSize/4 * 3 * maxParticles];
        this.controller = controller;

        //noinspection unchecked
        this.particles = (P[]) new Particle[maxParticles];
        for (int i = 0; i < maxParticles; i++) {
            this.particles[i] = controller.newParticle();
        }
        this.maxParticles = maxParticles;
    }

    public void update(float delta) {
        verticesDirty |= particleCount > 0;
        for (int i = 0; i < particleCount; ) {
            if (controller.update(particles[i], delta)) {
                i++;
            } else {
                particleCount--;
                P tmp = particles[i];
                particles[i] = particles[particleCount];
                particles[particleCount] = tmp;
            }
        }
    }

    public void spawn(int count) {
        verticesDirty |= count > 0;
        while (count != 0 && particleCount < maxParticles) {
            final P particle = particles[particleCount++];

            particle.position.setZero();
            particle.scale.set(1f, 1f, 1f);
            particle.rotation.idt();
            particle.color.set(1f, 1f, 1f, 1f);

            controller.spawn(particle);
            count--;
        }
    }

    public int getParticleCount() {
        return particleCount;
    }

    private final Matrix4 draw_transform = new Matrix4();
    private final Vector3 draw_point = new Vector3();

    public void draw(Camera camera) {
        if (particleCount == 0) return;

        if (verticesDirty) {
            verticesDirty = false;

            final float[] vertices = this.vertices;
            final Matrix4 transform = this.draw_transform;
            final Vector3 point = this.draw_point;

            int j = 0;
            for (int i = 0; i < particleCount; i++) {
                final Particle particle = particles[i];
                transform.idt();
                transform.translate(particle.position);
                transform.scl(particle.scale);
                transform.rotate(particle.rotation);
                final float color = particle.color.toFloatBits();

                // Equilateral triangle in XZ plane
                // Center is at 0.5, SQRT3/3*2
                final float centerX = 0.5f;
                final float centerZ = (SQRT3/6f);

                point.set(0f - centerX, 0f, 0f - centerZ);
                point.mul(transform);
                vertices[j++] = point.x;
                vertices[j++] = point.y;
                vertices[j++] = point.z;
                vertices[j++] = color;

                point.set(1f - centerX, 0f, 0f - centerZ);
                point.mul(transform);
                vertices[j++] = point.x;
                vertices[j++] = point.y;
                vertices[j++] = point.z;
                vertices[j++] = color;

                point.set(0.5f - centerX, 0f, SQRT3/2f - centerZ);
                point.mul(transform);
                vertices[j++] = point.x;
                vertices[j++] = point.y;
                vertices[j++] = point.z;
                vertices[j++] = color;
            }

            mesh.setVertices(vertices, 0, vertices.length);
        }

        final GL30 gl = Gdx.gl30;
        gl.glDisable(GL_CULL_FACE);
        shader.bind();
        shader.uniform("projectionMat").set(camera.combined);
        mesh.bind(shader);
        mesh.render(GL20.GL_TRIANGLES, 0, particleCount * 3);
        mesh.unbind();
        shader.unbind();
        gl.glEnable(GL_CULL_FACE);
    }


    public static class Particle {
        public final Vector3 position = new Vector3();
        public final Vector3 scale = new Vector3();
        public final Quaternion rotation = new Quaternion();
        public final Color color = new Color();
    }

    public interface ParticleController<P extends Particle> {

        P newParticle();

        void spawn(P particle);

        /** @return false if the particle is dead */
        boolean update(P particle, float delta);
    }
}
