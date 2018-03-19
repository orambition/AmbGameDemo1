#version 330

const int MAX_WEIGHTS = 4;//每个顶点最多关联权重数
const int MAX_JOINTS = 150;//最多骨骼数

layout (location=0) in vec3 position;//顶点位置
layout (location=1) in vec2 texCoord;//纹理坐标
layout (location=2) in vec3 vertexNormal;//法线
layout (location=3) in vec4 jointWeights;//关节权重
layout (location=4) in ivec4 jointIndices;//关节id

out vec2 outTexCoord;//将颜色传递给片段着色器
out vec3 mvVertexNormal;//顶点法线
out vec3 mvVertexPos;//顶点位置
//out vec4 mlightviewVertexPos;//？？？
out mat4 outModelViewMatrix;//为了实现法线纹理，需要将该矩阵传给片段着色器

uniform mat4 jointsMatrix[MAX_JOINTS];//关节信息数组

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

//uniform mat4 modelLightViewMatrix;//？？？
//uniform mat4 orthoProjectionMatrix;//？？？

void main(){
    vec4 initPos = vec4(0, 0, 0, 0);
    vec4 initNormal = vec4(0, 0, 0, 0);
    int count = 0;
    //根据关节位置及权重计算订单位置
    for(int i = 0; i < MAX_WEIGHTS; i++){
        float weight = jointWeights[i];
        if(weight > 0) {
            count++;
            int jointIndex = jointIndices[i];
            vec4 tmpPos = jointsMatrix[jointIndex] * vec4(position, 1.0);
            initPos += weight * tmpPos;//根据帧中骨骼位置改变顶点位置

            vec4 tmpNormal = jointsMatrix[jointIndex] * vec4(vertexNormal, 0.0);
            initNormal += weight * tmpNormal;//根据帧中骨骼位置改变顶点法线
        }
    }
    //没有关节信息的按照默认位置
    if (count == 0){
        initPos = vec4(position, 1.0);
        initNormal = vec4(vertexNormal, 0.0);
    }
    vec4 mvPos = modelViewMatrix * initPos;
    gl_Position = projectionMatrix * mvPos;
    outTexCoord = texCoord;
    mvVertexNormal = normalize(modelViewMatrix * initNormal).xyz;
    mvVertexPos = mvPos.xyz;

    //mlightviewVertexPos = orthoProjectionMatrix * modelLightViewMatrix * vec4(position, 1.0);//？？？

    outModelViewMatrix = modelViewMatrix;
}