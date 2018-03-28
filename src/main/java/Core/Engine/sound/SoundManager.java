package Core.Engine.sound;
//声音管理类
import Core.Engine.graph.Camera;
import Core.Engine.graph.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundManager {
    private long device;
    private long context;
    private SoundListener listener;//听众列表
    private final List<SoundBuffer> soundBufferList;//声音缓存列表
    private final Map<String, SoundSource> soundSourceMap;//音源Map，通过名称检索音源
    private final Matrix4f cameraMatrix;//摄像机矩阵

    public SoundManager() {
        soundBufferList = new ArrayList<>();
        soundSourceMap = new HashMap<>();
        cameraMatrix = new Matrix4f();
    }
    public void init() throws Exception {
        this.device = alcOpenDevice((ByteBuffer) null);//打开默认的设备
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);//为设备创建功能
        this.context = alcCreateContext(device, (IntBuffer) null);//创建上下文句柄
        if (context == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        alcMakeContextCurrent(context);//设置句柄为当前的上下文
        AL.createCapabilities(deviceCaps);
    }
    //音源map管理
    public void addSoundSource(String name, SoundSource soundSource) {
        this.soundSourceMap.put(name, soundSource);
    }
    public SoundSource getSoundSource(String name) {
        return this.soundSourceMap.get(name);
    }
    public void removeSoundSource(String name) {
        this.soundSourceMap.remove(name);
    }
    //播放特定音源
    public void playSoundSource(String name) {
        SoundSource soundSource = this.soundSourceMap.get(name);
        if (soundSource != null && !soundSource.isPlaying()) {
            soundSource.play();
        }
    }

    public void addSoundBuffer(SoundBuffer soundBuffer) {
        this.soundBufferList.add(soundBuffer);
    }
    public SoundListener getListener() {
        return this.listener;
    }
    public void setListener(SoundListener listener) {
        this.listener = listener;
    }
    //更新听众位置，将听众设置在摄像机的位置
    public void updateListenerPosition(Camera camera) {
        // Update camera matrix with camera data
        Transformation.updateGenericViewMatrix(camera.getPosition(), camera.getRotation(), cameraMatrix);
        listener.setPosition(camera.getPosition());
        //通过摄像机矩阵得出朝向和俯仰角
        Vector3f at = new Vector3f();
        cameraMatrix.positiveZ(at).negate();
        Vector3f up = new Vector3f();
        cameraMatrix.positiveY(up);
        listener.setOrientation(at, up);
    }
    //设置衰减模式
    public void setAttenuationModel(int model) {
        alDistanceModel(model);
    }
    public void cleanUp() {
        for (SoundSource soundSource : soundSourceMap.values()) {
            soundSource.cleanUp();
        }
        soundSourceMap.clear();
        for (SoundBuffer soundBuffer : soundBufferList) {
            soundBuffer.cleanUp();
        }
        soundBufferList.clear();
        if (context != NULL) {
            alcDestroyContext(context);
        }
        if (device != NULL) {
            alcCloseDevice(device);
        }
    }
}
