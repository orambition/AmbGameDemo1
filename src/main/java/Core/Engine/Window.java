package Core.Engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private Long windowHandle;
    private String windowTitle;
    private int windowWidth;
    private int windowHeight;

    private boolean vSync;//垂直同步
    private boolean resized;//允许调整窗口大小

    public Window(String windowTitle, int width, int height, boolean vSync){
        this.windowTitle = windowTitle;
        this.windowWidth = width;
        this.windowHeight = height;
        this.vSync = vSync;
        this.resized = false;
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
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // Create the window
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

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // 获得当前屏幕的分辨率
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // 根据分辨率将新创建的窗口至于屏幕中间
        glfwSetWindowPos(
                windowHandle,
                (vidmode.width() - windowWidth) / 2,
                (vidmode.height() - windowHeight) / 2
        );
        // 设置opengl的上下文
        glfwMakeContextCurrent(windowHandle);

        // 启用垂直同步
        if (isvSync())
            glfwSwapInterval(1);

        // 启动窗口
        glfwShowWindow(windowHandle);

        //重要的一步
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //启用深度测试？让像素点按照深度绘制，而不是随机顺序绘制
        glEnable(GL_DEPTH_TEST);
        //以多边形模式进行显示，这将显示模型的三角形框架
        //glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
        // 支持透明，首次添加于创建hud
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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



}
