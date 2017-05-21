#version 330

in vec3 a_position;
in vec3 a_normal;
in vec2 a_texCoord0;

out vec3 v_position;
out vec3 v_normal;
out vec2 v_texCoord0;

uniform mat4 projectionMat; // view-projection matrix
// Instance data
// model matrix
in vec4 modelMat1;
in vec4 modelMat2;
in vec4 modelMat3;
in vec4 modelMat4;
// transpose of inversed model matrix
in vec3 normalMat1;
in vec3 normalMat2;
in vec3 normalMat3;

void main() {
	mat4 modelMat = mat4(modelMat1, modelMat2, modelMat3, modelMat4);
	mat3 normalMat = mat3(normalMat1, normalMat2, normalMat3);

	vec4 worldPos = modelMat * vec4(a_position, 1.0);
    v_position = worldPos.xyz;
    v_normal = normalMat * a_normal;
    v_texCoord0 = a_texCoord0;

    gl_Position = projectionMat * worldPos;
}
