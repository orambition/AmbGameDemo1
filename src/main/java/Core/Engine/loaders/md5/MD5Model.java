package Core.Engine.loaders.md5;
//md5模型类，使用正则表达式从md5文件加载md5模型数据，转为类对象，
import Core.Engine.Utils;
import java.util.ArrayList;
import java.util.List;

public class MD5Model {
    //根据md5文件中的属性，建立以下类
    private MD5JointInfo jointInfo;//关节信息
    private MD5ModelHeader header;//头部信息
    private List<MD5Mesh> meshes;//mesh信息
    public MD5Model() {
        meshes = new ArrayList<>();
    }
    public MD5JointInfo getJointInfo() {
        return jointInfo;
    }
    public void setJointInfo(MD5JointInfo jointInfo) {
        this.jointInfo = jointInfo;
    }
    public MD5ModelHeader getHeader() {
        return header;
    }
    public void setHeader(MD5ModelHeader header) {
        this.header = header;
    }
    public List<MD5Mesh> getMeshes() {
        return meshes;
    }
    public void setMeshes(List<MD5Mesh> meshes) {
        this.meshes = meshes;
    }
    @Override
    public String toString() {
        //System.lineSeparator()为换行，好处是根据不同系统可以自动兼容
        StringBuilder str = new StringBuilder("MD5MeshModel: " + System.lineSeparator());
        str.append(getHeader()).append(System.lineSeparator());
        str.append(getJointInfo()).append(System.lineSeparator());
        for (MD5Mesh mesh : meshes) {
            str.append(mesh).append(System.lineSeparator());
        }
        return str.toString();
    }
    //从文件加载md5
    public static MD5Model parse(String meshModelFile) throws Exception {
        List<String> lines = Utils.readAllLines(meshModelFile);
        MD5Model result = new MD5Model();
        int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) {
            throw new Exception("Cannot parse empty file");
        }
        // 获取头文件Header的长度
        boolean headerEnd = false;
        int start = 0;
        for (int i = 0; i < numLines && !headerEnd; i++) {
            String line = lines.get(i);
            //String.trim()为去掉字符串收尾空格的函数；String.endsWith()为判断字符串是不是以特点字符结尾；
            headerEnd = line.trim().endsWith("{");
            start = i;
        }
        if (!headerEnd) {
            throw new Exception("Cannot find header");
        }
        //根据长度截取头文件，sublist（）函数不包含末尾值，所以上文以“{”判断是否为最后一行
        List<String> headerBlock = lines.subList(0, start);
        //通过类MD5ModelHeader解析头文件
        MD5ModelHeader header = MD5ModelHeader.parse(headerBlock);
        result.setHeader(header);

        // 解析其它块
        int blockStart = 0;
        boolean inBlock = false;
        String blockId = "";
        for (int i = start; i < numLines; i++) {
            String line = lines.get(i);
            if (line.endsWith("{")) {
                blockStart = i;
                //根据空格来获取块的ID
                blockId = line.substring(0, line.lastIndexOf(" "));
                inBlock = true;
            } else if (inBlock && line.endsWith("}")) {
                List<String> blockBody = lines.subList(blockStart + 1, i);
                parseBlock(result, blockId, blockBody);
                inBlock = false;
            }
        }
        return result;
    }
    //解析块的函数
    private static void parseBlock(MD5Model model, String blockId, List<String> blockBody) throws Exception {
        switch (blockId) {
            case "joints":
                MD5JointInfo jointInfo = MD5JointInfo.parse(blockBody);
                model.setJointInfo(jointInfo);
                break;
            case "mesh":
                MD5Mesh md5Mesh = MD5Mesh.parse(blockBody);
                model.getMeshes().add(md5Mesh);
                break;
            default:
                break;
        }

    }
}
