package Core.Engine.graph;
//网格类，用于储存网格中的点以及点的各种信息
//网格由点构成，点包含：位置坐标、纹理坐标、法线等信息
//网格新增一个材质属性，纹理包含在其中，材质含有光反射等信息
//一个obj模型文件就是对网格信息的描述文件
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
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

    /*private final int posVboId;
    private final int colourVboId;
    private final int idxVboId;*/
    private final List<Integer> vboIdList;

    private final int vertexCount;//顶点数量
    /*private Texture texture;//纹理
    private Vector3f colour;
    private static final Vector3f DEFAULT_COLOUR = new Vector3f(1.0f, 1.0f, 1.0f);*/
    private Material material;//材质

    //将传进来的数据，位置坐标、纹理坐标、顶点法线、顺序等vbo,通过缓存存入vao（显存？）
    public Mesh(float[] positions, float[] textCoords,float[] normals, int[] indices){
        FloatBuffer posBuffer = null;//位置缓存
        FloatBuffer textCoordsBuffer  = null;//纹理坐标缓存
        FloatBuffer vecNormalsBuffer = null;//法线缓存
        IntBuffer indicesBuffer = null;//序号缓存（确定了面）
        try {
            vertexCount = indices.length;
            vboIdList = new ArrayList<>();

            //创建vao,并绑定，绑定的意思是指，在之后的函数中操作的是这个vao
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            //创建 点 vbo,并绑定
            int vboId = glGenBuffers();
            vboIdList.add(vboId);
            //使用MemoryUtil在非堆内存创建缓冲区，因为java存储在堆内存的数据不能通过本地OpenGl代码访问
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
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

            //创建 纹理坐标 vbo
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();
            //绑定别的vbo，就是更改了操作对象
            glBindBuffer(GL_ARRAY_BUFFER,vboId);
            glBufferData(GL_ARRAY_BUFFER,textCoordsBuffer,GL_STATIC_DRAW);
            glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

            // 顶点法线 VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
            vecNormalsBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            //创建索引vbo,并绑定
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            //完成后解绑vbo和vao
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            //手动释放申请的内存
            if (posBuffer != null)
                MemoryUtil.memFree(posBuffer);
            if (textCoordsBuffer != null)
                MemoryUtil.memFree(textCoordsBuffer);
            if (vecNormalsBuffer != null)
                MemoryUtil.memFree(vecNormalsBuffer);
            if (indicesBuffer != null)
                MemoryUtil.memFree(indicesBuffer);
        }//完成以上步骤，数据就已经在显存中了
    }
    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }
    //通过控制vao中的数组，进行显示相应的信息
    public void render(){
        Texture texture = material.getTexture();
        if (texture != null){
            // 激活0号纹理单元
            glActiveTexture(GL_TEXTURE0);
            // 加载纹理。将传进来的纹理与其绑定
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        // Bind to the VAO
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);//启用数组1，对用位置
        glEnableVertexAttribArray(1);//启用数组2，对用纹理坐标
        glEnableVertexAttribArray(2);//启用数组2，对用顶点法线
        /*绘制图形，参数：
         * 模式：指定渲染的原语，在此情况下的三角形。
         * 计数：指定要呈现的元素的数目。
         * 类型：指定索引数据中的值类型。
         * 索引：指定应用于索引数据以开始呈现的偏移量。*/
        glDrawElements(GL_TRIANGLES,getVertexCount(),GL_UNSIGNED_INT,0);
        // Restore state
        glDisableVertexAttribArray(0);//关闭数组1
        glDisableVertexAttribArray(1);//关闭数组2
        glDisableVertexAttribArray(2);//关闭数组3
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D,0);
    }

    public void cleanUp() {
        deleteBuffers();
        // Delete the texture
        Texture texture = material.getTexture();
        if (texture != null)
            texture.cleanup();

    }
    public void deleteBuffers() {
        glDisableVertexAttribArray(0);
        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }
        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
