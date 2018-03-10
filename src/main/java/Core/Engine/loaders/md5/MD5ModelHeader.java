package Core.Engine.loaders.md5;
//md5文件中头文件header的专属类
import java.util.List;

public class MD5ModelHeader {
    //md5文件特有属性
    private String version;
    private String commandLine;
    private int numJoints;//关节（骨骼）数量
    private int numMeshes;

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getCommandLine() {
        return commandLine;
    }
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }
    public int getNumJoints() {
        return numJoints;
    }
    public void setNumJoints(int numJoints) {
        this.numJoints = numJoints;
    }
    public int getNumMeshes() {
        return numMeshes;
    }
    public void setNumMeshes(int numMeshes) {
        this.numMeshes = numMeshes;
    }
    //md5Model类中重写了toString方法，并调用了此类中的方法
    @Override
    public String toString() {
        return "[version: " + version + ", commandLine: " + commandLine +
                ", numJoints: " + numJoints + ", numMeshes: " + numMeshes + "]";
    }
    //从文件流(截取的String)中加载头文件
    public static MD5ModelHeader parse(List<String> lines) throws Exception {
        MD5ModelHeader header = new MD5ModelHeader();
        int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) {
            throw new Exception("Cannot parse empty file");
        }
        boolean finishHeader = false;
        for (int i = 0; i < numLines && !finishHeader; i++) {
            String line = lines.get(i);
            /**此处字符串按照正则表达式分割
             * "\\d"表示 0-9 的数字,
             * "\\s"表示 空格,回车,换行等空白符,
             * "\\w"表示单词字符(数字字母下划线)
             * +号表示一个或多个的意思,
             */
            String[] tokens = line.split("\\s+");
            int numTokens = tokens != null ? tokens.length : 0;

            if (numTokens > 1) {
                String paramName = tokens[0];
                String paramValue = tokens[1];
                switch (paramName) {
                    case "MD5Version":
                        header.setVersion(paramValue);
                        break;
                    case "commandline":
                        header.setCommandLine(paramValue);
                        break;
                    case "numJoints":
                        header.setNumJoints(Integer.parseInt(paramValue));
                        break;
                    case "numMeshes":
                        header.setNumMeshes(Integer.parseInt(paramValue));
                        break;
                    case "joints":
                        finishHeader = true;
                        break;
                }
            }
        }
        return header;
    }
}
