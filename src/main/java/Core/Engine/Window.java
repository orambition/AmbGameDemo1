package Core.Engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClearColor;
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

        // 设置改变窗口大学时的回掉函数，当窗口大小发生变化时执行该函数
        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.windowWidth = width;
            this.windowHeight = height;
            this.resized = true;
            System.out.println("窗口大小改变");
        });

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center the window
        glfwSetWindowPos(
                windowHandle,
                (vidmode.width() - windowWidth) / 2,
                (vidmode.height() - windowHeight) / 2
        );
        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);
        // Enable v-sync
        if (isvSync())
            glfwSwapInterval(1);
        // Make the window visible
        glfwShowWindow(windowHandle);
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
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
