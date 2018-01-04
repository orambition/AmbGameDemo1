package Core.Engine;
//文字类，用于加载文字纹理，在建立Hud时首次添加

import Core.Engine.graph.Material;
import Core.Engine.graph.Mesh;
import Core.Engine.graph.Texture;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TextItem extends GameItem{

    private static final float ZPOS = 0.0f;
    private static final int VERTICES_PER_QUAD = 4;

    private String text;
    private final int numCols;
    private final int numRows;

    public TextItem(String text, String fontFileName, int numCols, int numRows) throws Exception {
        super();
        this.text = text;
        this.numCols = numCols;
        this.numRows = numRows;
        Texture texture = new Texture(fontFileName);
        this.setMesh(buildMesh(texture, numCols, numRows));
    }
    private Mesh buildMesh(Texture texture, int numCols, int numRows) {
        byte[] chars = text.getBytes(Charset.forName("ISO-8859-1"));
        int numChars = chars.length;
        List<Float> positions = new ArrayList();//点的位置坐标
        List<Float> textCoords = new ArrayList();//纹理坐标
        float[] normals = new float[0];//法线
        List<Integer> indices = new ArrayList();//顺序
        //每个字的宽高
        float tileWidth = (float)texture.getWidth() / (float)numCols;
        float tileHeight = (float)texture.getHeight() / (float)numRows;
        for(int i=0; i<numChars; i++) {
            byte currChar = chars[i];
            int col = currChar % numCols;
            int row = currChar / numCols;
            // 分割图片通过四个点确定每个子的纹理位置；延x周排列
            // Left Top vertex
            positions.add((float)i*tileWidth); // x
            positions.add(0.0f); //y
            positions.add(ZPOS); //z
            textCoords.add((float)col / (float)numCols );
            textCoords.add((float)row / (float)numRows );
            indices.add(i*VERTICES_PER_QUAD);
            // Left Bottom vertex
            positions.add((float)i*tileWidth); // x
            positions.add(tileHeight); //y
            positions.add(ZPOS); //z
            textCoords.add((float)col / (float)numCols );
            textCoords.add((float)(row + 1) / (float)numRows );
            indices.add(i*VERTICES_PER_QUAD + 1);
            // Right Bottom vertex
            positions.add((float)i*tileWidth + tileWidth); // x
            positions.add(tileHeight); //y
            positions.add(ZPOS); //z
            textCoords.add((float)(col + 1)/ (float)numCols );
            textCoords.add((float)(row + 1) / (float)numRows );
            indices.add(i*VERTICES_PER_QUAD + 2);
            // Right Top vertex
            positions.add((float)i*tileWidth + tileWidth); // x
            positions.add(0.0f); //y
            positions.add(ZPOS); //z
            textCoords.add((float)(col + 1)/ (float)numCols );
            textCoords.add((float)row / (float)numRows );
            indices.add(i*VERTICES_PER_QUAD + 3);
            // Add indices por left top and bottom right vertices
            indices.add(i*VERTICES_PER_QUAD);
            indices.add(i*VERTICES_PER_QUAD + 2);
        }
        float[] posArr = Utils.listToArray(positions);
        float[] textCoordsArr = Utils.listToArray(textCoords);
        int[] indicesArr = indices.stream().mapToInt(i->i).toArray();
        //mesh存储数据，材质指定纹理
        Mesh mesh = new Mesh(posArr, textCoordsArr, normals, indicesArr);
        mesh.setMaterial(new Material(texture));
        return mesh;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
        Texture texture = this.getMesh().getMaterial().getTexture();
        this.getMesh().deleteBuffers();
        this.setMesh(buildMesh(texture, numCols, numRows));
    }
}
