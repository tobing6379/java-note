package top.tobing.synchronized_lock;

/**
 * @author tobing
 * @date 2021/8/23 10:53
 * @description
 */
public class LockDemo {
    public static void main(String[] args) {
        Source source = new Source(0);
        Thread threadZero = new Thread(() -> {
            while (true) {
                source.printZero();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread threadOne = new Thread(() -> {
            while (true) {
                source.printOne();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread threadTwo = new Thread(() -> {
            while (true) {
                source.printTwo();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        threadZero.start();
        threadOne.start();
        threadTwo.start();

    }
}

class Source {

    private int flag;

    public Source(int flag) {
        this.flag = flag;
    }

    public void printZero() {
        synchronized (this) {
            while (flag % 3 == 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(Thread.currentThread().getName() + ": 0");
            flag++;
            notifyAll();

        }
    }

    public void printOne() {
        synchronized (this) {
            while (flag % 3 == 1) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(Thread.currentThread().getName() + ": " + flag);
            flag++;
            notifyAll();
        }
    }


    public void printTwo() {
        synchronized (this) {
            while (flag % 3 == 2) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(Thread.currentThread().getName() + ": " + flag);

            flag++;
            notifyAll();

        }
    }
}
