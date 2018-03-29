package Core.Engine;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    //视野矩阵参数
    private static final float FOV = (float) Math.toRadians(60.0f);//视野（FOV）
    private static final float Z_NEAR = 0.01f;//近平面距离
    private static final float Z_FAR = 1000.f;//远平面距离

    private Long windowHandle;
    private String windowTitle;
    private int windowWidth;
    private int windowHeight;

    private boolean vSync;//垂直同步
    private boolean resized;//允许调整窗口大小
    private WindowOptions opts;//窗口设置内部类
    private Matrix4f projectionMatrix;//透视矩阵，重构前在Transformation类中

    public Window(String windowTitle, int width, int height, boolean vSync,WindowOptions opts){
        this.windowTitle = windowTitle;
        this.windowWidth = width;
        this.windowHeight = height;
        this.vSync = vSync;
        this.resized = false;
        this.opts = opts;
        projectionMatrix = new Matrix4f();
    }
    //初始化
    public void init(){
        GLFWErrorCallback.createPrint(System.err).set();
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,2);
        if (opts.compatibleProfile) {//兼容模式
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }

        boolean maximized = false;
        // 如果没有宽或高，则将窗口设置为最大
        if (windowWidth == 0 || windowHeight == 0) {
            // Set up a fixed width and height so window initialization does not fail
            windowWidth = 100;
            windowHeight = 100;
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            maximized = true;
        }

        // 创建窗口
        windowHandle = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);
        if ( windowHandle == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // 设置改变窗口大小时的回掉函数，当窗口大小发生变化时执行该函数
        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.windowWidth = width;
            this.windowHeight = height;
            this.resized = true;
            //System.out.println("窗口大小改变");
        });
        // 窗口化时将窗口置于屏幕中央
        if (!maximized) {
            // 获得当前屏幕的分辨率
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // 根据分辨率将新创建的窗口至于屏幕中间
            glfwSetWindowPos(
                    windowHandle,
                    (vidmode.width() - windowWidth) / 2,
                    (vidmode.height() - windowHeight) / 2
            );
        }

        // 设置按键回掉
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // 设置opengl的上下文
        glfwMakeContextCurrent(windowHandle);

        // 启用垂直同步
        if (isvSync())
            glfwSwapInterval(1);

        // 启动窗口
        glfwShowWindow(windowHandle);

        //重要的一步
        GL.createCapabilities();

        glClearColor(0f, 0f, 0f, 0.0f);
        //启用深度测试？让像素点按照深度绘制，而不是随机顺序绘制
        glEnable(GL_DEPTH_TEST);
        //启用模板测试，控制哪些像素该被绘制，最后一个测试？
        glEnable(GL_STENCIL_TEST);
        if (opts.showTriangles) {
            //以多边形模式进行显示，这将显示模型的三角形框架;
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
        // 支持透明，首次添加于创建hud
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if (opts.cullFace) {
            //启用 面剔除，并设置back为删除的规则，不渲染看不到的面，以面的朝向确定是否可见，确定面的3个点是逆时针排列的则是正面。
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
        if (opts.antialiasing) {
            //抗锯齿
            glfwWindowHint(GLFW_SAMPLES, 4);
        }
    }
    public long getWindowHandle() {
        return windowHandle;
    }
    public void setClearColor(float r, float g, float b, float alpha) {
        glClearColor(r, g, b, alpha);
    }
    public boolean isKeyPressed(int keyCode){
        return glfwGetKey(windowHandle,keyCode) == GLFW_PRESS;
    }
    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }
    public boolean isvSync(){
        return vSync;
    }
    public void setvSync(boolean vSync){
        this.vSync = vSync;
    }
    public boolean isResized(){
        return resized;
    }
    public void setResized(boolean resized){
        this.resized = resized;
    }
    public int getWindowWidth(){
        return windowWidth;
    }
    public int getWindowHeight(){
        return windowHeight;
    }
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
    public Matrix4f updateProjectionMatrix() {
        float aspectRatio = (float)windowWidth / (float)windowHeight;
        return projectionMatrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }
    //恢复窗口状态，首次创建是由于NanoGV在绘制HUD时会改变OpenGl的状态
    public void restoreState() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if (opts.cullFace) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
    }
    //重要函数，在game主循环的render中调用，用于绘制画面
    public void update(){
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }
    public void dispose(){
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
    }

    public WindowOptions getWindowOptions() {
        return opts;
    }

    public void setWindowTitle(String title) {
        glfwSetWindowTitle(windowHandle, title);
    }

    //窗口设置内部类
    public static class WindowOptions {
        public boolean cullFace;//裁剪看不见的面
        public boolean showTriangles;//显示网格
        public boolean showFps;//显示FPS
        public boolean compatibleProfile;//兼容渲染
        public boolean antialiasing;//抗锯齿
        public boolean frustumCulling;//视野锥裁减
    }
}
