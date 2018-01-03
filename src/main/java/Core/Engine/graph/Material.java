package Core.Engine.graph;

import org.joml.Vector4f;

//材质类，用于存储材质所需的数据，添加光时所创建的类
public class Material {
    //默认的颜色
    private static final Vector4f DEFAULT_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    //环境光参数
    private Vector4f ambientColour;
    //漫反射参数
    private Vector4f diffuseColour;
    //镜面反射参数
    private Vector4f specularColour;
    //反射率
    private float reflectance;
    //纹理
    private Texture texture;

    //各种构造函数
    public Material() {
        this.ambientColour = DEFAULT_COLOUR;
        this.diffuseColour = DEFAULT_COLOUR;
        this.specularColour = DEFAULT_COLOUR;
        this.texture = null;
        this.reflectance = 0;
    }
    public Material(Vector4f colour, float reflectance) {
        this(colour, colour, colour, null, reflectance);
    }
    public Material(Texture texture) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, 0);
    }
    public Material(Texture texture, float reflectance) {
        this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, reflectance);
    }
    public Material(Vector4f ambientColour, Vector4f diffuseColour, Vector4f specularColour, Texture texture, float reflectance) {
        this.ambientColour = ambientColour;
        this.diffuseColour = diffuseColour;
        this.specularColour = specularColour;
        this.texture = texture;
        this.reflectance = reflectance;
    }

    public Vector4f getAmbientColour() {
        return ambientColour;
    }
    public void setAmbientColour(Vector4f ambientColour) {
        this.ambientColour = ambientColour;
    }
    public Vector4f getDiffuseColour() {
        return diffuseColour;
    }
    public void setDiffuseColour(Vector4f diffuseColour) {
        this.diffuseColour = diffuseColour;
    }
    public Vector4f getSpecularColour() {
        return specularColour;
    }
    public void setSpecularColour(Vector4f specularColour) {
        this.specularColour = specularColour;
    }
    public float getReflectance() {
        return reflectance;
    }
    public void setReflectance(float reflectance) {
        this.reflectance = reflectance;
    }
    public boolean isTextured() {
        return this.texture != null;
    }
    public Texture getTexture() {
        return texture;
    }
    public void setTexture(Texture texture) {
        this.texture = texture;
    }
}
