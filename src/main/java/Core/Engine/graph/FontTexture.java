package Core.Engine.graph;
//字符纹理类，通过文字动态生产纹理
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

public class FontTexture {
    private static final String IMAGE_FORMAT = "png";
    private final Font font;
    private final String charSetName;
    private final Map<Character, CharInfo> charMap;
    private Texture texture;
    private int height;
    private int width;

    public FontTexture(Font font, String charSetName) throws Exception {
        this.font = font;
        this.charSetName = charSetName;
        charMap = new HashMap<>();
        buildTexture();
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public Texture getTexture() {
        return texture;
    }
    public CharInfo getCharInfo(char c) {
        return charMap.get(c);
    }
    //将一个字符集中的所有字符放入一个字符串中
    private String getAllAvailableChars(String charsetName) {
        CharsetEncoder ce = Charset.forName(charsetName).newEncoder();
        StringBuilder result = new StringBuilder();
        for (char c = 0; c < Character.MAX_VALUE; c++) {
            if (ce.canEncode(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }
    private void buildTexture() throws Exception {
        // 使用图像获取所选字体的每个字符的字体度量值
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = img.createGraphics();//创建2d图形
        g2D.setFont(font);//设置图形的字体
        FontMetrics fontMetrics = g2D.getFontMetrics();//获取字体矩阵

        String allChars = getAllAvailableChars(charSetName);
        this.width = 0;
        this.height = 0;
        for (char c : allChars.toCharArray()) {
            // 过去每一个字母的大小并更新图形大小
            CharInfo charInfo = new CharInfo(width, fontMetrics.charWidth(c));
            charMap.put(c, charInfo);
            width += charInfo.getWidth();
            height = Math.max(height, fontMetrics.getHeight());
        }
        g2D.dispose();
        // Create the image associated to the charset
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2D = img.createGraphics();//创建大小能容纳所有子母的图形
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setFont(font);
        fontMetrics = g2D.getFontMetrics();
        g2D.setColor(Color.WHITE);
        g2D.drawString(allChars, 0, fontMetrics.getAscent());
        g2D.dispose();
        // 转储图像到字节缓冲区
        InputStream is;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            //之所以要设置文件类型是因为在纹理类中使用PNGDecoder加载纹理
            ImageIO.write(img, IMAGE_FORMAT, out);
            //ImageIO.write(img, IMAGE_FORMAT, new java.io.File("Temp.png"));
            out.flush();
            is = new ByteArrayInputStream(out.toByteArray());
        }
        texture = new Texture(is);
    }
    //内部类，用于存储每个字符的信息，即开始位置和宽度，因为所有字符按最高字符同一设置高度，并水平排列，所以只需要知道起始位置和宽度
    public static class CharInfo {
        private final int startX;
        private final int width;
        public CharInfo(int startX, int width) {
            this.startX = startX;
            this.width = width;
        }
        public int getStartX() {
            return startX;
        }
        public int getWidth() {
            return width;
        }
    }
}
