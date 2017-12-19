package Core.Game;

import Core.Engine.Utils;
import Core.Engine.Window;
import Core.Engine.graph.ShaderProgram;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    /*Vertex Array Objects
    * 一个数组是一个对象，可以包含多个vbo
    * 每个vbo相当于一个属性
    * 如坐标、纹理、颜色、等*/
    private int vaoId;

    /*Vertex Buffer Object
    * 可以包含坐标、纹理、颜色、等信息*/
    private int vboId;
    private ShaderProgram shaderProgram;

    public Renderer(){

    }

    public void init() throws Exception{
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/fragment.fs"));
        shaderProgram.link();
        //要绘制的三角形顶点坐标，在右手三维坐标系中XYZ
        float[] vertices = new float[]{
                0.0f,0.5f,0.0f,
                -0.5f,-0.5f,0.0f,
                0.5f,-0.5f,0.0f
        };
        //创建缓冲区为了使用OpenGL库
        FloatBuffer verticesBuffer = null;
        try {
            //使用MemoryUtil在非堆内存创建缓冲区，因为java存储在堆内存的数据不能通过本地OpenGl代码访问
            verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
            verticesBuffer.put(vertices).flip();
            //创建vao,并绑定
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);
            //创建vbo,并绑定
            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER,vboId);
            glBufferData(GL_ARRAY_BUFFER,verticesBuffer,GL_STATIC_DRAW);
            /*定义数据结构和存储在其中的VAO属性列表
            * 索引：指定着色器期望此数据的位置。
            * 大小：指定每个顶点属性的组件数（从1到4）。
            * 在这种情况下，我们传递的是三维坐标，所以应该是3。
            * 类型：指定数组中每个组件的类型，在这种情况下是浮点。
            * 规范化：指定值是否应该规范化。
            * 步幅：指定连续的通用顶点属性之间的字节偏移量。
            * 偏移量：指定缓冲区中第一个组件的偏移量。*/
            glVertexAttribPointer(0,3,GL_FLOAT,false,0,0);
            //完成后解绑vbo和vao
            glBindBuffer(GL_ARRAY_BUFFER,0);
            glBindVertexArray(0);
        }finally {
            //手动释放申请的内存
            if (verticesBuffer!=null)
                MemoryUtil.memFree(verticesBuffer);
        }
        //完成以上步骤，数据就已经在显存中了
    }
    //清屏函数
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//清屏
    }
    //渲染函数
    public void render(Window window) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWindowWidth(), window.getWindowHeight());
            window.setResized(false);
        }
        shaderProgram.bind();
        // Bind to the VAO
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        // Draw the vertices
        glDrawArrays(GL_TRIANGLES, 0, 3);
        // Restore state
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shaderProgram.unbind();
    }
    //
    public void clearup(){
        if (shaderProgram != null)
            shaderProgram.cleanup();

        glDisableVertexAttribArray(0);
        // Delete the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
