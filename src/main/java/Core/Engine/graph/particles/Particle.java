package Core.Engine.graph.particles;
//粒子类，作为特殊的item，其集成自gameItem

import Core.Engine.graph.Mesh;
import Core.Engine.graph.Texture;
import Core.Engine.items.GameItem;
import org.joml.Vector3f;

public class Particle extends GameItem{
    private Vector3f speed;//速度
    private long ttl;//生存时间time to live（毫秒）

    private long updateTextureMillis;//每个纹理块的停留时间
    private long currentAnimTimeMillis;//当前纹理块的停留时间
    private int animFrames;//总帧数，根据纹理行列数确定

    public Particle(Mesh mesh, Vector3f speed, long ttl, long updateTextureMillis) {
        super(mesh);
        this.speed = new Vector3f(speed);
        this.ttl = ttl;
        this.updateTextureMillis = updateTextureMillis;
        this.currentAnimTimeMillis = 0;
        Texture texture = this.getMesh().getMaterial().getTexture();
        this.animFrames = texture.getNumCols() * texture.getNumRows();
    }
    // 产生新粒子是粒子发生器通过复杂基本粒子完成的，所以定义一个拷贝构造函数。
    public Particle(Particle baseParticle) {
        super(baseParticle.getMesh());
        Vector3f aux = baseParticle.getPosition();
        setPosition(aux.x, aux.y, aux.z);
        setRotation(baseParticle.getRotation());
        setScale(baseParticle.getScale());
        this.speed = new Vector3f(baseParticle.speed);
        this.ttl = baseParticle.getTtl();
        this.updateTextureMillis = baseParticle.getUpdateTextureMillis();
        this.currentAnimTimeMillis = 0;
        this.animFrames = baseParticle.getAnimFrames();
    }
    public Vector3f getSpeed() {
        return speed;
    }
    public void setSpeed(Vector3f speed) {
        this.speed = speed;
    }
    public long getTtl() {
        return ttl;
    }
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public long getUpdateTextureMillis() {
        return updateTextureMillis;
    }
    public void setUpdateTextureMillis(long updateTextureMillis) {
        this.updateTextureMillis = updateTextureMillis;
    }
    public int getAnimFrames() {
        return animFrames;
    }
    public void setAnimFrames(int animFrames) {
        this.animFrames = animFrames;
    }
    //更新剩余时间
    public long updateTtl(long elapsedTime) {
        this.ttl -= elapsedTime;
        this.currentAnimTimeMillis += elapsedTime;
        if ( this.currentAnimTimeMillis >= this.getUpdateTextureMillis() && this.animFrames > 0 ) {
            this.currentAnimTimeMillis = 0;
            int pos = this.getTextPos();
            pos++;
            if ( pos < this.animFrames ) {
                this.setTextPos(pos);
            } else {
                this.setTextPos(0);
            }
        }
        return this.ttl;
    }
}
