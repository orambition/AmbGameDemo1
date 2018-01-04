package Core.Engine.graph;
/*渲染类
* 用于渲染画面*/

import Core.Engine.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    //弧度视野
    //Field of view（FOV）
    private static final float FOV = (float) Math.toRadians(60.0f);
    //近平面距离
    private static final float Z_NEAR = 0.01f;
    //远平面距离
    private static final float Z_FAR = 1000.f;

    //着色器程序，场景和hud的
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
        setupSceneShader();
        setupHudShader();
    }
    //创建场景着色器
    public void setupSceneShader() throws Exception {
        //加载着色器文件
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/vertex.vs"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/fragment.fs"));
        sceneShaderProgram.link();

        //为世界和投影矩阵 创建 Uniforms,vertex.vs的main中没有使用该值则会报错
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("modelViewMatrix");
        sceneShaderProgram.createUniform("texture_sampler");
        // 创建材质Uniform
        sceneShaderProgram.createMaterialUniform("material");
        // 创建镜面反射率、环境光uniform
        sceneShaderProgram.createUniform("specularPower");
        sceneShaderProgram.createUniform("ambientLight");
        // 创建点光源数组、聚光灯数组、平行光源uniform
        sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        sceneShaderProgram.createDirectionalLightUniform("directionalLight");
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
    }
    //清屏函数
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//清屏
    }

    //渲染函数
    public void render(Window window, Camera camera, GameItem[] gameItems, SceneLight sceneLight, IHud hud) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWindowWidth(), window.getWindowHeight());
            window.setResized(false);
        }

        renderScene(window, camera, gameItems, sceneLight);
        //绘制HUD
        renderHud(window, hud);
    }
    //绘制场景
    public void renderScene(Window window, Camera camera, GameItem[] gameItems, SceneLight sceneLight) {
        sceneShaderProgram.bind();

        //投影矩阵，将三维坐标投影到二位屏幕上
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWindowWidth(), window.getWindowHeight(), Z_NEAR, Z_FAR);
        sceneShaderProgram.setUniform("projectionMatrix",projectionMatrix);

        // 视野矩阵,根据当前摄像机的位置和角度得到该矩阵，用于修正物体的显示坐标
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        //设置环境光、点光源数组、聚光灯数组、平行光和强度
        renderLights(viewMatrix, sceneLight);

        //设置纹理单元，为显存中的0号纹理单元，将纹理放入0号单元的操作有Mesh完成
        sceneShaderProgram.setUniform("texture_sampler", 0);

        //绘制每一个gameItem
        for (GameItem gameItem : gameItems){
            Mesh mesh = gameItem.getMesh();
            //设置该物体的模型视野矩阵；将物体的位置矩阵与视野矩阵相乘，因为视野会影响物体的显示坐标，所以需要进行改处理
            Matrix4f modelViewMatrix  = transformation.getModelViewMatrix(gameItem,viewMatrix);
            sceneShaderProgram.setUniform("modelViewMatrix",modelViewMatrix);
            //设置该物体的材质
            sceneShaderProgram.setUniform("material", mesh.getMaterial());
            //绘制该物体的mesh
            mesh.render();
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
        //正向矩阵，用来随窗口变化而变化，保证比例
        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, window.getWindowWidth(), window.getWindowHeight(), 0);
        for (GameItem gameItem : hud.getGameItems()) {
            Mesh mesh = gameItem.getMesh();
            // hud的投影矩阵
            Matrix4f projModelMatrix = transformation.getOrtoProjModelMatrix(gameItem, ortho);
            hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
            hudShaderProgram.setUniform("colour", gameItem.getMesh().getMaterial().getAmbientColour());
            // Render the mesh for this HUD item
            mesh.render();
        }
        hudShaderProgram.unbind();
    }
    //
    public void cleanUp(){
        if (sceneShaderProgram != null)
            sceneShaderProgram.cleanUp();

        if (hudShaderProgram != null)
            hudShaderProgram.cleanUp();
    }
}
