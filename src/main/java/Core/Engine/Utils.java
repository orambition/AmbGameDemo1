package Core.Engine;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
//工具类
public class Utils {
    //从类路径获取指定资源文件
    public static String loadResource(String fileName)throws Exception{
        String result;
        //此处try（）的用法为java7新增特性，及在（）内的代码在执行完try块后会自动释放资源
        //http://www.oschina.net/question/12_10706
        try (InputStream in = Class.forName(Utils.class.getName()).getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in,"UTF-8")) {
            //useDelimiter设置scanner的分隔符为指定符号；但此处为什么转成\A?
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }
    //从指定位置读出所有行，用于读取obj、md5等模型
    public static List<String> readAllLines(String fileName) throws Exception {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Class.forName(Utils.class.getName()).getResourceAsStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }
    //浮点型列表 转 数组
    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }
    //int型列表 转 数组
    public static int[] listIntToArray(List<Integer> list) {
        int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
        return result;
    }
    //检查文件是否存在
    public static boolean existsResourceFile(String fileName) {
        boolean result;
        try (InputStream is = Utils.class.getResourceAsStream(fileName ) ) {
            result = is != null;
        } catch (Exception excp) {
            result = false;
        }
        return result;
    }
    //文件源转ByteBuffer
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;
        //在传统java.io中， 文件和目录都被抽象成File对象， 即 File file = new File(".");
        //NIO.2中则引入接口Path代表与平台无关的路径，文件和目录都用Path对象表示
        //通过路径工具类Paths返回一个路径对象Path
        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {//是否可读。以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
            //java7特性NIO
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {//此处涉及文件编码问题
                //如果文件是可读取的，且编码格式已知，则可以使用Files类进行读取，且可以获取大小
                buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1) ;
            }
        } else {//当文件编码未知（如ogg压缩音频），不能通过Files读取时，则只能使用getResource转为流读取
            try (InputStream source = Utils.class.getResourceAsStream(resource);
                 ReadableByteChannel rbc = Channels.newChannel(source)) {
                buffer = BufferUtils.createByteBuffer(bufferSize);
                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {//buffer空间不足时，扩充空间为原来的2倍
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                    }
                }
            }
        }
        buffer.flip();
        return buffer;
    }
    //重新定义的一个buffer，容量为newCapacity
    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
    //计算从向量o到f的欧拉角
    public static Vector3f vectorToEuler(Vector3f oDirection, Vector3f fDirection){
        Vector3f result = new Vector3f();
        Vector2f tempO = new Vector2f();
        Vector2f tempF = new Vector2f();

        //旋转x轴2
        if (!((oDirection.z==0&&oDirection.y==0) || (fDirection.z==0&&fDirection.y==0))){
            tempO.set(oDirection.z,oDirection.y);
            tempF.set(fDirection.z,fDirection.y);
            result.x = -(float) Math.toDegrees(tempO.angle(tempF));
            oDirection.set(oDirection.x,fDirection.y,fDirection.z);
        }
        //旋转y轴1
        if (!((oDirection.x==0&&oDirection.z==0) || (fDirection.x==0&&fDirection.z==0))){
            tempO.set(oDirection.x,oDirection.z);
            tempF.set(fDirection.x,fDirection.z);
            result.y = (float) Math.toDegrees(tempO.angle(tempF));
            oDirection.set(fDirection.x,oDirection.y,fDirection.z);
        }
        //旋转z轴0
        if (!((oDirection.x==0&&oDirection.y==0) || (fDirection.x==0&&fDirection.y==0))) {
            tempO.set(oDirection.x, oDirection.y);
            tempF.set(fDirection.x, fDirection.y);
            result.z = -(float) Math.toDegrees(tempO.angle(tempF));
            oDirection.set(fDirection.x,fDirection.y, oDirection.z);
        }
        return result;
    }
}
