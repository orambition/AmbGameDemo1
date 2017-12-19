package Core.Game;

import Core.Engine.IGameLogic;
import Core.Engine.Window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

public class GameDemo1Logic implements IGameLogic {

    private final Renderer renderer;

    private int direction = 0;
    private float color = 0.0f;

    public GameDemo1Logic(){
        renderer = new Renderer();
    }

    @Override
    public void init() throws Exception {
        renderer.init();
    }

    @Override
    public void input(Window window) {
        if (window.isKeyPressed(GLFW_KEY_UP)){
            direction = 1;
        }else if (window.isKeyPressed(GLFW_KEY_DOWN)){
            direction = -1;
        }else {
            direction = 0;
        }
    }

    @Override
    public void update(float interval) {
        color += direction * 0.01f;
        if (color>1){
            color = 1.0f;
        }else if (color<0){
            color = 0.0f;
        }
    }

    @Override
    public void render(Window window) {
        window.setClearColor(color,color,color,0);
        renderer.render(window);
    }

    @Override
    public void cleanup() {
        renderer.clearup();
    }
}
