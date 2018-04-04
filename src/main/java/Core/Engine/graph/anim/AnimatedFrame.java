package Core.Engine.graph.anim;
//动画帧
import org.joml.Matrix4f;

import java.util.Arrays;

public class AnimatedFrame {
    public static final int MAX_JOINTS = 150;//与scene_vertex着色器对应
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
    private final Matrix4f[] localJointMatrices;//注意此对象final属性
    private final Matrix4f[] jointMatrices;//每帧的骨骼位置矩阵
    public AnimatedFrame() {
        localJointMatrices = new Matrix4f[MAX_JOINTS];
        Arrays.fill(localJointMatrices, IDENTITY_MATRIX);
        jointMatrices = new Matrix4f[MAX_JOINTS];
        Arrays.fill(jointMatrices, IDENTITY_MATRIX);
    }
    public Matrix4f[] getLocalJointMatrices() {
        return localJointMatrices;
    }
    public Matrix4f[] getJointMatrices() {
        return jointMatrices;
    }
    public void setMatrix(int pos, Matrix4f localJointMatrix, Matrix4f invJointMatrix) {
        localJointMatrices[pos] = localJointMatrix;//当前位置的变量
        Matrix4f mat = new Matrix4f(localJointMatrix);
        mat.mul(invJointMatrix);//将变量作用于初始位置，得到最终位置
        jointMatrices[pos] = mat;
    }
}
