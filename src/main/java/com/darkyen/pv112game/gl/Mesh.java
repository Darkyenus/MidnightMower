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
    public final VertexAttributes instancedAttributes;

    /** Locations of attributes + instancedAttributes */
    private final int[] attributeLocations;
    private int attributeLocationsShaderProgram = -1;

    //region Vertex data
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
    private boolean indexBufferDirty = false;
    private final int indexBufferUsage;
    //endregion

    //region Instancing
    private final boolean instanced;
    private final FloatBuffer instancedBuffer;
    /** Managed by bind() */
    private final ByteBuffer instancedByteBuffer;
    private int instancedBufferHandle;
    private boolean instancedBufferDirty = false;
    private final int instancedBufferUsage;
    //endregion

    public Mesh (VertexAttributes attributes, boolean staticVertices, int maxVertices, boolean staticIndices, int maxIndices) {
        this(attributes, null, staticVertices, maxVertices, staticIndices, maxIndices, true, 0);
    }

    public Mesh (VertexAttributes attributes, VertexAttributes instancedAttributes, boolean staticVertices, int maxVertices, boolean staticIndices, int maxIndices, boolean staticInstances, int maxInstances) {
        // Vertex init
        this.attributes = attributes;
        this.attributeLocations = new int[attributes.size() + (instancedAttributes == null ? 0 : instancedAttributes.size())];

        vertexByteBuffer = BufferUtils.newUnsafeByteBuffer(attributes.vertexSize * maxVertices);
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

        // Instancing init
        this.instancedAttributes = instancedAttributes;
        if (instancedAttributes == null) {
            instanced = false;
            instancedBuffer = null;
            instancedByteBuffer = null;
            instancedBufferHandle = -1;
            instancedBufferUsage = -1;
        } else {
            instanced = true;
            instancedByteBuffer = BufferUtils.newUnsafeByteBuffer(instancedAttributes.vertexSize * maxInstances);
            instancedBuffer = instancedByteBuffer.asFloatBuffer();
            instancedByteBuffer.flip();//TODO Flips necessary?
            instancedBuffer.flip();
            instancedBufferHandle = Gdx.gl20.glGenBuffer();
            instancedBufferUsage = staticInstances ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
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

    public void setInstanceData (float[] data, int offset, int count) {
        assert instanced;
        instancedBufferDirty = true;
        BufferUtils.copy(data, instancedByteBuffer, count, offset << 2);
        instancedBuffer.position(0);
        instancedBuffer.limit(count);
    }

    public void setInstanceDataCount(int floatCount) {
        assert instanced;
        instancedBufferDirty = true;

        instancedBuffer.position(0);
        instancedBuffer.limit(floatCount);
    }

    public void putInstanceData (int targetOffset, float[] data, int offset, int count) {
        assert instanced;
        instancedBufferDirty = true;

        instancedByteBuffer.position(targetOffset * 4);
        BufferUtils.copy(data, offset, instancedByteBuffer, count);
        instancedByteBuffer.position(0);
    }

    public void bind (Shader shader) {
        final GL30 gl = Gdx.gl30;

        gl.glBindVertexArray(vaoHandle);

        // VAO Check
        if (attributeLocationsShaderProgram != shader.getProgram()) {
            // Disable attributes if bound
            if (attributeLocationsShaderProgram != -1) {
                for (int location : attributeLocations) {
                    if (location == -1) continue;
                    gl.glDisableVertexAttribArray(location);
                }
            }

            attributeLocationsShaderProgram = shader.getProgram();
            // Enable and setup attributes
            int attribLocationIndex = 0;
            // Standard attributes
            gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexBufferHandle);
            for (VertexAttribute attribute : attributes) {
                final int location = gl.glGetAttribLocation(shader.getProgram(), attribute.alias);
                attributeLocations[attribLocationIndex++] = location;
                if (location == -1) {
                    continue;
                }

                gl.glEnableVertexAttribArray(location);
                gl.glVertexAttribPointer(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize, attribute.offset);
            }

            // Instanced attributes
            if (instanced) {
                gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, instancedBufferHandle);
                for (VertexAttribute instancedAttribute : instancedAttributes) {
                    final int location = gl.glGetAttribLocation(shader.getProgram(), instancedAttribute.alias);
                    attributeLocations[attribLocationIndex++] = location;
                    if (location == -1) {
                        continue;
                    }

                    gl.glEnableVertexAttribArray(location);
                    gl.glVertexAttribPointer(location, instancedAttribute.numComponents, instancedAttribute.type, instancedAttribute.normalized, instancedAttributes.vertexSize, instancedAttribute.offset);
                    gl.glVertexAttribDivisor(location, 1);
                }
            }

            gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        }

        // Vertices check
        if (vertexBufferDirty) {
            vertexByteBuffer.limit(vertexBuffer.limit() * 4);
            gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexBufferHandle);
            gl.glBufferData(GL20.GL_ARRAY_BUFFER, vertexByteBuffer.limit(), vertexByteBuffer, vertexBufferUsage);
            gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
            vertexBufferDirty = false;
        }

        // Instanced check
        if (instancedBufferDirty) {
            instancedByteBuffer.limit(instancedBuffer.limit() * 4);
            gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, instancedBufferHandle);
            gl.glBufferData(GL20.GL_ARRAY_BUFFER, instancedByteBuffer.limit(), instancedByteBuffer, instancedBufferUsage);
            gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
            instancedBufferDirty = false;
        }

        // Indices check & bind
        if (indexBufferHandle != -1) {
            gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBufferHandle);

            if (indexBufferDirty) {
                indexByteBuffer.limit(indexBuffer.limit() * 2);
                gl.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, indexByteBuffer.limit(), indexByteBuffer, indexBufferUsage);
                indexBufferDirty = false;
            }
        }
    }

    public void unbind () {
        Gdx.gl30.glBindVertexArray(0);
    }

    public void render (int primitiveType, int offset, int count) {
        assert !instanced;
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

    public void renderInstanced (int primitiveType, int offset, int count, int instances) {
        assert instanced;
        assert offset >= 0;
        assert count > 0;
        assert indexBuffer == null || offset < getIndexCount();
        assert indexBuffer == null || offset + count <= getIndexCount();
        assert !vertexBufferDirty;
        assert !indexBufferDirty;
        if (indexBufferHandle == -1) {
            Gdx.gl30.glDrawArraysInstanced(primitiveType, offset, count, instances);
        } else {
            Gdx.gl30.glDrawElementsInstanced(primitiveType, count, GL20.GL_UNSIGNED_SHORT, offset * 2, instances);
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
