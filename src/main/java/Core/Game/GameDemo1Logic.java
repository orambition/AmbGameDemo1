package Core.Game;

import Core.Engine.*;
import Core.Engine.graph.*;
import Core.Engine.graph.anim.AnimGameItem;
import Core.Engine.graph.lights.DirectionalLight;
import Core.Engine.graph.weather.Fog;
import Core.Engine.items.GameItem;
import Core.Engine.items.SkyBox;
import Core.Engine.items.Terrain;
import Core.Engine.loaders.md5.MD5AnimModel;
import Core.Engine.loaders.md5.MD5Loader;
import Core.Engine.loaders.md5.MD5Model;
import Core.Engine.loaders.obj.OBJLoader;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class GameDemo1Logic implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;//鼠标敏感度
    private static float CAMERA_POS_STEP = 0.05f;//视角移动步长

    private final Camera camera;//摄像机，视野

    private final Renderer renderer;//渲染器

    private Scene scene;//场景，包含物体和场景灯光，场景光，包含环境光、点光源数组、聚光灯光源数组、平行光源

    private Hud hud;

    private float lightAngle;//平行光角度、方向

    private Terrain terrain;//地形

    private final Vector3f cameraInc;//视野移动变量
    private final Vector3f cameraros;//
    private AnimGameItem demo4GameItem;//视野移动变量

    GameItem quadGameItem1;
    private float demoItemX = 0f;
    private float demoItemY = 1f;
    private float demoItemZ = 0;
    public GameDemo1Logic(){
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0, 0, 0);
        cameraros = new Vector3f(0,0,0);
        lightAngle = 90;
    }

    @Override
    public void init(Window window) throws Exception {
        //初始化渲染
        renderer.init(window);
        //创建场景
        scene = new Scene();

        //创建地形
        float terrainScale = 1;//地形的缩放
        int terrainSize = 5;//地形快的平铺行列数
        float minY = -1f;
        float maxY = 1f;
        int textInc = 1;//地形材质的缩小倍数
        terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "/textures/map_height.png", "/textures/map_texture.png", textInc);
        GameItem[] temp = terrain.getGameItems();

        /**示例代码 - 开始*/
        //创建物体 方块

        Mesh quadMesh1 = OBJLoader.loadMesh("/models/cube.obj");
        Texture texture = new Texture("/textures/texture1.png");
        Texture normalMap = new Texture("/textures/texture1_NORM.png");
        Material quadMaterial1 = new Material(texture,10f);
        quadMaterial1.setNormalMap(normalMap);
        quadMesh1.setMaterial(quadMaterial1);
        quadGameItem1 = new GameItem(quadMesh1);
        quadGameItem1.setPosition(0f, 0f, 0f);
        quadGameItem1.setScale(0.5f);

        //加载md5
        MD5Model md5Model = MD5Model.parse("/models/test.md5mesh");
        MD5AnimModel md5AnimModel = MD5AnimModel.parse("/models/test.md5anim");
        demo4GameItem = MD5Loader.process(md5Model,md5AnimModel,new Vector4f(1,1,1,1));
        demo4GameItem.setScale(0.2f);
        demo4GameItem.setPosition(demoItemX, demoItemY,demoItemZ);
        demo4GameItem.setRotation(90f,0f,-90f);

        /**示例代码 - 结束*/

        //加载物体
        GameItem[] gameItems = new GameItem[temp.length+2];
        System.arraycopy(temp,0,gameItems,0,temp.length);
        gameItems[temp.length] = quadGameItem1;
        gameItems[temp.length+1] = demo4GameItem;
        scene.setGameItems(gameItems);

        //开启雾
        scene.setFog(new Fog(true, new Vector3f(0.3f, 0.3f, 0.3f), 0.05f));

        // 初始化天空盒
        float skyBoxScale = 60.0f;//天空盒的缩放
        SkyBox skyBox = new SkyBox("/models/skybox.obj","/textures/skybox1.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // 初始化光
        setupLights();

        //创建hud
        hud = new Hud("DEMO");

        //camera.setPosition(0,10,0);
        //设置相机的位置
        camera.getPosition().x = 0f;
        camera.getPosition().y = 1f;
        camera.getPosition().z = 0f;
    }
    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);
        // 环境光
        sceneLight.setAmbientLight(new Vector3f(1.0f, 1.0f, 1.0f));
        // 平行光
        float lightIntensity = 1f;
        //光的方向与物体不同，该方向是向量值，其它方向是轴的旋转角度
        Vector3f lightDirection  = new Vector3f(0f, 1f, 0f);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMult(15);
        directionalLight.setOrthoCords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
        sceneLight.setDirectionalLight(directionalLight);
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
            cameraInc.y = -1f;
        }
        if(window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)){
            CAMERA_POS_STEP = 1f;
        }else {
            CAMERA_POS_STEP = 0.05f;
        }
        if (window.isKeyPressed(GLFW_KEY_Q)) {
            cameraros.z = 1f;
        } else if (window.isKeyPressed(GLFW_KEY_E)) {
            cameraros.z = -1f;
        }else {
            cameraros.z = 0;
        }

        if (window.isKeyPressed(GLFW_KEY_UP)) {
            demoItemZ -= 0.1;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            demoItemZ += 0.1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            demoItemX -= 0.1;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            demoItemX += 0.1;
        }
        if (window.isKeyPressed(GLFW_KEY_ENTER) ) {
            demo4GameItem.nextFrame();
        }
    }

    @Override
    public void update(float interval,MouseInput mouseInput) {
        // 更改相机的角度，根据鼠标
        if (mouseInput.isLeftButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, cameraros.z);
            //hud更新，罗盘的旋转
            hud.rotateCompass(camera.getRotation().y);
        }
        // 更改相机的位置
        //保存现在的位置，然后移动，如果碰撞了就恢复现在的位置。
        Vector3f prevPos = new Vector3f(camera.getPosition());
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);
        //检测碰撞
        float height = terrain.getHeight(camera.getPosition())+0.1f;
        if ( camera.getPosition().y < height )  {
            camera.setPosition(prevPos.x, height, prevPos.z);
        }else if ( camera.getPosition().y > height )  {
            //camera.movePosition(0,-2*CAMERA_POS_STEP,0);
        }
        demo4GameItem.getPosition().x=demoItemX;
        demo4GameItem.getPosition().y=demoItemY;
        demo4GameItem.getPosition().z=demoItemZ;
        quadGameItem1.getPosition().x=-demoItemX;
        //quadGameItem1.getPosition().y=-demoItemY;
        quadGameItem1.getPosition().z=-demoItemZ;
        //更新场景灯光
        SceneLight sceneLight = this.scene.getSceneLight();
        //更新平行光的角度，模拟太阳
        DirectionalLight directionalLight = sceneLight.getDirectionalLight();
        Vector3f lightDirection = directionalLight.getDirection();

        if (lightAngle > 180) {//落山
            directionalLight.setIntensity(0);
            if (lightAngle >= 360) {
                lightAngle = 0;
            }
            //更新环境光，控制亮度
            sceneLight.getAmbientLight().set(0.3f, 0.3f, 0.4f);
        } else if (lightAngle <= 30 || lightAngle >= 150) {//日出日落
            sceneLight.getAmbientLight().set(0.5f, 0.5f, 0.5f);
            directionalLight.setIntensity(0.5f);
            directionalLight.getColor().y = Math.max(0.5f, 0.9f);
            directionalLight.getColor().z = Math.max(0.5f, 0.5f);
        } else {
            sceneLight.getAmbientLight().set(1, 1, 1);
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle++);
        lightDirection.x = (float) Math.cos(angRad);
        lightDirection.y = (float) Math.sin(angRad);
        lightDirection.z = 0;
        lightDirection.normalize();
        //更新HUD
        //hud.setStatusText(String.valueOf(Math.toDegrees(Math.acos(lightDirection.x)))+":"+lightAngle+":"+lightDirection.x);
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
