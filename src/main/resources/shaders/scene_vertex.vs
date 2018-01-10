#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;

out vec2 outTexCoord;//将颜色传递给片段着色器
out vec3 mvVertexNormal;//顶点法线
out vec3 mvVertexPos;//顶点位置
out mat4 outModelViewMatrix;//为了实现法线纹理，需要将该矩阵传给片段着色器

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main(){
    vec4 mvPos = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPos;
    outTexCoord = texCoord;
    mvVertexNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
    mvVertexPos = mvPos.xyz;
    outModelViewMatrix = modelViewMatrix;
}