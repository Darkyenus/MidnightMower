#version 330

in vec3 v_normal;
in vec3 v_position;
in vec2 v_texCoord0;

out vec4 o_fragColor;

const int MAX_POINT_LIGHTS = 8;

uniform vec3 eye_position;

layout(packed) uniform Environment {
	int pointLightCount;

	vec3 ambientLight;
	vec3 pointLightPosition[MAX_POINT_LIGHTS];
	vec3 pointLightColor[MAX_POINT_LIGHTS];
	vec3 pointLightAttenuation[MAX_POINT_LIGHTS];
	vec4 pointLightDirection[MAX_POINT_LIGHTS];
};

layout(packed, shared) uniform Material {
	vec3 material_ambientColor;
	vec3 material_diffuseColor;
	vec3 material_specularColor;
	float material_shininess;
};

void main() {
	vec3 normal = normalize(v_normal);

	// Diffuse & Specular
	vec3 diffuseLight = vec3(0.0);
	vec3 specularLight = vec3(0.0);

	for (int i = 0; i < pointLightCount; i++) {
		vec3 lightPosition = pointLightPosition[i];
		vec3 lightColor = pointLightColor[i];
		vec3 lightAttenuation = pointLightAttenuation[i];
		vec4 lightDirection = pointLightDirection[i];

		vec3 directionToLight;
		float intensity;
		if (lightAttenuation != vec3(0.0)) {
			directionToLight = lightPosition - v_position;
			float distance = length(directionToLight);
			directionToLight = normalize(directionToLight);

			intensity = 1.0 / (lightAttenuation.x
								+ lightAttenuation.y * distance
								+ lightAttenuation.z * distance * distance);
		} else {
			directionToLight = normalize(lightPosition);
			intensity = 1.0;
		}

		if (lightDirection.w > -2.0) {
			float cone = dot(-directionToLight, lightDirection.xyz);
			if (cone < lightDirection.w) {
				intensity = 0.0;
			}
		}

		float baseDiffuseIntensity = max(dot(normal, directionToLight), 0.0);
		float diffuseIntensity = intensity * baseDiffuseIntensity;
		diffuseLight += diffuseIntensity * lightColor;

		if (material_shininess != 0) {
			vec3 reflected = reflect(-directionToLight, normal);
			vec3 directionToEye = normalize(eye_position - v_position);
			float specularIntensity = baseDiffuseIntensity * pow(max(dot(reflected, directionToEye), 0.0), material_shininess);
        	specularLight += specularIntensity * lightColor;
		}
	}

    vec3 color = ambientLight * material_ambientColor
            + diffuseLight * material_diffuseColor
            + specularLight * material_specularColor;

    o_fragColor = vec4(color, 1.0);
}
