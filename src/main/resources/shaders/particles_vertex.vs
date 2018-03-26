#version 330
//用于绘制粒子的着色器
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
//实例化（分组）渲染
layout (location=5) in mat4 modelViewMatrix;
layout (location=13) in vec2 texOffset;

out vec2 outTexCoord;

uniform mat4 projectionMatrix;

uniform int numCols;
uniform int numRows;

void main(){
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    // Support for texture atlas, update texture coordinates
    float x = (texCoord.x / numCols + texOffset.x);
    float y = (texCoord.y / numRows + texOffset.y);

    outTexCoord = vec2(x, y);
}