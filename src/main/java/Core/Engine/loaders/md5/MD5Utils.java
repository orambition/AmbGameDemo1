package Core.Engine.loaders.md5;
//md5模型加载的所需要的工具类
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MD5Utils {
    //匹配关节中的位置和方向
    public static final String FLOAT_REGEXP = "[+-]?\\d*\\.?\\d*";//匹配正负小数
    //匹配括号中有三个小数的值
    public static final String VECTOR3_REGEXP = "\\(\\s*(" + FLOAT_REGEXP + ")\\s*(" + FLOAT_REGEXP + ")\\s*(" + FLOAT_REGEXP + ")\\s*\\)";

    private MD5Utils() {

    }
    //根据三维坐标生产4元数
    public static Quaternionf calculateQuaternion(Vector3f vec) {
        //四元数类
        Quaternionf orientation = new Quaternionf(vec.x, vec.y, vec.z, 0);
        float temp = 1.0f - (orientation.x * orientation.x) - (orientation.y * orientation.y) - (orientation.z * orientation.z);
        if (temp < 0.0f) {
            orientation.w = 0.0f;
        } else {
            orientation.w = -(float) (Math.sqrt(temp));
        }
        return orientation;
    }
}
