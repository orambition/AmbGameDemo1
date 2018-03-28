package Core.Engine.sound;
//音源类，发出声音的位置，多个音源可以共享一个声音缓存
//声音位置，增益，播放、停止、暂停的控制方法
//该类很简单，就是通过al函数控制sourceId
import org.joml.Vector3f;
import static org.lwjgl.openal.AL10.*;

public class SoundSource {
    private final int sourceId;
    public SoundSource(boolean loop, boolean relative) {
        this.sourceId = alGenSources();
        if (loop) {//循环播放
            alSourcei(sourceId, AL_LOOPING, AL_TRUE);
        }
        if (relative) {//相对位置，如背景音乐，不随距离衰减
            alSourcei(sourceId, AL_SOURCE_RELATIVE, AL_TRUE);
        }
    }
    //设置声音缓存，也就是要播放的音乐
    public void setBuffer(int bufferId) {
        stop();
        alSourcei(sourceId, AL_BUFFER, bufferId);
    }
    //设置音源位置
    public void setPosition(Vector3f position) {
        alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
    }
    //设置声音速度，所有方向
    public void setSpeed(Vector3f speed) {
        alSource3f(sourceId, AL_VELOCITY, speed.x, speed.y, speed.z);
    }
    //设置增益，放大或缩小声音
    public void setGain(float gain) {
        alSourcef(sourceId, AL_GAIN, gain);
    }
    //通用设置函数，根据参数名设置值
    public void setProperty(int param, float value) {
        alSourcef(sourceId, param, value);
    }
    //播放
    public void play() {
        alSourcePlay(sourceId);
    }
    //是否播放中
    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }
    //暂停
    public void pause() {
        alSourcePause(sourceId);
    }
    //停止
    public void stop() {
        alSourceStop(sourceId);
    }
    public void cleanUp() {
        stop();
        alDeleteSources(sourceId);
    }
}
