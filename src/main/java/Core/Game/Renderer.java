package Core.Game;
/*渲染类
* 用于渲染画面*/

import Core.Engine.GameItem;
import Core.Engine.Utils;
import Core.Engine.Window;
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
    //投影矩阵，将三维坐标投影到二位屏幕上
    private Matrix4f projectionMatrix;

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
        shaderProgram.createVertexShader(Utils.loadResource("/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/fragment.fs"));
        shaderProgram.link();

        //为世界和投影矩阵 创建 Uniforms
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("worldMatrix");
        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    //清屏函数
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//清屏
    }

    //渲染函数
    public void render(Window window, GameItem[] gameItems) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWindowWidth(), window.getWindowHeight());
            window.setResized(false);
        }
        shaderProgram.bind();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWindowWidth(), window.getWindowHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix",projectionMatrix);

        //绘制每一个gameItem
        for (GameItem gameItem : gameItems){
            Matrix4f worldMatrix = transformation.getWorldMatrix(gameItem.getPosition(),gameItem.getRotation(),gameItem.getScale());
            shaderProgram.setUniform("worldMatrix",worldMatrix);
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
