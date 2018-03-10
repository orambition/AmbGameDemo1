package Core.Engine.loaders.md5;
//md5文件中关节块的专属类，用于解析joint块
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5JointInfo {
    private List<MD5JointData> joints;//关节内部类

    public List<MD5JointData> getJoints() {
        return joints;
    }
    public void setJoints(List<MD5JointData> joints) {
        this.joints = joints;
    }
    //该方法同header类相似，都为md5Model类中调用
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("joints [" + System.lineSeparator());
        for (MD5JointData joint : joints) {
            str.append(joint).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        return str.toString();
    }
    //加载函数
    public static MD5JointInfo parse(List<String> blockBody) {
        MD5JointInfo result = new MD5JointInfo();
        List<MD5JointData> joints = new ArrayList<>();
        for (String line : blockBody) {
            //调用内部类中的解析函数，解析一行关节文件
            MD5JointData jointData = MD5JointData.parseLine(line);
            if (jointData != null) {
                joints.add(jointData);
            }
        }
        result.setJoints(joints);
        return result;
    }
    //关节内部类
    public static class MD5JointData {
        private static final String PARENT_INDEX_REGEXP = "([-]?\\d+)";//父id匹配规则：可以有-号，至少有一个数字
        private static final String NAME_REGEXP = "\\\"([^\\\"]+)\\\"";//名称匹配规则：\" 非\"的其他字符 \"
        private static final String JOINT_REGEXP = "\\s*" + NAME_REGEXP + "\\s*" + PARENT_INDEX_REGEXP + "\\s*"
                + MD5Utils.VECTOR3_REGEXP + "\\s*" + MD5Utils.VECTOR3_REGEXP + ".*";
        //匹配规则：可以有空白符+名字+空白符+父id+空白符+（小数 小数 小数）+空白符 + （小数 小数 小数）+任意字符
        //如："XXX"	-1 ( 0.000000 0.145125 4.756525 ) ( -0.633072 -0.000000 -0.000000 )		//
        //Pattern类用于编译正则表达式后创建一个匹配模式
        //Matcher类使用Pattern实例提供的模式信息对正则表达式进行匹配
        //http://blog.csdn.net/yin380697242/article/details/52049999
        private static final Pattern PATTERN_JOINT = Pattern.compile(JOINT_REGEXP);

        private String name;
        private int parentIndex;
        private Vector3f position;
        private Quaternionf orientation;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getParentIndex() {
            return parentIndex;
        }
        public void setParentIndex(int parentIndex) {
            this.parentIndex = parentIndex;
        }
        public Vector3f getPosition() {
            return position;
        }
        public void setPosition(Vector3f position) {
            this.position = position;
        }
        public Quaternionf getOrientation() {
            return orientation;
        }
        public void setOrientation(Vector3f vec) {
            this.orientation = MD5Utils.calculateQuaternion(vec);
        }

        @Override
        public String toString() {
            return "[name: " + name + ", parentIndex: " + parentIndex + ", position: " + position + ", orientation: " + orientation + "]";
        }
        //加载函数
        public static MD5JointData parseLine(String line) {
            MD5JointData result = null;
            //Matcher类使用Pattern实例提供的模式信息对正则表达式进行匹配
            //通过PATTERN_JOINT为line创建Matcher对象
            Matcher matcher = PATTERN_JOINT.matcher(line);
            //匹配成功返回true
            if (matcher.matches()) {
                result = new MD5JointData();
                result.setName(matcher.group(1));
                result.setParentIndex(Integer.parseInt(matcher.group(2)));
                float x = Float.parseFloat(matcher.group(3));
                float y = Float.parseFloat(matcher.group(4));
                float z = Float.parseFloat(matcher.group(5));
                result.setPosition(new Vector3f(x, y, z));

                x = Float.parseFloat(matcher.group(6));
                y = Float.parseFloat(matcher.group(7));
                z = Float.parseFloat(matcher.group(8));
                result.setOrientation(new Vector3f(x, y, z));
            }
            return result;
        }
    }
}
