package Core.Game;
/*渲染类
* 用于渲染画面*/

import Core.Engine.GameItem;
import Core.Engine.Utils;
import Core.Engine.Window;
import Core.Engine.graph.*;
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

    //着色器程序
    private ShaderProgram shaderProgram;

    //获取投影、视角等各种矩阵
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

    public void init(Window window) throws Exception{
        //创建着色器
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/shaders/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/shaders/fragment.fs"));
        shaderProgram.link();

        //为世界和投影矩阵 创建 Uniforms,vertex.vs的main中没有使用该值则会报错
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
        // 创建材质Uniform
        shaderProgram.createMaterialUniform("material");
        // 创建镜面反射率、环境光uniform
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        // 创建点光源数组、聚光灯数组、平行光源uniform
        shaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        shaderProgram.createDirectionalLightUniform("directionalLight");
    }

    //清屏函数
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//清屏
    }

    //渲染函数
    public void render(Window window, Camera camera, GameItem[] gameItems,Vector3f ambientLight, PointLight[] pointLightList, SpotLight[] spotLightList,DirectionalLight directionalLight) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWindowWidth(), window.getWindowHeight());
            window.setResized(false);
        }
        shaderProgram.bind();
        //投影矩阵，将三维坐标投影到二位屏幕上
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWindowWidth(), window.getWindowHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix",projectionMatrix);

        // 视野矩阵,用于根据视角确定物体的具体位置
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        //设置环境光、点光源数组、聚光灯数组、平行光和强度
        renderLights(viewMatrix, ambientLight, pointLightList, spotLightList, directionalLight);

        //设置纹理单元，为显存中的0号纹理单元，将纹理放入0号单元的操作有Mesh完成
        shaderProgram.setUniform("texture_sampler", 0);

        //绘制每一个gameItem
        for (GameItem gameItem : gameItems){
            Mesh mesh = gameItem.getMesh();
            //设置该物体的模型视野矩阵
            Matrix4f modelViewMatrix  = transformation.getModelViewMatrix(gameItem,viewMatrix);
            shaderProgram.setUniform("modelViewMatrix",modelViewMatrix);
            //设置该物体的材质
            shaderProgram.setUniform("material", mesh.getMaterial());
            //绘制该物体的mesh
            mesh.render();
        }

        shaderProgram.unbind();
    }
    private void renderLights(Matrix4f viewMatrix, Vector3f ambientLight, PointLight[] pointLightList, SpotLight[] spotLightList, DirectionalLight directionalLight) {
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", specularPower);
        // Process Point Lights
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
            shaderProgram.setUniform("pointLights", currPointLight, i);
        }
        // Process Spot Ligths
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
            shaderProgram.setUniform("spotLights", currSpotLight, i);
        }
        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        shaderProgram.setUniform("directionalLight", currDirLight);
    }
    //
    public void clearUp(){
        if (shaderProgram != null)
            shaderProgram.cleanUp();
    }
}
