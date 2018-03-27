package Core.Engine.graph;
//实例化网格类。继承自Mesh，用于实例化渲染
// 在其基础上增加模型视野矩阵和光视野矩阵的VBO
// 重点是该类中的numInstances属性，通过该属性分组，并根据组别传递以上两个VBO
// 以组的方式进行绘制glDrawElementsInstanced，而不是单一对象绘制一次以组的方式进行绘制glDrawElements
// 减少了调用OpenGL函数的次数，优化了性能
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
    private static final int FLOAT_SIZE_BYTES = 4;//Float的字节数
    private static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;//4元向量的字节数
    private static final int MATRIX_SIZE_FLOATS = 4 * 4;//矩阵的元素数量
    private static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;//矩阵的字节数
    private static final int INSTANCE_SIZE_BYTES = MATRIX_SIZE_BYTES * 2 + FLOAT_SIZE_BYTES * 2;//一个属性的字节数
    private static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS * 2 + 2;//一个属性的元素数量：两个矩阵（模型*视野、模型*光视野矩阵）加一个纹理坐标
    private final int numInstances;//对象数量
    private final int instanceDataVBO;//实例化（分组）渲染所需的VBO，包含两个矩阵和一个二维纹理坐标
    private FloatBuffer instanceDataBuffer;//
    public InstancedMesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int numInstances) {
        super(positions, textCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0));
        this.numInstances = numInstances;
        glBindVertexArray(vaoId);

        instanceDataVBO = glGenBuffers();
        vboIdList.add(instanceDataVBO);
        this.instanceDataBuffer = MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
        //创建vbo类型，此处是4*4矩阵两个加一个2数据的纹理坐标
        int start = 5;//从5开始是因为Mesh来中已经有4个属性了
        int strideStart = 0;
        // 模型*摄像机视野矩阵
        for (int i = 0; i < 4; i++) {
            //循环4次的原因是每个vbo属性最多只能有4个GL_FLOAT，而一个矩阵是4*4的
            //参数5：步长，这一点对于理解这一点非常重要，这就设置了连续属性之间的字节偏移量。在这种情况下，我们需要将它设置为以字节为单位的整个矩阵大小。
            //参数6：指针，这个属性定义应用到的偏移量。在我们的例子中，我们需要将矩阵定义分成四个调用。
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            //重要函数，这是实例化渲染的基础，也是区别于普通渲染的地方
            //属性数组中每隔divisor个实例都会读取一个新的数值（而不是之前的每个顶点）。此时在这个属性所对应的顶点属性数组中，数据索引值的计算将变成instance/divisor的形式，其中instance表示当前的实例数目，而divisor就是当前属性的更新频率值
            //说白了就是每个值有效一次，这样一个对象就能对应一个值，而且不需要制定位置，因为值使用过后就失效了，下次自动使用下一个
            glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }
        // 模型*灯光视野矩阵
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }
        // 纹理坐标起始位置
        glVertexAttribPointer(start, 2, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        glVertexAttribDivisor(start, 1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    @Override
    public void cleanUp() {
        super.cleanUp();
        if (this.instanceDataBuffer != null) {
            MemoryUtil.memFree(this.instanceDataBuffer);
            this.instanceDataBuffer = null;
        }
    }
    @Override
    protected void initRender() {
        super.initRender();
        int start = 5;
        int numElements = 4 * 2 + 1;
        for (int i = 0; i < numElements; i++) {
            glEnableVertexAttribArray(start + i);//激活数组
        }
    }
    @Override
    protected void endRender() {
        int start = 5;
        int numElements = 4 * 2 + 1;
        for (int i = 0; i < numElements; i++) {
            glDisableVertexAttribArray(start + i);//关闭数组
        }
        super.endRender();
    }
    public void renderListInstanced(List<GameItem> gameItems, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        renderListInstanced(gameItems, false, transformation, viewMatrix, lightViewMatrix);
    }
    public void renderListInstanced(List<GameItem> gameItems, boolean billBoard, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        initRender();
        int chunkSize = numInstances;
        int length = gameItems.size();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            List<GameItem> subList = gameItems.subList(i, end);
            //一个Chunk绘制一次，此处是与原Mesh类中绘制函数的区别
            renderChunkInstanced(subList, billBoard, transformation, viewMatrix, lightViewMatrix);
        }
        endRender();
    }
    private void renderChunkInstanced(List<GameItem> gameItems, boolean billBoard, Transformation transformation, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {

        this.instanceDataBuffer.clear();
        int i = 0;
        Texture text = getMaterial().getTexture();
        for (GameItem gameItem : gameItems) {//遍历共享一个Mesh的对象
            Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);//获取模型矩阵
            if (viewMatrix != null) {//如果摄像机视野矩阵不为空
                if (billBoard) {//粒子不随摄像机转动
                    viewMatrix.transpose3x3(modelMatrix);
                }
                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);//获取模型*视野矩阵
                if (billBoard) {//恢复粒子的缩放效果
                    modelViewMatrix.scale(gameItem.getScale());
                }
                modelViewMatrix.get(INSTANCE_SIZE_FLOATS * i, instanceDataBuffer);//将数据放入Buffer中的index位置
            }
            if (lightViewMatrix != null) {
                Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
                modelLightViewMatrix.get(INSTANCE_SIZE_FLOATS * i + MATRIX_SIZE_FLOATS, this.instanceDataBuffer);
            }
            if (text != null) {//如果纹理不为空，则进行分块渲染，texture atlas
                int col = gameItem.getTextPos() % text.getNumCols();
                int row = gameItem.getTextPos() / text.getNumCols();
                float textXOffset = (float) col / text.getNumCols();
                float textYOffset = (float) row / text.getNumRows();
                int buffPos = INSTANCE_SIZE_FLOATS * i + MATRIX_SIZE_FLOATS * 2;
                this.instanceDataBuffer.put(buffPos, textXOffset);
                this.instanceDataBuffer.put(buffPos + 1, textYOffset);
            }
            i++;
        }//完成以上步骤，所有对象的视野矩阵就都在一个buffer中了
        //然后，对共享同一Mesh的一组对象设置VBO数据
        glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
        glBufferData(GL_ARRAY_BUFFER, instanceDataBuffer, GL_DYNAMIC_DRAW);
        //从而，一组对象只调用glDrawElementsInstanced函数一次，减少了调用次数，优化了性能
        glDrawElementsInstanced(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, gameItems.size());
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

}
