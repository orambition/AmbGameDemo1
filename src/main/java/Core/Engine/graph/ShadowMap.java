package Core.Engine.graph;
//阴影图类，创建阴影映射FBO（Frame Buffers Objects）
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowMap {
    public static final int SHADOW_MAP_WIDTH = 1024;
    public static final int SHADOW_MAP_HEIGHT = 1024;

    private final int depthMapFBO;
    private final Texture depthMap;

    public ShadowMap() throws Exception {
        // 创建一个FBO去绘制深度图
        depthMapFBO = glGenFramebuffers();
        // 创建纹理形式的深度图
        depthMap = new Texture(SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, GL_DEPTH_COMPONENT);
        // 绑定深度图的FBO
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap.getId(), 0);
        // 只需要深度，所以将颜色等其他的buffer设置为none
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("Could not create FrameBuffer");
        }
        // Unbind
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    public Texture getDepthMapTexture() {
        return depthMap;
    }
    public int getDepthMapFBO() {
        return depthMapFBO;
    }
    public void cleanup() {
        glDeleteFramebuffers(depthMapFBO);
        depthMap.cleanup();
    }
}
