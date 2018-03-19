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
    private final Matrix4f orthoProjMatrix;//正交矩阵

    private final Matrix4f viewMatrix;//摄像机视野矩阵
    private final Matrix4f lightViewMatrix;//光源视野矩阵

    private final Matrix4f modelMatrix;//模型矩阵，用于缩放、旋转、移动模型本身
    private final Matrix4f modelViewMatrix;//模型*摄像机视野矩阵

    private final Matrix4f modelLightMatrix;//模型矩阵，同上，用于绘制深度图
    private final Matrix4f modelLightViewMatrix;//模型*光源视野矩阵

    private final Matrix4f ortho2DMatrix;//2d的正交矩阵，用于绘制hud

    private static final Vector3f X_AXIS = new Vector3f(1, 0, 0);
    private static final Vector3f Y_AXIS = new Vector3f(0, 1, 0);

    public Transformation() {
        projectionMatrix = new Matrix4f();
        orthoProjMatrix = new Matrix4f();

        viewMatrix = new Matrix4f();
        lightViewMatrix = new Matrix4f();

        modelMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();

        modelLightMatrix = new Matrix4f();
        modelLightViewMatrix = new Matrix4f();

        ortho2DMatrix = new Matrix4f();
    }

    //获取透视矩阵。
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
    //生成透视矩阵，将三维坐标近大远小的投影到二位屏幕上
    public final Matrix4f updateProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    //获取正交矩阵。
    public final Matrix4f getOrthoProjectionMatrix() {
        return orthoProjMatrix;
    }
    //生成正交矩阵
    public Matrix4f updateOrthoProjectionMatrix(float left, float right, float bottom, float top, float zNear, float zFar) {
        orthoProjMatrix.identity();
        orthoProjMatrix.setOrtho(left, right, bottom, top, zNear, zFar);
        return orthoProjMatrix;
    }

    //通用的视野矩阵生成函数
    private Matrix4f updateGenericViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix) {
        matrix.identity();
        // 首先旋转，使摄像机旋转到该方向。
        matrix.rotate((float)Math.toRadians(rotation.x), X_AXIS)
                .rotate((float)Math.toRadians(rotation.y), Y_AXIS);
        // 让后移动到该位置，相机移动x的距离，就是物体移动-x的距离，相机是不动的，动的是世界
        matrix.translate(-position.x, -position.y, -position.z);
        return matrix;
    }
    // 获取摄像机视野矩阵
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }
    //生成摄像机视野矩阵,根据当前摄像机的位置和角度得到该矩阵，用于修正物体的显示坐标
    public Matrix4f updateViewMatrix(Camera camera) {
        return updateGenericViewMatrix(camera.getPosition(), camera.getRotation(), viewMatrix);
    }
    //获取光源视野矩阵
    public Matrix4f getLightViewMatrix() {
        return lightViewMatrix;
    }
    //生成光源视野矩阵，将光源作为摄像机形成的视野矩阵
    public Matrix4f updateLightViewMatrix(Vector3f position, Vector3f rotation) {
        return updateGenericViewMatrix(position, rotation, lightViewMatrix);
    }

    //将物体的位置矩阵与参数中的视野矩阵相乘，因为视野会影响物体的显示坐标，所以需要进行改处理
    public Matrix4f buildModelViewMatrix(GameItem gameItem, Matrix4f matrix) {
        Vector3f rotation = gameItem.getRotation();
        modelMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        modelViewMatrix.set(matrix);
        return modelViewMatrix.mul(modelMatrix);
    }
    //将物体的位置矩阵与参数中的光源视野矩阵相乘
    public Matrix4f buildModelLightViewMatrix(GameItem gameItem, Matrix4f matrix) {
        Vector3f rotation = gameItem.getRotation();
        modelLightMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        modelLightViewMatrix.set(matrix);
        return modelLightViewMatrix.mul(modelLightMatrix);
    }
    //将物体的位置矩阵与参数中的正交矩阵相乘
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
    //获取特定的2d正交矩阵，该矩阵无纵向属性，用于绘制hud等
    public final Matrix4f getOrtho2DProjectionMatrix(float left, float right, float bottom, float top) {
        ortho2DMatrix.identity();
        ortho2DMatrix.setOrtho2D(left, right, bottom, top);
        return ortho2DMatrix;
    }
}
