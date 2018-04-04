package Core.Engine.graph.lights;
//平行光，类似太阳光
import org.joml.Vector3f;

public class DirectionalLight {
    private Vector3f color;
    private Vector3f direction;//方向，eg.方向(1,0,0,0)是右向左的光，从x轴的正半轴设想负半轴
    private float intensity;//强度

    public DirectionalLight(Vector3f color, Vector3f direction, float intensity) {
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;
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
}
