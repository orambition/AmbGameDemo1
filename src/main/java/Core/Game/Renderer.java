package Core.Game;
/*渲染类
* 用于渲染画面*/

import Core.Engine.Utils;
import Core.Engine.Window;
import Core.Engine.graph.Mesh;
import Core.Engine.graph.ShaderProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Renderer {
    //弧度视野
    //Field of view（FOV）
    private static final float FOV = (float) Math.toRadians(60.0f);
    //近平面距离
    private static final float Z_NEAR = 0.01f;
    //远平面距离
    private static final float Z_FAR = 1000.f;
    //投影矩阵
    private Matrix4f projectionMatrix;

    //着色器程序
    private ShaderProgram shaderProgram;

    public Renderer(){

    }

    public void init(Window window) throws Exception{
        //创建着色器
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/fragment.fs"));
        shaderProgram.link();

        //创建投影矩阵
        float aspectRatio = (float) window.getWindowWidth() / window.getWindowHeight();//宽高比
        projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.createUniform("projectionMatrix");
    }

    //清屏函数
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//清屏
    }

    //渲染函数
    public void render(Window window, Mesh mesh) {
        clear();
        if (window.isResized()) {
            glViewport(0, 0, window.getWindowWidth(), window.getWindowHeight());
            window.setResized(false);
        }
        shaderProgram.bind();
        shaderProgram.setUniform("projectionMatrix",projectionMatrix);
        // Bind to the VAO
        glBindVertexArray(mesh.getVaoId());
        glEnableVertexAttribArray(0);//启用数组1，对用位置
        glEnableVertexAttribArray(1);//启用数组2，对用颜色
        /*绘制图形，参数：
        * 模式：指定渲染的原语，在此情况下的三角形。
        * 计数：指定要呈现的元素的数目。
        * 类型：指定索引数据中的值类型。
        * 索引：指定应用于索引数据以开始呈现的偏移量。*/
        glDrawElements(GL_TRIANGLES,mesh.getVertexCount(),GL_UNSIGNED_INT,0);
        // Restore state
        glDisableVertexAttribArray(0);//关闭数组1
        glDisableVertexAttribArray(1);//关闭数组2
        glBindVertexArray(0);
        shaderProgram.unbind();
    }
    //
    public void clearUp(){
        if (shaderProgram != null)
            shaderProgram.cleanUp();
    }
}
