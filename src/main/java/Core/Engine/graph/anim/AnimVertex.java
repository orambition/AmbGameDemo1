package Core.Engine.graph.anim;
//md5网格的顶点信息，原loader类中顶点信息内部类
import org.joml.Vector2f;
import org.joml.Vector3f;

public class AnimVertex {
    public Vector3f position;//位置
    public Vector2f textCoords;//纹理坐标
    public Vector3f normal;//法线
    public float[] weights;//权重数组
    public int[] jointIndices;//关节数组
    public AnimVertex() {
        super();
        normal = new Vector3f(0, 0, 0);
    }
}
