package Core.Engine.graph.shadow;
//阴影渲染类，从Renderer类中独立，使用分层阴影渲染（CSM）。
//阴影通过FBO存储到纹理中（ShadowBuffer中定义的数组纹理）
//每层有自己的纹理和光视野、正交投影矩阵，通过ShadowCascade类计算和维护
//在改类中通过调用渲染函数（单独和分组），生成没层的阴影纹理

import Core.Engine.Scene;
import Core.Engine.SceneLight;
import Core.Engine.Utils;
import Core.Engine.Window;
import Core.Engine.graph.*;
import Core.Engine.graph.anim.AnimGameItem;
import Core.Engine.graph.anim.AnimatedFrame;
import Core.Engine.graph.lights.DirectionalLight;
import Core.Engine.items.GameItem;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowRenderer {
    public static final int NUM_CASCADES = 3;//分层次数，与scene_vertex着色器对应
    //每层的远平面距离50、100、1000
    public static final float[] CASCADE_SPLITS = new float[]{Window.Z_FAR / 20.0f, Window.Z_FAR / 10.0f, Window.Z_FAR};
    //深度图着色器程序
    private ShaderProgram depthShaderProgram;
    //阴影层列表，每层阴影包含独立的光视野矩阵和正交投影矩阵
    private List<ShadowCascade> shadowCascades;

    private ShadowBuffer shadowBuffer;
    //渲染的物体列表
    private final List<GameItem> filteredItems;

    public ShadowRenderer() {
        filteredItems = new ArrayList<>();
    }

    public void init(Window window) throws Exception {
        shadowBuffer = new ShadowBuffer();
        shadowCascades = new ArrayList<>();
        //初始化深度着色器程序
        setupDepthShader();

        float zNear = Window.Z_NEAR;
        for (int i = 0; i < NUM_CASCADES; i++) {
            //根据近平面和远平面生成每层视野锥
            ShadowCascade shadowCascade = new ShadowCascade(zNear, CASCADE_SPLITS[i]);
            shadowCascades.add(shadowCascade);
            zNear = CASCADE_SPLITS[i];
        }
    }

    public List<ShadowCascade> getShadowCascades() {
        return shadowCascades;
    }

    public void bindTextures(int start) {
        this.shadowBuffer.bindTextures(start);
    }

    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource("/shaders/depth_vertex.vs"));
        depthShaderProgram.createFragmentShader(Utils.loadResource("/shaders/depth_fragment.fs"));
        depthShaderProgram.link();

        depthShaderProgram.createUniform("isInstanced");//实例化渲染
        depthShaderProgram.createUniform("modelNonInstancedMatrix");//非实例化模型矩阵
        depthShaderProgram.createUniform("lightViewMatrix");//光视野矩阵
        depthShaderProgram.createUniform("jointsMatrix");//骨骼矩阵
        depthShaderProgram.createUniform("orthoProjectionMatrix");//正交矩阵
    }
    //更新所有层的视野矩阵和投影矩阵
    private void update(Window window, Matrix4f viewMatrix, Scene scene) {
        SceneLight sceneLight = scene.getSceneLight();
        DirectionalLight directionalLight = sceneLight != null ? sceneLight.getDirectionalLight() : null;
        for (int i = 0; i < NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);
            shadowCascade.update(window, viewMatrix, directionalLight);
        }
    }
    //渲染深度图
    public void render(Window window, Scene scene, Camera camera, Transformation transformation) {
        //计算每层的光视野矩阵和正交投影矩阵
        update(window, camera.getViewMatrix(), scene);
        //*重要*绑定深度图纹理的FBO，帧缓冲对象FBO是一个buffer集合，可以存储渲染结果，将用于绘制深度图从视野到纹理中
        glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer.getDepthMapFBO());
        //设置屏幕为深度图纹理大小，以通过屏幕输出深度图
        glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);
        //清空深度缓存
        glClear(GL_DEPTH_BUFFER_BIT);
        //绑定深度图着色器
        depthShaderProgram.bind();
        // 绘制每个层的深度图所需要的数据
        for (int i = 0; i < NUM_CASCADES; i++) {
            ShadowCascade shadowCascade = shadowCascades.get(i);
            //设置每层的光视野矩阵和正交投影矩阵
            depthShaderProgram.setUniform("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix());
            depthShaderProgram.setUniform("lightViewMatrix", shadowCascade.getLightViewMatrix());
            //改变使用的纹理，每层阴影绘制到不同的纹理上
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowBuffer.getDepthMapTexture().getIds()[i], 0);

            glClear(GL_DEPTH_BUFFER_BIT);
            //函数内完成了对场景中单独对象的渲染
            renderNonInstancedMeshes(scene, transformation);
            //函数内完成了场景中分组对象的渲染
            renderInstancedMeshes(scene, transformation);
        }
        // 解绑
        depthShaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void renderNonInstancedMeshes(Scene scene, Transformation transformation) {
        depthShaderProgram.setUniform("isInstanced", 0);
        //计算每个对象的模型矩阵
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
                //此处仅传递绘制阴影需要的数据，如材质、纹理、视野等数据均不需要
                    Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
                    depthShaderProgram.setUniform("modelNonInstancedMatrix", modelMatrix);
                    if (gameItem instanceof AnimGameItem) {
                        AnimGameItem animGameItem = (AnimGameItem) gameItem;
                        AnimatedFrame frame = animGameItem.getCurrentFrame();
                        depthShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                    }
                }
            );
        }
    }

    private void renderInstancedMeshes(Scene scene, Transformation transformation) {
        depthShaderProgram.setUniform("isInstanced", 1);

        Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getGameInstancedMeshes();
        for (InstancedMesh mesh : mapMeshes.keySet()) {
            //根据视野锥裁减对象
            filteredItems.clear();
            for (GameItem gameItem : mapMeshes.get(mesh)) {
                if (gameItem.isInsideFrustum()) {
                    filteredItems.add(gameItem);
                }
            }
            //绑定并激活多个纹理
            //bindTextures(GL_TEXTURE2);
            //调用glDrawElementsInstanced渲染场景，视野矩阵置空，因为这是渲染阴影，不需要摄像头视野
            mesh.renderListInstanced(filteredItems, transformation, null);
        }
    }

    public void cleanup() {
        if (shadowBuffer != null) {
            shadowBuffer.cleanUp();
        }
        if (depthShaderProgram != null) {
            depthShaderProgram.cleanUp();
        }
    }

}
