package com.darkyen.midnightmower.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.IntBuffer;

import static com.badlogic.gdx.graphics.GL20.*;

/**
 * Represents a shader program
 */
public final class Shader {

    private static final Logger LOG = LoggerFactory.getLogger(Shader.class);

    private int program = -1;

    private final FileHandle vertexShaderFile;
    private int vertexShader = -1;

    private final FileHandle fragmentShaderFile;
    private int fragmentShader = -1;

    private final ObjectMap<String, Uniform> uniforms = new ObjectMap<>();

    private volatile boolean keepReloading = false;

    public Shader(FileHandle vertexShaderFile, FileHandle fragmentShaderFile) {
        this.vertexShaderFile = vertexShaderFile;
        this.fragmentShaderFile = fragmentShaderFile;

        compile();

        if ("true".equalsIgnoreCase(System.getenv("SHADERS_AUTORELOAD"))) {
            keepReloading = true;

            final Thread reloader = new Thread(() -> {
                try {
                    final File vertexFile = vertexShaderFile.file().getCanonicalFile();
                    final File fragmentFile = fragmentShaderFile.file().getCanonicalFile();

                    long vertexModified = vertexFile.lastModified();
                    long fragmentModified = fragmentFile.lastModified();

                    while (Shader.this.keepReloading) {
                        long vMod = vertexFile.lastModified();
                        long fMod = fragmentFile.lastModified();

                        if (vMod != vertexModified || fragmentModified != fMod) {
                            vertexModified = vMod;
                            fragmentModified = fMod;
                            LOG.info("Reloading shader {}", program);
                            Gdx.app.postRunnable(Shader.this::compile);
                        }

                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    LOG.error("Reload thread failed", e);
                }
            }, "Shader Reloader for " + vertexShaderFile.name() + " - " + fragmentShaderFile.name());
            reloader.setDaemon(true);
            reloader.start();
            LOG.info("Started {}", reloader.getName());
        }
    }

    public final int getProgram() {
        return program;
    }

    public final Uniform uniform(String name) {
        Uniform uniform = uniforms.get(name);
        if (uniform == null) {
            uniform = new Uniform(name, false);
            uniforms.put(name, uniform);
        } else assert !uniform.block;
        return uniform;
    }

    public final Uniform uniformBlock(String name) {
        Uniform uniform = uniforms.get(name);
        if (uniform == null) {
            uniform = new Uniform(name, true);
            uniforms.put(name, uniform);
        } else assert uniform.block;
        return uniform;
    }

    /** Compile this shader program. Call while not bound!
     * Can be called repeatedly for shader hotswapping. */
    public final void compile() {
        GL30 gl = Gdx.gl30;
        final int vertexShader = createShader(vertexShaderFile, GL_VERTEX_SHADER);
        final int fragmentShader = createShader(fragmentShaderFile, GL_FRAGMENT_SHADER);

        final int program = gl.glCreateProgram();
        gl.glAttachShader(program, vertexShader);
        gl.glAttachShader(program, fragmentShader);
        gl.glLinkProgram(program);


        final IntBuffer status = BufferUtils.newIntBuffer(1);
        gl.glGetProgramiv(program, GL20.GL_LINK_STATUS, status);

        if (status.get(0) == GL_FALSE) {
            final String log = gl.glGetProgramInfoLog(program);
            LOG.error("Failed to compile shader {}-{}:\n{}", vertexShaderFile, fragmentShaderFile, log);
            gl.glDeleteShader(vertexShader);
            gl.glDeleteShader(fragmentShader);
            gl.glDeleteProgram(program);
            return;
        }

        if (this.program != -1) {
            gl.glDetachShader(this.program, this.vertexShader);
            gl.glDetachShader(this.program, this.fragmentShader);
            gl.glDeleteShader(this.vertexShader);
            gl.glDeleteShader(this.fragmentShader);
            gl.glDeleteProgram(this.program);

            for (Uniform uniform : uniforms.values()) {
                uniform.location = LOCATION_UNKNOWN;
            }
        }

        this.program = program;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    private static int createShader(FileHandle shaderFile, int type) {
        GL30 gl = Gdx.gl30;
        final String source = shaderFile.readString();

        final int shader = gl.glCreateShader(type);
        gl.glShaderSource(shader, source);
        gl.glCompileShader(shader);

        final IntBuffer status = BufferUtils.newIntBuffer(1);
        gl.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, status);

        if (status.get(0) == GL_FALSE) {
            final String log = gl.glGetShaderInfoLog(shader);
            LOG.error("Failed to compile shader {}:\n{}", shaderFile.name(), log);
            gl.glDeleteShader(shader);
            return -1;
        }

        return shader;
    }

    public void bind() {
        Gdx.gl30.glUseProgram(program);
    }

    public void unbind() {
        Gdx.gl30.glUseProgram(0);
    }


    public void dispose () {
        keepReloading = false;

        GL30 gl = Gdx.gl30;
        gl.glUseProgram(0);
        gl.glDeleteShader(vertexShader);
        gl.glDeleteShader(fragmentShader);
        gl.glDeleteProgram(program);
    }

    private static final int LOCATION_UNKNOWN = -2;

    public final class Uniform {
        private final String name;
        private final boolean block;
        private int location = LOCATION_UNKNOWN;

        private Uniform(String name, boolean block) {
            this.name = name;
            this.block = block;
        }

        public int getProgram() {
            return program;
        }

        public int getLocation() {
            if (location == LOCATION_UNKNOWN) {
                if (!block) {
                    location = Gdx.gl30.glGetUniformLocation(program, name);
                    if (location == -1) {
                        LOG.warn("Uniform {} not found", name);
                    }
                } else {
                    location = Gdx.gl30.glGetUniformBlockIndex(program, name);
                    if (location == -1) {
                        LOG.warn("Uniform block {} not found", name);
                    }
                }

            }
            return location;
        }

        /** Call while bound */
        public boolean set(int value) {
            assert !block;
            final int location = getLocation();
            if (location < 0) return false;
            Gdx.gl30.glUniform1i(location, value);
            return true;
        }

        /** Call while bound */
        public boolean set(float value) {
            assert !block;
            final int location = getLocation();
            if (location < 0) return false;
            Gdx.gl30.glUniform1f(location, value);
            return true;
        }

        /** Call while bound */
        public boolean set(float x, float y) {
            assert !block;
            final int location = getLocation();
            if (location < 0) return false;
            Gdx.gl30.glUniform2f(location, x, y);
            return true;
        }

        /** Call while bound */
        public boolean set(float x, float y, float z) {
            assert !block;
            final int location = getLocation();
            if (location < 0) return false;
            Gdx.gl30.glUniform3f(location, x, y, z);
            return true;
        }

        /** Call while bound */
        public boolean set(Vector3 value) {
            assert !block;
            final int location = getLocation();
            if (location < 0) return false;
            Gdx.gl30.glUniform3f(location, value.x, value.y, value.z);
            return true;
        }

        /** Call while bound */
        public boolean setRGB(Color value) {
            assert !block;
            final int location = getLocation();
            if (location < 0) return false;
            Gdx.gl30.glUniform3f(location, value.r, value.g, value.b);
            return true;
        }

        /** Call while bound */
        public boolean setRGBA(Color value) {
            assert !block;
            final int location = getLocation();
            if (location < 0) return false;
            Gdx.gl30.glUniform4f(location, value.r, value.g, value.b, value.a);
            return true;
        }

        /** Call while bound */
        public boolean set(Matrix3 value) {
            assert !block;
            final int location = getLocation();
            if (location < 0) return false;
            Gdx.gl30.glUniformMatrix3fv(location, 1, false, value.getValues(), 0);
            return true;
        }

        /** Call while bound */
        public boolean set(Matrix4 value) {
            assert !block;
            final int location = getLocation();
            if (location < 0) return false;
            Gdx.gl30.glUniformMatrix4fv(location, 1, false, value.getValues(), 0);
            return true;
        }
    }
}
