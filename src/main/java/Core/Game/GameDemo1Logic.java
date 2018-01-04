package Core.Game;

import Core.Engine.GameItem;
import Core.Engine.IGameLogic;
import Core.Engine.MouseInput;
import Core.Engine.Window;
import Core.Engine.graph.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class GameDemo1Logic implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;//鼠标敏感度
    private static final float CAMERA_POS_STEP = 0.05f;//视角移动步长
    private final Vector3f cameraInc;//视野移动变量

    private final Camera camera;
    private final Renderer renderer;
    private GameItem[] gameItems;

    private Vector3f ambientLight;//环境光
    private PointLight[] pointLightList;//点光源数组
    private SpotLight[] spotLightList;//聚光灯光源数组
    private DirectionalLight directionalLight;//平行光源
    private float lightAngle;//平行光角度

    private float spotAngle = 0;
    private float spotInc = 1;

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
        //材质的反射率
        float reflectance = 1f;
        Mesh mesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture texture = new Texture("/textures/texture1.png");
        Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);
        GameItem gameItem1 = new GameItem(mesh);
        gameItem1.setScale(0.25f);
        gameItem1.setPosition(0, 0, -2);
        GameItem gameItem2 = new GameItem(mesh);
        gameItem2.setScale(0.25f);
        gameItem2.setPosition(0.5f, 0.5f, -2);
        GameItem gameItem3 = new GameItem(mesh);
        gameItem3.setScale(0.25f);
        gameItem3.setPosition(0, 0, -2.5f);
        GameItem gameItem4 = new GameItem(mesh);
        gameItem4.setScale(0.25f);
        gameItem4.setPosition(0.5f, 0, -2.5f);
        gameItems = new GameItem[]{gameItem1, gameItem2, gameItem3, gameItem4};
        /*Mesh mesh = OBJLoader.loadMesh("/models/Chess Set.obj");
        GameItem gameItem = new GameItem(mesh);
        gameItem.setScale(0.01f);
        gameItem.setRotation(90,0,0);
        gameItem.setPosition(0,0,-2);
        gameItems = new GameItem[]{gameItem};*/
        //初始环境光
        ambientLight = new Vector3f(0.3f, 0.3f, 0.3f);
        //初始化点光源
        Vector3f lightColour = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(1, 0, 1);
        float lightIntensity = 1.0f;
        PointLight pointLight = new PointLight(lightColour, lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);//衰减
        pointLightList = new PointLight[]{pointLight};
        //初始化聚光灯
        lightPosition = new Vector3f(0, 0.0f, 10f);
        pointLight = new PointLight(lightColour, lightPosition, lightIntensity);
        att = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
        pointLight.setAttenuation(att);
        Vector3f coneDir = new Vector3f(0, 0, -1);
        float cutoff = (float) Math.cos(Math.toRadians(140));
        SpotLight spotLight = new SpotLight(pointLight, coneDir, cutoff);
        spotLightList = new SpotLight[]{spotLight};
        //初始化平行光源
        lightPosition = new Vector3f(-1, 0, 0);
        lightColour = new Vector3f(1, 1, 1);
        directionalLight = new DirectionalLight(lightColour, lightPosition, lightIntensity);
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
        float lightPos = pointLightList[0].getPosition().y;
        float lightPos2 = spotLightList[0].getPointLight().getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            this.pointLightList[0].getPosition().y = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            this.pointLightList[0].getPosition().y = lightPos - 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            this.spotLightList[0].getPointLight().getPosition().z = lightPos2 + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            this.spotLightList[0].getPointLight().getPosition().z = lightPos2 - 0.1f;
        }
    }

    @Override
    public void update(float interval,MouseInput mouseInput) {
        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);
        // Update camera based on mouse
        if (mouseInput.isLeftButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        // 更新聚光灯
        spotAngle += spotInc * 0.05f;
        if (spotAngle > 2) {
            spotInc = -1;
        } else if (spotAngle < -2) {
            spotInc = 1;
        }
        double spotAngleRad = Math.toRadians(spotAngle);
        Vector3f coneDir = spotLightList[0].getConeDirection();
        coneDir.x = (float) Math.sin(spotAngleRad);

        //更新平行光的角度，模拟太阳
        lightAngle += 1.1f;
        if (lightAngle > 90) {//落山
            directionalLight.setIntensity(0);
            if (lightAngle >= 270) {
                lightAngle = -90;
            }
        } else if (lightAngle <= -80 || lightAngle >= 80) {//日出日落
            float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
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
        renderer.render(window,camera,gameItems,ambientLight,pointLightList, spotLightList,directionalLight);
    }

    @Override
    public void cleanup() {
        renderer.clearUp();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }
}
