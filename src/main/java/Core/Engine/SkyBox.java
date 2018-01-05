package Core.Engine;
//天空盒类，用于加载天空盒这一个“物体”
import Core.Engine.graph.Material;
import Core.Engine.graph.Mesh;
import Core.Engine.graph.OBJLoader;
import Core.Engine.graph.Texture;

public class SkyBox extends GameItem {
    public SkyBox(String objModel, String textureFile)throws Exception{
        super();
        Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
        Texture skyBoxtexture = new Texture(textureFile);
        skyBoxMesh.setMaterial(new Material(skyBoxtexture,0.0f));
        setMesh(skyBoxMesh);
        setPosition(0,0,0);
    }
}
