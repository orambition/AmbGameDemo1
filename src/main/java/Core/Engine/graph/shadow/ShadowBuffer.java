package Core.Engine.graph.shadow;
//深度图Buffer类，用于将渲染信息传入显存，原ShadowMap
//创建阴影映射FBO（Frame Buffers Objects）

import Core.Engine.graph.ArrTexture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;

public class ShadowBuffer {
    public static final int SHADOW_MAP_WIDTH = (int)Math.pow(65, 2);

    public static final int SHADOW_MAP_HEIGHT = SHADOW_MAP_WIDTH;

    private final int depthMapFBO;
    //深度图的数组纹理，每层绘制深度图到相应的纹理中
    private final ArrTexture depthMap;

    public ShadowBuffer() throws Exception {
        // 1创建深度图FBO，FBO可以将深度信息存入纹理中
        depthMapFBO = glGenFramebuffers();
        // 2创建纹理，根据层数和大小创建相应个数的深度图纹理
        depthMap = new ArrTexture(ShadowRenderer.NUM_CASCADES, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, GL_DEPTH_COMPONENT);
        // 3绑定刚刚创建的FBO
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        // 4将纹理附加到FBO中,？此处只附加了一个，不知道为什么
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap.getIds()[0], 0);
        // 只需要深度，所以将颜色等其他的buffer设置为none
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("Could not create FrameBuffer");
        }
        //解绑FBO
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public ArrTexture getDepthMapTexture() {
        return depthMap;
    }

    public int getDepthMapFBO() {
        return depthMapFBO;
    }

    public void bindTextures(int start) {
        for (int i = 0; i < ShadowRenderer.NUM_CASCADES; i++) {
            // 激活start + i号纹理单元
            glActiveTexture(start + i);
            // 将depthMap.getIds()[i]纹理与start + i号纹理单元绑定
            glBindTexture(GL_TEXTURE_2D, depthMap.getIds()[i]);
        }
    }

    public void cleanUp() {
        glDeleteFramebuffers(depthMapFBO);
        depthMap.cleanUp();
    }
}
