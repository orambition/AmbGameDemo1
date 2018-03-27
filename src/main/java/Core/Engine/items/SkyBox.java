package Core.Engine.items;
//天空盒类，用于加载天空盒这一个“物体”

import Core.Engine.graph.Material;
import Core.Engine.graph.Mesh;
import Core.Engine.graph.Texture;
import Core.Engine.loaders.obj.OBJLoader;
import org.joml.Vector4f;

public class SkyBox extends GameItem {
    public SkyBox(String objModel, String textureFile)throws Exception{
        super();
        Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
        Texture skyBoxtexture = new Texture(textureFile);
        skyBoxMesh.setMaterial(new Material(skyBoxtexture,0.0f));
        setMesh(skyBoxMesh);
        setPosition(0,0,0);
    }
    //不通过纹理创建天空盒
    public SkyBox(String objModel, Vector4f colour) throws Exception {
        super();
        Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
        Material material = new Material(colour, 0);
        skyBoxMesh.setMaterial(material);
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
    }
}
