package Core.Game;

import Core.Engine.*;
import Core.Engine.graph.*;
import Core.Engine.graph.lights.DirectionalLight;
import Core.Engine.graph.particles.FlowParticleEmitter;
import Core.Engine.graph.particles.Particle;
import Core.Engine.graph.weather.Fog;
import Core.Engine.items.GameItem;
import Core.Engine.items.SkyBox;
import Core.Engine.items.Terrain;
import Core.Engine.loaders.obj.OBJLoader;
import Core.Engine.sound.SoundBuffer;
import Core.Engine.sound.SoundListener;
import Core.Engine.sound.SoundManager;
import Core.Engine.sound.SoundSource;
import de.matthiasmann.twl.utils.PNGDecoder;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.openal.AL11;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class GameDemo1Logic implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;//鼠标敏感度
    private static float CAMERA_POS_STEP = 0.05f;//视角移动步长

    private final Renderer renderer;//渲染器

    private final SoundManager soundMgr;//声音管理

    private final Camera camera;//摄像机，视野

    private Scene scene;//场景，包含物体和场景灯光，场景光，包含环境光、点光源数组、聚光灯光源数组、平行光源

    private Hud hud;

    private enum Sounds { MUSIC, BEEP, FIRE };//声音的名字

    private Terrain terrain;//地形

    private FlowParticleEmitter particleEmitter;//粒子发生器
    //private CameraBoxSelectionDetector selectDetector;//选中检测
    private MouseBoxSelectionDetector selectDetector;//选中检测

    private float lightAngle;//平行光角度、方向
    private final Vector3f cameraInc;//视野移动变量
    private final Vector3f cameraros;//
    private boolean leftButtonPressed;
    private GameItem[] gameItems;

    public GameDemo1Logic(){
        renderer = new Renderer();
        hud = new Hud();
        soundMgr = new SoundManager();
        camera = new Camera();

        cameraInc = new Vector3f(0, 0, 0);
        cameraros = new Vector3f(0,0,0);
        lightAngle = 90;
    }

    @Override
    public void init(Window window) throws Exception {
        //初始化HUD，场景渲染使用hud，所以先渲染
        hud.init(window);
        //初始化渲染
        renderer.init(window);
        //初始化声音
        soundMgr.init();
        //创建场景
        scene = new Scene();
        //selectDetector = new CameraBoxSelectionDetector();
        selectDetector = new MouseBoxSelectionDetector();
        leftButtonPressed = false;
        /*//创建地形
        float terrainScale = 1;//地形的缩放
        int terrainSize = 5;//地形快的平铺行列数
        float minY = -1f;
        float maxY = 1f;
        int textInc = 1;//地形材质的缩小倍数
        terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "/textures/map_height.png", "/textures/map_texture.png", textInc);
        GameItem[] temp = terrain.getGameItems();*/

        /**示例代码 - 开始*/
        float reflectance = 1f;
        float blockScale = 0.5f;
        float skyBoxScale = 64.0f;
        float extension = 1.0f;
        float startx = extension * (-skyBoxScale + blockScale);
        float startz = extension * (skyBoxScale - blockScale);
        float starty = -1.0f;
        float inc = blockScale * 2;
        float posx = startx;
        float posz = startz;
        float incy = 0.0f;
        PNGDecoder decoder = new PNGDecoder(getClass().getResourceAsStream("/textures/map_height.png"));
        int height = decoder.getHeight();
        int width = decoder.getWidth();
        ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
        decoder.decode(buf, width * 4, PNGDecoder.Format.RGBA);
        buf.flip();
        int instances = height * width;
        Mesh mesh = OBJLoader.loadMesh("/models/cube.obj",instances);
        //设置包围盒半径
        mesh.setBoundingRadius(1);

        Texture texture = new Texture("/textures/texture1.png");
        Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);
        gameItems = new GameItem[instances];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                GameItem gameItem = new GameItem(mesh);
                gameItem.setScale(blockScale);
                int rgb = HeightMapMesh.getRGB(i, j, width, buf);
                incy = rgb/10;
                gameItem.setPosition(posx, starty+incy, posz);
                //int textPos = Math.random() > 0.5f ? 0 : 1;
                //gameItem.setTextPos(textPos);
                gameItems[i * width + j] = gameItem;
                posx += inc;
            }
            posx = startx;
            posz -= inc;
        }
        /*//创建物体 方块
        Mesh quadMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture texture = new Texture("/textures/texture1.png");
        Texture normalMap = new Texture("/textures/texture1_NORM.png");
        Material quadMaterial1 = new Material(texture,10f);
        quadMaterial1.setNormalMap(normalMap);
        quadMesh.setMaterial(quadMaterial1);
        quadGameItem1 = new GameItem(quadMesh);
        quadGameItem1.setPosition(0f, 0f, 0f);
        quadGameItem1.setScale(0.5f);*/


        /*//加载md5
        MD5Model md5Model = MD5Model.parse("/models/test.md5mesh");
        MD5AnimModel md5AnimModel = MD5AnimModel.parse("/models/test.md5anim");
        demo4GameItem = MD5Loader.process(md5Model,md5AnimModel,new Vector4f(1,1,1,1));
        demo4GameItem.setScale(0.2f);
        demo4GameItem.setPosition(demoItemX, demoItemY,demoItemZ);
        demo4GameItem.setRotation(new Quaternionf(90f,0f,-90f,0));

        //加载物体
        GameItem[] gameItems = new GameItem[temp.length+2];
        System.arraycopy(temp,0,gameItems,0,temp.length);
        gameItems[temp.length] = quadGameItem1;
        gameItems[temp.length+1] = demo4GameItem;*/

        /**示例代码 - 结束*/
        scene.setGameItems(gameItems);
        //添加粒子
        Vector3f particleSpeed = new Vector3f(0,1,0);
        particleSpeed.mul(2.5f);
        long ttl = 2000;
        int maxParticles = 100;
        long creationPeriodMillis = 300;
        float range = 0.2f;
        Mesh partMesh = OBJLoader.loadMesh("/models/particle.obj",maxParticles);
        Texture partTexture = new Texture("/textures/particle_anim.png",4,4);
        Material partMaterial = new Material(partTexture,10f);
        partMesh.setMaterial(partMaterial);
        Particle particle = new Particle(partMesh,particleSpeed,ttl,80);
        particle.setScale(1f);
        particleEmitter = new FlowParticleEmitter(particle,maxParticles,creationPeriodMillis);
        particleEmitter.setActive(true);
        particleEmitter.setPositionRndRange(range);
        particleEmitter.setSpeedRndRange(range);
        particleEmitter.setAnimRange(10);
        scene.setParticleEmitters(new FlowParticleEmitter[] {particleEmitter});

        //渲染阴影
        scene.setRenderShadows(true);
        //开启雾
        scene.setFog(new Fog(true, new Vector3f(0.3f, 0.35f, 0.5f), 0.011f));

        // 初始化天空盒
        //float skyBoxScale = 60.0f;//天空盒的缩放
        //SkyBox skyBox = new SkyBox("/models/skybox.obj","/textures/skybox1.png");
        SkyBox skyBox = new SkyBox("/models/skybox.obj", new Vector4f(0.6f, 0.7f, 1.0f, 1.0f));
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // 初始化光
        setupLights();

        //创建hud
        //hud = new Hud("DEMO");
        //camera.setPosition(0,10,0);
        //设置相机的位置
        camera.getPosition().x = 0f;
        camera.getPosition().y = 1f;
        camera.getPosition().z = 0f;

        // 声音
        soundMgr.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        setupSounds();
    }
    //设置灯光
    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);
        // 环境光
        sceneLight.setAmbientLight(new Vector3f(1.0f, 1.0f, 1.0f));
        // 天空盒环境光
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));
        // 平行光
        float lightIntensity = 1f;
        //光的方向与物体不同，该方向是向量值，其它方向是轴的旋转角度
        Vector3f lightDirection  = new Vector3f(0f, 1f, 0f);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMult(15);
        directionalLight.setOrthoCords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
        sceneLight.setDirectionalLight(directionalLight);
    }
    //设置声音
    private void setupSounds() throws Exception {
        SoundBuffer buffBack = new SoundBuffer("/sounds/231976__diboz__3am-in-a-deserted-spaceport.ogg");//加载声音
        soundMgr.addSoundBuffer(buffBack);//向管理器添加声音
        SoundSource sourceBack = new SoundSource(true, true);//创建背景音源，循环，不衰减
        sourceBack.setBuffer(buffBack.getBufferId());
        soundMgr.addSoundSource(Sounds.MUSIC.toString(), sourceBack);//向管理器添加音源
        sourceBack.play();

        SoundBuffer buffBeep = new SoundBuffer("/sounds/beep.ogg");
        soundMgr.addSoundBuffer(buffBeep);
        SoundSource sourceBeep = new SoundSource(false, true);
        sourceBeep.setBuffer(buffBeep.getBufferId());
        soundMgr.addSoundSource(Sounds.BEEP.toString(), sourceBeep);

        SoundBuffer buffFire = new SoundBuffer("/sounds/fire.ogg");//火焰的声音
        soundMgr.addSoundBuffer(buffFire);
        SoundSource sourceFire = new SoundSource(true, false);
        Vector3f pos = particleEmitter.getBaseParticle().getPosition();//获取火焰粒子发生器的位置
        sourceFire.setPosition(pos);
        sourceFire.setBuffer(buffFire.getBufferId());
        sourceFire.setGain(10f);
        soundMgr.addSoundSource(Sounds.FIRE.toString(), sourceFire);
        sourceFire.play();
        //创建听众，听众位置会通过updateListenerPosition自动设置为相机位置
        soundMgr.setListener(new SoundListener(new Vector3f(0, 0, 0)));

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
            //demoItemZ -= 0.1;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            //demoItemZ += 0.1;
            soundMgr.playSoundSource(Sounds.BEEP.toString());
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            //demoItemX -= 0.1;
            soundMgr.playSoundSource(Sounds.BEEP.toString());
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            //demoItemX += 0.1;
            soundMgr.playSoundSource(Sounds.BEEP.toString());
        }
        if (window.isKeyPressed(GLFW_KEY_ENTER) ) {
            //demo4GameItem.nextFrame();
        }
    }

    @Override
    public void update(float interval,MouseInput mouseInput,Window window) {
        // 更改相机的角度，根据鼠标
        if (mouseInput.isLeftButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, cameraros.z);
            //hud更新，罗盘的旋转
            //hud.rotateCompass(camera.getRotation().y);
        }
        // 更改相机的位置
        //保存现在的位置，然后移动，如果碰撞了就恢复现在的位置。
        Vector3f prevPos = new Vector3f(camera.getPosition());
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);
        //检测碰撞
        float height = terrain != null ? terrain.getHeight(camera.getPosition())+0.1f:-Float.MAX_VALUE;
        if ( camera.getPosition().y < height )  {
            camera.setPosition(prevPos.x, height, prevPos.z);
        }else if ( camera.getPosition().y > height )  {
            //camera.movePosition(0,-2*CAMERA_POS_STEP,0);
        }
        // 更新视野矩阵
        camera.updateViewMatrix();
        // 更新听众位置
        soundMgr.updateListenerPosition(camera);
        // 更新选中目标
        //this.selectDetector.selectGameItem(gameItems,camera);
        boolean aux = mouseInput.isLeftButtonPressed();
        if (aux && !this.leftButtonPressed && this.selectDetector.selectGameItem(gameItems, window, mouseInput.getCurrentPos(), camera)) {
            this.hud.incCounter();//点击hud，计数
        }
        this.leftButtonPressed = aux;
        /*demo4GameItem.getPosition().x=demoItemX;
        demo4GameItem.getPosition().z=demoItemZ;*/
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
            sceneLight.getSkyBoxLight().set(0.3f, 0.3f, 0.4f);
        } else if (lightAngle <= 30 || lightAngle >= 150) {//日出日落
            sceneLight.getAmbientLight().set(0.5f, 0.5f, 0.5f);
            sceneLight.getSkyBoxLight().set(0.5f, 0.5f, 0.5f);
            directionalLight.setIntensity(0.5f);
            directionalLight.getColor().y = Math.max(0.5f, 0.9f);
            directionalLight.getColor().z = Math.max(0.5f, 0.5f);
        } else {
            sceneLight.getAmbientLight().set(1, 1, 1);
            sceneLight.getSkyBoxLight().set(1, 1, 1);
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle+=0.1);
        lightDirection.x = (float) Math.cos(angRad);
        lightDirection.y = (float) Math.sin(angRad);
        lightDirection.z = 0;
        lightDirection.normalize();
        //更新粒子
        particleEmitter.update((long)(interval*1000));
        //更新HUD
        //hud.setStatusText(String.valueOf(particleEmitter.getParticles().size()));
    }
    @Override
    public void render(Window window) {
        /*if (hud != null) {
            hud.updateSize(window);
        }*/
        renderer.render(window,camera,scene);
        hud.render(window);
    }
    @Override
    public void cleanUp() {
        renderer.cleanUp();
        soundMgr.cleanUp();
        scene.cleanUp();
        if (hud != null) {
            hud.cleanUp();
        }
    }
}
