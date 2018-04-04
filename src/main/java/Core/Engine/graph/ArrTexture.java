package Core.Engine.graph;
//数组纹理类，首次应用与绘制分层的深度图
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;

public class ArrTexture {
    private final int[] ids;//纹理id

    private final int width;

    private final int height;
    //根据数量、大小、像素值创建多个纹理
    public ArrTexture(int numTextures, int width, int height, int pixelFormat) throws Exception {
        ids = new int[numTextures];
        glGenTextures(ids);//创建纹理，并将纹理句柄（id）存入数组中
        this.width = width;
        this.height = height;

        for (int i = 0; i < numTextures; i++) {
            //设置每个纹理的参数
            glBindTexture(GL_TEXTURE_2D, ids[i]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, this.width, this.height, 0, pixelFormat, GL_FLOAT, (ByteBuffer) null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            //完成以上步骤已将id为ids[i]的纹理存入显存
        }
    }
    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int[] getIds() {
        return ids;
    }

    public void cleanUp() {
        for (int id : ids) {
            glDeleteTextures(id);
        }
    }
}
