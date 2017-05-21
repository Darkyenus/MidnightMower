package com.darkyen.pv112game.gl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 *
 */
public final class Model {

    private static final Logger LOG = LoggerFactory.getLogger(Model.class);

    private static final VertexAttributes INSTANCED_ATTRIBUTES = new VertexAttributes(
            new VertexAttribute(VertexAttributes.Usage.Position, 4, GL20.GL_FLOAT, false, "modelMat1"),
            new VertexAttribute(VertexAttributes.Usage.Position, 4, GL20.GL_FLOAT, false, "modelMat2"),
            new VertexAttribute(VertexAttributes.Usage.Position, 4, GL20.GL_FLOAT, false, "modelMat3"),
            new VertexAttribute(VertexAttributes.Usage.Position, 4, GL20.GL_FLOAT, false, "modelMat4"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, GL20.GL_FLOAT, false, "normalMat1"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, GL20.GL_FLOAT, false, "normalMat2"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, GL20.GL_FLOAT, false, "normalMat3")
    );

    public final boolean instanced;
    private final int maxInstances;
    private final Mesh[] meshes;
    private final String[] meshIds;
    private final MeshPart[][] meshParts;

    private final Array<Node> nodes = new Array<>();
    private final Array<NodePart> nodeParts = new Array<>();

    //region DrawCache
    private final Matrix4 draw_transform = new Matrix4();
    private final Matrix3 draw_normal = new Matrix3();
    private final float[] drawInstanced_data;
    //endregion

    private static String emptyIfNull(String s) {
        return s == null ? "" : s;
    }

    private MeshPart getMeshPart(String meshId, String meshPartId) {
        for (int meshI = 0; meshI < meshes.length; meshI++) {
            if (emptyIfNull(meshIds[meshI]).equals(emptyIfNull(meshId))) {
                // Mesh found

                for (int partI = 0; partI < meshParts[meshI].length; partI++) {
                    if (emptyIfNull(meshParts[meshI][partI].meshPartId).equals(emptyIfNull(meshPartId))) {
                        return meshParts[meshI][partI];
                    }
                }

                throw new IllegalArgumentException("No mesh part with id '"+meshPartId+"' in mesh '"+meshId+"'");
            }
        }
        throw new IllegalArgumentException("No mesh with id '"+meshId+"'");
    }

    private static Material getMaterial(Array<Material> materials, String materialId) {
        for (Material material : materials) {
            if (materialId.equals(material.id)) {
                return material;
            }
        }
        throw new IllegalArgumentException("No material with id "+materialId);
    }

    private void addNode(Array<Material> materials, ModelNode modelNode, Node parent) {
        final Matrix4 transform = new Matrix4();
        if(modelNode.scale != null) transform.scale(modelNode.scale.x, modelNode.scale.y, modelNode.scale.z);
        if(modelNode.rotation != null) transform.rotate(modelNode.rotation);
        if(modelNode.translation != null) transform.translate(modelNode.translation);

        final Node node = new Node(modelNode.id, parent, transform);

        if (modelNode.parts != null) {
            for (int modelNodeI = 0; modelNodeI < modelNode.parts.length; modelNodeI++) {
                final ModelNodePart modelNodePart = modelNode.parts[modelNodeI];

                final MeshPart meshPart = getMeshPart(modelNode.meshId, modelNodePart.meshPartId);
                final Material material = getMaterial(materials, modelNodePart.materialId);

                if (modelNodePart.bones != null && modelNodePart.bones.size != 0) {
                    LOG.warn("Bones are not supported");
                }

                nodeParts.add(new NodePart(meshPart, node, material));
            }
        }

        if (modelNode.children != null) {
            for (ModelNode childNode : modelNode.children) {
                addNode(materials, childNode, parent);
            }
        }
    }

    public Model(ModelData modelData) {
        this(modelData, 1, false);
    }

    /**
     * @param maxInstances if >1, meshes will be drawn instanced */
    public Model(ModelData modelData, int maxInstances, boolean staticInstances) {
        this.instanced = maxInstances > 1;
        this.maxInstances = maxInstances > 1 ? maxInstances : 1;
        this.drawInstanced_data = instanced ? new float[(INSTANCED_ATTRIBUTES.vertexSize / 4) * maxInstances] : null;
        this.meshes = new Mesh[modelData.meshes.size];
        this.meshIds = new String[modelData.meshes.size];
        this.meshParts = new MeshPart[modelData.meshes.size][];

        // Prepare materials
        final Array<Material> materials = new Array<>(modelData.materials.size);
        for (int i = 0; i < modelData.materials.size; i++) {
            final ModelMaterial modelMaterial = modelData.materials.get(i);
            final Color ambient = modelMaterial.ambient == null ? new Color(modelMaterial.diffuse).mul(0.5f) : modelMaterial.ambient;
            final Color diffuse = modelMaterial.diffuse;
            final Color specular = modelMaterial.specular;
            final float shininess = modelMaterial.specular == null ? 0f : modelMaterial.shininess;

            materials.add(new Material(modelMaterial.id, ambient, diffuse, specular, shininess, null));
        }

        final VertexAttributes instancedAttributes = maxInstances > 1 ? INSTANCED_ATTRIBUTES : null;

        // Prepare meshes
        for (int meshI = 0; meshI < modelData.meshes.size; meshI++) {
            final ModelMesh modelMesh = modelData.meshes.get(meshI);

            if (modelMesh.parts == null || modelMesh.parts.length == 0) {
                throw new IllegalArgumentException("Mesh must have at leas one part");
            }

            // Prepare parts and indices
            final MeshPart[] meshParts = new MeshPart[modelMesh.parts.length];
            int indicesCount = 0;
            for (int meshPartI = 0; meshPartI < meshParts.length; meshPartI++) {
                final ModelMeshPart modelMeshPart = modelMesh.parts[meshPartI];
                meshParts[meshPartI] = new MeshPart(modelMeshPart.id, meshI, indicesCount, modelMeshPart.indices.length, modelMeshPart.primitiveType);
                indicesCount += modelMeshPart.indices.length;
            }

            // Prepare mesh
            final VertexAttributes vertexAttributes = new VertexAttributes(modelMesh.attributes);
            final Mesh mesh = new Mesh(vertexAttributes, instancedAttributes,
                    true, modelMesh.vertices.length / (vertexAttributes.vertexSize / 4),
                    true, indicesCount, staticInstances, maxInstances);
            // Fill vertices
            mesh.setVertices(modelMesh.vertices, 0, modelMesh.vertices.length);
            mesh.setIndexCount(indicesCount);
            // Fill indices
            for (int meshPartI = 0; meshPartI < meshParts.length; meshPartI++) {
                final ModelMeshPart modelMeshPart = modelMesh.parts[meshPartI];
                final MeshPart meshPart = meshParts[meshPartI];

                mesh.putIndices(meshPart.indicesOffset, modelMeshPart.indices, 0, meshPart.indicesCount);
            }

            this.meshes[meshI] = mesh;
            this.meshIds[meshI] = modelMesh.id;
            this.meshParts[meshI] = meshParts;
        }

        // Prepare nodes
        for (ModelNode node : modelData.nodes) {
            addNode(materials, node, null);
        }

        this.nodes.sort();
        this.nodeParts.sort();
    }

    public void draw(Shader shader, Matrix4 transform) {
        assert !instanced;

        Mesh lastBoundMesh = null;
        Node lastBoundNode = null;

        for (NodePart part : nodeParts) {
            final Mesh mesh = meshes[part.meshPart.meshIndex];
            if (mesh != lastBoundMesh) {
                if (lastBoundMesh != null) {
                    lastBoundMesh.unbind();
                }
                lastBoundMesh = mesh;
                mesh.bind(shader);
            }

            if (part.node != lastBoundNode) {
                lastBoundNode = part.node;

                final Matrix4 modelMat = draw_transform.set(transform).mul(part.node.transform);
                shader.uniform("modelMat").set(modelMat);
                final Matrix3 normalMat = draw_normal.set(modelMat).inv().transpose();
                shader.uniform("normalMat").set(normalMat);
            }

            final Shader.Uniform materialBlock = shader.uniformBlock("Material");
            final Material material = part.material;
            material.bind(materialBlock, 1);

            final MeshPart meshPart = part.meshPart;
            mesh.render(meshPart.primitive, meshPart.indicesOffset, meshPart.indicesCount);
        }

        if (lastBoundMesh != null) {
            lastBoundMesh.unbind();
        }
    }

    public void draw(Shader shader, Array<Matrix4> models) {
        if (instanced) {
            int offset = 0;
            while (offset != models.size) {
                int count = Math.min(models.size - offset, maxInstances);
                drawInstanced(shader, models, offset, count);
                offset += count;
            }
        } else {
            for (Matrix4 model : models) {
                draw(shader, model);
            }
        }
    }

    public void drawInstanced(Shader shader, Array<Matrix4> models, int modelOffset, int modelCount) {
        assert instanced;

        final float[] instancedData = this.drawInstanced_data;

        // We need to set fresh material for each node part, but
        Mesh lastBoundMesh = null;
        Node lastBoundNode = null;

        for (NodePart part : nodeParts) {
            final Mesh mesh = meshes[part.meshPart.meshIndex];

            if (part.node != lastBoundNode) {
                if (lastBoundMesh != null) lastBoundMesh.unbind();
                lastBoundMesh = mesh;
                lastBoundNode = part.node;

                // All nodes have equal transform, and we set it here, for all instances
                int instancedDataI = 0;
                for (int i = 0; i < modelCount; i++) {
                    final Matrix4 modelMatRaw = models.get(modelOffset + i);
                    final Matrix4 modelMat = draw_transform.set(modelMatRaw).mul(part.node.transform);
                    final Matrix3 normalMat = draw_normal.set(modelMat).inv().transpose();

                    System.arraycopy(modelMat.val, 0, instancedData, instancedDataI, 16);
                    instancedDataI += 16;
                    System.arraycopy(normalMat.val, 0, instancedData, instancedDataI, 9);
                    instancedDataI += 9;
                }

                mesh.setInstanceData(instancedData, 0, instancedDataI);
                mesh.bind(shader);
            }

            // All node parts have different material
            final Shader.Uniform materialBlock = shader.uniformBlock("Material");
            final Material material = part.material;
            material.bind(materialBlock, 1);

            final MeshPart meshPart = part.meshPart;
            mesh.renderInstanced(meshPart.primitive, meshPart.indicesOffset, meshPart.indicesCount, modelCount);
        }

        if (lastBoundMesh != null) {
            lastBoundMesh.unbind();
        }

    }

    private static class MeshPart implements Comparable<MeshPart> {
        private final String meshPartId;
        private final int meshIndex;
        private final int indicesOffset, indicesCount;
        private final int primitive;

        private MeshPart(String meshPartId, int meshIndex, int indicesOffset, int indicesCount, int primitive) {
            this.meshPartId = meshPartId;
            this.meshIndex = meshIndex;
            this.indicesOffset = indicesOffset;
            this.indicesCount = indicesCount;
            this.primitive = primitive;
        }

        @Override
        public int compareTo(MeshPart o) {
            if (this.meshIndex == o.meshIndex) {
                return this.indicesOffset - o.indicesOffset;
            }
            return this.meshIndex - o.meshIndex;
        }
    }

    private static class Node implements Comparable<Node> {
        private final String nodeId;
        private final Node parent;
        private final Matrix4 transform;

        private Node(String nodeId, Node parent, Matrix4 transform) {
            this.nodeId = nodeId;
            this.parent = parent;
            this.transform = transform;
        }

        private int parents() {
            if (parent == null) {
                return 0;
            } else {
                return parent.parents() + 1;
            }
        }

        @Override
        public int compareTo(Node o) {
            return this.parents() - o.parents();
        }
    }

    private static class NodePart implements Comparable<NodePart> {
        private final MeshPart meshPart;
        private final Node node;
        private final Material material;

        private NodePart(MeshPart meshPart, Node node, Material material) {
            this.meshPart = meshPart;
            this.node = node;
            this.material = material;
        }

        @Override
        public int compareTo(NodePart o) {
            final int meshComparison = meshPart.compareTo(o.meshPart);
            if (meshComparison != 0) return meshComparison;
            return emptyIfNull(material.id).compareTo(emptyIfNull(o.material.id));
        }
    }
}
