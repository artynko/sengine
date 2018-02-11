#version 330

in vec4 theColor;
in vec3 outNormal;
in vec4 modelSpacePosition;

uniform vec3 pickupColor;

out vec4 outputColor;
uniform sampler2D textureSampler;
uniform sampler2D alphaSampler;
void main()
{
    outputColor = vec4(pickupColor, 1);
        
}