package Core.Engine.loaders.md5;
//用于从md5model 加载模型到引擎中，转为gameitem

import Core.Engine.Utils;
import Core.Engine.graph.Material;
import Core.Engine.graph.Mesh;
import Core.Engine.graph.Texture;
import Core.Engine.items.GameItem;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class MD5Loader {
    private static final String NORMAL_FILE_SUFFIX = "_normal";

    public static GameItem process(MD5Model md5Model, Vector4f defaultColour) throws Exception {
        //List<MD5Mesh> md5MeshList = md5Model.getMeshes();
        List<Mesh> list = new ArrayList<>();
        for (MD5Mesh md5Mesh : md5Model.getMeshes()) {
            //1、生产网格
            Mesh mesh = generateMesh(md5Model, md5Mesh);
            //2、处理纹理
            handleTexture(mesh, md5Mesh, defaultColour);
            list.add(mesh);
        }
        Mesh[] meshes = new Mesh[list.size()];
        meshes = list.toArray(meshes);
        GameItem gameItem = new GameItem(meshes);
        return gameItem;
    }
    //从模型类生产mesh网格类
    private static Mesh generateMesh(MD5Model md5Model, MD5Mesh md5Mesh) throws Exception {
        List<VertexInfo> vertexInfoList = new ArrayList<>();//顶点内部类
        List<Float> textCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        List<MD5Mesh.MD5Vertex> vertices = md5Mesh.getVertices();//获取定点
        List<MD5Mesh.MD5Weight> weights = md5Mesh.getWeights();//获取权重
        List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();//获取关节

        for (MD5Mesh.MD5Vertex vertex : vertices) {
            //md5文件中的顶点信息
            Vector3f vertexPos = new Vector3f();
            Vector2f vertexTextCoords = vertex.getTextCoords();
            textCoords.add(vertexTextCoords.x);
            textCoords.add(vertexTextCoords.y);
            int startWeight = vertex.getStartWeight();
            int numWeights = vertex.getWeightCount();
            for (int i = startWeight; i < startWeight + numWeights; i++) {
                //每个顶点的权重信息
                MD5Mesh.MD5Weight weight = weights.get(i);
                //通过权重信息，获取关节
                MD5JointInfo.MD5JointData joint = joints.get(weight.getJointIndex());
                //根据关节的角度旋转权重的位置
                Vector3f rotatedPos = new Vector3f(weight.getPosition()).rotate(joint.getOrientation());
                //根据关节的位置最终确定权重的位置
                Vector3f acumPos = new Vector3f(joint.getPosition()).add(rotatedPos);
                //将权重位置与权重相乘，得到一个关节对相应顶点的影响
                acumPos.mul(weight.getBias());
                //将所有关节对该顶点的影响叠加，得到最终位置
                vertexPos.add(acumPos);
            }
            vertexInfoList.add(new VertexInfo(vertexPos));
        }
        for (MD5Mesh.MD5Triangle tri : md5Mesh.getTriangles()) {
            //三角面信息
            indices.add(tri.getVertex0());
            indices.add(tri.getVertex1());
            indices.add(tri.getVertex2());
            // 计算法线Normals
            VertexInfo v0 = vertexInfoList.get(tri.getVertex0());
            VertexInfo v1 = vertexInfoList.get(tri.getVertex1());
            VertexInfo v2 = vertexInfoList.get(tri.getVertex2());
            Vector3f pos0 = v0.position;
            Vector3f pos1 = v1.position;
            Vector3f pos2 = v2.position;
            Vector3f normal = (new Vector3f(pos2).sub(pos0)).cross(new Vector3f(pos1).sub(pos0));
            v0.normal.add(normal);
            v1.normal.add(normal);
            v2.normal.add(normal);
        }
        // 将法线标准化，长度归一
        for(VertexInfo v : vertexInfoList) {
            v.normal.normalize();
        }
        //最终数据数组
        float[] positionsArr = VertexInfo.toPositionsArr(vertexInfoList);
        float[] textCoordsArr = Utils.listToArray(textCoords);
        float[] normalsArr = VertexInfo.toNormalArr(vertexInfoList);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();
        Mesh mesh = new Mesh(positionsArr, textCoordsArr, normalsArr, indicesArr);
        return mesh;
    }
    //纹理处理函数
    private static void handleTexture(Mesh mesh, MD5Mesh md5Mesh, Vector4f defaultColour) throws Exception {
        String texturePath = md5Mesh.getTexture();
        if (texturePath != null && texturePath.length() > 0) {
            //根据文件创建纹理和材质
            Texture texture = new Texture(texturePath);
            Material material = new Material(texture);
            // 根据材质名称拼接出法线贴图名称;
            int pos = texturePath.lastIndexOf(".");
            if (pos > 0) {
                String basePath = texturePath.substring(0, pos);
                String extension = texturePath.substring(pos, texturePath.length());
                String normalMapFileName = basePath + NORMAL_FILE_SUFFIX + extension;
                if (Utils.existsResourceFile(normalMapFileName)) {
                    Texture normalMap = new Texture(normalMapFileName);
                    material.setNormalMap(normalMap);
                }
            }
            mesh.setMaterial(material);
        } else {
            mesh.setMaterial(new Material(defaultColour, 1));
        }
    }
    //顶点信息内部类
    private static class VertexInfo {
        public Vector3f position;//位置
        public Vector3f normal;//法线

        public VertexInfo(Vector3f position) {
            this.position = position;
            normal = new Vector3f(0, 0, 0);
        }
        public VertexInfo() {
            position = new Vector3f();
            normal = new Vector3f();
        }
        //将顶点位置转成1维数组
        public static float[] toPositionsArr(List<VertexInfo> list) {
            int length = list != null ? list.size() * 3 : 0;
            float[] result = new float[length];
            int i = 0;
            for (VertexInfo v : list) {
                result[i] = v.position.x;
                result[i + 1] = v.position.y;
                result[i + 2] = v.position.z;
                i += 3;
            }
            return result;
        }
        //将顶点法线转成1维数组
        public static float[] toNormalArr(List<VertexInfo> list) {
            int length = list != null ? list.size() * 3 : 0;
            float[] result = new float[length];
            int i = 0;
            for (VertexInfo v : list) {
                result[i] = v.normal.x;
                result[i + 1] = v.normal.y;
                result[i + 2] = v.normal.z;
                i += 3;
            }
            return result;
        }
    }
}
