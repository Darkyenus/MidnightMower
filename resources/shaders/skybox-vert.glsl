#version 330

in vec2 a_position;
out vec2 v_position;

void main() {
    v_position = a_position;
    gl_Position = vec4(a_position, 0.99998, 1.0);
}
