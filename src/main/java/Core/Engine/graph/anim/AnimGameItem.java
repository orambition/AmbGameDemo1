package Core.Engine.graph.anim;
//动作物体类，用于加载md5模型动画
import Core.Engine.graph.Mesh;
import Core.Engine.items.GameItem;
import org.joml.Matrix4f;

import java.util.List;

public class AnimGameItem extends GameItem {
    private int currentFrame;
    private List<AnimatedFrame> frames;//每帧中关节的位置
    private List<Matrix4f> invJointMatrices;//关节初始位置
    //网格、每帧中关节的位置、关节初始位置
    public AnimGameItem(Mesh[] meshes, List<AnimatedFrame> frames, List<Matrix4f> invJointMatrices) {
        super(meshes);
        this.frames = frames;
        this.invJointMatrices = invJointMatrices;
        currentFrame = 0;
    }
    public List<AnimatedFrame> getFrames() {
        return frames;
    }
    public void setFrames(List<AnimatedFrame> frames) {
        this.frames = frames;
    }
    public AnimatedFrame getCurrentFrame() {
        return this.frames.get(currentFrame);
    }
    public AnimatedFrame getNextFrame() {
        int nextFrame = currentFrame + 1;
        if ( nextFrame > frames.size() - 1) {
            nextFrame = 0;
        }
        return this.frames.get(nextFrame);
    }
    public void nextFrame() {
        int nextFrame = currentFrame + 1;
        if ( nextFrame > frames.size() - 1) {
            currentFrame = 0;
        } else {
            currentFrame = nextFrame;
        }
    }
    public List<Matrix4f> getInvJointMatrices() {
        return invJointMatrices;
    }
}
