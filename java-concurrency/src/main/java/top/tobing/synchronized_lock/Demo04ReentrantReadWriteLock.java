package top.tobing.synchronized_lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author tobing
 * @date 2021/10/10 19:23
 * @description 可重入读写锁
 */
public class Demo04ReentrantReadWriteLock {

    static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    static ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    static ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> testReadLock()).start();
        new Thread(() -> testReadLock()).start();
        Thread.sleep(100);
        new Thread(() -> testWriteLock()).start();

        while (true) {
        }
    }

    private static void testReadLock() {
        readLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 执行读锁！");
            while (true) {
            }
        } finally {
            readLock.unlock();
        }
    }

    private static void testWriteLock() {
        writeLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 执行写锁！");
            while (true) {
            }
        } finally {
            writeLock.unlock();
        }
    }
}
