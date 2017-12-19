package Core.Engine;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Timer {
    private double lastLoopTime;

    //初始化
    public void init(){
        lastLoopTime = getTime();
    }
    //获取当前时间
    public double getTime(){
        //return System.nanoTime()/ 1000000000.0;
        return glfwGetTime();
    }
    //获取两次调用的间隔时间
    public float getElapsedTime(){
        double time = getTime();
        float elapsedTime = (float) (time - lastLoopTime);
        lastLoopTime = time;
        return elapsedTime;
    }
    //获取上一次的调用时间
    public double getLastLoopTime(){
        return lastLoopTime;
    }
}
