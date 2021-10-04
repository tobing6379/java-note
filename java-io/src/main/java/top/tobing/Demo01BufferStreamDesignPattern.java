package top.tobing;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author tobing
 * @date 2021/10/1 12:10
 * @description IO装饰器模式
 */
public class Demo01BufferStreamDesignPattern {
    public static void main(String[] args) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream("test.txt");
        // BufferedInputStream 通过聚合 FileInputStream 并调用FIS的方法来增强其功能；
        // 又由于BufferedInputStream和FileInputStream都继承了InputStream可以利用多态特性进行替换
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
    }
}
