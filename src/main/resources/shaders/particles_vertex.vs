#version 330
//用于绘制粒子的着色器
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
//实例化（分组）渲染
layout (location=5) in mat4 modelMatrix;//模型矩阵
layout (location=9) in vec2 texOffset;
layout (location=10) in float scale;

out vec2 outTexCoord;

uniform mat4 viewMatrix;//视野矩阵
uniform mat4 projectionMatrix;//透视矩阵

uniform int numCols;
uniform int numRows;

void main(){
    mat4 modelViewMatrix = viewMatrix * modelMatrix;
    // 保持缩放
    modelViewMatrix[0][0] = scale;
    modelViewMatrix[1][1] = scale;
    modelViewMatrix[2][2] = scale;
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    // Support for texture atlas, update texture coordinates
    float x = (texCoord.x / numCols + texOffset.x);
    float y = (texCoord.y / numRows + texOffset.y);

    outTexCoord = vec2(x, y);
}