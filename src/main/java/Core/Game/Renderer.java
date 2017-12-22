package Core.Game;
/*渲染类
* 用于渲染画面*/

import Core.Engine.GameItem;
import Core.Engine.Utils;
import Core.Engine.Window;
import Core.Engine.graph.Camera;
import Core.Engine.graph.ShaderProgram;
import Core.Engine.graph.Transformation;
import org.joml.Matrix4f;

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

    //
    private final Transformation transformation;

    public Renderer(){
        transformation = new Transformation();
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
        //window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    //清屏函数
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//清屏
    }

    //渲染函数
    public void render(Window window, Camera camera, GameItem[] gameItems) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWindowWidth(), window.getWindowHeight());
            window.setResized(false);
        }
        shaderProgram.bind();
        //投影矩阵，将三维坐标投影到二位屏幕上
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWindowWidth(), window.getWindowHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix",projectionMatrix);

        // 视野矩阵
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        //设置纹理单元，为显存中的0号纹理单元
        shaderProgram.setUniform("texture_sampler", 0);

        //绘制每一个gameItem
        for (GameItem gameItem : gameItems){
            Matrix4f modelViewMatrix  = transformation.getModelViewMatrix(gameItem,viewMatrix);
            shaderProgram.setUniform("modelViewMatrix",modelViewMatrix);
            //绘制
            gameItem.getMesh().render();
        }

        shaderProgram.unbind();
    }
    //
    public void clearUp(){
        if (shaderProgram != null)
            shaderProgram.cleanUp();
    }
}
