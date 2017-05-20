package com.darkyen.pv112game.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Represents a set of vertices and indices.
 *
 * Based on architecture and functionality of {@link com.badlogic.gdx.graphics.Mesh}, because it is a darn good abstraction.
 */
public final class Mesh implements Disposable {

    public final VertexAttributes attributes;

    //region Vertex data
    private final int[] vertexAttributeLocations;
    private int vertexAttributeLocationsShaderProgram = -1;

    private final FloatBuffer vertexBuffer;
    /** Managed by bind() */
    private final ByteBuffer vertexByteBuffer;
    private int vertexBufferHandle;
    private int vaoHandle;
    private final int vertexBufferUsage;
    private boolean vertexBufferDirty = false;
    //endregion

    //region Index data
    private final ShortBuffer indexBuffer;
    /** Managed by bind() */
    private final ByteBuffer indexByteBuffer;
    private int indexBufferHandle;
    private boolean indexBufferDirty = true;
    private final int indexBufferUsage;
    //endregion

    public Mesh (VertexAttributes attributes, boolean staticVertices, int maxVertices, boolean staticIndices, int maxIndices) {
        // Vertex init
        this.attributes = attributes;
        this.vertexAttributeLocations = new int[attributes.size()];

        vertexByteBuffer = BufferUtils.newUnsafeByteBuffer(this.attributes.vertexSize * maxVertices);
        vertexBuffer = vertexByteBuffer.asFloatBuffer();
        vertexBuffer.flip();
        vertexByteBuffer.flip();
        vertexBufferHandle = Gdx.gl20.glGenBuffer();
        vertexBufferUsage = staticVertices ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;

        final IntBuffer vaoHandlePtr = BufferUtils.newIntBuffer(1);
        Gdx.gl30.glGenVertexArrays(1, vaoHandlePtr);
        vaoHandle = vaoHandlePtr.get(0);

        // Index init
        if (maxIndices == 0) {
            indexBuffer = null;
            indexByteBuffer = null;
            indexBufferHandle = -1;
            indexBufferDirty = false;
            indexBufferUsage = -1;
        } else {
            indexByteBuffer = BufferUtils.newUnsafeByteBuffer(maxIndices * 2);

            indexBuffer = indexByteBuffer.asShortBuffer();
            indexBuffer.flip();
            indexByteBuffer.flip();
            indexBufferHandle = Gdx.gl20.glGenBuffer();
            indexBufferUsage = staticIndices ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
        }
    }

    public int getVertexCount() {
        return vertexBuffer.limit() * 4 / attributes.vertexSize;
    }

    public int getMaxVertexCount() {
        return vertexByteBuffer.capacity() / attributes.vertexSize;
    }

    public int getIndexCount() {
        return indexBuffer.limit();
    }

    public int getMaxIndexCount() {
        return indexBuffer.capacity();
    }

    public void setVertices (float[] vertices, int offset, int count) {
        vertexBufferDirty = true;
        BufferUtils.copy(vertices, vertexByteBuffer, count, offset);
        vertexBuffer.position(0);
        vertexBuffer.limit(count);
    }

    public void setVertexCount(int count) {
        vertexBufferDirty = true;

        vertexBuffer.position(0);
        vertexBuffer.limit(count);
    }

    public void putVertices (int targetOffset, float[] vertices, int offset, int count) {
        vertexBufferDirty = true;

        vertexByteBuffer.position(targetOffset * 4);
        BufferUtils.copy(vertices, offset, vertexByteBuffer, count);
        vertexByteBuffer.position(0);
    }

    public void setIndices (short[] indices, int offset, int count) {
        indexBufferDirty = true;

        indexBuffer.clear();
        indexBuffer.put(indices, offset, count);
        indexBuffer.flip();
        indexByteBuffer.position(0);
    }

    public void setIndexCount(int count) {
        indexBufferDirty = true;

        indexBuffer.position(0);
        indexBuffer.limit(count);
    }

    public void putIndices (int targetOffset, short[] indices, int offset, int count) {
        indexBufferDirty = true;

        indexByteBuffer.position(targetOffset * 2);
        BufferUtils.copy(indices, offset, indexByteBuffer, count);
        indexByteBuffer.position(0);
    }

    public void bind (Shader shader) {
        final GL30 gl = Gdx.gl30;

        gl.glBindVertexArray(vaoHandle);
        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexBufferHandle);
        if (indexBufferHandle != -1) gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBufferHandle);

        // VAO Check
        if (vertexAttributeLocationsShaderProgram != shader.getProgram()) {
            // Disable attributes if bound
            if (vertexAttributeLocationsShaderProgram != -1) {
                for (int location : vertexAttributeLocations) {
                    if (location == -1) continue;
                    gl.glDisableVertexAttribArray(location);
                }
            }

            vertexAttributeLocationsShaderProgram = shader.getProgram();
            // Enable and setup attributes
            for (int i = 0; i < vertexAttributeLocations.length; i++) {
                final VertexAttribute attribute = attributes.get(i);
                final int location = gl.glGetAttribLocation(shader.getProgram(), attribute.alias);
                vertexAttributeLocations[i] = location;
                if (location == -1) {
                    continue;
                }

                gl.glEnableVertexAttribArray(location);
                gl.glVertexAttribPointer(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize, attribute.offset);
            }
        }

        // Vertices check
        if (vertexBufferDirty) {
            vertexByteBuffer.limit(vertexBuffer.limit() * 4);
            gl.glBufferData(GL20.GL_ARRAY_BUFFER, vertexByteBuffer.limit(), vertexByteBuffer, vertexBufferUsage);
            vertexBufferDirty = false;
        }

        // Indices check
        if (indexBufferDirty) {
            indexByteBuffer.limit(indexBuffer.limit() * 2);
            gl.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, indexByteBuffer.limit(), indexByteBuffer, indexBufferUsage);
            indexBufferDirty = false;
        }
    }

    public void unbind () {
        Gdx.gl30.glBindVertexArray(0);
    }

    public void render (int primitiveType, int offset, int count) {
        assert offset >= 0;
        assert count > 0;
        assert indexBuffer == null || offset < getIndexCount();
        assert indexBuffer == null || offset + count <= getIndexCount();
        assert !vertexBufferDirty;
        assert !indexBufferDirty;
        if (indexBufferHandle != -1) {
            Gdx.gl30.glDrawElements(primitiveType, count, GL20.GL_UNSIGNED_SHORT, offset * 2);
        } else {
            Gdx.gl30.glDrawArrays(primitiveType, offset, count);
        }
    }

    public void dispose () {
        GL30 gl = Gdx.gl30;

        // Dispose vertices
        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        gl.glDeleteBuffer(vertexBufferHandle);
        vertexBufferHandle = 0;
        BufferUtils.disposeUnsafeByteBuffer(vertexByteBuffer);
        final IntBuffer vaoHandlePtr = BufferUtils.newIntBuffer(1);
        vaoHandlePtr.put(vaoHandle);
        vaoHandlePtr.flip();
        gl.glDeleteVertexArrays(1, vaoHandlePtr);
        vaoHandle = -1;

        // Dispose indices
        gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        gl.glDeleteBuffer(indexBufferHandle);
        indexBufferHandle = 0;

        BufferUtils.disposeUnsafeByteBuffer(indexByteBuffer);
    }

}
