package Core.Engine.graph.shadow;
//阴影层类，分层阴影渲染中的每层
//对于每个子层，首先，通过视野锥8个顶点计算视野锥的中心，然后通过该位置和光的方向计算光的位置
import Core.Engine.Window;
import Core.Engine.graph.Transformation;
import Core.Engine.graph.lights.DirectionalLight;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShadowCascade {
    private static final int FRUSTUM_CORNERS = 8;//锥角数
    //每层独立的投影矩阵（摄像机的）和光的正交矩阵及光视野矩阵
    private final Matrix4f projViewMatrix;
    private final Matrix4f orthoProjMatrix;
    private final Matrix4f lightViewMatrix;
    // 视野锥中心在世界空间的坐标
    private final Vector3f centroid;
    //锥角坐标，用于计算锥的中心
    private final Vector3f[] frustumCorners;

    private final float zNear;

    private final float zFar;

    private final Vector4f tmpVec;
    //通过近平面和远平面距离创建分层
    public ShadowCascade(float zNear, float zFar) {
        this.zNear = zNear;
        this.zFar = zFar;
        this.projViewMatrix = new Matrix4f();
        this.orthoProjMatrix = new Matrix4f();
        this.centroid = new Vector3f();
        this.lightViewMatrix = new Matrix4f();
        this.frustumCorners = new Vector3f[FRUSTUM_CORNERS];
        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            frustumCorners[i] = new Vector3f();
        }
        tmpVec = new Vector4f();
    }

    public Matrix4f getLightViewMatrix() {
        return lightViewMatrix;
    }

    public Matrix4f getOrthoProjMatrix() {
        return orthoProjMatrix;
    }
    //更新每层的深度图
    public void update(Window window, Matrix4f viewMatrix, DirectionalLight light) {
        // 构建每层的投影*视野矩阵，此处不用window的投影矩阵，是因为每层的近平面和远平面都不同
        float aspectRatio = (float) window.getWindowWidth() / (float) window.getWindowHeight();
        projViewMatrix.setPerspective(Window.FOV, aspectRatio, zNear, zFar);
        projViewMatrix.mul(viewMatrix);
        //通过视野锥的8个顶点确定其z轴的范围，并计算出其中心点
        float maxZ = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;
        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            Vector3f corner = frustumCorners[i];
            corner.set(0, 0, 0);
            projViewMatrix.frustumCorner(i, corner);//获取锥角坐标
            centroid.add(corner);

            minZ = Math.min(minZ, corner.z);//获取z值最小的锥角
            maxZ = Math.max(maxZ, corner.z);//获取z值最大的锥角
        }centroid.div(8.0f);//除8？
        // 根据光的方向和锥中心点确定光的位置
        Vector3f lightDirection = light.getDirection();
        Vector3f lightPosInc = new Vector3f().set(lightDirection);
        float distance = maxZ - minZ;
        lightPosInc.mul(distance);
        Vector3f lightPosition = new Vector3f();
        lightPosition.set(centroid);
        lightPosition.add(lightPosInc);
        //更新视野矩阵
        updateLightViewMatrix(lightDirection, lightPosition);

        //更新投影矩阵
        updateLightProjectionMatrix();
    }
    //根据光的位置和方向计算光视野矩阵
    private void updateLightViewMatrix(Vector3f lightDirection, Vector3f lightPosition) {
        float lightAngleX = 90;
        float lightAngleY = 0;
        float lightAngleZ = 0;
        Transformation.updateGenericViewMatrix(lightPosition, new Vector3f(lightAngleX, lightAngleY, lightAngleZ), lightViewMatrix);
    }
    //根据每层摄像机视野的透视锥计算光的正交投影矩阵
    private void updateLightProjectionMatrix() {
        // 将视野锥的坐标从世界坐标，转换为光空间
        float minX =  Float.MAX_VALUE;
        float maxX = -Float.MIN_VALUE;
        float minY =  Float.MAX_VALUE;
        float maxY = -Float.MIN_VALUE;
        float minZ =  Float.MAX_VALUE;
        float maxZ = -Float.MIN_VALUE;
        for (int i = 0; i < FRUSTUM_CORNERS; i++) {
            Vector3f corner = frustumCorners[i];
            tmpVec.set(corner, 1);
            tmpVec.mul(lightViewMatrix);//视野锥顶点*光视野矩阵
            //找包围盒的两个顶点
            minX = Math.min(tmpVec.x, minX);
            maxX = Math.max(tmpVec.x, maxX);
            minY = Math.min(tmpVec.y, minY);
            maxY = Math.max(tmpVec.y, maxY);
            minZ = Math.min(tmpVec.z, minZ);
            maxZ = Math.max(tmpVec.z, maxZ);
        }
        float distz = maxZ - minZ;

        orthoProjMatrix.setOrtho(minX, maxX, minY, maxY, 0, distz);
    }
}
