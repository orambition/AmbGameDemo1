package Core.Engine.graph;
/*坐标转换很简单，不要想的很复杂！
* 1、模型有自己的坐标系；
* 2、WorldMatrix。将模型的放入世界中时，需要对其进行旋转、缩放和平移，这就需要用到WorldMatrix矩阵，仅仅是齐次坐标系中简单的矩阵变换；
* 3、ViewMatrix。得到模型世界坐标后，还要根据相机对其进行变化，这原本是一个坐标系的转换，但可以通过简单的方式实现，即相机不同，移动世界坐标，来模拟视角移动。这也是为什么ViewMatrix中的值是负值；
* 4、ProjectionMatrix、OrthoMatrix。得到根据相机位置而确定的世界坐标后，就可以对其进行绘制了，但绘制分为两种，即透视法和正交法，这两不同的投影方式对应着不同的投影矩阵。
* 而上述步骤中，WorldMatrix和ViewMatrix可以放在一起实现。*/

import Core.Engine.items.GameItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {
    //优化，将透视矩阵和视野矩阵的get函数改为get+update函数的方式，这样多个item可以共用一个矩阵，而不是每次都新建一个
    private final Matrix4f projectionMatrix;//透视矩阵
    private final Matrix4f modelViewMatrix;//模型X视野矩阵
    private final Matrix4f viewMatrix;//视野矩阵
    private final Matrix4f orthoMatrix;//正交矩阵

    public Transformation() {
        modelViewMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        orthoMatrix = new Matrix4f();
    }

    //透视矩阵。将三维坐标近大远小的投影到二位屏幕上
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
    public final Matrix4f updateProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }
    //正交矩阵。用于获取正交投影矩阵，此处还可以避免失真当窗口变化时hud要相应的变化而不是拉伸
    public final Matrix4f getOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        orthoMatrix.identity();
        orthoMatrix.setOrtho2D(left, right, bottom, top);
        return orthoMatrix;
    }

    // 视野矩阵,根据当前摄像机的位置和角度得到该矩阵，用于修正物体的显示坐标
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }
    public Matrix4f updateViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();

        viewMatrix.identity();
        // First do the rotation so camera rotates over its position
        viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
        // 相机移动x的距离，就是物体移动-x的距离，相机是不动的，动的是世界
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    //将物体的位置矩阵与视野矩阵相乘，因为视野会影响物体的显示坐标，所以需要进行改处理
    public Matrix4f buildModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix) {
        Vector3f rotation = gameItem.getRotation();
        modelViewMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        //mul矩阵乘法
        return viewCurr.mul(modelViewMatrix);
    }
    //用于获取hud的投影矩阵
    public Matrix4f buildOrtoProjModelMatrix(GameItem gameItem, Matrix4f orthoMatrix) {
        Vector3f rotation = gameItem.getRotation();
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        Matrix4f orthoMatrixCurr = new Matrix4f(orthoMatrix);
        orthoMatrixCurr.mul(modelMatrix);
        return orthoMatrixCurr;
    }
}
