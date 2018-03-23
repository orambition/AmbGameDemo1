package Core.Engine.graph.particles;
//粒子发生器接口，用于建立通用的粒子源模型
import Core.Engine.items.GameItem;
import java.util.List;

public interface IParticleEmitter {
    void cleanUp();
    Particle getBaseParticle();//过得基本粒子，当粒子到期时，会根据该基本粒子产生新粒子，像一个模型
    List<GameItem> getParticles();//
}