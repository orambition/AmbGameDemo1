package Core.Engine.sound;
//声音缓存类，用于加载音频文件
import Core.Engine.Utils;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundBuffer {
    private final int bufferId;//缓存id,句柄
    private ShortBuffer pcm = null;
    private ByteBuffer vorbis = null;
    //通过文件加载到al的Buffer中
    public SoundBuffer(String file) throws Exception {
        this.bufferId = alGenBuffers();//创建al缓存，类似gl的vbo
        //Vorbis，音频压缩格式
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {//创建缓存
            pcm = readVorbis(file, 32 * 1024, info);//读取文件到buffer中
            // 将buffer文件存入
            alBufferData(bufferId, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
        }
    }
    public int getBufferId() {
        return this.bufferId;
    }
    public void cleanUp() {
        alDeleteBuffers(this.bufferId);
        if (pcm != null) {
            MemoryUtil.memFree(pcm);
        }
    }
    private ShortBuffer readVorbis(String resource, int bufferSize, STBVorbisInfo info) throws Exception {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            vorbis = Utils.ioResourceToByteBuffer(resource, bufferSize);//将文件源转为ByteBuffer
            IntBuffer error = stack.mallocInt(1);
            long decoder = stb_vorbis_open_memory(vorbis, error, null);//从buffer中解码文件？
            if (decoder == NULL) {
                throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
            }
            stb_vorbis_get_info(decoder, info);//获取压缩文件信息
            //垃圾stb库，连个注释都没有
            //主要功能就读取ogg压缩文件到buffer中
            int channels = info.channels();
            int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);
            pcm = MemoryUtil.memAllocShort(lengthSamples);
            pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
            stb_vorbis_close(decoder);
            return pcm;
        }
    }
}
