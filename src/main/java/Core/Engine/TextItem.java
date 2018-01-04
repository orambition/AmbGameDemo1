package Core.Engine;
//文字类，用于加载文字纹理，在建立Hud时首次添加

import Core.Engine.graph.FontTexture;
import Core.Engine.graph.Material;
import Core.Engine.graph.Mesh;

import java.util.ArrayList;
import java.util.List;

public class TextItem extends GameItem{

    private static final float ZPOS = 0.0f;
    private static final int VERTICES_PER_QUAD = 4;
    private final FontTexture fontTexture;
    private String text;

    public TextItem(String text, FontTexture fontTexture) throws Exception {
        super();
        this.text = text;
        this.fontTexture = fontTexture;
        setMesh(buildMesh());
    }

    private Mesh buildMesh() {

        char[] characters = text.toCharArray();
        int numChars = characters.length;

        List<Float> positions = new ArrayList();//点的位置坐标
        List<Float> textCoords = new ArrayList();//纹理坐标
        float[] normals = new float[0];//法线
        List<Integer> indices = new ArrayList();//顺序

        float startx = 0;//字母起始位置
        for(int i=0; i<numChars; i++) {
            FontTexture.CharInfo charInfo = fontTexture.getCharInfo(characters[i]);
            // 分割图片通过四个点确定每个子的纹理位置；延x周排列
            // Left Top vertex
            positions.add(startx); // x
            positions.add(0.0f); //y
            positions.add(ZPOS); //z
            textCoords.add((float)charInfo.getStartX() / (float)fontTexture.getWidth());
            textCoords.add(0.0f);
            indices.add(i*VERTICES_PER_QUAD);
            // Left Bottom vertex
            positions.add(startx); // x
            positions.add((float)fontTexture.getHeight()); //y
            positions.add(ZPOS); //z
            textCoords.add((float)charInfo.getStartX() / (float)fontTexture.getWidth());
            textCoords.add(1.0f);
            indices.add(i*VERTICES_PER_QUAD + 1);
            // Right Bottom vertex
            positions.add(startx + charInfo.getWidth()); // x
            positions.add((float)fontTexture.getHeight()); //y
            positions.add(ZPOS); //z
            textCoords.add((float)(charInfo.getStartX() + charInfo.getWidth() )/ (float)fontTexture.getWidth());
            textCoords.add(1.0f);
            indices.add(i*VERTICES_PER_QUAD + 2);
            // Right Top vertex
            positions.add(startx + charInfo.getWidth()); // x
            positions.add(0.0f); //y
            positions.add(ZPOS); //z
            textCoords.add((float)(charInfo.getStartX() + charInfo.getWidth() )/ (float)fontTexture.getWidth());
            textCoords.add(0.0f);
            indices.add(i*VERTICES_PER_QUAD + 3);
            // Add indices por left top and bottom right vertices
            indices.add(i*VERTICES_PER_QUAD);
            indices.add(i*VERTICES_PER_QUAD + 2);

            startx += charInfo.getWidth();
        }
        float[] posArr = Utils.listToArray(positions);
        float[] textCoordsArr = Utils.listToArray(textCoords);
        int[] indicesArr = indices.stream().mapToInt(i->i).toArray();
        //mesh存储数据，材质指定纹理
        Mesh mesh = new Mesh(posArr, textCoordsArr, normals, indicesArr);
        mesh.setMaterial(new Material(fontTexture.getTexture()));
        return mesh;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
        this.getMesh().deleteBuffers();
        this.setMesh(buildMesh());
    }
}
