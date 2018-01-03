#version 330

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;

in vec2 outTexCoord;
in vec3 mvVertexNormal;
in vec3 mvVertexPos;

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
    int hasTexture;
    float reflectance;
};
uniform sampler2D texture_sampler;
uniform vec3 ambientLight;//环境光，以相同的方式影响每一个面
uniform float specularPower;//镜面反射率
uniform Material material;//材质

uniform PointLight pointLights[MAX_POINT_LIGHTS];//点光源
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];//聚光灯
uniform DirectionalLight directionalLight;//平行光源

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
void main(){
    setupColours(material, outTexCoord);

    vec4 diffuseSpecularComp = calcDirectionalLight(directionalLight, mvVertexPos, mvVertexNormal);
    for (int i=0; i<MAX_POINT_LIGHTS; i++){
        if ( pointLights[i].intensity > 0 ){
            diffuseSpecularComp += calcPointLight(pointLights[i], mvVertexPos, mvVertexNormal);
        }
    }
    for (int i=0; i<MAX_SPOT_LIGHTS; i++){
        if ( spotLights[i].pl.intensity > 0 ){
            diffuseSpecularComp += calcSpotLight(spotLights[i], mvVertexPos, mvVertexNormal);
        }
    }
    fragColor = ambientC * vec4(ambientLight, 1) + diffuseSpecularComp;
}