package Core.Engine.graph;
//优化类-视野锥形裁减，通过视野对看不见的对象进行裁减

import Core.Engine.items.GameItem;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class FrustumCullingFilter {

    private final Matrix4f prjViewMatrix;//透视矩阵

    private FrustumIntersection frustumInt;//视野锥的面函数

    public FrustumCullingFilter() {
        prjViewMatrix = new Matrix4f();
        frustumInt = new FrustumIntersection();
    }
    //计算锥面
    public void updateFrustum(Matrix4f projMatrix, Matrix4f viewMatrix) {
        // 计算透视*视野矩阵
        prjViewMatrix.set(projMatrix);
        prjViewMatrix.mul(viewMatrix);
        // Update frustum intersection class
        frustumInt.set(prjViewMatrix);
    }
    //检测物体是否在视野锥内，通过物体包围球计算，包围球半径通过网格得出
    public void filter(Map<? extends Mesh, List<GameItem>> mapMesh) {
        for (Map.Entry<? extends Mesh, List<GameItem>> entry : mapMesh.entrySet()) {
            List<GameItem> gameItems = entry.getValue();
            filter(gameItems, entry.getKey().getBoundingRadius());
        }
    }
    //检测物体是否在视野锥内，通过物体位置和网格包围球半径
    public void filter(List<GameItem> gameItems, float meshBoundingRadius) {
        float boundingRadius;
        Vector3f pos;
        for (GameItem gameItem : gameItems) {
            if (!gameItem.isDisableFrustumCulling()) {
                boundingRadius = gameItem.getScale() * meshBoundingRadius;
                pos = gameItem.getPosition();
                gameItem.setInsideFrustum(insideFrustum(pos.x, pos.y, pos.z, boundingRadius));
            }
        }
    }
    //检测球体是否在视野锥内，球心和半径
    public boolean insideFrustum(float x0, float y0, float z0, float boundingRadius) {
        return frustumInt.testSphere(x0, y0, z0, boundingRadius);
    }
}
