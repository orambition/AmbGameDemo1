package Core.Engine.graph;
/*着色器
* 用于从源文件创建着色器*/

import org.joml.Matrix4f;
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

    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final Map<String, Integer> uniforms;
    //新建GL程序
    public ShaderProgram() throws Exception{
        programId = glCreateProgram();
        if (programId == 0)
            throw new Exception("Could not create Shader");
        uniforms = new HashMap<>();
    }
    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new Exception("Could not find uniform:" + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }
    public void setUniform(String uniformName, Matrix4f value) {
        // Dump the matrix into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
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
        //创建着色器
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0)
            throw new Exception("Error creating shader. Type: " + shaderType);
        //读取源文件
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
