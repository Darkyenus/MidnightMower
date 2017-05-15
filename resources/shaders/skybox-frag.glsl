#version 330

in vec2 v_position;
out vec4 o_fragColor;

uniform float fovFactor;
uniform vec3 cameraDirection;
uniform vec2 screenDimensions;

// Camera to world tranformation, calcNormal, based on: https://www.shadertoy.com/view/Xds3zN
mat3 setCamera (in vec3 direction, float roll) {
	vec3 cw = normalize(direction);
	vec3 cp = vec3(sin(roll), cos(roll), 0.0);
	vec3 cu = normalize( cross(cw,cp) );
	vec3 cv = normalize( cross(cu,cw) );
    return mat3( cu, cv, cw );
}

struct GradientPoint {
	vec3 color;
	float stop;
};

void main() {
	mat3 camera = setCamera(cameraDirection, 0.0);
    vec2 pos = (v_position * screenDimensions) / screenDimensions.y;
    vec3 ray = camera * normalize(vec3(pos.xy, fovFactor));

	GradientPoint low = GradientPoint(vec3(78, 102, 76), -1.0);
	GradientPoint mid = GradientPoint(vec3(115, 130, 133), -0.5);
	GradientPoint high = GradientPoint(vec3(41, 162, 255), 1.0);

	float a = ray.y;
	vec3 color;
	if (a < mid.stop) {
		color = mix(low.color, mid.color, (a - low.stop) / (mid.stop - low.stop));
	} else {
		color = mix(mid.color, high.color, (a - mid.stop) / (high.stop - mid.stop));
	}

	o_fragColor = vec4(color / vec3(255), 1.0);
}