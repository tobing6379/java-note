package top.tobing.synchronized_lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author tobing
 * @date 2021/9/29 17:12
 * @description
 */
public class Demo02ThreadPool {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
    }
}
