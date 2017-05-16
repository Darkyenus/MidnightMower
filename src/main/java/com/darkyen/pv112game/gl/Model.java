package com.darkyen.pv112game.gl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class Model {

    private static final Logger LOG = LoggerFactory.getLogger(Model.class);

    private final Mesh[] meshes;
    private final String[] meshIds;
    private final MeshPart[][] meshParts;

    private final Array<Node> nodes = new Array<>();
    private final Array<NodePart> nodeParts = new Array<>();

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
        final Matrix4 transform = new Matrix4();//TODO Order of operations?
        if(modelNode.translation != null) transform.translate(modelNode.translation);
        if(modelNode.scale != null) transform.scale(modelNode.scale.x, modelNode.scale.y, modelNode.scale.z);
        if(modelNode.rotation != null) transform.rotate(modelNode.rotation);

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
            final Mesh mesh = new Mesh(vertexAttributes,
                    true, modelMesh.vertices.length / (vertexAttributes.vertexSize / 4),
                    true, indicesCount);
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

    private final Matrix4 draw_transform = new Matrix4();
    private final Matrix3 draw_normal = new Matrix3();

    public void draw(Shader shader, Matrix4 transform) {
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

                final Matrix4 modelMat = draw_transform.set(part.node.transform).mul(transform);
                shader.uniform("modelMat").set(modelMat);//TODO Order of multiplication?
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
