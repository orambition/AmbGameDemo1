#version 330

const int MAX_WEIGHTS = 4;//每个顶点最多关联权重数，与Mesh类对应
const int MAX_JOINTS = 150;//最多骨骼数，与AnimatedFrame类对应
const int NUM_CASCADES = 3;//阴影层的层数，与ShadowRenderer类对应

layout (location=0) in vec3 position;//顶点位置
layout (location=1) in vec2 texCoord;//纹理坐标
layout (location=2) in vec3 vertexNormal;//法线
layout (location=3) in vec4 jointWeights;//关节权重
layout (location=4) in ivec4 jointIndices;//关节id
//用于实例化（分组）绘制的数组数据
layout (location=5) in mat4 modelInstancedMatrix;//模型矩阵
layout (location=9) in vec2 texOffset;//纹理起始位置
layout (location=10) in float selectedInstanced;//是否选择该对象

out vec2 outTexCoord;//将颜色传递给片段着色器
out vec3 mvVertexNormal;//顶点法线
out vec3 mvVertexPos;//顶点位置
out vec4 mlightviewVertexPos[NUM_CASCADES];//模型在光源视角下的位置矩阵，判断该位置是否在阴影中（分层）
out mat4 outModelViewMatrix;//模型*视野矩阵，为了实现法线纹理，需要将该矩阵传给片段着色器
out float outSelected;//为了选中后高亮，需要将该值传递给片段着色器0

uniform int isInstanced;//是否是实例化（分组）渲染的对象，数组是组内的对象数量
uniform mat4 modelNonInstancedMatrix;//模型矩阵
uniform float selectedNonInstanced;//是否被选中

uniform mat4 jointsMatrix[MAX_JOINTS];//关节信息数组

//摄像机相关矩阵
uniform mat4 viewMatrix;//摄像机视野矩阵
uniform mat4 projectionMatrix;//透视矩阵
//阴影相关矩阵
uniform mat4 lightViewMatrix[NUM_CASCADES];//光视野矩阵（分层）
uniform mat4 orthoProjectionMatrix[NUM_CASCADES];//光的正交矩阵（分层）
//纹理的行列数
uniform int numCols;
uniform int numRows;

void main(){
    vec4 initPos = vec4(0, 0, 0, 0);//位置
    vec4 initNormal = vec4(0, 0, 0, 0);//法线
    mat4 modelMatrix;//模型矩阵
    if ( isInstanced > 0 ){//实例（分组）渲染
        outSelected = selectedInstanced;
        modelMatrix = modelInstancedMatrix;

        initPos = vec4(position, 1.0);
        initNormal = vec4(vertexNormal, 0.0);
    }else{
        outSelected = selectedNonInstanced;
        modelMatrix = modelNonInstancedMatrix;
        int count = 0;
        for(int i = 0; i < MAX_WEIGHTS; i++){
            float weight = jointWeights[i];
            if(weight > 0) {
                count++;
                int jointIndex = jointIndices[i];
                vec4 tmpPos = jointsMatrix[jointIndex] * vec4(position, 1.0);
                initPos += weight * tmpPos;
                vec4 tmpNormal = jointsMatrix[jointIndex] * vec4(vertexNormal, 0.0);
                initNormal += weight * tmpNormal;
            }
        }
        if (count == 0){
            initPos = vec4(position, 1.0);
            initNormal = vec4(vertexNormal, 0.0);
        }
    }
    mat4 modelViewMatrix =  viewMatrix * modelMatrix;
    vec4 mvPos = modelViewMatrix * initPos;
    gl_Position = projectionMatrix * mvPos;
    // Support for texture atlas, update texture coordinates
    float x = (texCoord.x / numCols + texOffset.x);
    float y = (texCoord.y / numRows + texOffset.y);
    outTexCoord = vec2(x, y);

    mvVertexNormal = normalize(modelViewMatrix * initNormal).xyz;
    mvVertexPos = mvPos.xyz;
    for (int i = 0 ; i < NUM_CASCADES ; i++) {
        mlightviewVertexPos[i] = orthoProjectionMatrix[i] * lightViewMatrix[i] * modelMatrix * initPos;
    }
    outModelViewMatrix = modelViewMatrix;
}