package Core.Engine.graph.lights;
//聚光灯类，就是把点光源加一个锥形限制
import org.joml.Vector3f;

public class SpotLight {
    private PointLight pointLight;
    private Vector3f coneDirection;//方向
    private float cutOff;//锥角？

    public SpotLight(PointLight pointLight, Vector3f coneDirection, float cutOffAngle) {
        this.pointLight = pointLight;
        this.coneDirection = coneDirection;
        setCutOffAngle(cutOffAngle);
    }
    public SpotLight(SpotLight spotLight) {
        this(new PointLight(spotLight.getPointLight()),
                new Vector3f(spotLight.getConeDirection()),
                0);
        setCutOff(spotLight.getCutOff());
    }
    public PointLight getPointLight() {
        return pointLight;
    }
    public void setPointLight(PointLight pointLight) {
        this.pointLight = pointLight;
    }
    public Vector3f getConeDirection() {
        return coneDirection;
    }
    public void setConeDirection(Vector3f coneDirection) {
        this.coneDirection = coneDirection;
    }
    public float getCutOff() {
        return cutOff;
    }
    public void setCutOff(float cutOff) {
        this.cutOff = cutOff;
    }
    public final void setCutOffAngle(float cutOffAngle) {
        this.setCutOff((float)Math.cos(Math.toRadians(cutOffAngle)));
    }
}
