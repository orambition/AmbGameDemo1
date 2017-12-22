package Core.Engine;

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
    //从指定位置读出所有行，用于读取obj模型
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
}
