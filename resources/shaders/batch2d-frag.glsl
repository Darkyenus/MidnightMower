#version 330

in vec4 v_color;
in vec2 v_texCoord0;

out vec4 o_fragColor;

uniform sampler2D texture0;

void main() {
	vec4 texColor = texture(texture0, v_texCoord0);

    o_fragColor = texColor * v_color;
}