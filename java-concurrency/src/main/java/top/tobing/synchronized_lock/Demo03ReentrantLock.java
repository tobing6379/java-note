package top.tobing.synchronized_lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author tobing
 * @date 2021/10/10 17:30
 * @description
 */
public class Demo03ReentrantLock {
    static Lock lock = new ReentrantLock();
    public static void main(String[] args) {
        new Thread(() -> {
            testConcurrency();
        }).start();
        new Thread(() -> {
            testConcurrency();
        }).start();
        while (true) {
        }
    }

    public static void testConcurrency() {
        lock.lock();
        try {
            System.out.println("xxxxx");
            while (true) {

            }
        } finally {
            lock.unlock();
        }
    }
}
