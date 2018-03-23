package Core.Engine;

import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    //方向向量旋转 转 欧拉角，有误，尚未确认
    public static Vector3f VectorsEulerian(Vector3f VectorA,Vector3f VectorB){
        Vector3f result = new Vector3f(0,0,0);

        Vector3f normal = VectorA.cross(VectorB);
        normal.normalize();

        float fai = (float)(normal.y==0?0:Math.atan(normal.x/normal.y));
        float xita = (float) Math.acos(normal.dot(0,0,1));
        float omiga = (float) Math.acos(VectorA.dot(VectorB)/Math.abs(VectorA.length()*VectorB.length()));
        result.y = (float)Math.toDegrees(2*Math.asin(Math.sin(omiga/2)*Math.sin(xita)));

        //a+b = 2*Math.atan(Math.tan(omiga/2)*Math.cos(xita));
        //a-b = 2*fai;
        result.x = (float)Math.toDegrees(Math.atan(Math.tan(omiga/2)*Math.cos(xita)) + fai);
        result.z = (float)Math.toDegrees(Math.atan(Math.tan(omiga/2)*Math.cos(xita)) - fai);
        return result;
    }

}
