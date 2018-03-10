package Core.Engine.loaders.md5;
//加载md5模型文件中的mesh

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5Mesh {
    //匹配材质
    private static final Pattern PATTERN_SHADER = Pattern.compile("\\s*shader\\s*\\\"([^\\\"]+)\\\"");
    //匹配顶点顺序，如：vert 0 ( 0.422449 0.980429 ) 0 8
    private static final Pattern PATTERN_VERTEX = Pattern.compile("\\s*vert\\s*(\\d+)\\s*\\(\\s*("
            + MD5Utils.FLOAT_REGEXP + ")\\s*(" + MD5Utils.FLOAT_REGEXP + ")\\s*\\)\\s*(\\d+)\\s*(\\d+)");
    //匹配三角形，如：tri 1873 5619 5621 5620
    private static final Pattern PATTERN_TRI = Pattern.compile("\\s*tri\\s*(\\d+)\\s*(\\d+)\\s*(\\d+)\\s*(\\d+)");
    //匹配顶点权重，如：weight 2341 9 0.122088 ( 5.125965 -0.424148 -0.252234 )
    private static final Pattern PATTERN_WEIGHT = Pattern.compile("\\s*weight\\s*(\\d+)\\s*(\\d+)\\s*" +
            "(" + MD5Utils.FLOAT_REGEXP + ")\\s*" + MD5Utils.VECTOR3_REGEXP );

    private String texture;
    private List<MD5Vertex> vertices;
    private List<MD5Triangle> triangles;
    private List<MD5Weight> weights;

    public MD5Mesh() {
        this.vertices = new ArrayList<>();
        this.triangles = new ArrayList<>();
        this.weights = new ArrayList<>();
    }
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("mesh [" + System.lineSeparator());
        str.append("texture: ").append(texture).append(System.lineSeparator());
        str.append("vertices [").append(System.lineSeparator());
        for (MD5Vertex vertex : vertices) {
            str.append(vertex).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        str.append("triangles [").append(System.lineSeparator());
        for (MD5Triangle triangle : triangles) {
            str.append(triangle).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        str.append("weights [").append(System.lineSeparator());
        for (MD5Weight weight : weights) {
            str.append(weight).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        return str.toString();
    }
    //从块中解析模型文件，同header和joint
    public static MD5Mesh parse(List<String> meshBlock) {
        MD5Mesh mesh = new MD5Mesh();
        List<MD5Vertex> vertices = mesh.getVertices();
        List<MD5Triangle> triangles = mesh.getTriangles();
        List<MD5Weight> weights = mesh.getWeights();

        for (String line : meshBlock) {
            if (line.contains("shader")) {
                Matcher textureMatcher = PATTERN_SHADER.matcher(line);
                if (textureMatcher.matches()) {
                    mesh.setTexture(textureMatcher.group(1));
                }
            } else if (line.contains("vert")) {
                Matcher vertexMatcher = PATTERN_VERTEX.matcher(line);
                if (vertexMatcher.matches()) {
                    //顶点内部类
                    MD5Vertex vertex = new MD5Vertex();
                    vertex.setIndex(Integer.parseInt(vertexMatcher.group(1)));//订单索引
                    float x = Float.parseFloat(vertexMatcher.group(2));
                    float y = Float.parseFloat(vertexMatcher.group(3));
                    vertex.setTextCoords(new Vector2f(x, y));//材质坐标
                    vertex.setStartWeight(Integer.parseInt(vertexMatcher.group(4)));//权重id
                    vertex.setWeightCount(Integer.parseInt(vertexMatcher.group(5)));//权重数量
                    vertices.add(vertex);
                }
            } else if (line.contains("tri")) {
                Matcher triMatcher = PATTERN_TRI.matcher(line);
                if (triMatcher.matches()) {
                    //三角面内部类
                    MD5Triangle triangle = new MD5Triangle();
                    triangle.setIndex(Integer.parseInt(triMatcher.group(1)));//id
                    triangle.setVertex0(Integer.parseInt(triMatcher.group(2)));//点1
                    triangle.setVertex1(Integer.parseInt(triMatcher.group(3)));//点2
                    triangle.setVertex2(Integer.parseInt(triMatcher.group(4)));//点3
                    triangles.add(triangle);
                }
            } else if (line.contains("weight")) {
                Matcher weightMatcher = PATTERN_WEIGHT.matcher(line);
                if (weightMatcher.matches()) {
                    //权重内部类
                    MD5Weight weight = new MD5Weight();
                    weight.setIndex(Integer.parseInt(weightMatcher.group(1)));//id
                    weight.setJointIndex(Integer.parseInt(weightMatcher.group(2)));//关节id
                    weight.setBias(Float.parseFloat(weightMatcher.group(3)));//权重
                    float x = Float.parseFloat(weightMatcher.group(4));
                    float y = Float.parseFloat(weightMatcher.group(5));
                    float z = Float.parseFloat(weightMatcher.group(6));
                    weight.setPosition(new Vector3f(x, y, z));//位置
                    weights.add(weight);
                }
            }
        }
        return mesh;
    }

    public String getTexture() {
        return texture;
    }
    public void setTexture(String texture) {
        this.texture = texture;
    }
    public List<MD5Vertex> getVertices() {
        return vertices;
    }
    public void setVertices(List<MD5Vertex> vertices) {
        this.vertices = vertices;
    }
    public List<MD5Triangle> getTriangles() {
        return triangles;
    }
    public void setTriangles(List<MD5Triangle> triangles) {
        this.triangles = triangles;
    }
    public List<MD5Weight> getWeights() {
        return weights;
    }
    public void setWeights(List<MD5Weight> weights) {
        this.weights = weights;
    }
    //顶点内部类
    public static class MD5Vertex {
        private int index;
        private Vector2f textCoords;
        private int startWeight;
        private int weightCount;
        public int getIndex() {
            return index;
        }
        public void setIndex(int index) {
            this.index = index;
        }
        public Vector2f getTextCoords() {
            return textCoords;
        }
        public void setTextCoords(Vector2f textCoords) {
            this.textCoords = textCoords;
        }
        public int getStartWeight() {
            return startWeight;
        }
        public void setStartWeight(int startWeight) {
            this.startWeight = startWeight;
        }
        public int getWeightCount() {
            return weightCount;
        }
        public void setWeightCount(int weightCount) {
            this.weightCount = weightCount;
        }
        @Override
        public String toString() {
            return "[index: " + index + ", textCoods: " + textCoords
                    + ", startWeight: " + startWeight + ", weightCount: " + weightCount + "]";
        }
    }
    //三角面内部类
    public static class MD5Triangle {
        private int index;
        private int vertex0;
        private int vertex1;
        private int vertex2;
        public int getIndex() {
            return index;
        }
        public void setIndex(int index) {
            this.index = index;
        }
        public int getVertex0() {
            return vertex0;
        }
        public void setVertex0(int vertex0) {
            this.vertex0 = vertex0;
        }
        public int getVertex1() {
            return vertex1;
        }
        public void setVertex1(int vertex1) {
            this.vertex1 = vertex1;
        }
        public int getVertex2() {
            return vertex2;
        }
        public void setVertex2(int vertex2) {
            this.vertex2 = vertex2;
        }
        @Override
        public String toString() {
            return "[index: " + index + ", vertex0: " + vertex0
                    + ", vertex1: " + vertex1 + ", vertex2: " + vertex2 + "]";
        }
    }
    //权重内部类
    public static class MD5Weight {
        private int index;
        private int jointIndex;
        private float bias;
        private Vector3f position;
        public int getIndex() {
            return index;
        }
        public void setIndex(int index) {
            this.index = index;
        }
        public int getJointIndex() {
            return jointIndex;
        }
        public void setJointIndex(int jointIndex) {
            this.jointIndex = jointIndex;
        }
        public float getBias() {
            return bias;
        }
        public void setBias(float bias) {
            this.bias = bias;
        }
        public Vector3f getPosition() {
            return position;
        }
        public void setPosition(Vector3f position) {
            this.position = position;
        }
        @Override
        public String toString() {
            return "[index: " + index + ", jointIndex: " + jointIndex
                    + ", bias: " + bias + ", position: " + position + "]";
        }
    }
}
