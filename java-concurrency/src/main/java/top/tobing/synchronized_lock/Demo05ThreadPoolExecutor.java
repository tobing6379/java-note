package top.tobing.synchronized_lock;

/**
 * @author tobing
 * @date 2021/10/13 0:02
 * @description 线程池
 */
public class Demo05ThreadPoolExecutor {
    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().availableProcessors() + "核");
        System.out.println(Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");
        System.out.println(Runtime.getRuntime().freeMemory() / 1024 / 1024 + "MB");
    }
}
