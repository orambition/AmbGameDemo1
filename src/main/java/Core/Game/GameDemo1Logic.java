package Core.Game;

import Core.Engine.GameItem;
import Core.Engine.IGameLogic;
import Core.Engine.Window;
import Core.Engine.graph.Mesh;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class GameDemo1Logic implements IGameLogic {

    private int displxInc = 0;
    private int displyInc = 0;
    private int displzInc = 0;
    private int rotatxInc = 0;
    private int rotatyInc = 0;
    private int rotatzInc = 0;
    private int scaleInc = 0;

    private final Renderer renderer;
    private GameItem[] gameItems;
    /*private int direction = 0;
    private float color = 0.0f;*/
    public GameDemo1Logic(){
        renderer = new Renderer();
    }

    @Override
    public void init(Window window) throws Exception {
        //初始化渲染
        renderer.init(window);
        //创建Mesh
        float[] positions = new float[]{
                -0.5f, 0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
        };
        float[] colours = new float[]{
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
        };
        int[] indices = new int[]{
                0, 1, 2, 2, 0, 3,
        };
        Mesh mesh = new Mesh(positions, colours, indices);
        GameItem gameItem = new GameItem(mesh);
        gameItem.setPosition(0,0,-2);
        gameItems = new GameItem[]{gameItem};
    }

    @Override
    public void input(Window window) {
        displyInc = 0;
        displxInc = 0;
        displzInc = 0;
        rotatxInc = 0;
        rotatyInc = 0;
        rotatzInc = 0;
        scaleInc = 0;
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            rotatxInc = 1;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            rotatxInc = -1;
        } else if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            rotatyInc = -1;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            rotatyInc = 1;
        } else if (window.isKeyPressed(GLFW_KEY_W)) {
            displyInc = 1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            displyInc = -1;
        } else if (window.isKeyPressed(GLFW_KEY_A)) {
            displxInc = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            displxInc = 1;
        } else if (window.isKeyPressed(GLFW_KEY_SPACE)){
            scaleInc = 1;
        } else if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)){
            scaleInc = -1;
        }
    }

    @Override
    public void update(float interval) {
        for (GameItem gameItem : gameItems) {
            // Update position
            Vector3f itemPos = gameItem.getPosition();
            float posx = itemPos.x + displxInc * 0.01f;
            float posy = itemPos.y + displyInc * 0.01f;
            float posz = itemPos.z + displzInc * 0.01f;
            gameItem.setPosition(posx, posy, posz);
            // Update scale
            float scale = gameItem.getScale();
            scale += scaleInc * 0.05f;
            if ( scale < 0 ) {
                scale = 0;
            }
            gameItem.setScale(scale);
            // Update rotation angle
            float rotatx = gameItem.getRotation().x + rotatxInc * 1.5f;
            float rotaty = gameItem.getRotation().y + rotatyInc * 1.5f;
            if ( rotatx > 360 ) {
                rotatx = 0;
            }
            gameItem.setRotation(rotatx, rotaty, 0);
        }
    }

    @Override
    public void render(Window window) {
        renderer.render(window,gameItems);
    }

    @Override
    public void cleanup() {
        renderer.clearUp();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }
}
