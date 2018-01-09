package Core.Engine.items;
//地形类，根据高度图生产的MESH拼接成地形

import Core.Engine.graph.HeightMapMesh;
import de.matthiasmann.twl.utils.PNGDecoder;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

public class Terrain {
    private final GameItem[] gameItems;
    private final int terrainSize;//行列数
    private final int verticesPerCol;//每小块的像素列数
    private final int verticesPerRow;//没小块的像素行数
    private final HeightMapMesh heightMapMesh;
    private final Box2D[][] boundingBoxes;

    //根据terrainSize确定多少行列，并绘制相应的地形快，这里的地形是相同地形快的平铺！
    public Terrain(int terrainSize, float scale, float minY, float maxY, String heightMapFile, String textureFile, int textInc) throws Exception {
        this.terrainSize = terrainSize;//绘制的行列个数
        gameItems = new GameItem[terrainSize * terrainSize];

        //加载高度图文件
        PNGDecoder decoder = new PNGDecoder(getClass().getResourceAsStream(heightMapFile));
        int height = decoder.getHeight();//高度图的宽高
        int width = decoder.getWidth();
        //将图片放入缓存
        ByteBuffer buf = ByteBuffer.allocateDirect(
                4 * decoder.getWidth() * decoder.getHeight());
        decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
        buf.flip();

        verticesPerCol = width - 1;
        verticesPerRow = height - 1;
        //根据高度图创建mesh
        heightMapMesh = new HeightMapMesh(minY, maxY, buf, width, height, textureFile, textInc);
        //存储每块的边界，用于计算当前玩家在哪块
        boundingBoxes = new Box2D[terrainSize][terrainSize];
        for (int row = 0; row < terrainSize; row++) {
            for (int col = 0; col < terrainSize; col++) {
                float xDisplacement = (col - ((float) terrainSize - 1) / (float) 2) * scale * HeightMapMesh.getXLength();
                float zDisplacement = (row - ((float) terrainSize - 1) / (float) 2) * scale * HeightMapMesh.getZLength();
                GameItem terrainBlock = new GameItem(heightMapMesh.getMesh());
                terrainBlock.setScale(scale);
                terrainBlock.setPosition(xDisplacement, 0, zDisplacement);
                gameItems[row * terrainSize + col] = terrainBlock;
                boundingBoxes[row][col] = getBoundingBox(terrainBlock);
            }
        }
    }
    public GameItem[] getGameItems() {
        return gameItems;
    }
    //获取地形边界
    private Box2D getBoundingBox(GameItem terrainBlock) {
        float scale = terrainBlock.getScale();
        Vector3f position = terrainBlock.getPosition();
        float topLeftX = HeightMapMesh.STARTX * scale + position.x;
        float topLeftZ = HeightMapMesh.STARTZ * scale + position.z;
        float width = Math.abs(HeightMapMesh.STARTX * 2) * scale;
        float height = Math.abs(HeightMapMesh.STARTZ * 2) * scale;
        Box2D boundingBox = new Box2D(topLeftX, topLeftZ, width, height);
        return boundingBox;
    }
    //一个块的左上角坐标，以及宽高
    static class Box2D {
        public float x;
        public float y;
        public float width;
        public float height;
        public Box2D(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        //判断一个点是否在该块内
        public boolean contains(float x2, float y2) {
            return x2 >= x
                    && y2 >= y
                    && x2 < x + width
                    && y2 < y + height;
        }
    }

    //获取输入点的高度
    public float getHeight(Vector3f position) {
        float result = Float.MIN_VALUE;
        // For each terrain block we get the bounding box, translate it to view coodinates
        // and check if the position is contained in that bounding box
        Box2D boundingBox = null;
        boolean found = false;
        GameItem terrainBlock = null;
        //查找点在哪个块内
        for (int row = 0; row < terrainSize && !found; row++) {
            for (int col = 0; col < terrainSize && !found; col++) {
                terrainBlock = gameItems[row * terrainSize + col];
                boundingBox = boundingBoxes[row][col];
                found = boundingBox.contains(position.x, position.z);
            }
        }
        //找到后计算该处高度
        if (found) {
            //获得所在的三角形
            Vector3f[] triangle = getTriangle(position, boundingBox, terrainBlock);
            //获取三角形在该位置的高度
            result = interpolateHeight(triangle[0], triangle[1], triangle[2], position.x, position.z);
        }
        return result;
    }
    //获取点所在的三角形，要寻找的点、所在地形块的边界也就是位置、该地型块的物体模型
    protected Vector3f[] getTriangle(Vector3f position, Box2D boundingBox, GameItem terrainBlock) {
        // 获取每个小块内点的间隔
        float cellWidth = boundingBox.width / (float) verticesPerCol;
        float cellHeight = boundingBox.height / (float) verticesPerRow;
        //获取该点在该小块的第几行列，注意此处的int转型
        int col = (int) ((position.x - boundingBox.x) / cellWidth);
        int row = (int) ((position.z - boundingBox.y) / cellHeight);

        Vector3f[] triangle = new Vector3f[3];
        //确定三角形对角线的两点
        triangle[1] = new Vector3f(
                boundingBox.x + col * cellWidth,
                getWorldHeight(row + 1, col, terrainBlock),
                boundingBox.y + (row + 1) * cellHeight);
        triangle[2] = new Vector3f(
                boundingBox.x + (col + 1) * cellWidth,
                getWorldHeight(row, col + 1, terrainBlock),
                boundingBox.y + row * cellHeight);
        //确定剩余的一个顶点，因为上文中将col和row确定到了四边形的左上角，而一个四边形由两个三角形组成
        if (position.z < getDiagonalZCoord(triangle[1].x, triangle[1].z, triangle[2].x, triangle[2].z, position.x)) {
            triangle[0] = new Vector3f(
                    boundingBox.x + col * cellWidth,
                    getWorldHeight(row, col, terrainBlock),
                    boundingBox.y + row * cellHeight);
        } else {
            triangle[0] = new Vector3f(
                    boundingBox.x + (col + 1) * cellWidth,
                    getWorldHeight(row + 2, col + 1, terrainBlock),
                    boundingBox.y + (row + 1) * cellHeight);
        }
        return triangle;
    }
    //该函数很妙，获取该四边形对角线上，在x处点的y值
    protected float getDiagonalZCoord(float x1, float z1, float x2, float z2, float x) {
        float z = ((z1 - z2) / (x1 - x2)) * (x - x1) + z1;
        return z;
    }
    //获取该物体中一个点的世界坐标y值
    protected float getWorldHeight(int row, int col, GameItem gameItem) {
        float y = heightMapMesh.getHeight(row, col);
        return y * gameItem.getScale() + gameItem.getPosition().y;
    }
    //根据三点确定一个三角形函数，并根据xz求y值
    protected float interpolateHeight(Vector3f pA, Vector3f pB, Vector3f pC, float x, float z) {
        // Plane equation ax+by+cz+d=0
        float a = (pB.y - pA.y) * (pC.z - pA.z) - (pC.y - pA.y) * (pB.z - pA.z);
        float b = (pB.z - pA.z) * (pC.x - pA.x) - (pC.z - pA.z) * (pB.x - pA.x);
        float c = (pB.x - pA.x) * (pC.y - pA.y) - (pC.x - pA.x) * (pB.y - pA.y);
        float d = -(a * pA.x + b * pA.y + c * pA.z);
        // y = (-d -ax -cz) / b
        float y = (-d - a * x - c * z) / b;
        return y;
    }
}
