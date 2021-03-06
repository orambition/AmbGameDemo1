package Core.Engine;
//场景灯光，在Renderer类中首次使用，
//将环境光、点光源、聚光灯、平行光全部汇集在此类中

import Core.Engine.graph.lights.DirectionalLight;
import Core.Engine.graph.lights.PointLight;
import Core.Engine.graph.lights.SpotLight;
import org.joml.Vector3f;

public class SceneLight {
    private Vector3f ambientLight;//环境光颜色RGB
    private Vector3f skyBoxLight;//天空盒环境光颜色
    private PointLight[] pointLightList;//点光源
    private SpotLight[] spotLightList;//聚光灯
    private DirectionalLight directionalLight;//平行光

    public Vector3f getAmbientLight() {
        return ambientLight;
    }
    public void setAmbientLight(Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }
    public PointLight[] getPointLightList() {
        return pointLightList;
    }
    public void setPointLightList(PointLight[] pointLightList) {
        this.pointLightList = pointLightList;
    }
    public SpotLight[] getSpotLightList() {
        return spotLightList;
    }
    public void setSpotLightList(SpotLight[] spotLightList) {
        this.spotLightList = spotLightList;
    }
    public DirectionalLight getDirectionalLight() {
        return directionalLight;
    }
    public void setDirectionalLight(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
    }

    public Vector3f getSkyBoxLight() {
        return skyBoxLight;
    }
    public void setSkyBoxLight(Vector3f skyBoxLight) {
        this.skyBoxLight = skyBoxLight;
    }
}
