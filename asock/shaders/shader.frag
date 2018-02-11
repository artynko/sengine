#version 330

in vec4 theColor;
in vec3 outNormal;
in vec4 modelSpacePosition;
in vec2 uvs;

uniform vec3 diffuseLightDirection;
uniform mat4 modelToWorld;

uniform sampler2D textureSampler;
uniform sampler2D alphaSampler;

uniform vec4 colorTint;

out vec4 outputColor;

void main()
{
	vec3 lightDir = normalize(inverse(mat3(modelToWorld)) * diffuseLightDirection - modelSpacePosition.xyz);
	//vec3 lightDir = normalize(diffuseLightDirection);
    
    vec3 n = outNormal;
    n = normalize(n);
    float dotN = max(dot(n, lightDir), 0.0);
	vec4 alpha = texture2D(alphaSampler, uvs); 
	if (alpha.x < 0.01)
		discard;
    vec4 color = vec4(texture2D(textureSampler, uvs).xyz, alpha.x);
    outputColor = vec4(0.4, 0.4, 0.4, 1) * color; // ambient
    vec4 sun = (color *  dotN) * vec4(0.9, 0.9, 0.9, 1);
    // * vec4(max(modelSpacePosition.y + 0.5, 1), max(modelSpacePosition.y + 0.5, 1), max(modelSpacePosition.y + 0.5, 1), 1) * vec4(vec3(0.9), 1); // diffuse

 
 	if (any(notEqual(colorTint, vec4(0, 0, 0, 0)))) {
     	outputColor = mix(outputColor, colorTint, 0.5);
    }
    outputColor += sun;
        
}