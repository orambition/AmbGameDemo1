package Core.Game;
//基于NanoVG的hud类

import Core.Engine.Utils;
import Core.Engine.Window;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;

public class Hud{
    private static final String FONT_NAME = "BOLD";
    private long vg;
    private NVGColor colour;
    private ByteBuffer fontBuffer;//用于加载字体，为了不被回收，将其定义为类属性
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private DoubleBuffer posx;
    private DoubleBuffer posy;
    private int counter;//绘制的文本内容

    public void init(Window window) throws Exception {
        //根据抗锯齿选择创建vg
        this.vg = window.getWindowOptions().antialiasing ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
        if (this.vg == NULL) {
            throw new Exception("Could not init nanovg");
        }
        //加载字体
        fontBuffer = Utils.ioResourceToByteBuffer("/fonts/OpenSans-Bold.ttf", 150 * 1024);
        int font = nvgCreateFontMem(vg, FONT_NAME, fontBuffer, 0);
        if (font == -1) {
            throw new Exception("Could not add font");
        }
        colour = NVGColor.create();
        posx = MemoryUtil.memAllocDouble(1);
        posy = MemoryUtil.memAllocDouble(1);
        counter = 0;
    }

    public void render(Window window) {
        //nanoVG的渲染必须在BeginFrame和EndFrame之间
        nvgBeginFrame(vg, window.getWindowWidth(), window.getWindowHeight(), 1);//……像素比例
        // 上方的条
        nvgBeginPath(vg);//绘制图形前必须调用，文字则不需要
        nvgRect(vg, 0, window.getWindowHeight() - 100, window.getWindowWidth(), 50);//绘制矩形
        nvgFillColor(vg, rgba(0x23, 0xa1, 0xf1, 200, colour));//填冲颜色
        nvgFill(vg);//绘制
        // 下方的条
        nvgBeginPath(vg);
        nvgRect(vg, 0, window.getWindowHeight() - 50, window.getWindowWidth(), 10);
        nvgFillColor(vg, rgba(0xc1, 0xe3, 0xf9, 200, colour));
        nvgFill(vg);

        //获取光标位置，存入pos
        glfwGetCursorPos(window.getWindowHandle(), posx, posy);
        int xcenter = 50;//圆心坐标
        int ycenter = window.getWindowHeight() - 75;
        int radius = 20;//半径
        int x = (int) posx.get(0);
        int y = (int) posy.get(0);
        //是否在圆内
        boolean hover = Math.pow(x - xcenter, 2) + Math.pow(y - ycenter, 2) < Math.pow(radius, 2);
        // 绘制椭圆
        nvgBeginPath(vg);
        nvgCircle(vg, xcenter, ycenter, radius);
        nvgFillColor(vg, rgba(0xc1, 0xe3, 0xf9, 200, colour));
        nvgFill(vg);
        // 绘制文本
        nvgFontSize(vg, 25.0f);//大小
        nvgFontFace(vg, FONT_NAME);//字体
        nvgTextAlign(vg, NVG_ALIGN_CENTER | NVG_ALIGN_TOP);//左右上下居中
        if (hover) {
            nvgFillColor(vg, rgba(0x00, 0x00, 0x00, 255, colour));
        } else {
            nvgFillColor(vg, rgba(0x23, 0xa1, 0xf1, 255, colour));
        }
        nvgText(vg, 50, window.getWindowHeight() - 87, String.format("%02d", counter));
        // 绘制时间
        nvgFontSize(vg, 40.0f);
        nvgFontFace(vg, FONT_NAME);
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgFillColor(vg, rgba(0xe6, 0xea, 0xed, 255, colour));
        nvgText(vg, window.getWindowWidth() - 150, window.getWindowHeight() - 95, dateFormat.format(new Date()));
        nvgEndFrame(vg);
        // 恢复OpenGL状态。nanovg修改OpenGL状态以执行操作
        window.restoreState();
    }
    public void incCounter() {
        counter++;
        if (counter > 99) {
            counter = 0;
        }
    }
    private NVGColor rgba(int r, int g, int b, int a, NVGColor colour) {
        colour.r(r / 255.0f);
        colour.g(g / 255.0f);
        colour.b(b / 255.0f);
        colour.a(a / 255.0f);
        return colour;
    }
    public void cleanUp() {
        nvgDelete(vg);
        if (posx != null) {
            MemoryUtil.memFree(posx);
        }
        if (posy != null) {
            MemoryUtil.memFree(posy);
        }
    }
}
