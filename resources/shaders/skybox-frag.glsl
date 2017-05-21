#version 330

in vec2 v_position;
out vec4 o_fragColor;

uniform float fovFactor;
uniform vec3 cameraDirection;
uniform vec2 screenDimensions;

uniform vec3 moonPos;
uniform sampler2D moon;

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

// Star sampler, based on https://www.shadertoy.com/view/Md2SR3
//-------------------------------------------------------------------------------------------------------
// Return random noise in the range [0.0, 1.0], as a function of x.
float noise3d( in vec3 x )
{
    float xhash = sin( x.x * 37.0 );
    float yhash = sin( x.y * 57.0 );
    float zhash = sin( x.z * 77.0 );
    return fract(415.92653 * (xhash + yhash + zhash));
}

// Convert noise3d() into a "star field" by stomping everthing below fThreshhold to zero.
float NoisyStarField(in vec3 vSamplePos, float fThreshhold) {
    float result = noise3d( vSamplePos );
    if (result >= fThreshhold) {
        return pow( (result - fThreshhold)/(1.0 - fThreshhold), 6.0 );
    } else {
        return 0.0;
    }
}

// Stabilize NoisyStarField() by only sampling at integer values.
float StableStarField( in vec3 samplePoint, float threshold )
{
    // Linear interpolation between four samples.
    // Note: This approach has some visual artifacts.
    // There must be a better way to "anti alias" the star field.
    float fractX = fract( samplePoint.x );
    float fractY = fract( samplePoint.y );
    float fractZ = fract( samplePoint.z );
    vec3 floorSamplePoint = floor( samplePoint );

    float s000 = NoisyStarField(floorSamplePoint, threshold);
    float s001 = NoisyStarField(floorSamplePoint + vec3(0.0, 0.0, 1.0), threshold);
    float s010 = NoisyStarField(floorSamplePoint + vec3(0.0, 1.0, 0.0), threshold);
    float s011 = NoisyStarField(floorSamplePoint + vec3(0.0, 1.0, 1.0), threshold);
    float s100 = NoisyStarField(floorSamplePoint + vec3(1.0, 0.0, 0.0), threshold);
    float s101 = NoisyStarField(floorSamplePoint + vec3(1.0, 0.0, 1.0), threshold);
    float s110 = NoisyStarField(floorSamplePoint + vec3(1.0, 1.0, 0.0), threshold);
    float s111 = NoisyStarField(floorSamplePoint + vec3(1.0, 1.0, 1.0), threshold);

    vec4 sYZ = mix(vec4(s000, s001, s010, s011), vec4(s100, s101, s110, s111), fractX);
    vec2 sZ = mix(sYZ.xy, sYZ.zw, fractY);
    float s = mix(sZ.x, sZ.y, fractZ);
    return s;
}
//-------------------------------------------------------------------------------------------------------

//-------------------------------------------------------------------------------------------------------
// http://www.neilmendoza.com/glsl-rotation-about-an-arbitrary-axis/
mat3 rotationMatrix(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat3(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c);
}
//-------------------------------------------------------------------------------------------------------

const float PI = 3.1415926536;
const float TAU = 6.2831853072;
const float moonSize = 0.15;

vec2 toSpericalAngle(vec3 v) {
	return vec2(atan(v.z, v.x), acos(v.y));
}

void main() {
	mat3 camera = setCamera(cameraDirection, 0.0);
    vec2 pos = (v_position * screenDimensions) / screenDimensions.y;
    vec3 ray = camera * normalize(vec3(pos.xy, fovFactor));

// Day
/*
	GradientPoint low = GradientPoint(vec3(78, 102, 76), -1.0);
	GradientPoint mid = GradientPoint(vec3(115, 130, 133), -0.5);
	GradientPoint high = GradientPoint(vec3(41, 162, 255), 1.0);
*/
	// Night
	GradientPoint low = GradientPoint(vec3(15, 25, 15), -1.0);
    GradientPoint mid = GradientPoint(vec3(5, 7, 5), -0.5);
    GradientPoint high = GradientPoint(vec3(3, 3, 5), 1.0);

	float a = ray.y;
	vec3 color;
	if (a < mid.stop) {
		color = mix(low.color, mid.color, (a - low.stop) / (mid.stop - low.stop));
	} else {
		color = mix(mid.color, high.color, (a - mid.stop) / (high.stop - mid.stop));
	}
	color /= vec3(255);

	float star = StableStarField(ray * 350.0 , 0.99);
	star *= max(ray.y + 0.8, 0.0) / 1.5;
	color += vec3(star);

	vec2 moonAngle = toSpericalAngle(moonPos);

	vec3 projectedRay = rotationMatrix(vec3(0.0, 0.0, 1.0), -moonAngle.y) * rotationMatrix(vec3(0.0, 1.0, 0.0), -moonAngle.x) * ray;
	vec2 moonUV = projectedRay.xz;

	if (length(moonUV) < moonSize && projectedRay.y > 0.0) {
		vec2 uv = ((moonUV / moonSize) + 1.0) * 0.5;
		vec4 moonColor = texture(moon, uv.yx);
		color = (color * (1.0 - moonColor.a)) + moonColor.rgb * moonColor.a;
	}

	o_fragColor = vec4(color, 1.0);
}