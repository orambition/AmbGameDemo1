package Core.Engine.graph;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
/*需要渲染的数据*/
public class Mesh {
    /*Vertex Array Objects
     * 一个数组是一个对象，可以包含多个vbo
     * 每个vbo相当于一个属性
     * 如坐标、纹理、颜色、等*/
    /*Vertex Buffer Object
     * 可以包含坐标、纹理、颜色、等信息*/
    private final int vaoId;
    private final int posVboId;
    private final int colourVboId;
    private final int idxVboId;
    private final int vertexCount;
    //位置，颜色，顺序
    public Mesh(float[] positions, float[] colours, int[] indices){
        FloatBuffer posBuffer = null;
        FloatBuffer colourBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            vertexCount = indices.length;

            //创建vao,并绑定
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            //创建 点 vbo,并绑定
            posVboId = glGenBuffers();
            //使用MemoryUtil在非堆内存创建缓冲区，因为java存储在堆内存的数据不能通过本地OpenGl代码访问
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            /*定义数据结构和存储在其中的VAO属性列表
             * 索引：指定着色器期望此数据的位置。
             * 大小：指定每个顶点属性的组件数（从1到4）。
             * 在这种情况下，我们传递的是三维坐标，所以应该是3。
             * 类型：指定数组中每个组件的类型，在这种情况下是浮点。
             * 规范化：指定值是否应该规范化。
             * 步幅：指定连续的通用顶点属性之间的字节偏移量。
             * 偏移量：指定缓冲区中第一个组件的偏移量。*/
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            //创建 Color vbo
            colourVboId = glGenBuffers();
            colourBuffer = MemoryUtil.memAllocFloat(colours.length);
            colourBuffer.put(colours).flip();
            glBindBuffer(GL_ARRAY_BUFFER,colourVboId);
            glBufferData(GL_ARRAY_BUFFER,colourBuffer,GL_STATIC_DRAW);
            glVertexAttribPointer(1,3,GL_FLOAT,false,0,0);

            //创建索引vbo,并绑定
            idxVboId = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            //完成后解绑vbo和vao
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            //手动释放申请的内存
            if (posBuffer != null)
                MemoryUtil.memFree(posBuffer);
            if (colourBuffer != null)
                MemoryUtil.memFree(colourBuffer);
            if (indicesBuffer != null)
                MemoryUtil.memFree(indicesBuffer);
        }//完成以上步骤，数据就已经在显存中了
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void render(){
        // Bind to the VAO
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);//启用数组1，对用位置
        glEnableVertexAttribArray(1);//启用数组2，对用颜色
        /*绘制图形，参数：
         * 模式：指定渲染的原语，在此情况下的三角形。
         * 计数：指定要呈现的元素的数目。
         * 类型：指定索引数据中的值类型。
         * 索引：指定应用于索引数据以开始呈现的偏移量。*/
        glDrawElements(GL_TRIANGLES,getVertexCount(),GL_UNSIGNED_INT,0);
        // Restore state
        glDisableVertexAttribArray(0);//关闭数组1
        glDisableVertexAttribArray(1);//关闭数组2
        glBindVertexArray(0);
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(colourVboId);
        glDeleteBuffers(idxVboId);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
