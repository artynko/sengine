#version 330

layout (location = 0) in vec4 position;
layout (location = 1) in vec4 color;
layout (location = 2) in vec3 normal;
layout (location = 3) in vec2 inUvs;

out vec4 theColor;
out vec3 outNormal;
out vec2 uvs;
out vec4 modelSpacePosition;
uniform mat4 modelToWorld;
uniform sampler2D textureSampler;
uniform sampler2D alphaSampler;


layout(std140) uniform GlobalMatrices
{
	uniform mat4 modelToCamera; 
	uniform mat4 perspectiveMatrix;
};

void main()
{
    gl_Position = perspectiveMatrix * modelToWorld * position;
	modelSpacePosition = position;
	outNormal = normal;
	theColor = color;
	uvs = inUvs;

}