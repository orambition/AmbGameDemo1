package Core.Engine.loaders.md5;
//用于从md5model 和md5animModel加载模型到引擎中，转为Animgameitem

import Core.Engine.Utils;
import Core.Engine.graph.Material;
import Core.Engine.graph.Mesh;
import Core.Engine.graph.Texture;
import Core.Engine.graph.anim.AnimGameItem;
import Core.Engine.graph.anim.AnimVertex;
import Core.Engine.graph.anim.AnimatedFrame;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MD5Loader {
    private static final String NORMAL_FILE_SUFFIX = "_normal";

    //解析md5模型文件和动作文件
    public static AnimGameItem process(MD5Model md5Model, MD5AnimModel animModel, Vector4f defaultColour) throws Exception {
        List<Matrix4f> invJointMatrices = calcInJointMatrices(md5Model);//获取关节初始位置矩阵
        //生成动画帧列表，包含每1帧中每个骨头的位置
        List<AnimatedFrame> animatedFrames = processAnimationFrames(md5Model, animModel, invJointMatrices);

        List<Mesh> list = new ArrayList<>();
        for (MD5Mesh md5Mesh : md5Model.getMeshes()) {
            Mesh mesh = generateMesh(md5Model, md5Mesh);//1、生产网格
            handleTexture(mesh, md5Mesh, defaultColour);//2、处理纹理
            list.add(mesh);
        }
        Mesh[] meshes = new Mesh[list.size()];
        meshes = list.toArray(meshes);
        //网格、每帧的位置，初始位置
        AnimGameItem result = new AnimGameItem(meshes, animatedFrames, invJointMatrices);
        return result;
    }
    //获取关节的初始位置和方向，生产位置矩阵
    private static List<Matrix4f> calcInJointMatrices(MD5Model md5Model) {
        List<Matrix4f> result = new ArrayList<>();
        List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();
        for (MD5JointInfo.MD5JointData joint : joints) {
            //用关节位置计算平移矩阵，用关节方向计算旋转矩阵
            //利用旋转矩阵乘以平移矩阵得到变换矩阵，应用内部优化的旋转代替乘法。
            Matrix4f mat = new Matrix4f()
                    .translate(joint.getPosition())
                    .rotate(joint.getOrientation())
                    .invert();
            result.add(mat);
        }
        return result;
    }
    //生产动画帧列表  -  关键函数
    private static List<AnimatedFrame> processAnimationFrames(MD5Model md5Model, MD5AnimModel animModel, List<Matrix4f> invJointMatrices) {
        List<AnimatedFrame> animatedFrames = new ArrayList<>();
        List<MD5Frame> frames = animModel.getFrames();//获取所有帧
        for (MD5Frame frame : frames) {//逐帧遍历
            AnimatedFrame data = processAnimationFrame(md5Model, animModel, frame, invJointMatrices);
            animatedFrames.add(data);
        }
        return animatedFrames;
    }
    //生产每一帧
    private static AnimatedFrame processAnimationFrame(MD5Model md5Model, MD5AnimModel animModel, MD5Frame frame, List<Matrix4f> invJointMatrices) {
        AnimatedFrame result = new AnimatedFrame();
        MD5BaseFrame baseFrame = animModel.getBaseFrame();//获取基础帧
        List<MD5Hierarchy.MD5HierarchyData> hierarchyList = animModel.getHierarchy().getHierarchyDataList();//获取动作文件的关节信息，关节的id
        List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();//获取模型文件的关节信息，含有位置和旋转的初始值
        int numJoints = joints.size();
        float[] frameData = frame.getFrameData();
        for (int i = 0; i < numJoints; i++) {//根据编号进行关联
            MD5JointInfo.MD5JointData joint = joints.get(i);
            MD5BaseFrame.MD5BaseFrameData baseFrameData = baseFrame.getFrameDataList().get(i);
            Vector3f position = baseFrameData.getPosition();//基础帧的位置
            Quaternionf orientation = baseFrameData.getOrientation();//基础帧的旋转
            int flags = hierarchyList.get(i).getFlags();//是否含有位置和旋转数据的标志位
            int startIndex = hierarchyList.get(i).getStartIndex();//帧数据中关节i数据的开始位置
            //设置为当前帧的位置和旋转
            if ((flags & 1) > 0) {
                position.x = frameData[startIndex++];
            }
            if ((flags & 2) > 0) {
                position.y = frameData[startIndex++];
            }
            if ((flags & 4) > 0) {
                position.z = frameData[startIndex++];
            }
            if ((flags & 8) > 0) {
                orientation.x = frameData[startIndex++];
            }
            if ((flags & 16) > 0) {
                orientation.y = frameData[startIndex++];
            }
            if ((flags & 32) > 0) {
                orientation.z = frameData[startIndex++];
            }
            // 计算方向四元组中的w
            orientation = MD5Utils.calculateQuaternion(orientation.x, orientation.y, orientation.z);
            // 计算关节的平移矩阵和旋转矩阵
            Matrix4f translateMat = new Matrix4f().translate(position);
            Matrix4f rotationMat = new Matrix4f().rotate(orientation);
            Matrix4f jointMat = translateMat.mul(rotationMat);
            // 如果关节有父对象，位置由其父关节确定
            if (joint.getParentIndex() > -1) {
                Matrix4f parentMatrix = result.getLocalJointMatrices()[joint.getParentIndex()];
                jointMat = new Matrix4f(parentMatrix).mul(jointMat);
            }
            //序号、帧中位置变化量、初始位置
            //根据每个骨头的位置变化量和初始位置确定当帧最终位置
            result.setMatrix(i, jointMat, invJointMatrices.get(i));
        }
        //结果是每帧中的变化量数组和最终位置数组
        return result;
    }
    //从模型类生产mesh网格类,md5模型加载的重要函数之一，md5模型的顶点位置，由骨骼位置及骨骼对顶点的影响权重确定。
    private static Mesh generateMesh(MD5Model md5Model, MD5Mesh md5Mesh){

        List<AnimVertex> vertices = new ArrayList<>();//网格顶点列表
        List<Integer> indices = new ArrayList<>();//网格三角面列表

        List<MD5Mesh.MD5Vertex> md5Vertices = md5Mesh.getVertices();//获取所有顶点信息
        List<MD5Mesh.MD5Weight> weights = md5Mesh.getWeights();//获取权重
        List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();//获取关节
        //遍历所有顶点，得到顶点位置、纹理坐标、关联关节和权重
        for (MD5Mesh.MD5Vertex md5Vertex : md5Vertices) {
            //md5文件中的顶点信息
            AnimVertex vertex = new AnimVertex();
            vertex.position = new Vector3f();
            vertex.textCoords = md5Vertex.getTextCoords();//获取顶点纹理坐标
            int startWeight = md5Vertex.getStartWeight();//权重数据在文件中开始的位置
            int numWeights = md5Vertex.getWeightCount();//权重个数
            vertex.jointIndices = new int[numWeights];
            Arrays.fill(vertex.jointIndices, -1);
            vertex.weights = new float[numWeights];
            Arrays.fill(vertex.weights, -1);
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
                vertex.position.add(acumPos);
                vertex.jointIndices[i - startWeight] = weight.getJointIndex();
                vertex.weights[i - startWeight] = weight.getBias();
            }
            vertices.add(vertex);
        }
        //遍历所有三角面
        for (MD5Mesh.MD5Triangle tri : md5Mesh.getTriangles()) {
            //三角面信息
            indices.add(tri.getVertex0());
            indices.add(tri.getVertex1());
            indices.add(tri.getVertex2());
            // 计算法线Normals
            AnimVertex  v0 = vertices.get(tri.getVertex0());
            AnimVertex  v1 = vertices.get(tri.getVertex1());
            AnimVertex  v2 = vertices.get(tri.getVertex2());
            Vector3f pos0 = v0.position;
            Vector3f pos1 = v1.position;
            Vector3f pos2 = v2.position;
            Vector3f normal = (new Vector3f(pos2).sub(pos0)).cross(new Vector3f(pos1).sub(pos0));
            v0.normal.add(normal);
            v1.normal.add(normal);
            v2.normal.add(normal);
        }
        // 将法线标准化，长度归一
        for(AnimVertex v : vertices) {
            v.normal.normalize();
        }
        //最终数据数组
        Mesh mesh = createMesh(vertices, indices);
        return mesh;
    }
    //用于生成带关节信息的Mesh对象，使用顶点和三角面信息
    private static Mesh createMesh(List<AnimVertex> vertices, List<Integer> indices) {
        //获取顶点的各种信息
        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> jointIndices = new ArrayList<>();
        List<Float> weights = new ArrayList<>();
        for (AnimVertex vertex : vertices) {
            positions.add(vertex.position.x);
            positions.add(vertex.position.y);
            positions.add(vertex.position.z);
            textCoords.add(vertex.textCoords.x);
            textCoords.add(vertex.textCoords.y);
            normals.add(vertex.normal.x);
            normals.add(vertex.normal.y);
            normals.add(vertex.normal.z);
            int numWeights = vertex.weights.length;
            for (int i = 0; i < Mesh.MAX_WEIGHTS; i++) {
                if (i < numWeights) {
                    jointIndices.add(vertex.jointIndices[i]);
                    weights.add(vertex.weights[i]);
                } else {
                    jointIndices.add(-1);
                    weights.add(-1.0f);
                }
            }
        }
        //将各种信息转为数组形式，用于生产mesh中vbo
        float[] positionsArr = Utils.listToArray(positions);
        float[] textCoordsArr = Utils.listToArray(textCoords);
        float[] normalsArr = Utils.listToArray(normals);
        int[] indicesArr = Utils.listIntToArray(indices);
        int[] jointIndicesArr = Utils.listIntToArray(jointIndices);
        float[] weightsArr = Utils.listToArray(weights);
        Mesh result = new Mesh(positionsArr, textCoordsArr, normalsArr, indicesArr, jointIndicesArr, weightsArr);
        return result;
    }
    //纹理处理函数
    private static void handleTexture(Mesh mesh, MD5Mesh md5Mesh, Vector4f defaultColour) throws Exception {
        String texturePath = md5Mesh.getTexture();
        if (texturePath != null && texturePath.length() > 0) {
            //根据文件创建纹理和材质
            Texture texture = new Texture(texturePath);
            Material material = new Material(texture,200f);
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
}
