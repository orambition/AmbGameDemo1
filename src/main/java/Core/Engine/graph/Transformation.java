package Core.Engine.graph;
/*坐标转换很简单，不要想的很复杂！
* 1、模型有自己的坐标系；
* 2、WorldMatrix。将模型的放入世界中时，需要对其进行旋转、缩放和平移，这就需要用到WorldMatrix矩阵，仅仅是齐次坐标系中简单的矩阵变换；
* 3、ViewMatrix。得到模型世界坐标后，还要根据相机对其进行变化，这原本是一个坐标系的转换，但可以通过简单的方式实现，即相机不同，移动世界坐标，来模拟视角移动。这也是为什么ViewMatrix中的值是负值；
* 4、ProjectionMatrix、OrthoMatrix。得到根据相机位置而确定的世界坐标后，就可以对其进行绘制了，但绘制分为两种，即透视法和正交法，这两不同的投影方式对应着不同的投影矩阵。
* 而上述步骤中，WorldMatrix和ViewMatrix可以放在一起实现。*/

import Core.Engine.items.GameItem;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Transformation {
    //优化，将透视矩阵和视野矩阵的get函数改为get+update函数的方式，这样多个item可以共用一个矩阵，而不是每次都新建一个
    //private final Matrix4f projectionMatrix;//透视矩阵，已转移至Window类中
    //private final Matrix4f viewMatrix;//摄像机视野矩阵，已转移至Camaera类中
    //private final Matrix4f orthoProjMatrix;//正交矩阵，已转移到ShadowCascad类中
    //private final Matrix4f lightViewMatrix;//光源视野矩阵，已转移到ShadowCascad类中
    //private final Matrix4f modelLightViewMatrix;//模型*光源视野矩阵，已转移到ShadowCascad类中
    private final Matrix4f modelMatrix;//模型矩阵，用于缩放、旋转、移动模型本身
    private final Matrix4f modelViewMatrix;//模型*摄像机视野矩阵

    public Transformation() {
        modelMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
    }
    //通用的视野矩阵生成函数
    public static Matrix4f updateGenericViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix) {
        return matrix.identity().rotationX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z))
                .translate(-position.x, -position.y, -position.z);
    }
    //生成物体矩阵
    public Matrix4f buildModelMatrix(GameItem gameItem) {
        Quaternionf rotation = gameItem.getRotation();
        return modelMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
    }
    //将物体矩阵与视野矩阵相乘
    public Matrix4f buildModelViewMatrix(Matrix4f modelMatrix, Matrix4f viewMatrix) {
        //return modelViewMatrix.set(viewMatrix).mul(modelMatrix);
        return viewMatrix.mulAffine(modelMatrix, modelViewMatrix);
    }
    //将物体的位置矩阵与参数中的视野矩阵相乘，因为视野会影响物体的显示坐标，所以需要进行改处理
    public Matrix4f buildModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix) {
        return buildModelViewMatrix(buildModelMatrix(gameItem), viewMatrix);
        /*Vector3f rotation = gameItem.getRotation();
        modelMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        modelViewMatrix.set(viewMatrix);
        return modelViewMatrix.mul(modelMatrix);*/
    }
}
