package Core.Engine.graph.particles;
//粒子发生器实现类，浮动粒子
import Core.Engine.items.GameItem;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlowParticleEmitter implements IParticleEmitter {
    private int maxParticles;//同时可存活的做多粒子数

    private boolean active;//是否启动
    private final List<GameItem> particles;//粒子列表
    private final Particle baseParticle;//基本粒子

    private long creationPeriodMillis;//创建粒子的最小周期

    private long lastCreationTime;//上一次创造的时间
    private float speedRndRange;//新建粒子的速度随机范围
    private float positionRndRange;//新建粒子的位置随机范围
    private float scaleRndRange;//新建粒子的缩放随机范围

    private long animRange;//每帧的时间范围
    public FlowParticleEmitter(Particle baseParticle, int maxParticles, long creationPeriodMillis) {
        particles = new ArrayList<>();
        this.baseParticle = baseParticle;
        this.maxParticles = maxParticles;
        this.active = false;
        this.lastCreationTime = 0;
        this.creationPeriodMillis = creationPeriodMillis;
    }

    @Override
    public Particle getBaseParticle() {
        return baseParticle;
    }
    public long getCreationPeriodMillis() {
        return creationPeriodMillis;
    }
    public int getMaxParticles() {
        return maxParticles;
    }
    @Override
    public List<GameItem> getParticles() {
        return particles;
    }
    public float getPositionRndRange() {
        return positionRndRange;
    }
    public float getScaleRndRange() {
        return scaleRndRange;
    }
    public float getSpeedRndRange() {
        return speedRndRange;
    }
    public void setCreationPeriodMillis(long creationPeriodMillis) {
        this.creationPeriodMillis = creationPeriodMillis;
    }
    public void setMaxParticles(int maxParticles) {
        this.maxParticles = maxParticles;
    }
    public void setPositionRndRange(float positionRndRange) {
        this.positionRndRange = positionRndRange;
    }
    public void setScaleRndRange(float scaleRndRange) {
        this.scaleRndRange = scaleRndRange;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public void setSpeedRndRange(float speedRndRange) {
        this.speedRndRange = speedRndRange;
    }
    public long getAnimRange() {
        return animRange;
    }
    public void setAnimRange(long animRange) {
        this.animRange = animRange;
    }
    //更新粒子列表
    public void update(long elapsedTime) {
        long now = System.currentTimeMillis();
        if (lastCreationTime == 0) {
            lastCreationTime = now;
        }
        //迭代器遍历所产生的粒子
        Iterator<? extends GameItem> it = particles.iterator();
        while (it.hasNext()) {
            Particle particle = (Particle) it.next();
            if (particle.updateTtl(elapsedTime) < 0) {
                it.remove();//过期移除，使用迭代器的原因
            } else {
                updatePosition(particle, elapsedTime);//更新位置
            }
        }
        int length = this.getParticles().size();
        //创造粒子的时间和数量符合标准则创建新粒子
        if (now - lastCreationTime >= this.creationPeriodMillis && length < maxParticles) {
            createParticle();
            this.lastCreationTime = now;
        }
    }
    //创建新粒子
    private void createParticle() {
        Particle particle = new Particle(this.getBaseParticle());
        // 随机正负值
        float sign = Math.random() > 0.5d ? -1.0f : 1.0f;
        float speedInc = sign * (float)Math.random() * this.speedRndRange;
        float posInc = sign * (float)Math.random() * this.positionRndRange;
        float scaleInc = sign * (float)Math.random() * this.scaleRndRange;
        long updateAnimInc = (long)sign *(long)(Math.random() * (float)this.animRange);
        particle.getPosition().add(posInc, posInc, posInc);
        particle.getSpeed().add(speedInc, speedInc, speedInc);
        particle.setScale(particle.getScale() + scaleInc);
        particle.setUpdateTextureMillis(particle.getUpdateTextureMillis() + updateAnimInc);
        particles.add(particle);
    }
    //更新粒子的位置
    public void updatePosition(Particle particle, long elapsedTime) {
        Vector3f speed = particle.getSpeed();
        float delta = elapsedTime / 1000.0f;
        float dx = speed.x * delta;
        float dy = speed.y * delta;
        float dz = speed.z * delta;
        Vector3f pos = particle.getPosition();
        particle.setPosition(pos.x + dx, pos.y + dy, pos.z + dz);

    }
    @Override
    public void cleanUp() {
        for (GameItem particle : getParticles()) {
            particle.cleanUp();
        }
    }


}