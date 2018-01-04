package Core.Game;
//hud类

import Core.Engine.GameItem;
import Core.Engine.IHud;
import Core.Engine.TextItem;
import Core.Engine.Window;
import Core.Engine.graph.FontTexture;
import Core.Engine.graph.Material;
import Core.Engine.graph.Mesh;
import Core.Engine.graph.OBJLoader;
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
        this.statusTextItem = new TextItem(statusText, fontTexture);
        this.statusTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(1, 1, 1, 1));

        // 加载罗盘
        Mesh mesh = OBJLoader.loadMesh("/models/compass.obj");
        Material material = new Material();
        material.setAmbientColour(new Vector4f(1, 0, 0, 1));
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
    }

}
