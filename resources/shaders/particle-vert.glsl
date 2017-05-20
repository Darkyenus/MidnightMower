#version 330

in vec3 a_position;
in vec4 a_color;

out vec4 v_color;

uniform mat4 projectionMat; // view-projection matrix

void main() {
	v_color = a_color;
    gl_Position = projectionMat * vec4(a_position, 1.0);
}
