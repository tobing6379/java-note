package top.tobing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author tobing
 * @date 2021/10/1 12:32
 * @description InputStream源码分析
 */
public class Demo02InputStreamSourceAnalysis {
    public static void main(String[] args) throws IOException {
        String pathName = "E:\\文件\\系统镜像\\ubuntu-16.04.7-desktop-amd64.iso";
        FileInputStream fileInputStream = new FileInputStream(pathName);
        int byteSize = fileInputStream.available();
        double byteGB = 1.0 * byteSize / 1024 / 1024 / 1024;
        System.out.println("总大小为（GB）：" + byteGB);

    }
}
