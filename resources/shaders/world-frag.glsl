#version 330

in vec3 v_normal;
in vec3 v_position;
in vec2 v_texCoord0;

out vec4 o_fragColor;

uniform vec3 eye_position;

const int MAX_POINT_LIGHTS = 8;

uniform int pointLightCount;
uniform struct PointLight {
	vec3 position;
	vec3 color;
	vec3 attenuation;
} pointLights[MAX_POINT_LIGHTS];

uniform vec3 material_ambientColor;
uniform vec3 material_diffuseColor;
uniform vec3 material_specularColor;
uniform float material_shininess;

void main() {
	vec3 normal = normalize(v_normal);

	// Diffuse & Specular
	vec3 diffuseLight = vec3(0.0);
	vec3 specularLight = vec3(0.0);

/*
	for (int i = 0; i < pointLightCount; i++) {
		PointLight light = pointLights[i];
		vec3 directionToLight;
		float intensity;

		vec3 attenuation = light.attenuation;
		if (attenuation != vec3(0.0)) {
			directionToLight = light.position - v_position;
			float distance = length(directionToLight);
			directionToLight = normalize(directionToLight);

			intensity = 1.0 / (attenuation.x
								+ attenuation.y * distance
								+ attenuation.z * distance * distance);
		} else {
			directionToLight = normalize(light.position);
			intensity = 1.0;
		}

		float baseDiffuseIntensity = max(dot(normal, directionToLight), 0.0);
		float diffuseIntensity = intensity * baseDiffuseIntensity;
		diffuseLight += diffuseIntensity * light.color;

		if (material_shininess != 0) {
			vec3 reflected = reflect(-directionToLight, normal);
			vec3 directionToEye = normalize(eye_position - v_position);
			float specularIntensity = baseDiffuseIntensity * pow(max(dot(reflected, directionToEye), 0.0), material_shininess);
        	specularLight += specularIntensity * light.color;
		}

	}*/

    vec3 color = material_ambientColor
            + diffuseLight * material_diffuseColor
            + specularLight * material_specularColor;

    o_fragColor = vec4(color, 1.0);
}
