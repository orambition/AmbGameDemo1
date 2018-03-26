#version 330

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;

in vec2 outTexCoord;
in vec3 mvVertexNormal;
in vec3 mvVertexPos;
in vec4 mlightviewVertexPos;//物体在光源视角的位置，用来判断物体是否在阴影中
in mat4 outModelViewMatrix;//因为点的法线是跟世界坐标变化的，所以需要该矩阵

out vec4 fragColor;
//衰减
struct Attenuation{
    float constant;
    float linear;
    float exponent;
};
//点光源的数据结构
struct PointLight{
    vec3 colour;
    // Light position is assumed to be in view coordinates
    vec3 position;
    float intensity;//强度
    Attenuation att;//点光源有衰减
};
//聚光灯光源
struct SpotLight{
    PointLight pl;
    vec3 conedir;
    float cutoff;
};
//平行光源
struct DirectionalLight{
    vec3 colour;
    vec3 direction;
    float intensity;
};
//材质
struct Material{
    vec4 ambient;//颜色的环境光成分
    vec4 diffuse;//漫反射成分
    vec4 specular;//镜面反射成分
    int hasTexture;//是否有纹理
    int hasNormalMap;//是否有法线纹理
    float reflectance;//反射率
};
//雾
struct Fog{
    int activeFog;//是否启用
    vec3 colour;//颜色
    float density;//密度
};

uniform sampler2D texture_sampler;//纹理
uniform sampler2D normalMap;//法线纹理
uniform vec3 ambientLight;//环境光，以相同的方式影响每一个面
uniform float specularPower;//高光（镜面反射率）
uniform Material material;//材质

uniform PointLight pointLights[MAX_POINT_LIGHTS];//点光源
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];//聚光灯
uniform DirectionalLight directionalLight;//平行光源

uniform Fog fog;//雾

uniform sampler2D shadowMap;//阴影图，由深度着色器绘制得出
uniform int renderShadow;//是否绘制阴影
//全局变量
vec4 ambientC;
vec4 diffuseC;
vec4 speculrC;

//根据材质的特性设置颜色
void setupColours(Material material, vec2 textCoord){
    if (material.hasTexture == 1){
        ambientC = texture(texture_sampler, textCoord);
        diffuseC = ambientC;
        speculrC = ambientC;
    }else{
        ambientC = material.ambient;
        diffuseC = material.diffuse;
        speculrC = material.specular;
    }
}
//计算光对顶点的颜色作用
vec4 calcLightColour(vec3 light_colour, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal){
    vec4 diffuseColour = vec4(0, 0, 0, 0);
    vec4 specColour = vec4(0, 0, 0, 0);
    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColour = diffuseC * vec4(light_colour, 1.0) * light_intensity * diffuseFactor;
    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir , normal));
    float specularFactor = max( dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColour = speculrC * light_intensity  * specularFactor * material.reflectance * vec4(light_colour, 1.0);
    return (diffuseColour + specColour);
}
//计算点光源对每个顶点的作用
vec4 calcPointLight(PointLight light, vec3 position, vec3 normal){
    // Diffuse Light
    vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_colour = calcLightColour(light.colour, light.intensity, position, to_light_dir, normal);
    // Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
        light.att.exponent * distance * distance;
    return light_colour / attenuationInv;
}
//计算聚光灯光源对每个顶点的作用
vec4 calcSpotLight(SpotLight light, vec3 position, vec3 normal){
    vec3 light_direction = light.pl.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec3 from_light_dir  = -to_light_dir;
    float spot_alfa = dot(from_light_dir, normalize(light.conedir));
    vec4 colour = vec4(0, 0, 0, 0);
    if ( spot_alfa > light.cutoff ){
        colour = calcPointLight(light.pl, position, normal);
        colour *= (1.0 - (1.0 - spot_alfa)/(1.0 - light.cutoff));
    }
    return colour;
}
//计算平行光
vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal){
    return calcLightColour(light.colour, light.intensity, position, normalize(light.direction), normal);
}
//计算雾对颜色的影响
vec4 calcFog(vec3 pos, vec4 colour, Fog fog, vec3 ambientLight, DirectionalLight dirLight){
    vec3 fogColor = fog.colour * (ambientLight + dirLight.colour * dirLight.intensity);
    float distance = length(pos);
    float fogFactor = 1.0 / exp( (distance * fog.density)* (distance * fog.density));
    fogFactor = clamp( fogFactor, 0.0, 1.0 );
    vec3 resultColour = mix(fogColor, colour.xyz, fogFactor);
    return vec4(resultColour.xyz, colour.w);
}
//转换法线纹理，材质、顶点法线、纹理坐标、模型视图矩阵。
vec3 calcNormal(Material material, vec3 normal, vec2 text_coord, mat4 modelViewMatrix){
    vec3 newNormal = normal;
    if ( material.hasNormalMap == 1 ){
        newNormal = texture(normalMap, text_coord).rgb;
        newNormal = normalize(newNormal * 2 - 1);
        newNormal = normalize(modelViewMatrix * vec4(newNormal, 0.0)).xyz;
    }
    return newNormal;
}
//计算传入的位置是否在阴影中，是返回1否0
float calcShadow(vec4 position){
    if ( renderShadow == 0 ){
        return 1.0;
    }
    vec3 projCoords = position.xyz;
    // 从屏幕坐标转换到纹理坐标
    projCoords = projCoords * 0.5 + 0.5;
    float bias = 0.05;
    float shadowFactor = 0.0;
    vec2 inc = 1.0 / textureSize(shadowMap, 0);
    //获取改点周围的9个阴影值
    for(int row = -1; row <= 1; ++row){
        for(int col = -1; col <= 1; ++col){
            float textDepth = texture(shadowMap, projCoords.xy + vec2(row, col) * inc).r;
            shadowFactor += projCoords.z - bias > textDepth ? 1.0 : 0.0;
        }
    }
    shadowFactor /= 9.0;
    if(projCoords.z > 1.0){
        shadowFactor = 1.0;
    }
    return 1 - shadowFactor;
}
void main(){
    setupColours(material, outTexCoord);

    vec3 currNomal = calcNormal(material, mvVertexNormal, outTexCoord, outModelViewMatrix);

    vec4 diffuseSpecularComp = calcDirectionalLight(directionalLight, mvVertexPos, currNomal);
    for (int i=0; i<MAX_POINT_LIGHTS; i++){
        if ( pointLights[i].intensity > 0 ){
            diffuseSpecularComp += calcPointLight(pointLights[i], mvVertexPos, currNomal);
        }
    }
    for (int i=0; i<MAX_SPOT_LIGHTS; i++){
        if ( spotLights[i].pl.intensity > 0 ){
            diffuseSpecularComp += calcSpotLight(spotLights[i], mvVertexPos, currNomal);
        }
    }
    float shadow = calcShadow(mlightviewVertexPos);
    fragColor = clamp(ambientC * vec4(ambientLight, 1) + diffuseSpecularComp * shadow, 0, 1);
    //fragColor = ambientC * vec4(ambientLight, 1) + diffuseSpecularComp;
    if ( fog.activeFog == 1 ){
        fragColor = calcFog(mvVertexPos, fragColor, fog, ambientLight, directionalLight);
    }
}