package Core.Engine.graph.weather;

import org.joml.Vector3f;

//雾，雾的作用是通过片段着色器实现的，和光一样都是改变点的颜色
public class Fog {
    private boolean active;
    private Vector3f colour;
    private float density;
    public static Fog NOFOG = new Fog();
    public Fog() {
        active = false;
        this.colour = new Vector3f(0, 0, 0);
        this.density = 0;
    }
    public Fog(boolean active, Vector3f colour, float density) {
        this.colour = colour;
        this.density = density;
        this.active = active;
    }
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Vector3f getColour() {
        return colour;
    }

    public void setColour(Vector3f colour) {
        this.colour = colour;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }
}
