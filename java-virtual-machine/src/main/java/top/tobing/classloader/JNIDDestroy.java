package top.tobing.classloader;

import java.sql.*;

/**
 * @author tobing
 * @date 2021/8/17 11:20
 * @description
 */
public class JNIDDestroy {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
         Class<?> clazz = Class.forName("com.mysql.cj.jdbc.Driver");
        Driver driver = DriverManager.getDriver("jdbc:mysql://localhost:3306/shopping?user=root&password=root&serverTimezone=GMT");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/shopping?user=root&password=root&serverTimezone=GMT");
        System.out.println(driver);
        ClassLoader classLoader = Connection.class.getClassLoader();
        ClassLoader classLoaderA = connection.getClass().getClassLoader();
        ClassLoader classLoaderB = DriverManager.class.getClassLoader();
        // 对于Connection.class，属于java.sql包下，位于rt.jar,自然是启动类加载器加载
        System.out.println("Connection clazz classloader: " + classLoader);
        // 对于Connection接口，不同数据厂商需要根据接口定义相应的具体实现。
        // 我们使用时，如果希望通过多态的方式来加载不同的class驱动，就需要让启动类加载器在加载时能够获得相应的class
        // 但是启动类加载器只会扫描「JAVA_HOME\bin」目录下的，但显然，数据库厂商的jar包不是在这个路径
        // 为了能够让启动类加载器的类在产生加载时能够正确获得用户指定路径下的class，引入了线程上下文引导器（默认为应用程序类加载器）
        //
        System.out.println("Connection instance classloader: " + classLoaderA);
        System.out.println("DriverManager clazz classloader: " + classLoaderB);
    }
}
