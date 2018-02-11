#version 330

in vec4 theColor;
in vec2 uvs;

out vec4 outputColor;
uniform sampler2D textureSampler;
uniform sampler2D alphaSampler;
uniform vec4 colorTint;


void main()
{
        vec4 alpha = texture2D(alphaSampler, uvs);
        if (alpha.x < 0.01)
          discard;
          
     outputColor = vec4(texture2D(textureSampler, uvs).xyz, alpha.x);
     if (any(notEqual(colorTint, vec4(0, 0, 0, 0)))) {
     	outputColor = mix(outputColor, colorTint, 0.5);
     }
 //  outputColor = vec4(texture2D(textureSampler, uvs).xyz, 1.0f);
        
}