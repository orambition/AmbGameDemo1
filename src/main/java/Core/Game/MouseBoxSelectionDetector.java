package Core.Game;
//鼠标选择检测类，继承自摄像机选择检测类

import Core.Engine.Window;
import Core.Engine.graph.Camera;
import Core.Engine.items.GameItem;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MouseBoxSelectionDetector extends CameraBoxSelectionDetector {
    private final Matrix4f invProjectionMatrix;
    private final Matrix4f invViewMatrix;
    private final Vector3f mouseDir;
    private final Vector4f tmpVec;
    public MouseBoxSelectionDetector() {
        super();
        invProjectionMatrix = new Matrix4f();
        invViewMatrix = new Matrix4f();
        mouseDir = new Vector3f();
        tmpVec = new Vector4f();
    }
    public boolean  selectGameItem(GameItem[] gameItems, Window window, Vector2d mousePos, Camera camera) {
        int wdwWitdh = window.getWindowWidth();
        int wdwHeight = window.getWindowHeight();
        //根据鼠标位置,获取点击处的空间坐标，鼠标原点在屏幕中央，所以*2
        float x = (float)(2 * mousePos.x) / (float)wdwWitdh - 1.0f;
        float y = 1.0f - (float)(2 * mousePos.y) / (float)wdwHeight;
        float z = -1.0f;
        invProjectionMatrix.set(window.getProjectionMatrix());
        invProjectionMatrix.invert();
        tmpVec.set(x, y, z, 1.0f);
        tmpVec.mul(invProjectionMatrix);
        tmpVec.z = -1.0f;
        tmpVec.w = 0.0f;
        Matrix4f viewMatrix = camera.getViewMatrix();
        invViewMatrix.set(viewMatrix);
        invViewMatrix.invert();
        tmpVec.mul(invViewMatrix);
        mouseDir.set(tmpVec.x, tmpVec.y, tmpVec.z);
        return selectGameItem(gameItems, camera.getPosition(), mouseDir);
    }
}
