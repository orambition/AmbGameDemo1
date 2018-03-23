package Core.Engine.items;
/*模型信息类*/
import Core.Engine.graph.Mesh;
import org.joml.Vector3f;

public class GameItem {
    //网格数据
    private Mesh[] meshes;//修改为数组类型，用于支持多网格模型
    //位置
    private final Vector3f position;
    //缩放
    private float scale;
    //旋转
    private final Vector3f rotation;//方向旋转由于计算矩阵过程中是右乘的，所以顺序是zyx
    private int textPos;//纹理中块的位置，纹理分为行列之后新增的属性，纹理默认不分割，默认为0就是整个纹理，
    public GameItem() {
        position = new Vector3f(0, 0, 0);
        scale = 1;
        rotation = new Vector3f(0, 0, 0);
        textPos = 0;
    }
    public GameItem(Mesh mesh) {
        this();
        this.meshes = new Mesh[]{mesh};
    }
    public GameItem(Mesh[] meshes) {
        this();
        this.meshes = meshes;
    }

    public int getTextPos() {
        return textPos;
    }
    public void setTextPos(int textPos) {
        this.textPos = textPos;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public Mesh getMesh() {
        return meshes[0];
    }
    public Mesh[] getMeshes() {
        return meshes;
    }
    public void setMesh(Mesh mesh){
        this.meshes = new Mesh[]{mesh};
    }
    public void setMeshes(Mesh[] meshes) {
        this.meshes = meshes;
    }

    public void cleanUp() {
        int numMeshes = this.meshes != null ? this.meshes.length : 0;
        for(int i=0; i<numMeshes; i++) {
            this.meshes[i].cleanUp();
        }
    }
}
