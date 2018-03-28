package Core.Engine.sound;
//听众类，接受声音的位置和朝向
import org.joml.Vector3f;
import static org.lwjgl.openal.AL10.*;

public class SoundListener {
    //听众不用创建，有一个默认的听众
    public SoundListener() {
        this(new Vector3f(0, 0, 0));
    }
    public SoundListener(Vector3f position) {
        //位置
        alListener3f(AL_POSITION, position.x, position.y, position.z);
        //速度，方向
        alListener3f(AL_VELOCITY, 0, 0, 0);
    }
    public void setSpeed(Vector3f speed) {
        alListener3f(AL_VELOCITY, speed.x, speed.y, speed.z);
    }
    public void setPosition(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
    }
    public void setOrientation(Vector3f at, Vector3f up) {
        //at朝向，up是仰俯角
        float[] data = new float[6];
        data[0] = at.x;
        data[1] = at.y;
        data[2] = at.z;
        data[3] = up.x;
        data[4] = up.y;
        data[5] = up.z;
        alListenerfv(AL_ORIENTATION, data);
    }
}
