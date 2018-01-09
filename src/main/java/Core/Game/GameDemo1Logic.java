package Core.Game;

import Core.Engine.*;
import Core.Engine.graph.*;
import Core.Engine.graph.lights.DirectionalLight;
import Core.Engine.items.GameItem;
import Core.Engine.items.SkyBox;
import Core.Engine.items.Terrain;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class GameDemo1Logic implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;//鼠标敏感度
    private static final float CAMERA_POS_STEP = 0.05f;//视角移动步长

    private final Camera camera;//摄像机，视野
    private final Vector3f cameraInc;//视野移动变量
    private final Renderer renderer;//渲染器

    private Scene scene;//场景，包含物体和场景灯光，场景光，包含环境光、点光源数组、聚光灯光源数组、平行光源

    private Hud hud;

    private float lightAngle;//平行光角度、方向

    private Terrain terrain;//地形

    public GameDemo1Logic(){
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0, 0, 0);
        lightAngle = -90;
    }

    @Override
    public void init(Window window) throws Exception {
        //初始化渲染
        renderer.init(window);
        //创建场景
        scene = new Scene();

        //创建地形
        float terrainScale = 10;//地形的缩放
        int terrainSize = 3;
        float minY = -0.1f;
        float maxY = 0.01f;
        int textInc = 1;
        terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "/textures/texture1.png", "/textures/texture1.png", textInc);
        scene.setGameItems(terrain.getGameItems());

        // 初始化天空盒
        float skyBoxScale = 30.0f;//天空盒的缩放
        SkyBox skyBox = new SkyBox("/models/skybox.obj","/textures/skybox.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // 初始化光
        setupLights();

        //创建hud
        hud = new Hud("DEMO");

        //camera.setPosition(0,10,0);
        //设置相机的位置
        camera.getPosition().x = 0f;
        camera.getPosition().y = 0f;
        camera.getPosition().z = 0f;
    }
    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);
        // 环境光
        sceneLight.setAmbientLight(new Vector3f(1.0f, 1.0f, 1.0f));
        // 平行光
        float lightIntensity = 1.0f;
        Vector3f lightPosition = new Vector3f(-1, 0, 0);
        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));
    }
    @Override
    public void input(Window window , MouseInput mouseInput) {
        cameraInc.set(0,0,0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)){
            cameraInc.y = 5;
        } else if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)){
            cameraInc.y = -0.01f;
        }
    }

    @Override
    public void update(float interval,MouseInput mouseInput) {
        // 更改相机的角度，根据鼠标
        if (mouseInput.isLeftButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            //hud更新，罗盘的旋转
            hud.rotateCompass(camera.getRotation().y);
        }
        // 更改相机的位置
        //保存现在的位置，然后移动，如果碰撞了就恢复现在的位置。
        //
        Vector3f prevPos = new Vector3f(camera.getPosition());
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);
        //检测碰撞
        float height = terrain.getHeight(camera.getPosition())+0.1f;
        if ( camera.getPosition().y < height )  {
            camera.setPosition(prevPos.x, height, prevPos.z);
        }else if ( camera.getPosition().y > height )  {
            camera.movePosition(0,-2*CAMERA_POS_STEP,0);
        }
        //更新场景灯光
        SceneLight sceneLight = scene.getSceneLight();
        //更新平行光的角度，模拟太阳
        DirectionalLight directionalLight = sceneLight.getDirectionalLight();
        lightAngle += 0.1f;
        if (lightAngle > 90) {//落山
            directionalLight.setIntensity(0);
            if (lightAngle >= 270) {
                lightAngle = -90;
            }
            //更新环境光，控制亮度
            sceneLight.getAmbientLight().set(0.3f, 0.3f, 0.4f);
        } else if (lightAngle <= -80 || lightAngle >= 80) {//日出日落
            float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
            sceneLight.getAmbientLight().set(factor, factor, factor);
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            sceneLight.getAmbientLight().set(1, 1, 1);
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void render(Window window) {
        hud.updateSize(window);
        renderer.render(window,camera,scene,hud);
    }

    @Override
    public void cleanup() {
        renderer.cleanUp();
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            mesh.cleanUp();
        }
        hud.cleanUp();
    }
}
