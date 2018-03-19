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
    public static Quaternionf calculateQuaternion(Vector3f vec) {
        return calculateQuaternion(vec.x, vec.y, vec. z);
    }
    //根据三维坐标生产4元数，主要用于生成方向的w值
    public static Quaternionf calculateQuaternion(float x, float y, float z) {
        Quaternionf orientation = new Quaternionf(x, y, z, 0);
        //此处涉及四元数和转换矩阵的计算，暂时不明白，不知道xyz是旋转角度还是方向向量
        float temp = 1.0f - (orientation.x * orientation.x) - (orientation.y * orientation.y) - (orientation.z * orientation.z);
        if (temp < 0.0f) {
            orientation.w = 0.0f;//方向
        } else {
            orientation.w = -(float) (Math.sqrt(temp));//与旋转不能表示180°有关？
        }
        return orientation;

    }

}
