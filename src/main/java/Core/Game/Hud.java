package Core.Game;
//hud类

import Core.Engine.items.GameItem;
import Core.Engine.IHud;
import Core.Engine.items.TextItem;
import Core.Engine.Window;
import Core.Engine.graph.*;
import Core.Engine.loaders.obj.OBJLoader;
import org.joml.Vector4f;

import java.awt.*;

public class Hud implements IHud{

    private static final Font FONT = new Font("Arial", Font.PLAIN, 20);
    private static final String CHARSET = "ISO-8859-1";

    private final GameItem[] gameItems;
    private final TextItem statusTextItem;

    private final GameItem compassItem;//罗盘

    public Hud(String statusText) throws Exception {
        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        //文字hud
        this.statusTextItem = new TextItem(statusText, fontTexture,new Vector4f(0, 0, 0, 1));
        //this.statusTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0, 0, 0, 1));
        //this.statusTextItem.setRotation(0f,60f,0f);
        //this.statusTextItem.setScale(0.05f);
        // 加载罗盘
        Mesh mesh = OBJLoader.loadMesh("/models/compass.obj");
        Material material = new Material();
        material.setAmbientColour(new Vector4f(0, 0, 0, 1));
        mesh.setMaterial(material);
        compassItem = new GameItem(mesh);
        compassItem.setScale(40.0f);
        // 修正罗盘角度
        compassItem.setRotation(0f, 0f, 180f);

        gameItems = new GameItem[]{statusTextItem, compassItem};
    }
    public void setStatusText(String statusText) {
        this.statusTextItem.setText(statusText);
    }
    public void rotateCompass(float angle) {
        this.compassItem.setRotation(0, 0, 180 + angle);
    }
    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }
    public void updateSize(Window window) {
        this.statusTextItem.setPosition(10f, window.getWindowHeight() - 50f, 0);
        this.compassItem.setPosition(window.getWindowWidth() - 40f, 50f, 0);
        //this.statusTextItem.setPosition(-10, -7, -15f);
        //this.compassItem.setPosition(3f, 3f, -15f);
    }

}
