package Core.Engine.graph;
/*渲染类
* 用于渲染画面*/

import Core.Engine.*;
import Core.Engine.graph.anim.AnimGameItem;
import Core.Engine.graph.anim.AnimatedFrame;
import Core.Engine.graph.lights.DirectionalLight;
import Core.Engine.graph.lights.PointLight;
import Core.Engine.graph.lights.SpotLight;
import Core.Engine.items.GameItem;
import Core.Engine.items.SkyBox;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    //弧度视野
    //Field of view（FOV）
    private static final float FOV = (float) Math.toRadians(60.0f);
    //近平面距离
    private static final float Z_NEAR = 0.01f;
    //远平面距离
    private static final float Z_FAR = 1000.f;

    private ShadowMap shadowMap;//深度图

    //着色器程序，深度图、天空盒、场景和hud的
    private ShaderProgram depthShaderProgram;
    private ShaderProgram skyBoxShaderProgram;
    private ShaderProgram sceneShaderProgram;
    private ShaderProgram hudShaderProgram;

    //获取投影、视角等各种矩阵的工具类
    private final Transformation transformation;

    //镜面反射率
    private float specularPower;

    //光源数量
    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;

    public Renderer(){
        transformation = new Transformation();
        specularPower = 10f;
    }
    public void init(Window window) throws Exception {
        shadowMap = new ShadowMap();

        setupDepthShader();
        setupSkyBoxShader();
        setupSceneShader();
        setupHudShader();
    }
    //创建深度图着色器
    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource("/shaders/depth_vertex.vs"));
        depthShaderProgram.createFragmentShader(Utils.loadResource("/shaders/depth_fragment.fs"));
        depthShaderProgram.link();
        depthShaderProgram.createUniform("orthoProjectionMatrix");
        depthShaderProgram.createUniform("modelLightViewMatrix");
    }
    //创建天空盒着色器
    private void setupSkyBoxShader() throws Exception{
        skyBoxShaderProgram = new ShaderProgram();
        skyBoxShaderProgram.createVertexShader(Utils.loadResource("/shaders/skybox_vertex.vs"));
        skyBoxShaderProgram.createFragmentShader(Utils.loadResource("/shaders/skybox_fragment.fs"));
        skyBoxShaderProgram.link();

        skyBoxShaderProgram.createUniform("projectionMatrix");
        skyBoxShaderProgram.createUniform("modelViewMatrix");
        skyBoxShaderProgram.createUniform("texture_sampler");
        skyBoxShaderProgram.createUniform("ambientLight");

    }
    //创建场景着色器
    private void setupSceneShader() throws Exception {
        //加载着色器文件
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/scene_vertex.vs"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/scene_fragment.fs"));
        sceneShaderProgram.link();

        //为世界和投影矩阵 创建 Uniforms,vertex.vs的main中没有使用该值则会报错
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("modelViewMatrix");
        sceneShaderProgram.createUniform("texture_sampler");
        //创建法线纹理uniform
        sceneShaderProgram.createUniform("normalMap");
        // 创建材质Uniform
        sceneShaderProgram.createMaterialUniform("material");
        // 创建高光（镜面反射率）、环境光uniform
        sceneShaderProgram.createUniform("specularPower");
        sceneShaderProgram.createUniform("ambientLight");
        // 创建点光源数组、聚光灯数组、平行光源uniform
        sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        sceneShaderProgram.createDirectionalLightUniform("directionalLight");
        //创建雾uniform
        sceneShaderProgram.createFogUniform("fog");
        //创建阴影uniforms
        sceneShaderProgram.createUniform("shadowMap");
        sceneShaderProgram.createUniform("orthoProjectionMatrix");
        sceneShaderProgram.createUniform("modelLightViewMatrix");
        //创建关节信息uniform
        sceneShaderProgram.createUniform("jointsMatrix");
    }
    //创建hud着色器
    private void setupHudShader() throws Exception {
        hudShaderProgram = new ShaderProgram();
        hudShaderProgram.createVertexShader(Utils.loadResource("/shaders/hud_vertex.vs"));
        hudShaderProgram.createFragmentShader(Utils.loadResource("/shaders/hud_fragment.fs"));
        hudShaderProgram.link();
        //创建hud投影矩阵和颜色的uniform
        hudShaderProgram.createUniform("projModelMatrix");
        hudShaderProgram.createUniform("colour");
        hudShaderProgram.createUniform("hasTexture");
    }

    //清屏函数
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//清屏
    }

    //渲染函数
    public void render(Window window, Camera camera, Scene scene, IHud hud) {
        clear();
        //渲染深度图，在glViewport（）之前是因为，渲染深度图需要改变窗口大小为深度图纹理大小
        renderDepthMap(window, camera, scene);
        //if (window.isResized()) {
            glViewport(0, 0, window.getWindowWidth(), window.getWindowHeight());
            //window.setResized(false);
        //}
        //设置共享的投影矩阵和视野矩阵
        transformation.updateProjectionMatrix(FOV, window.getWindowWidth(), window.getWindowHeight(), Z_NEAR, Z_FAR);
        transformation.updateViewMatrix(camera);

        renderScene(window, camera, scene);

        renderSkyBox(window,camera,scene);
        //绘制HUD
        renderHud(window, hud);
    }
    //绘制深度图
    private void renderDepthMap(Window window, Camera camera, Scene scene) {
        //绑定深度图fbo
        glBindFramebuffer(GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
        // 设置视野为深度图纹理的大小
        glViewport(0, 0, ShadowMap.SHADOW_MAP_WIDTH, ShadowMap.SHADOW_MAP_HEIGHT);
        glClear(GL_DEPTH_BUFFER_BIT);//清楚深度缓存的内容
        //深度图着色器绑定
        depthShaderProgram.bind();
        DirectionalLight light = scene.getSceneLight().getDirectionalLight();//获取场景中的平行光
        Vector3f lightDirection = light.getDirection();//光的方向
        //转换平行光的方向，因为光的方向与物体方向不同，向量转为每轴旋转的角度
        float lightAngleX = 90;
        float lightAngleY = 0;
        float lightAngleZ = 90-(float)Math.toDegrees(Math.acos(lightDirection.x));
        //生产光源视野矩阵,生成平行光的位置，因为平光本身没有位置
        Matrix4f lightViewMatrix = transformation.updateLightViewMatrix(new Vector3f(lightDirection).mul(light.getShadowPosMult()), new Vector3f(lightAngleX,lightAngleY,lightAngleZ));
        DirectionalLight.OrthoCoords orthCoords = light.getOrthoCoords();//获取光源正交坐标
        //生产光源正交矩阵
        Matrix4f orthoProjMatrix = transformation.updateOrthoProjectionMatrix(orthCoords.left, orthCoords.right, orthCoords.bottom, orthCoords.top, orthCoords.near, orthCoords.far);
        depthShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
                        Matrix4f modelLightViewMatrix = transformation.buildModelViewMatrix(gameItem, lightViewMatrix);
                        depthShaderProgram.setUniform("modelLightViewMatrix", modelLightViewMatrix);
                        //如果是动画模型，则加载关键信息矩阵
                        if ( gameItem instanceof AnimGameItem) {
                            AnimGameItem animGameItem = (AnimGameItem)gameItem;
                            AnimatedFrame frame = animGameItem.getCurrentFrame();
                            sceneShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                        }
                    }
            );
        }
        //深度图着色器解绑
        depthShaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    //绘制天空盒
    private void renderSkyBox(Window window,Camera camera,Scene scene){
        skyBoxShaderProgram.bind();

        skyBoxShaderProgram.setUniform("texture_sampler",0);

        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        SkyBox skyBox = scene.getSkyBox();
        Matrix4f viewMatrix = transformation.getViewMatrix();
        //天空盒并不随着摄像机移动，所以将xyz的变化设置为0
        viewMatrix.m30 = 0;
        viewMatrix.m31 = 0;
        viewMatrix.m32 = 0;
        Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
        skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getAmbientLight());
        scene.getSkyBox().getMesh().render();
        skyBoxShaderProgram.unbind();
    }
    //绘制场景
    private void renderScene(Window window, Camera camera, Scene scene) {
        sceneShaderProgram.bind();

        //透视矩阵，将三维坐标投影到二位屏幕上
        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        sceneShaderProgram.setUniform("projectionMatrix",projectionMatrix);
        //正交矩阵，用于检测以光源为视野时物体的位置，以判断是否在阴影中
        Matrix4f orthoProjMatrix = transformation.getOrthoProjectionMatrix();
        sceneShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);

        //视野矩阵,根据当前摄像机的位置和角度得到该矩阵，用于修正物体的显示坐标
        Matrix4f viewMatrix = transformation.getViewMatrix();
        //光源视野矩阵
        Matrix4f lightViewMatrix = transformation.getLightViewMatrix();

        //设置环境光、点光源数组、聚光灯数组、平行光和强度
        renderLights(viewMatrix, scene.getSceneLight());

        //设置雾的uniform
        sceneShaderProgram.setUniform("fog", scene.getFog());

        //设置纹理单元，为显存中的0号纹理单元，将纹理放入0号单元的操作由Mesh建立时完成
        sceneShaderProgram.setUniform("texture_sampler", 0);
        //设置法线纹理单元，为显存中的1号法线纹理单元，将法线纹理放入1号单元的操作由Mesh建立时完成
        sceneShaderProgram.setUniform("normalMap", 1);
        //设置深度图单元，为显存中的2号，将深度图放入2号单元的操作在下面完成
        sceneShaderProgram.setUniform("shadowMap", 2);

        //绘制每一个gameItem
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            sceneShaderProgram.setUniform("material", mesh.getMaterial());

            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getId());

            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
                //模型*视野
                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(gameItem, viewMatrix);
                sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                //模型*光源视野
                Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(gameItem, lightViewMatrix);
                sceneShaderProgram.setUniform("modelLightViewMatrix", modelLightViewMatrix);
                //如果是动画模型，则加载关键信息矩阵
                if ( gameItem instanceof AnimGameItem) {
                    AnimGameItem animGameItem = (AnimGameItem)gameItem;
                    AnimatedFrame frame = animGameItem.getCurrentFrame();
                    sceneShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                }
            }

            );
        }
        sceneShaderProgram.unbind();
    }
    //绘制光的函数，就是把相应的uniform填冲上相应的值
    private void renderLights(Matrix4f viewMatrix, SceneLight sceneLight) {
        sceneShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
        sceneShaderProgram.setUniform("specularPower", specularPower);
        // 绘制点光源
        PointLight[] pointLightList = sceneLight.getPointLightList();
        int numLights = pointLightList != null ? pointLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the point light object and transform its position to view coordinates
            PointLight currPointLight = new PointLight(pointLightList[i]);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            sceneShaderProgram.setUniform("pointLights", currPointLight, i);
        }
        // 绘制聚光灯
        SpotLight[] spotLightList = sceneLight.getSpotLightList();
        numLights = spotLightList != null ? spotLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the spot light object and transform its position and cone direction to view coordinates
            SpotLight currSpotLight = new SpotLight(spotLightList[i]);
            Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));
            Vector3f lightPos = currSpotLight.getPointLight().getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            sceneShaderProgram.setUniform("spotLights", currSpotLight, i);
        }
        // 填充平行光
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);
    }
    //绘制hud的函数，填充hud相关uniform
    private void renderHud(Window window, IHud hud) {
        hudShaderProgram.bind();
        //正交矩阵
        Matrix4f ortho = transformation.getOrtho2DProjectionMatrix(0, window.getWindowWidth(), window.getWindowHeight(), 0);
        //Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWindowWidth(), window.getWindowHeight(), Z_NEAR, Z_FAR);

        for (GameItem gameItem : hud.getGameItems()) {
            Mesh mesh = gameItem.getMesh();
            // hud的投影矩阵
            Matrix4f projModelMatrix = transformation.buildOrtoProjModelMatrix(gameItem, ortho);
            //Matrix4f projModelMatrix = transformation.getWorldMatrix(gameItem,projectionMatrix);
            hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
            hudShaderProgram.setUniform("colour", gameItem.getMesh().getMaterial().getAmbientColour());
            hudShaderProgram.setUniform("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1 : 0);
            // Render the mesh for this HUD item
            mesh.render();
        }
        hudShaderProgram.unbind();
    }
    //
    public void cleanUp(){
        if (skyBoxShaderProgram != null) {
            skyBoxShaderProgram.cleanUp();
        }
        if (sceneShaderProgram != null)
            sceneShaderProgram.cleanUp();

        if (hudShaderProgram != null)
            hudShaderProgram.cleanUp();
    }
}
