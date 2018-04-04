package Core.Engine.graph;
/*渲染类
* 用于渲染画面*/

import Core.Engine.Scene;
import Core.Engine.SceneLight;
import Core.Engine.Utils;
import Core.Engine.Window;
import Core.Engine.graph.anim.AnimGameItem;
import Core.Engine.graph.anim.AnimatedFrame;
import Core.Engine.graph.lights.DirectionalLight;
import Core.Engine.graph.lights.PointLight;
import Core.Engine.graph.lights.SpotLight;
import Core.Engine.graph.particles.IParticleEmitter;
import Core.Engine.graph.shadow.ShadowCascade;
import Core.Engine.graph.shadow.ShadowRenderer;
import Core.Engine.items.GameItem;
import Core.Engine.items.SkyBox;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;

public class Renderer {
    //光源数量
    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;
    //获取投影、视角等各种矩阵的工具类
    private final Transformation transformation;
    //着色器程序，天空盒、场景、粒子的
    private ShaderProgram skyBoxShaderProgram;
    private ShaderProgram sceneShaderProgram;
    private ShaderProgram particlesShaderProgram;
    //阴影独立渲染，由ShadowRenderer完成，HUD也单独在主循环中渲染，由NanoVg完成
    private final ShadowRenderer shadowRenderer;
    //视野锥裁减检测类
    private final FrustumCullingFilter frustumFilter;
    //视野锥裁减后剩余的对象
    private final List<GameItem> filteredItems;
    //镜面反射率
    private float specularPower;

    public Renderer(){
        transformation = new Transformation();
        specularPower = 10f;
        shadowRenderer = new ShadowRenderer();
        frustumFilter = new FrustumCullingFilter();
        filteredItems = new ArrayList<>();
    }
    //初始化
    public void init(Window window) throws Exception {
        shadowRenderer.init(window);
        setupSkyBoxShader();
        setupSceneShader();
        setupParticlesShader();
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
        skyBoxShaderProgram.createUniform("colour");
        skyBoxShaderProgram.createUniform("hasTexture");
    }
    //创建粒子着色器
    private void setupParticlesShader() throws Exception {
        particlesShaderProgram = new ShaderProgram();
        particlesShaderProgram.createVertexShader(Utils.loadResource("/shaders/particles_vertex.vs"));
        particlesShaderProgram.createFragmentShader(Utils.loadResource("/shaders/particles_fragment.fs"));
        particlesShaderProgram.link();
        particlesShaderProgram.createUniform("viewMatrix");
        particlesShaderProgram.createUniform("projectionMatrix");
        particlesShaderProgram.createUniform("texture_sampler");

        particlesShaderProgram.createUniform("numCols");
        particlesShaderProgram.createUniform("numRows");
    }
    //创建场景着色器
    private void setupSceneShader() throws Exception {
        //加载着色器文件
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/scene_vertex.vs"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/scene_fragment.fs"));
        sceneShaderProgram.link();
        //为视野矩阵和投影矩阵 创建 Uniforms,vertex.vs的main中没有使用该值则会报错
        sceneShaderProgram.createUniform("viewMatrix");
        sceneShaderProgram.createUniform("projectionMatrix");
        //创建纹理uniform
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

        //是否渲染阴影的uniforms
        sceneShaderProgram.createUniform("renderShadow");
        //创建阴影纹理uniforms，阴影采用CMS分层渲染得出，所以有多个
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            sceneShaderProgram.createUniform("shadowMap_" + i);
        }
        //创建每层阴影的正交投影矩阵uniforms
        sceneShaderProgram.createUniform("orthoProjectionMatrix", ShadowRenderer.NUM_CASCADES);
        //创建每层阴影的光视野矩阵uniforms
        sceneShaderProgram.createUniform("lightViewMatrix", ShadowRenderer.NUM_CASCADES);
        //创建每层阴影的视野锥远平面距离uniforms，用于判断物体顶点在哪个层中（深度图中）
        sceneShaderProgram.createUniform("cascadeFarPlanes", ShadowRenderer.NUM_CASCADES);
        //模型矩阵
        sceneShaderProgram.createUniform("modelNonInstancedMatrix");
        //创建关节相对位置信息uniform
        sceneShaderProgram.createUniform("jointsMatrix");
        //是否是实例化（分组）渲染对象
        sceneShaderProgram.createUniform("isInstanced");
        //纹理的分割的行列数
        sceneShaderProgram.createUniform("numCols");
        sceneShaderProgram.createUniform("numRows");
        //是否被选择的uniform
        sceneShaderProgram.createUniform("selectedNonInstanced");
    }
    //清屏函数
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);//清屏
    }
    //渲染函数
    public void render(Window window, Camera camera, Scene scene, boolean sceneChanged) {
        clear();
        //根据视野锥裁减对象
        if (window.getWindowOptions().frustumCulling) {
            frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
            frustumFilter.filter(scene.getGameMeshes());
            frustumFilter.filter(scene.getGameInstancedMeshes());
        }
        // 当视野变化时重新渲染深度图，在glViewport（）之前是因为，渲染深度图需要改变窗口大小为深度图纹理大小
        if (scene.isRenderShadows() && sceneChanged) {
            shadowRenderer.render(window, scene, camera, transformation);
        }
        //重新设置窗口大小
        glViewport(0, 0, window.getWindowWidth(), window.getWindowHeight());
        //设置共享的投影矩阵和视野矩阵
        //transformation.updateViewMatrix(camera);//视野更新已放入logic的update中
        window.updateProjectionMatrix();//没必要每次都计算，建议放入init中

        renderScene(window, camera, scene);
        renderSkyBox(window,camera,scene);
        renderParticles(window, camera, scene);
        //渲染准星
        renderCrossHair(window);
    }
    //绘制场景
    private void renderScene(Window window, Camera camera, Scene scene) {
        sceneShaderProgram.bind();

        //透视矩阵，将三维坐标投影到二位屏幕上
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        sceneShaderProgram.setUniform("projectionMatrix",projectionMatrix);
        //视野矩阵
        Matrix4f viewMatrix = camera.getViewMatrix();
        sceneShaderProgram.setUniform("viewMatrix", viewMatrix);
        //各个深度图层的光视野、正交矩阵
        List<ShadowCascade> shadowCascades = shadowRenderer.getShadowCascades();
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);
            sceneShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i);
            sceneShaderProgram.setUniform("cascadeFarPlanes", ShadowRenderer.CASCADE_SPLITS[i], i);
            sceneShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix(), i);
        }
        //设置环境光、点光源数组、聚光灯数组、平行光和强度
        renderLights(viewMatrix, scene.getSceneLight());
        //设置雾的uniform
        sceneShaderProgram.setUniform("fog", scene.getFog());
        //设置纹理单元，为显存中的0号纹理单元，将纹理放入0号单元的操作由Mesh建立时完成
        sceneShaderProgram.setUniform("texture_sampler", 0);
        //设置法线纹理单元，为显存中的1号法线纹理单元，将法线纹理放入1号单元的操作由Mesh建立时完成
        sceneShaderProgram.setUniform("normalMap", 1);
        //设置层次深度图，为显存中的2、3、4、5号单元，将深度图放入2、3、4、5号单元的操作在下面完成
        int start = 2;
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            sceneShaderProgram.setUniform("shadowMap_" + i, start + i);
        }
        //设置是否渲染阴影
        sceneShaderProgram.setUniform("renderShadow", scene.isRenderShadows() ? 1 : 0);

        renderNonInstancedMeshes(scene);

        renderInstancedMeshes(scene, viewMatrix);

        sceneShaderProgram.unbind();
    }
    //单数绘制函数
    private void renderNonInstancedMeshes(Scene scene) {
        sceneShaderProgram.setUniform("isInstanced", 0);
        // Render each mesh with the associated game Items
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            //材质
            sceneShaderProgram.setUniform("material", mesh.getMaterial());

            Texture text = mesh.getMaterial().getTexture();
            if (text != null) {//如果存在纹理则设置纹理行列数
                sceneShaderProgram.setUniform("numCols", text.getNumCols());
                sceneShaderProgram.setUniform("numRows", text.getNumRows());
            }
            //绑定并激活GL_TEXTURE2后的三个纹理
            shadowRenderer.bindTextures(GL_TEXTURE2);

            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
                //设置是否被选中uniform
                sceneShaderProgram.setUniform("selectedNonInstanced", gameItem.isSelected() ? 1.0f : 0.0f);
                //模型矩阵
                Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
                sceneShaderProgram.setUniform("modelNonInstancedMatrix", modelMatrix);
                //如果是动画模型，则加载关键信息矩阵
                if (gameItem instanceof AnimGameItem) {
                    AnimGameItem animGameItem = (AnimGameItem) gameItem;
                    AnimatedFrame frame = animGameItem.getCurrentFrame();
                    sceneShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                }
            });
        }
    }
    //实例化（分组）绘制函数
    private void renderInstancedMeshes(Scene scene, Matrix4f viewMatrix) {
        sceneShaderProgram.setUniform("isInstanced", 1);
        // Render each mesh with the associated game Items
        Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
        for (InstancedMesh mesh : mapMeshes.keySet()) {
            Texture text = mesh.getMaterial().getTexture();
            if (text != null) {//如果存在纹理则设置纹理行列数
                sceneShaderProgram.setUniform("numCols", text.getNumCols());
                sceneShaderProgram.setUniform("numRows", text.getNumRows());
            }
            sceneShaderProgram.setUniform("material", mesh.getMaterial());

            filteredItems.clear();
            for (GameItem gameItem : mapMeshes.get(mesh)) {
                if (gameItem.isInsideFrustum()) {
                    filteredItems.add(gameItem);
                }
            }
            /*glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getId());*/
            shadowRenderer.bindTextures(GL_TEXTURE2);//分层阴影是多个纹理，所以使用该函数激活绑定多个纹理
            //此处可以不传视野矩阵
            mesh.renderListInstanced(filteredItems, transformation, null);
        }
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
    //绘制粒子
    private void renderParticles(Window window, Camera camera, Scene scene) {
        particlesShaderProgram.bind();
        Matrix4f viewMatrix = camera.getViewMatrix();
        particlesShaderProgram.setUniform("viewMatrix", viewMatrix);
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        particlesShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        particlesShaderProgram.setUniform("texture_sampler", 0);

        IParticleEmitter[] emitters = scene.getParticleEmitters();
        int numEmitters = emitters != null ? emitters.length : 0;

        glDepthMask(false);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);//粒子的颜色混合

        for (int i = 0; i < numEmitters; i++) {
            IParticleEmitter emitter = emitters[i];
            InstancedMesh mesh = (InstancedMesh)emitter.getBaseParticle().getMesh();
            Texture text = mesh.getMaterial().getTexture();
            particlesShaderProgram.setUniform("numCols", text.getNumCols());
            particlesShaderProgram.setUniform("numRows", text.getNumRows());
            //billBorad=true为粒子渲染，此处传视野矩阵，是因为粒子不随视野旋转，需要先将其乘以视野矩阵的逆
            mesh.renderListInstanced(emitter.getParticles(), true, transformation, viewMatrix);
        }
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(true);
        particlesShaderProgram.unbind();
    }
    //绘制天空盒
    private void renderSkyBox(Window window,Camera camera,Scene scene){
        SkyBox skyBox = scene.getSkyBox();
        if (skyBox != null) {
            skyBoxShaderProgram.bind();

            skyBoxShaderProgram.setUniform("texture_sampler", 0);

            Matrix4f projectionMatrix = window.getProjectionMatrix();
            skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);

            Matrix4f viewMatrix = camera.getViewMatrix();
            //天空盒并不随着摄像机移动，所以将xyz的变化设置为0
            float m30 = viewMatrix.m30();
            viewMatrix.m30(0);
            float m31 = viewMatrix.m31();
            viewMatrix.m31(0);
            float m32 = viewMatrix.m32();
            viewMatrix.m32(0);
            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
            skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getSkyBoxLight());
            Mesh mesh = skyBox.getMesh();
            skyBoxShaderProgram.setUniform("colour", mesh.getMaterial().getAmbientColour());
            skyBoxShaderProgram.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);
            mesh.render();
            viewMatrix.m30(m30);
            viewMatrix.m31(m31);
            viewMatrix.m32(m32);
            skyBoxShaderProgram.unbind();
        }
    }
    //渲染准星
    private void renderCrossHair(Window window) {
        if (window.getWindowOptions().compatibleProfile) {
            glPushMatrix();
            glLoadIdentity();
            float inc = 0.05f;
            glLineWidth(2.0f);
            glBegin(GL_LINES);
            glColor3f(1.0f, 1.0f, 1.0f);
            // Horizontal line
            glVertex3f(-inc, 0.0f, 0.0f);
            glVertex3f(+inc, 0.0f, 0.0f);
            glEnd();
            // Vertical line
            glBegin(GL_LINES);
            glVertex3f(0.0f, -inc, 0.0f);
            glVertex3f(0.0f, +inc, 0.0f);
            glEnd();
            glPopMatrix();
        }
    }

    public void cleanUp(){
        if (skyBoxShaderProgram != null)
            skyBoxShaderProgram.cleanUp();

        if (sceneShaderProgram != null)
            sceneShaderProgram.cleanUp();

        if (particlesShaderProgram != null)
            particlesShaderProgram.cleanUp();
    }
}
