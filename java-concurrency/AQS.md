# AQS

## 简介

AbstractQueuedSynchronizer，是用来构建锁或其他同步最贱的基础框架，它使用一个int成员变量表示同步状态，通过内置的FIFO队列完成资源获取线程的排队工作。

AQS的主要使用方式是继承，子类通过继承AQS并实现它的抽象方法来管理同步状态，在抽象方法中实现中使用AQS提供的3个方法(getState/setState/compareAndSetState)来对状态进行操作。子类推荐被定义为自定义同步组件的静态内部类，AQS自身没有实现任何接口，仅仅定义了若干同步状态获取和释放的方法来供自定义同步组件使用，AQS即可以支持独占式获取同步状态，也可以支持共享式获取同步状态，这样可以方便实现不同类型的同步组件。

AQS是是实现锁的关键，锁的实现中聚合AQS，利用AQS实现锁的语义。

锁是面向使用者的，定义了使用者与锁交互的接口，隐藏了实现细节；AQS面向的实现者，简化了锁的实现方式，屏蔽了同步状态管理、线程的排队、等待与唤醒等底层操作。锁与AQS很好隔离了使用者与实现者关注的领域。

## AQS使用

AQS设计基于模板方法，使用者需要集成AQS并重写指定方法，可重写的方法包含：

| 方法名称                      | 描述                                                         |
| ----------------------------- | ------------------------------------------------------------ |
| boolean tryAcquire(int)       | 独占获取同步状态，实现该方法时需要判断当前状态和同步状态是否符合预期，然后对状态进行CAS设置 |
| boolean tryRelease(int)       | 独占释放同步状态，等待获取同步状态的线程将有机会获取同步状态 |
| int tryAcquireShared(int)     | 共享式获取同步状态，返回大于等于0表示成功，反之获取失败      |
| boolean tryReleaseShared(int) | 共享式释放同步状态                                           |
| boolean isHeldExclusively()   | 当前AQS是否在独占模式下被线程占用，一般该方法表示是否被当前线程占用 |

重写上述方法的时候，需要使用同步器提供的用于访问或修改同步状态方法：

| 方法名称                                   | 描述                                                |
| ------------------------------------------ | --------------------------------------------------- |
| getState()                                 | 获取当前同步状态                                    |
| setState(int newState)                     | 设置当前同步状态                                    |
| compareAndSetState(int expext, int update) | 使用CAS设置当前状态，该方法能够保证状态设置的原子性 |

在实现自定义同步器时会调用AQS提供的模板方法：

| 方法名称                                 | 描述                                                         |
| ---------------------------------------- | ------------------------------------------------------------ |
| void acquire(int)                        | 独占获取同步状态，如果当前线程获取同步状态成功，则由该方法返回，否则，将会进入同步队列等待，该方法将会调用重写的tryAcquire(int)方法。 |
| void acquireInterruptibly(int)           | 与acquire(int)相同，但是该方法响应中断，当前线程为获取未获取到同步状态而进入同步队列，如果当前线程被中断，则该方法会抛出InterruptedException并返回。 |
| boolean tryAcquireNanos(int,  long)      | 在acquireInterruptibly(int)基础上增加超时限制，如果当前线程在超时没有获得同步状态，将会返回fasle，如果获取到返回true。 |
| void acquireShared(int)                  | 共享式的获取同步状态，如果当前线程未获取到同步状态，将会进入同步队列等待，与独占式获取的主要区别是在同一时刻可以有多个线程获取到同步状态。 |
| void acquireSharedInterruptibly(int)     | 与acquireShared相同，该方法响应中断。                        |
| boolean tryAcquireSharedNanos(int, long) | 在acquireSharedInterruptibly基础上增加超时限制。             |
| boolean release(int)                     | 独占式释放同步状态，该方法会释放同步状态之后，将同步队列中第一个节点包含的线程唤醒。 |
| boolean releaseShared(int)               | 共享式释放同步状态。                                         |
| `Collection<Thread> getQueuedThreads()`  | 获取等待在同步队列上的线程集合。                             |

AQS提供的模板方法主要可以分为3类：

+ 独占式获取与释放同步状态；
+ 共享式获取与释放同步状态；
+ 查询同步队列中的等待线程的情况；

## 实现原理

AQS完成线程同步主要包含：同步队列、独占式同步状态获取和释放、共享时同步状态获取与释放以及超时获取同步状态等AQS的核心数据结构与模板方法。

### 同步队列

<font style="color:red"> **AQS依赖内部的FIFO双向同步队列来完成同步状态的管理。当前线程获取同步状态失败时，AQS会将当前线程以及等待状态等信息构造成一个节点并将其加入同步队列，同时会阻塞当前线程，当同步状态释放时，会把首节点中的线程唤醒，使其再次尝试获取同步状态。**</font>

同步队列中的节点用来保存获取同步状态失败的线程引用、等待状态以及前驱和后继节点，节点的属性类型与名称，节点的属性类型与名称以及描述如下所示。

>Node是过程同步队列的基础，同步器拥有首节点和尾节点，没有成功获取同步状态的线程会成为节点加入到该队列尾部。

```java
static final class Node {

    // 节点等待的状态：CANCELLED、SIGNAL、CONDITION、PROPAGATE、INITIAL
    volatile int waitStatus;
    // 节点线程等待超时或被中断
    static final int CANCELLED =  1;
	// 后继节点线程处于等待状态，当去节点线程释放同步状态会通知后继节点运行
    static final int SIGNAL    = -1;
    // 节点线程在等待在Condition中，其他线程调用了Condition.singal，会将其移动到同步队列
    static final int CONDITION = -2;
    // 表示下次共享是同步获取将无条件被传播下区
    static final int PROPAGATE = -3;

    // 前驱节（节点添加到同步队列中是被设置）点与后继节点
    volatile Node prev;
    volatile Node next;
    // 等待队列的后继节点。如果节点是共享的，字段是一个SHARED常量
    Node nextWaiter;
    // 常量
    static final Node SHARED = new Node();
    static final Node EXCLUSIVE = null;
    
	// 获取同步状态的线程  
    volatile Thread thread;
	
    final boolean isShared() {
        return nextWaiter == SHARED;
    }
    ...
}
```

> 同步器包含两个节点类型的引用，分别指向头节点和尾节点。
>
> 当一个线程成功获取同步状态，其他线程将无法获得同步状态，转而构造成节点并加入到同步队列中，这个加入队列的过程必须是线程安全的。
>
> 同步队列遵循FIFO，首节点是获取同步状态成功的节点，首节点的线程在释放同步状态时，将会唤醒后继节点，后继节点在获取同步状态成功时将自己设置为首节点。设置首部节点是通过获取同步状态成功的线程来完成，由于只能由于个线程能够成功获取同步状态，因此设置头结点无需CAS保证，只需将首节点设置为原首节点的后继节点，并断开原首节点的next引用即可。

```java
public abstract class AbstractQueuedSynchronizer {
    // 同步器包含指向同步度列头结点和尾节点的引用
    private transient volatile Node head;
    private transient volatile Node tail;

    // 使用CAS保证入队过程是线程安全
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }
}
```

AQS同步队列的基本组成结构如下图所示：

![image-20210927205024775](https://gitee.com/tobing/imagebed/raw/master/image-20210927205024775.png)

### 独占获取同步状态

通过AQS.acquire方法可以获取同步状态，该方法对中断不敏感。由于线程获取同步状态失败后进入同步队列中，后继对线程进行中断操作时，线程不会从同步队列移出。acquire的执行流程主要有这几个步骤**：完成同步状态的获取、构建节点、加入同步队列以及在同步队列中自旋**。

1. 首先调用自定义AQS实现的tryAcquire方法，线程安全获取同步状态；
2. 如果同步状态获取失败，构造同步节点(Node.EXCLUSIVE)；
3. 创建的同步节点会通过addWaiter方法将其加入到同步队列中；
4. addWaiter内部通过调研enq方法死循环保证节点正确添加；
5. 最后调用acquireQueued方法使得节点以「死循环」方式获取同步状态。

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&		// ①尝试获取同步状态
        acquireQueued(			// ③将节点以死循环方式获取同步状态
            addWaiter(Node.EXCLUSIVE), arg) // ②获取同步状态失败构造节点并将其加入到同步队列
       )
        selfInterrupt();		
}

//------------------------ 构造节点
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    // 尝试快速添加到尾部
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        // 使用CAS确保节点能够被安全添加到尾部【此处仅尝试一次】
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    enq(node);
    return node;
}

//------------------------ 将节点加入同步队列
private Node enq(final Node node) { 
    // 循环采用CAS的方式将节点设置为尾节点
    for (;;) {
        Node t = tail;  
        if (t == null) { // Must initialize
            if (compareAndSetHead(new Node()))
                tail = head;        
        } else {
            node.prev = t; 
            if (compareAndSetTail(t, node)) {  
                t.next = node;  
                return t;
            }
        }
    }
}

//------------------------ 在同步队列中自旋
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            // 循环判断当前节点的前驱节点是否为「首节点」，只有头节点才能尝试获取同步状态
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

同步队列中只允许前驱节点为头节点的节点才能尝试获取同步状态，主要有两个原因：

1. 头节点是成功获取到同步状态的节点，在头节点线程释放了同步状态之后，将会唤醒后继节点，后继节点的线程被唤醒后需要检查自己的前驱释放为头节点；

2. 维护同步队列的FIFO原则。

![image-20210927213027301](https://gitee.com/tobing/imagebed/raw/master/image-20210927213027301.png)

总而言之，独占式同步状态获取流程(acquire方法调用流程)如下图所示：

![image-20210927214658747](https://gitee.com/tobing/imagebed/raw/master/image-20210927214658747.png)

### 独占释放同步状态

通过调用AQS的release(int)方法可以释放同步状态，该方法在释放了同步状态之后，会唤醒其后继节点。

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {  // 先调用tryRelease操作释放资源
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h); 
        return true;
    }
    return false;
}

private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);
    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread); // 调用LockSupport将一个合法的线程唤醒
}
```

## AQS实践

### Mutex

同一时刻只允许一个线程占有锁。

```java
public class Mutex implements Lock {
    // 自定义AQS
    private static class Sync extends AbstractQueuedSynchronizer {
        // 是否处于占用状态
        @Override
        protected boolean isHeldExclusively() {            return getState() == 1;        }

        // 当状态为0的时候获取锁
        @Override
        protected boolean tryAcquire(int arg) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        // 释放锁，将状态设置为1
        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == 0) {
                throw new IllegalMonitorStateException(null);
            }
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        Condition newCondition() {
            return new ConditionObject();
        }
    }

    private final Sync sync = new Sync();

    @Override
    public void lock() {        sync.acquire(1);    }

    @Override
    public boolean tryLock() {        return sync.tryAcquire(1);    }

    @Override
    public void lockInterruptibly() throws InterruptedException {        sync.acquireInterruptibly(1);    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {        return sync.tryAcquireSharedNanos(1, unit.toNanos(time));    }

    @Override
    public void unlock() {        sync.release(1);    }

    @Override
    public Condition newCondition() {        return sync.newCondition();    }
}
```

### TwinsLock

工具同一时刻，只允许至多两个线程同时访问，超过两个线程访问被阻塞。

```java
public class Demo03TwinsLock implements Lock {

    private final Sync sync = new Sync(2);

    private static final class Sync extends AbstractQueuedSynchronizer {
        public Sync(int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("count must large than zero.");
            }
            setState(count);
        }

        @Override
        protected int tryAcquireShared(int arg) {
            for (; ; ) {
                int current = getState();
                int newCount = current - arg;
                if (newCount < 0 || compareAndSetState(current, newCount)) {
                    return newCount;
                }
            }
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            for (; ; ) {
                int current = getState();
                int newCount = current + arg;
                if (compareAndSetState(current, newCount)) {
                    return true;
                }
            }
        }
    }

    @Override
    public void lock() {        sync.acquireShared(1);    }

    @Override
    public void lockInterruptibly() throws InterruptedException {    }

    @Override
    public boolean tryLock() {        return false;    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {        return false;    }

    @Override
    public void unlock() {        sync.releaseShared(1);    }

    @Override
    public Condition newCondition() {        return null;    }
}
```

## ReentrantLock

### 简介

可重入锁，顾名思义就是支持重进入的锁，表示该锁能够支持一个线程对资源的重复加锁。

synchronized关键字隐式支持重进入，因此我们可以递归调用synchronized修饰的方法。在方法执行时，执行线程在获取了锁之后仍能连续多次获取锁。

ReentrantLock虽然没有synchronized一样支持隐式的可重入，但是调用lock方法是，已经后的锁的线程能够再次调用lock方法获取锁而不被阻塞。

除此之外ReentrantLock还引入了公平锁和非公平锁的概念。如果在绝对时间内，先对线程进行获取的请一定先被满足，那么整个锁就是公平的；反之是不公平的。公平获取锁即等待时间最长的线程最优先获取锁，即锁获取是顺序的。

事实上，公平的锁机制往往没有非公平的效率高，但是，并不是任何场景都是以TPS作为唯一指标，公平锁能够减少「饥饿」发生的概念。

### 可重入的实现

可重入值任意线程在获取锁之后能够再次获取该锁而不会被锁阻塞，该特性需要解决以下两个问题：

1. **线程再次获得锁**：锁需要识别获取锁的线程是否为当前占据的线程，虽然是则成功获取。
2. **锁的最终释放**：线程重复n次加锁，随后在第n次释放锁之后，其他线程能够获取该锁。锁的最终释放要求锁对于获取进行技术自增，计数表示当前锁重复获取的次数，而锁被释放时，计数自检，当前计数等于0时表示锁已经成功释放。

ReentrantLock通过内部组合自定义AQS来实现锁的获取与释放。非公平实现为例：

该方法怎加了再次获取同步状态的处理逻辑：

1. 通过判断当前线程是否为获得锁的线程来决定获取操作是否成功；
2. 如果获取锁的线程再次请求，则将同步状态值进行增加并返回true，表示获取同步状态成功

```java
final boolean nonfairTryAcquire(int acquires) {  
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {     
        if (compareAndSetState(0, acquires)) {  
            setExclusiveOwnerThread(current);   
            return true;                        
        }
    } else if (current == getExclusiveOwnerThread()) { // 增加了可重入的判断逻辑
        int nextc = c + acquires;                       
        if (nextc < 0) // overflow                      
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;  
}
```

成功获得锁在次获获取锁，只是增加同步状态值，即要求ReentrantLock在释放同步状态的时候减少同步状态值：

```java
protected final boolean tryRelease(int releases) {
    int c = getState() - releases;
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}
```

公平性与否是针对获取锁而言，如果一个锁是公平的，那么锁的获取顺序应该符合请求的决定时间顺序，即FIFO。

在非公平锁中，只要CAS设置同步状态成功，表示当前线程获取了锁，而非公平锁的处理逻辑有所不同：

```java
protected final boolean tryAcquire(int acquires) {      
    final Thread current = Thread.currentThread();     
    int c = getState();                               
    if (c == 0) {                                     
        if (!hasQueuedPredecessors() &&     // 先判断当前节点是否具有前驱节点【与非公平锁唯一的不同】
            compareAndSetState(0, acquires)) {     
            setExclusiveOwnerThread(current);      
            return true;
        }
    } else if (current == getExclusiveOwnerThread()) {    
        int nextc = c + acquires;
        if (nextc < 0)                                  
            throw new Error("Maximum lock count exceeded");
        setState(nextc);                        
        return true;
    }
    return false;
}
```

可以看出tryAcquire与nonfairTryAcquire唯一的不同就是判断条件处多了hasQueuedPredecessors方法。这个方法表示在加入同步队列中当前节点是否有前驱节点，如果方法返回true，表示有线程比当前线程更早地请求获取锁，因此需要等待前驱线程呼气并释放锁之后才能继续获取锁。

### 公平 vs 非公平

在nonfairTryAcquire方法中，但一个线程请求锁的时候，只要获取了同步状态即成功获取锁。在这个前提下，刚释放锁的线程再次获取同步状态的几率很大，使得其他线程只能在同步队列中等待。因此，非公平锁可能会使得线程饥饿，但由于非公平锁相比于公平锁具有更小的上下文切换开销，可以具有更大的吞吐量，因此将其作为ReentrantLock的具体实现。

### 执行过程

```java
public class Demo03ReentrantLock {
    static Lock lock = new ReentrantLock();
    public static void main(String[] args) {
        new Thread(() -> { testConcurrency(); }).start();// 线程A
        new Thread(() -> { testConcurrency(); }).start();// 线程B
        while (true) { }
    }

    public static void testConcurrency() {
        lock.lock();
        try {
            System.out.println("xxxxx");
            while (true) { }
        } finally { lock.unlock(); }
    }
}
```

假设线程A和线程B分别争夺Lock的使用权（非公平锁为例）：

【A线程】

+ `ReentrantLock#lock() `：调用Reentrant的lock方法；
+ `ReentrantLock$NonfairSync#lock()`：内部会调用NonfairSync的lock方法；
+ `AQS#compareAndSetState()`：内部尝试设置AQS中的状态；【初始为0，设置成功】
+ `AbstractOwnableSynchronizer#setExclusiveOwnerThread()`：设置独占线程为当前线程；
+ 执行完上述步骤，A线程获取锁成功，执行下面的流程，死循环执行。

【B线程】

+ `ReentrantLock#lock() `：调用Reentrant的lock方法；
+ `ReentrantLock$NonfairSync#lock()`：内部会调用NonfairSync的lock方法；
+ `AQS#compareAndSetState()`：内部尝试设置AQS中的状态；【A线程已经占用，设置失败】
+ `AQS#acquire()`：先调用ReentrantLock#tryAcquire&&AQS#acquireQueued(AQS#addWaiter)；
+ `ReentrantLock.NonfairSync#tryAcquire()`：调用ReentrantLock#nonfairTryAcquire；
+ `ReentrantLock#nonfairTryAcquire()`：再次判断state，以及判断当前线程是否为持有锁线程；【失败，当前锁被线程A持有】
+ `AQS#addWaiter()`：将当前线程封装为一个节点，CAS将其添加到链表尾部；
+ `AQS#acquireQueued()`：当前线程调用LockSuport#park将自身阻塞。

## 读写锁

### 简介

无论是Mutex还是ReentrantLock基本上都是排它锁，同一时刻只允许一个线程进行访问，而读写锁允许在同一时刻多个线程进行读访问，但是在写线程访问时，所有的读线程和其他写线程均被阻塞。读写锁维护了一对锁，一个读锁和一个写锁，通过分离读锁和写锁，使得并发性相对于一般排它锁有了很大的提升。

Java并发包中体用读写锁的实现是ReentrantReadWriteLock。

### 读写锁的实现

ReentrantReadWriteLock实现包括：读写状态的设计、写锁的获取与释放、读锁的获取与释放以及锁降级。

读写锁的实现同样依赖AQS同步器来实现，而读写锁的状态就是AQS的状态。但AQS只存一个状态，因此读写锁的实现的关键就是如何通过一个状态分别来维护读锁和写锁的状态。

ReentrantReadWriteLock通过该将整形变量「**按位切割使用**」的方式，将变量分为两部分，高16位用于读，低16位用于写。

+ 写状态等于 S & 0x0000FFFF，即将高16位全部抹去；
+ 读状态等于 S >>> 16，即将低16位全部抹去。

【写锁的获取】

写锁是一个支持可重入的排他锁。如果当前线程已经获取了写锁，则增加写状态。如果当前线程在获取写锁时，读锁以及被获取或者该线程不是以及获取写锁的线程，则当前线程进入等待状态。

```java
protected final boolean tryAcquire(int acquires) {
    Thread current = Thread.currentThread();   
    int c = getState();                        
    int w = exclusiveCount(c); // c & EXCLUSIVE_MASK
    if (c != 0) {       
        // 存在读锁或当前获取线程不是已经获取写锁的线程
        if (w == 0 || current != getExclusiveOwnerThread()) 
            return false;
        if (w + exclusiveCount(acquires) > MAX_COUNT)     
            throw new Error("Maximum lock count exceeded");
        // Reentrant acquire
        setState(c + acquires);
        return true;
    } 
    if (writerShouldBlock() || // 判断
        !compareAndSetState(c, c + acquires))
        return false;           
    setExclusiveOwnerThread(current);
    return true;
}
```

### 执行过程

```java
public class Demo04ReentrantReadWriteLock {

    static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    static ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    static ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> testReadLock()).start();	// A线程
        new Thread(() -> testReadLock()).start();	// B线程
        Thread.sleep(100);
        new Thread(() -> testWriteLock()).start();	// C线程
        while (true) {}
    }

    private static void testReadLock() {
        readLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 执行读锁！");
            while (true) {}
        } finally {
            readLock.unlock();
        }
    }

    private static void testWriteLock() {
        writeLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " 执行写锁！");
            while (true) {}
        } finally {
            writeLock.unlock();
        }
    }
}
```

加锁有A/B/C三个线程争夺读写锁，A/B两线程先争夺读锁，C线程争夺写锁。

【A线程】

+ `ReentrantReadWriteLock.ReadLock#lock`：调用该方法获取读锁；
+ `AQS#acquireShared`：调用AQS尝试获取共享锁，内部调用Sync#tryAcquireShared；
+ `ReentrantReadWriteLock.Sync#tryAcquireShared`：读写锁的主要逻辑
  1. 获取AQS中state状态值；
  2. 判断是否存在写锁；【A线程先执行，此时无写锁】
  3. 获取读锁的状态；【读写锁将state高16位作为读锁获取次数、低16位为写作时获取次数】
  4. 更新读锁的状态；
+ 执行完上述步骤，线程A获得读锁资源直接执行后继步骤。

【B线程】

+ `ReentrantReadWriteLock.ReadLock#lock`：调用该方法获取读锁；
+ `AQS#acquireShared`：调用AQS尝试获取共享锁，内部调用Sync#tryAcquireShared；
+ `ReentrantReadWriteLock.Sync#tryAcquireShared`：读锁的主要逻辑：
  1. 获取AQS中state状态值；
  2. 判断是否存在写锁；【B线程这是时，C线程尚为执行无写锁】
  3. 获取读锁的状态；【读写锁将state高16位作为读锁获取次数、低16位为写作时获取次数】
  4. 更新读锁的状态；
+ 执行完上述步骤，线程A获得读锁资源直接执行后继步骤。

【C线程】

+ `ReentrantReadWriteLock.WriteLock#lock`：调用该方法获取写锁；
+ `AQS#acquireShared`：调用AQS尝试获取排他，内部调用Sync#tryAcquire && acquireQueued(addWaiter(Node.EXCLUSIVE))；

+ 

+ `ReentrantLock.NonfairSync#tryAcquire()`：调用ReentrantLock#nonfairTryAcquire；
+ `ReentrantReadWriteLock.Sync#tryAcquire`：写锁的主要逻辑：
  1. 获取AQS中state状态值；
  2. 判断是否存在写锁；【无写锁】
  3. 判断当前是否属于持有当前锁；【被读锁持有，直接返回】
+ `AQS#addWaiter()`：将当前线程封装为一个节点，CAS将其添加到链表尾部；
+ `AQS#acquireQueued()`：当前线程调用LockSuport#park将自身阻塞。









