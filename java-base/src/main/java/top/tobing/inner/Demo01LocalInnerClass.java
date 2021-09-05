package top.tobing.inner;

/**
 * @author tobing
 * @date 2021/9/5 21:50
 * @description
 */
public class Demo01LocalInnerClass {
    public static void main(String[] args) {
        String name = "tobing";
        Thread thread = new Thread(() -> {
            System.out.println(name);
        });

    }
}
