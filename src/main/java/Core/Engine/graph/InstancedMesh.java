package Core.Engine.graph;
//实例化网格类。继承自Mesh，用于实例化渲染
// 在其基础上增加模型视野矩阵和光视野矩阵的VBO
// 重点是该类中的numInstances属性，通过该属性分组，并根据组别传递以上两个VBO
// 以组的方式进行绘制glDrawElementsInstanced，而不是单一对象绘制一次以组的方式进行绘制glDrawElements
import Core.Engine.items.GameItem;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstancedMesh extends Mesh {
    private static final int VECTOR4F_SIZE_BYTES = 4 * 4;//矩阵大小
    private static final int MATRIX_SIZE_BYTES = 4 * VECTOR4F_SIZE_BYTES;//byte类型的矩阵大小
    private static final int MATRIX_SIZE_FLOATS = 4 * 4;//float类型的矩阵大小
    private final int numInstances;//实例化对象的数量，就是共享一个mesh的对象数量
    private final int modelViewVBO;
    private final int modelLightViewVBO;
    private FloatBuffer modelViewBuffer;
    private FloatBuffer modelLightViewBuffer;
    public InstancedMesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int numInstances) {
        super(positions, textCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0));
        this.numInstances = numInstances;
        glBindVertexArray(vaoId);
        // 模型*摄像机视野矩阵
        modelViewVBO = glGenBuffers();
        vboIdList.add(modelViewVBO);
        this.modelViewBuffer = MemoryUtil.memAllocFloat(numInstances * MATRIX_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, modelViewVBO);
        int start = 5;//从5开始是因为Mesh来中已经有4个属性了
        for (int i = 0; i < 4; i++) {
            //创建vbo类型，循环4次的原因是每个vbo属性最多只能有4个GL_FLOAT，而一个矩阵是4*4的
            //参数5：步长，这一点对于理解这一点非常重要，这就设置了连续属性之间的字节偏移量。在这种情况下，我们需要将它设置为以字节为单位的整个矩阵大小。
            //参数6：指针，这个属性定义应用到的偏移量。在我们的例子中，我们需要将矩阵定义分成四个调用。
            glVertexAttribPointer(start, 4, GL_FLOAT, false, MATRIX_SIZE_BYTES, i * VECTOR4F_SIZE_BYTES);
            glVertexAttribDivisor(start, 1);//重要函数，第二个参数控制该vbo影响物体的次数
            start++;
        }
        // 模型*灯光视野矩阵
        modelLightViewVBO = glGenBuffers();
        vboIdList.add(modelLightViewVBO);
        this.modelLightViewBuffer = MemoryUtil.memAllocFloat(numInstances * MATRIX_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, modelLightViewVBO);
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, MATRIX_SIZE_BYTES, i * VECTOR4F_SIZE_BYTES);
            glVertexAttribDivisor(start, 1);
            start++;
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    @Override
    public void cleanUp() {
        super.cleanUp();
        if (this.modelViewBuffer != null) {
            MemoryUtil.memFree(this.modelViewBuffer);
            this.modelViewBuffer = null;
        }
        if (this.modelLightViewBuffer != null) {
            MemoryUtil.memFree(this.modelLightViewBuffer);
            this.modelLightViewBuffer = null;
        }
    }
    @Override
    protected void initRender() {
        super.initRender();
        int start = 5;
        int numElements = 4 * 2;
        for (int i = 0; i < numElements; i++) {
            glEnableVertexAttribArray(start + i);//激活数组
        }
    }
    @Override
    protected void endRender() {
        int start = 5;
        int numElements = 4 * 2;
        for (int i = 0; i < numElements; i++) {
            glDisableVertexAttribArray(start + i);//关闭数组
        }
        super.endRender();
    }
    public void renderListInstanced(List<GameItem> gameItems, boolean depthMap, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        initRender();
        int chunkSize = numInstances;
        int length = gameItems.size();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            List<GameItem> subList = gameItems.subList(i, end);
            //一个Chunk绘制一次，此处是与原Mesh类中绘制函数的区别
            renderChunkInstanced(subList, depthMap, transformation, viewMatrix, lightViewMatrix);
        }
        endRender();
    }
    private void renderChunkInstanced(List<GameItem> gameItems, boolean depthMap, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        this.modelViewBuffer.clear();
        this.modelLightViewBuffer.clear();
        int i = 0;
        for (GameItem gameItem : gameItems) {//遍历共享一个Mesh的对象
            Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
            if (!depthMap) {
                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
                modelViewMatrix.get(MATRIX_SIZE_FLOATS * i, modelViewBuffer);//将矩阵放入Buffer中的index位置
            }
            Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
            modelLightViewMatrix.get(MATRIX_SIZE_FLOATS * i, this.modelLightViewBuffer);
            i++;
        }//完成以上步骤，所有对象的视野矩阵就都在一个buffer中了
        //然后，对共享同一Mesh的一组对象设置VBO数据
        glBindBuffer(GL_ARRAY_BUFFER, modelViewVBO);
        glBufferData(GL_ARRAY_BUFFER, modelViewBuffer, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, modelLightViewVBO);
        glBufferData(GL_ARRAY_BUFFER, modelLightViewBuffer, GL_DYNAMIC_DRAW);
        //从而，一组对象只调用glDrawElementsInstanced函数一次，减少了调用次数，优化了性能
        glDrawElementsInstanced(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, gameItems.size());
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

}
