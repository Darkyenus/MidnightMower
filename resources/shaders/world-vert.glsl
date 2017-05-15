#version 330

in vec3 a_position;
in vec3 a_normal;
in vec2 a_texCoord0;

out vec3 v_position;
out vec3 v_normal;
out vec2 v_texCoord0;

uniform mat4 projectionMat; // view-projection matrix
uniform mat3 normalMat; // transpose of inversed model matrix
uniform mat4 modelMat; // model matrix

void main() {
    v_position = vec3(modelMat * vec4(a_position, 1.0));
    v_normal = normalize(normalMat * a_normal);
    v_texCoord0 = a_texCoord0;

    gl_Position = projectionMat * modelMat * vec4(a_position, 1.0);
}
