package com.darkyen.midnightmower.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 *
 */
public final class UniformBuffer {

    private int handle;

    public UniformBuffer() {
        final GL30 gl = Gdx.gl30;
        handle = gl.glGenBuffer();
    }

    public ByteBuffer prepareData(Shader.Uniform uniform, String[] names, int[] offsets, ByteBuffer oldBuffer) {
        assert names.length == offsets.length;

        final GL30 gl = Gdx.gl30;
        final int program = uniform.getProgram();

        final IntBuffer ints = BufferUtils.newIntBuffer(1);
        gl.glGetActiveUniformBlockiv(program, uniform.getLocation(), GL30.GL_UNIFORM_BLOCK_DATA_SIZE, ints);
        final int size = ints.get(0);

        final IntBuffer indices = BufferUtils.newIntBuffer(names.length);
        gl.glGetUniformIndices(program, names, indices);

        final IntBuffer offsetsBuffer = BufferUtils.newIntBuffer(names.length);
        gl.glGetActiveUniformsiv(program, names.length, indices, GL30.GL_UNIFORM_OFFSET, offsetsBuffer);

        for (int i = 0; i < names.length; i++) {
            offsets[i] = offsetsBuffer.get(i);
        }

        final ByteBuffer dataBuffer;
        if (oldBuffer == null || oldBuffer.capacity() != size) {
            dataBuffer = BufferUtils.newByteBuffer(size);
        } else {
            dataBuffer = oldBuffer;
        }

        dataBuffer.position(0);
        dataBuffer.limit(dataBuffer.capacity());
        return dataBuffer;
    }

    public void uploadData(ByteBuffer dataBuffer, boolean isStatic) {
        assert dataBuffer != null;

        final GL30 gl = Gdx.gl30;
        gl.glBindBuffer(GL30.GL_UNIFORM_BUFFER, handle);
        gl.glBufferData(GL30.GL_UNIFORM_BUFFER, dataBuffer.capacity(), dataBuffer, isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW);
    }

    public boolean bind(Shader.Uniform uniform, int unit) {
        final GL30 gl = Gdx.gl30;
        gl.glBindBuffer(GL30.GL_UNIFORM_BUFFER, handle);
        gl.glBindBufferBase(GL30.GL_UNIFORM_BUFFER, unit, handle);
        final int location = uniform.getLocation();
        if (location < 0) return false;
        Gdx.gl30.glUniformBlockBinding(uniform.getProgram(), unit, location);
        return true;
    }
}
