package Core.Engine;

import Core.Engine.graph.Mesh;
import Core.Engine.graph.particles.IParticleEmitter;
import Core.Engine.graph.weather.Fog;
import Core.Engine.items.GameItem;
import Core.Engine.items.SkyBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//场景类，用于设置场景中的物体、天空盒、灯光等信息
//就是对这些信息进行同一管理
public class Scene {
    private Map<Mesh,List<GameItem>> meshMap;//根据mesh存储gameitem，目的是优化渲染过程，不用每次都加载相同的mesh
    private SkyBox skyBox;//天空盒
    private SceneLight sceneLight;//灯光
    private Fog fog;//雾
    private IParticleEmitter[] particleEmitters;//粒子
    public Scene(){
        meshMap = new HashMap();
        fog = Fog.NOFOG;//场景都有雾，但默认雾是不开启的，有是因为着色器需要这个参数
    }
    public Map<Mesh, List<GameItem>> getGameMeshes() {
        return meshMap;
    }
    public void setGameItems(GameItem[] gameItems) {
        int numGameItems = gameItems != null ? gameItems.length : 0;
        for (int i=0; i<numGameItems; i++) {
            GameItem gameItem = gameItems[i];
            Mesh mesh = gameItem.getMesh();
            List<GameItem> list = meshMap.get(mesh);
            if ( list == null ) {
                list = new ArrayList<>();
                meshMap.put(mesh, list);
            }
            list.add(gameItem);
        }
    }
    public SkyBox getSkyBox() {
        return skyBox;
    }
    public void setSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }
    public SceneLight getSceneLight() {
        return sceneLight;
    }
    public void setSceneLight(SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }
    public Fog getFog() {
        return fog;
    }
    public void setFog(Fog fog) {
        this.fog = fog;
    }

    public IParticleEmitter[] getParticleEmitters() {
        return particleEmitters;
    }
    public void setParticleEmitters(IParticleEmitter[] particleEmitters) {
        this.particleEmitters = particleEmitters;
    }
    public void cleanUp() {
        for (Mesh mesh : meshMap.keySet()) {
            mesh.cleanUp();
        }
        for (IParticleEmitter particleEmitter : particleEmitters) {
            particleEmitter.cleanUp();
        }
    }
}
