package Core.Engine.graph;
//纹理类，用于加载纹理，并返回纹理id

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {

    private final int id;
    private final int width;
    private final int height;
    //根据大小创建空的材质，用于绘制深度图
    public Texture(int width, int height, int pixelFormat) throws Exception {
        this.id = glGenTextures();
        this.width = width;
        this.height = height;
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, this.width, this.height, 0, pixelFormat, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);//超出【0，1】时不重复
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }


    public Texture(String fileName) throws Exception {
        this(Texture.class.getResourceAsStream(fileName));
    }

    public Texture(InputStream is) throws Exception {
        // Load Texture file
        PNGDecoder decoder = new PNGDecoder(is);
        this.width = decoder.getWidth();//获取长宽，在建立hud时首次使用
        this.height = decoder.getHeight();
        // Load texture contents into a byte buffer
        // 每个RGBA像素点包含 红、绿、蓝、透明度4字节的信息所以*4
        ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
        /*解码函数
         * 缓冲区：ByteBuffer，将解码后的图像。
         * 步幅：指定从一行的开始到下一行的开始字节的距离。
         * 格式：目标格式的图像进行解码（RGBA）。*/
        decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
        buf.flip();

        /*Create a new OpenGL texture
         * 并将纹理上传到显存中首先!
         * 首先，需要创建一个新的纹理标识符。
         * 每个与该纹理相关的操作都将使用该标识符
         * 因此我们需要绑定它。*/
        int textureId = glGenTextures();
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, textureId);
        // 然后，需要告诉OpenGL如何打开我们的RGBA字节。每个组件的大小是一个字节
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        //当一个像素与一个纹理坐标没有直接的一对一关联时，它会选择最近的纹理坐标点。
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        /*最后，更新纹理
         * 目标：指定目标纹理（类型）。在这种情况下：gl_texture_2d。
         * 缩放级别：指定详细的数字的水平。0级为基础的图像水平。
         * 内部格式：指定的纹理颜色分量数。
         * 宽度：指定纹理图像的宽度
         * 高度：指定的纹理图像的高度。
         * 边界：此值必须为零。
         * 格式：指定的像素数据的格式：RGBA在这种情况下，
         * 类型：指定的像素数据的数据类型，我们使用无符号字节存储数据。
         * 数据缓冲区。*/
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);

        // Generate Mip Map
        glGenerateMipmap(GL_TEXTURE_2D);
        this.id = textureId;
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public int getId() {
        return id;
    }
    //private static int loadTexture(String fileName) throws Exception { }
    public void cleanup() {
        glDeleteTextures(id);
    }
}
