package Core.Engine.graph.lights;
//平行光，类似太阳光
import org.joml.Vector3f;

public class DirectionalLight {
    private Vector3f color;
    private Vector3f direction;
    private float intensity;//强度

    private OrthoCoords orthoCords;//正交坐标，用于生产正交矩阵，以绘制该光源产生的阴影的深度图
    private float shadowPosMult;//绘制阴影时，计算光源位置

    public DirectionalLight(Vector3f color, Vector3f direction, float intensity) {
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;

        this.orthoCords = new OrthoCoords();
        this.shadowPosMult = 1;
    }

    public DirectionalLight(DirectionalLight light) {
        this(new Vector3f(light.getColor()), new Vector3f(light.getDirection()), light.getIntensity());
    }

    public Vector3f getColor() {
        return color;
    }
    public void setColor(Vector3f color) {
        this.color = color;
    }
    public Vector3f getDirection() {
        return direction;
    }
    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }
    public float getIntensity() {
        return intensity;
    }
    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
    public float getShadowPosMult() {
        return shadowPosMult;
    }
    public void setShadowPosMult(float shadowPosMult) {
        this.shadowPosMult = shadowPosMult;
    }
    public OrthoCoords getOrthoCoords(){
        return orthoCords;
    }
    public void setOrthoCords(float left, float right, float bottom, float top, float near, float far) {
        orthoCords.left = left;
        orthoCords.right = right;
        orthoCords.bottom = bottom;
        orthoCords.top = top;
        orthoCords.near = near;
        orthoCords.far = far;
    }
    //正交坐标内部类
    public static class OrthoCoords {
        public float left;
        public float right;
        public float bottom;
        public float top;
        public float near;
        public float far;
    }
}
