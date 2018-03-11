package Core.Engine.loaders.md5;
//用于解析md5anim文件中基础帧的专属类，与MD5AnimHeader类相似
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5BaseFrame {
    private List<MD5BaseFrameData> frameDataList;
    public List<MD5BaseFrameData> getFrameDataList() {
        return frameDataList;
    }
    public void setFrameDataList(List<MD5BaseFrameData> frameDataList) {
        this.frameDataList = frameDataList;
    }
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("base frame [" + System.lineSeparator());
        for (MD5BaseFrameData frameData : frameDataList) {
            str.append(frameData).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        return str.toString();
    }
    public static MD5BaseFrame parse(List<String> blockBody) {
        MD5BaseFrame result = new MD5BaseFrame();
        List<MD5BaseFrameData> frameInfoList = new ArrayList<>();
        result.setFrameDataList(frameInfoList);
        for (String line : blockBody) {
            MD5BaseFrameData frameInfo = MD5BaseFrameData.parseLine(line);
            if (frameInfo != null) {
                frameInfoList.add(frameInfo);
            }
        }
        return result;
    }
    //基础帧，一个动画开始时所有关节的位置和方向
    public static class MD5BaseFrameData {
        private static final Pattern PATTERN_BASEFRAME = Pattern.compile("\\s*" + MD5Utils.VECTOR3_REGEXP + "\\s*" + MD5Utils.VECTOR3_REGEXP + ".*");
        //匹配示例：( 0.000000 0.000000 0.000000 ) ( 0.652890 0.271542 -0.271542 )
        private Vector3f position;
        private Quaternionf orientation;
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
            return "[position: " + position + ", orientation: " + orientation + "]";
        }
        public static MD5BaseFrameData parseLine(String line) {
            Matcher matcher = PATTERN_BASEFRAME.matcher(line);
            MD5BaseFrameData result = null;
            if (matcher.matches()) {
                result = new MD5BaseFrameData();
                float x = Float.parseFloat(matcher.group(1));
                float y = Float.parseFloat(matcher.group(2));
                float z = Float.parseFloat(matcher.group(3));
                result.setPosition(new Vector3f(x, y, z));
                x = Float.parseFloat(matcher.group(4));
                y = Float.parseFloat(matcher.group(5));
                z = Float.parseFloat(matcher.group(6));
                result.setOrientation(new Vector3f(x, y, z));
            }
            return result;
        }
    }
}
