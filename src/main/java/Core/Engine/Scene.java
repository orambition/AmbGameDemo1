package Core.Engine;
//场景类，用于设置场景中的物体、天空盒、灯光等信息
//就是对这些信息进行同一管理
public class Scene {
    private GameItem[] gameItems;
    private SkyBox skyBox;
    private SceneLight sceneLight;
    public GameItem[] getGameItems() {
        return gameItems;
    }
    public void setGameItems(GameItem[] gameItems) {
        this.gameItems = gameItems;
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
}
