package Core.Game;

import Core.Engine.*;
import Core.Engine.graph.*;
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
        //材质的反射率
        float reflectance = 1f;

        //创建方块物体的mesh
        Mesh mesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture texture = new Texture("/textures/texture1.png");
        Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);

        float blockScale = 0.25f;
        float skyBoxScale = 50.0f;
        float extension = 2.0f;

        float startx = extension * (-skyBoxScale + blockScale);
        float startz = extension * (skyBoxScale - blockScale);
        float starty = -1.0f;
        float inc = blockScale * 2;

        float posx = startx;
        float posz = startz;
        float incy = 0.0f;
        int NUM_ROWS = (int)(extension * skyBoxScale * 2 / inc);
        int NUM_COLS = (int)(extension * skyBoxScale * 2/ inc);
        GameItem[] gameItems  = new GameItem[NUM_ROWS * NUM_COLS];
        for(int i=0; i<NUM_ROWS; i++) {
            for(int j=0; j<NUM_COLS; j++) {
                GameItem gameItem = new GameItem(mesh);
                gameItem.setScale(blockScale);
                incy = Math.random() > 0.9f ? blockScale * 2 : 0f;
                gameItem.setPosition(posx, starty + incy, posz);
                gameItems[i*NUM_COLS + j] = gameItem;
                posx += inc;
            }
            posx = startx;
            posz -= inc;
        }
        scene.setGameItems(gameItems);

        // 初始化天空盒
        SkyBox skyBox = new SkyBox("/models/skybox.obj","/textures/skybox.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // 初始化光
        setupLights();

        //创建hud
        hud = new Hud("DEMO");

        //camera.setPosition(0,10,0);
        //设置相机的位置
        camera.getPosition().x = 0.65f;
        camera.getPosition().y = -0.1f;
        camera.getPosition().z = 4.34f;
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
            cameraInc.y = 1;
        } else if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)){
            cameraInc.y = -1;
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
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

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
        hud.updateSize(window,camera);
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
