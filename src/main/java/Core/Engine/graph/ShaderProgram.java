package Core.Engine.graph;
/*着色器
* 用于从源文件创建着色器
* 渲染的方式*/

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
/*1.Create a OpenGL Program
* 2.Load the vertex and shader code files.
* 3.For	each shader, create	a new shader program 并指定它们的类型(vertex,fragment).
* 4.编译着色器.
* 5.将着色器附加到程序中.
* 6. Link the program.*/

public class ShaderProgram {
    //句柄
    private final int programId;
    //顶点 着色器
    private int vertexShaderId;
    //片段 着色器
    private int fragmentShaderId;
    //uniform，其他要传递的数据
    private final Map<String, Integer> uniforms;

    //新建GL程序
    public ShaderProgram() throws Exception{
        programId = glCreateProgram();
        if (programId == 0)
            throw new Exception("Could not create Shader");
        uniforms = new HashMap<>();
    }
    //创建Uniform
    public void createUniform(String uniformName) throws Exception {
        //传入该着色器的句柄，获取vertex中的值？vertex.vs的main中没有使用该值则会报错
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0)
            throw new Exception("Could not find uniform:" + uniformName);
        uniforms.put(uniformName, uniformLocation);
    }
    //创建点光源uniform数组
    public void createPointLightListUniform(String uniformName, int size) throws Exception {
        for (int i = 0; i < size; i++) {
            createPointLightUniform(uniformName + "[" + i + "]");
        }
    }
    //创建点光源所需的Uniform
    public void createPointLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".colour");
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".att.constant");
        createUniform(uniformName + ".att.linear");
        createUniform(uniformName + ".att.exponent");
    }
    //创建聚光灯uniform数组
    public void createSpotLightListUniform(String uniformName, int size) throws Exception {
        for (int i = 0; i < size; i++) {
            createSpotLightUniform(uniformName + "[" + i + "]");
        }
    }
    //创建聚光灯uniform
    public void createSpotLightUniform(String uniformName) throws Exception {
        createPointLightUniform(uniformName + ".pl");
        createUniform(uniformName + ".conedir");
        createUniform(uniformName + ".cutoff");
    }
    //创建平行光源所需的Uniform
    public void createDirectionalLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".colour");
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".intensity");
    }
    //创建材质Uniform
    public void createMaterialUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".ambient");
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
        createUniform(uniformName + ".hasTexture");
        createUniform(uniformName + ".reflectance");
    }

    //设置值为矩阵的Uniform
    public void setUniform(String uniformName, Matrix4f value) {
        // Dump the matrix into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }
    //设置值为整形的Uniform
    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }
    //设置值为浮点数的Uniform
    public void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }
    //设置值为3元向量的Uniform
    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }
    //设置值为4元向量的Uniform
    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }
    //设置值为点光源数组的Uniform
    public void setUniform(String uniformName, PointLight[] pointLights) {
        int numLights = pointLights != null ? pointLights.length : 0;
        for (int i = 0; i < numLights; i++) {
            setUniform(uniformName, pointLights[i], i);
        }
    }
    public void setUniform(String uniformName, PointLight pointLight, int pos) {
        setUniform(uniformName + "[" + pos + "]", pointLight);
    }
    //设置值为点光源的Uniform
    public void setUniform(String uniformName, PointLight pointLight) {
        setUniform(uniformName + ".colour", pointLight.getColor());
        setUniform(uniformName + ".position", pointLight.getPosition());
        setUniform(uniformName + ".intensity", pointLight.getIntensity());
        PointLight.Attenuation att = pointLight.getAttenuation();
        setUniform(uniformName + ".att.constant", att.getConstant());
        setUniform(uniformName + ".att.linear", att.getLinear());
        setUniform(uniformName + ".att.exponent", att.getExponent());
    }
    //设置值为聚光灯数组的Uniform
    public void setUniform(String uniformName, SpotLight[] spotLights) {
        int numLights = spotLights != null ? spotLights.length : 0;
        for (int i = 0; i < numLights; i++) {
            setUniform(uniformName, spotLights[i], i);
        }
    }
    public void setUniform(String uniformName, SpotLight spotLight, int pos) {
        setUniform(uniformName + "[" + pos + "]", spotLight);
    }
    //设置值为聚光灯的Uniform
    public void setUniform(String uniformName, SpotLight spotLight) {
        setUniform(uniformName + ".pl", spotLight.getPointLight());
        setUniform(uniformName + ".conedir", spotLight.getConeDirection());
        setUniform(uniformName + ".cutoff", spotLight.getCutOff());
    }
    //设置值为平行光源的Uniform
    public void setUniform(String uniformName, DirectionalLight dirLight) {
        setUniform(uniformName + ".colour", dirLight.getColor());
        setUniform(uniformName + ".direction", dirLight.getDirection());
        setUniform(uniformName + ".intensity", dirLight.getIntensity());
    }
    //设置值为材质的Uniform
    public void setUniform(String uniformName, Material material) {
        setUniform(uniformName + ".ambient", material.getAmbientColour());
        setUniform(uniformName + ".diffuse", material.getDiffuseColour());
        setUniform(uniformName + ".specular", material.getSpecularColour());
        setUniform(uniformName + ".hasTexture", material.isTextured() ? 1 : 0);
        setUniform(uniformName + ".reflectance", material.getReflectance());
    }

    //创建点着色器
    public void createVertexShader(String shaderCode)throws Exception{
        vertexShaderId = createShader(shaderCode,GL_VERTEX_SHADER);
    }
    //创建片段着色器
    public void createFragmentShader(String shaderCode)throws Exception{
        fragmentShaderId = createShader(shaderCode,GL_FRAGMENT_SHADER);
    }
    //创建着色器，根据类型
    protected int createShader(String shaderCode,int shaderType)throws Exception{
        //创建着色器，shaderId为句柄
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0)
            throw new Exception("Error creating shader. Type: " + shaderType);
        //读取源文件eg. vertex.vs
        glShaderSource(shaderId,shaderCode);
        //编译着色器
        glCompileShader(shaderId);
        //判断着色器编译是否完成
        if (glGetShaderi(shaderId,GL_COMPILE_STATUS)==0)
            throw new Exception("Error compiling Shader code: "+glGetShaderInfoLog(shaderId,1024));
        //附加到程序中
        glAttachShader(programId,shaderId);
        return shaderId;
    }
    //链接程序
    public void link()throws Exception{
        glLinkProgram(programId);
        if (glGetProgrami(programId,GL_LINK_STATUS) == 0)
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId,1024));
        //一旦着色器程序已经编译链接，顶点和片段着色器可以释放（通过gldetachshader）
        if (vertexShaderId != 0)
            glDetachShader(programId,vertexShaderId);
        if (fragmentShaderId != 0)
            glDetachShader(programId,fragmentShaderId);
        //验证函数，主要用于调试目的，当游戏完成制作，它应该被删除，
        glValidateProgram(programId);
        if (glGetProgrami(programId,GL_VALIDATE_STATUS) == 0)
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId,1024));
    }
    public void bind(){
        glUseProgram(programId);
    }
    public void unbind(){
        glUseProgram(0);
    }
    //清理？
    public void cleanUp(){
        unbind();
        if (programId != 0)
            glDeleteProgram(programId);
    }

}
