package Core.Engine.graph;
//网格类，用于储存网格中的点以及点的各种信息
//网格由点构成，点包含：位置坐标、纹理坐标、法线等信息
//网格新增一个材质属性，纹理包含在其中，材质含有光反射等信息
//一个obj模型文件就是对网格信息的描述文件

import Core.Engine.items.GameItem;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
/*需要渲染的数据*/
public class Mesh {
    /*Vertex Array Objects
     * 一个数组是一个对象，可以包含多个vbo
     * 每个vbo相当于一个属性
     * 如坐标、纹理、颜色、等*/

    private final int vaoId;

    /*Vertex Buffer Object
     * 可以包含坐标、纹理、颜色、等信息
     * vbo是GPU内存中的存储单元，将要绘制的数据存入vbo opengl才可见*/
    private final List<Integer> vboIdList;

    private final int vertexCount;//顶点数量

    private Material material;//材质

    public static final int MAX_WEIGHTS = 4;//每个顶点可以关联的最多权重数量，
    public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices) {
        this(positions, textCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0));
    }

    //将传进来的数据，位置坐标、纹理坐标、顶点法线、顺序、关节id、关节权重等vbo,通过缓存存入vao（显存？）
    public Mesh(float[] positions, float[] textCoords,float[] normals, int[] indices, int[] jointIndices, float[] weights){
        FloatBuffer posBuffer = null;//位置缓存
        FloatBuffer textCoordsBuffer  = null;//纹理坐标缓存
        FloatBuffer vecNormalsBuffer = null;//法线缓存
        IntBuffer indicesBuffer = null;//序号缓存（确定了面）
        IntBuffer jointIndicesBuffer = null;//关节id缓存
        FloatBuffer weightsBuffer = null;//关节权重缓存
        try {
            vertexCount = indices.length;
            vboIdList = new ArrayList<>();

            //创建vao,并绑定，绑定的意思是指，在之后的函数中操作的是这个vao
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            //创建 点 vbo,并绑定
            //1、创建一个vbo对象
            int vboId = glGenBuffers();
            vboIdList.add(vboId);
            //使用MemoryUtil在非堆内存创建缓冲区，因为java存储在堆内存的数据不能通过本地OpenGl代码访问
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            //2、将新建的BO绑定到GL_ARRAY_BUFFER上下文中
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            //3、为vbo分配空间，并将数据从内存RAM中拷贝数据到BO中。
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            /*4、定义数据结构和存储在其中的VAO属性列表
             * 索引：指定着色器期望此数据的位置。
             * 大小：指定每个顶点属性的组件数（从1到4）。
             * 在这种情况下，我们传递的是三维坐标，所以应该是3。
             * 类型：指定数组中每个组件的类型，在这种情况下是浮点。
             * 规范化：指定值是否应该规范化。
             * 步幅：指定连续的通用顶点属性之间的字节偏移量。
             * 偏移量：指定缓冲区中第一个组件的偏移量。*/
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            //完成以上步骤，顶点数据已经在gpu内存中了

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

            // 权重vbo，注意此处顺序
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            weightsBuffer = MemoryUtil.memAllocFloat(weights.length);
            weightsBuffer.put(weights).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

            // 关节idVbo
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            jointIndicesBuffer = MemoryUtil.memAllocInt(jointIndices.length);
            jointIndicesBuffer.put(jointIndices).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, jointIndicesBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0);

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
            if (weightsBuffer != null)
                MemoryUtil.memFree(weightsBuffer);
            if (jointIndicesBuffer != null)
                MemoryUtil.memFree(jointIndicesBuffer);
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
    private void initRender() {
        Texture texture = material.getTexture();
        if (texture != null){
            // 激活0号纹理单元
            glActiveTexture(GL_TEXTURE0);
            // 加载纹理。将传进来的纹理与其绑定
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        //法线纹理
        Texture normalMap = material.getNormalMap();
        if (normalMap != null){
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, normalMap.getId());
        }
        // Bind to the VAO
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);//启用数组1，对用位置
        glEnableVertexAttribArray(1);//启用数组2，对用纹理坐标
        glEnableVertexAttribArray(2);//启用数组2，对用顶点法线
        glEnableVertexAttribArray(3);//启用数组3，对应权重
        glEnableVertexAttribArray(4);//对应关节id
    }
    private void endRender() {
        glDisableVertexAttribArray(0);//关闭数组1
        glDisableVertexAttribArray(1);//关闭数组2
        glDisableVertexAttribArray(2);//关闭数组3
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D,0);
    }
    //通过控制vao中的数组，进行显示相应的信息
    public void render(){
        initRender();
        /*绘制图形，参数：
         * 模式：指定渲染的原语，在此情况下的三角形。
         * 计数：指定要呈现的元素的数目。
         * 类型：指定索引数据中的值类型。
         * 索引：指定应用于索引数据以开始呈现的偏移量。*/
        glDrawElements(GL_TRIANGLES,getVertexCount(),GL_UNSIGNED_INT,0);
        endRender();
    }
    //列表绘制，优化渲染性能，以mesh为单位进行渲染
    public void renderList(List<GameItem> gameItems, Consumer<GameItem> consumer) {
        initRender();
        for (GameItem gameItem : gameItems) {
            // java新特性，函数式编程，consumer为一个处理函数，accept为执行该处理函数
            consumer.accept(gameItem);
            // Render this game item
            glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
        }
        endRender();
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
    //创建空的浮点数组，用于创建没有动画的mesh时，提供默认的参数
    private static float[] createEmptyFloatArray(int length, float defaultValue) {
        float[] result = new float[length];
        Arrays.fill(result, defaultValue);
        return result;
    }
    //创建空的整数数组，同上
    private static int[] createEmptyIntArray(int length, int defaultValue) {
        int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }
}
