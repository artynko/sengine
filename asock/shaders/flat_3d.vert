#version 330

layout (location = 0) in vec4 position;
layout (location = 1) in vec4 color;
layout (location = 3) in vec2 uv;

out vec4 theColor;
out vec2 uvs;
uniform mat4 modelToWorld;
uniform mat4 modelToCamerac; 
uniform sampler2D textureSampler;
uniform sampler2D alphaSampler;


layout(std140) uniform GlobalMatrices
{
	uniform mat4 modelToCamera; 
	uniform mat4 perspectiveMatrix;
};
void main()
{
    gl_Position = perspectiveMatrix * modelToWorld * mat4(inverse(mat3(modelToCamerac))) * position;
	theColor = color;
	uvs = uv;

}