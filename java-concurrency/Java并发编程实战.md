# Java并发编程实战

## 一、开篇词

#### 并发问题可以总结为三个问题：分工、同步、互斥

**分工**指的是如何高效地拆解任务并分配给线程，而**同步**指的是线程之间如何协作，**互斥**则是保证同一个时刻只允许一个线程访问共享资源。

Java SDK 并发包很大部分内容都是按照这三个维度组织的，例如 Fork/Join 框架就是一种分工模式，CountDownLatch 就是一种典型的同步方式，而可重入锁则是一种互斥手段。

这三个核心问题是跨语言的，你如果要学习其他语言的并发编程类库，完全可以顺着这三个问题按图索骥。

Java SDK 并发包其余的一部分则是并发容器和原子类，这些比较容易理解，属于辅助工具，其他语言里基本都能找到对应的。

## 二、学习攻略

#### 跳出来，看全景

学习最忌讳的就是“盲人摸象”，只看到局部，而没有看到全局。所以，你需要从一个个单一的知识和技术中“跳出来”，高屋建瓴地看并发编程。当然，这**首要之事就是你建立起一张全景图**。

并发编程领域可以抽象成**三个核心问题：分工、同步和互斥**。

**分工**

Java SDK 并发包里的 Executor、Fork/Join、Future 本质上都是一种分工方法。

除此之外，并发编程领域还总结了一些设计模式，基本上都是和分工方法相关的，例如生产者 - 消费者、Thread-Per-Message、Worker Thread 模式等都是用来指导你如何分工的。

**同步**

在并发编程领域里的同步，主要指的就是线程间的协作，本质上和现实生活中的协作没区别，不过是**一个线程执行完了一个任务，如何通知执行后续任务的线程开工**而已。

协作一般是和分工相关的。Java SDK 并发包里的 Executor、Fork/Join、Future 本质上都是分工方法，但同时也能解决线程协作的问题。例如，用 Future 可以发起一个异步调用，当主线程通过 get() 方法取结果时，主线程就会等待，当异步执行的结果返回时，get() 方法就自动返回了。主线程和异步线程之间的协作，Future 工具类已经帮我们解决了。除此之外，Java SDK 里提供的 CountDownLatch、CyclicBarrier、Phaser、Exchanger 也都是用来解决线程协作问题的。

有很多场景，是需要你自己来处理线程之间的协作的。

工作中遇到的线程协作问题，基本上都可以描述为这样的一个问题：**当某个条件不满足时，线程需要等待，当某个条件满足时，线程需要被唤醒执行**。例如，在生产者 - 消费者模型里，也有类似的描述，“当队列满时，生产者线程等待，当队列不满时，生产者线程需要被唤醒执行；当队列空时，消费者线程等待，当队列不空时，消费者线程需要被唤醒执行。”

在 Java 并发编程领域，解决协作问题的核心技术是**管程**，上面提到的所有线程协作技术底层都是利用管程解决的。管程是一种解决并发问题的通用模型，除了能解决线程协作问题，还能解决下面我们将要介绍的互斥问题。可以这么说，**管程是解决并发问题的万能钥匙**。

**互斥**

分工、同步主要强调的是性能，但并发程序里还有一部分是关于正确性的，用专业术语叫“**线程安全**”。并发程序里，当多个线程同时访问同一个共享变量的时候，结果是不确定的。不确定，则意味着可能正确，也可能错误，事先是不知道的。而导致不确定的主要源头是可见性问题、有序性问题和原子性问题，为了解决这三个问题，Java 语言引入了内存模型，内存模型提供了一系列的规则，利用这些规则，我们可以避免可见性问题、有序性问题，但是还不足以完全解决线程安全问题。解决线程安全问题的核心方案还是互斥。

**所谓互斥，指的是同一时刻，只允许一个线程访问共享变量。**

实现互斥的核心技术就是锁，Java 语言里 synchronized、SDK 里的各种 Lock 都能解决互斥问题。虽说锁解决了安全性问题，但同时也带来了性能问题，那如何保证安全性的同时又尽量提高性能呢？可以分场景优化，Java SDK 里提供的 ReadWriteLock、StampedLock 就可以优化读多写少场景下锁的性能。还可以使用无锁的数据结构，例如 Java SDK 里提供的原子类都是基于无锁技术实现的。

除此之外，还有一些其他的方案，原理是不共享变量或者变量只允许读。这方面，Java 提供了 Thread Local 和 final 关键字，还有一种 Copy-on-write 的模式。

使用锁除了要注意性能问题外，还需要注意死锁问题。

这部分内容比较复杂，往往还是跨领域的，例如要理解可见性，就需要了解一些 CPU 和缓存的知识；要理解原子性，就需要理解一些操作系统的知识；很多无锁算法的实现往往也需要理解 CPU 缓存。这部分内容的学习，需要博览群书，在大脑里建立起 CPU、内存、I/O 执行的模拟器。这样遇到问题就能得心应手了。

知识全局图如下：

![image-20210604172216073](https://gitee.com/tobing/imagebed/raw/master/image-20210604172216073.png)

#### 钻进去，看本质

下一步，就是在某个问题上钻进去，深入理解，找到本质。

**工程上的解决方案，一定要有理论做基础**。

## 三、可见性、原子性和有序性问题

#### 概述

为了合理利用 CPU 的高性能，平衡CPU、内存、硬盘三者之间的速度差异，计算机体系机构、操作系统、编译程序都做出了贡献，主要体现为：

1. CPU 增加了缓存，以均衡与内存的速度差异；【预先将要用的数据更快的缓存中，避免访问磁盘】
2. 操作系统增加了进程、线程，以分时复用 CPU，进而均衡 CPU 与 I/O 设备的速度差异；【等IO的时候可以其他事】
3. 编译程序优化指令执行次序，使得缓存能够得到更加合理地利用。【合理控制流程，提高效率】

虽然使用这些技术可以通过CPU的利用效率，但是在并发程序下会导致以下意外问题。

#### 问题一：缓存导致可见性问题

单核CPU时，所有缓存都在一个CPU上执行，CPU缓存与内存数据的一致性容易解决。因为所有的线程都是在操作通过一个CPU缓存，一个线程对缓存写，另一个线程一定可见。一个线程对共享变量的修改，另外一个线程能够立刻看到，我们称为**可见性**。

多核CPU时，每颗CPU都有自己缓存，这是CPU缓存与内存的数据一致性就没这么容易解决，此时多个线程在不同的CPU上执行，不同线程操作的是不同的CPU缓存。此时不同线程操作的是基于缓存中的内存拷贝，由于每一个线程操作的是自己的缓存，此时如果对缓存中的修改无法映射给其他线程，此时其他线程对变量的操作可以是基于“旧版”来操作，这就出现了问题。

![image-20210604175814700](https://gitee.com/tobing/imagebed/raw/master/image-20210604175814700.png)

#### 问题二：线程切换导致原子性问题

由于IO操作很慢，操作采用分时调度，使得我们等待IO的时候CPU可以调度其他进程执行。操作系统按照时间来为每个进程分配一段时间，当一个进程的时间片用完，操作系统将会调度其他进程执行。**这种分时复用的方式可以很好地提高CPU资源的利用率。**

早期操作系统基于进程调度CPU，不同进程间不共享内存空间，因此进程要做任务切换就要切换内存映射地，而进程差劲啊的所有线程可以共享内存空间，因此线程切换的成本很低。因此现代操作系统基于更加轻量的线程来调度。

Java并发程序都是基于多线程，任务切换的时间大多发生在CPU指令完成，但高级语言中的一行代码可能会包含多条指令，如下：count+=1

+ 指令1：把变量count从内存load到CPU的寄存器
+ 指令2：把变量count+1
+ 指令3：把结果写入内存（可能只是写入缓存 ）

这样在多线程环境下就可能出现这样的问题：

|        线程A        |        线程B        |
| :-----------------: | :-----------------: |
| count=0加载到寄存器 |                     |
|    【线程切换】     |                     |
|                     | count=0加载到寄存器 |
|                     |      count=0+1      |
|                     |   count=1写入内存   |
|      count=0+1      |                     |
|   count=1写入内存   |                     |

由于在执行count+=1的时候，读取count的状态与写count的状态不具有原子性，在读与写之间CPU可能会调度到其他线程，就可能导致最终写的数据不是基于最新的数据来写。**把一个或者多个操作在 CPU 执行的过程中不被中断的特性称为原子性**。CPU 能保证的原子操作是 CPU 指令级别的，而不是高级语言的操作符，这是违背我们直觉的地方。因此，很多时候我们需要在高级语言层面保证操作的原子性。

#### 问题三：编译优化带来有序性问题

编译器为了优化性能，值程序执行的语句中可能与我们编码的顺序执行不同，称之为指令重排序。编译器能够保证单线程环境下指令重排序运行的正确性，但是并发环境下就可能出现一些不可预知的结果。下面以双重校验锁为例：

```java
public class Singleton03 {
    private static volatile Singleton03 instance;
    private Singleton03() {
    }
    private static Singleton03 newInstance() {
        if (instance == null) {
            synchronized (Singleton03.class) {
                if (instance == null) {
                    instance = new Singleton03();
                }
            }
        }
        return instance;
    }
}
```

在执行`instance = new Singleton03();`一句代码时，操作可以被拆分为：

1. 为实例分配内存
2. 将内存进行初始化
3. 将分配的内存地址指向Instance变量

由于存在指令重排序的优化，这行代码可能会按照以下顺序执行：

1. 为实例分配内存
2. 将分配的内存地址指向Instance变量
3. 将内存进行初始化

此时分析代码就发现可能存在一些问题：

|              线程A               |            线程B             |
| :------------------------------: | :--------------------------: |
|        instance == null？        |                              |
|             分配内存             |                              |
| 将分配的内存地址指向Instance变量 |                              |
|           【线程调度】           |                              |
|                                  |      instance == null？      |
|                                  | 成功获取到未初始化的Instance |
|         将内存进行初始化         |                              |
|                                  |                              |
|                                  |                              |

#### 思考

分析：在 32 位的机器上对 long 型变量进行加减操作存在并发隐患？

首先，在Java中long类型的的长度为8个字节，而32位的操作系统需要多条组合出来，无法保证原子性，因此并发时可能会出现问题。

https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.7

For the purposes of the Java programming language memory model, a single write to a non-volatile long or double value is treated as two separate writes: one to each 32-bit half. This can result in a situation where a thread sees the first 32 bits of a 64-bit value from one write, and the second 32 bits from another write.

Writes and reads of volatile long and double values are always atomic.、

Writes to and reads of references are always atomic, regardless of whether they are implemented as 32-bit or 64-bit values.

Some implementations may find it convenient to divide a single write action on a 64-bit long or double value into two write actions on adjacent 32-bit values. For efficiency's sake, this behavior is implementation-specific; an implementation of the Java Virtual Machine is free to perform writes to long and double values atomically or in two parts.

Implementations of the Java Virtual Machine are encouraged to avoid splitting 64-bit values where possible. Programmers are encouraged to declare shared 64-bit values as volatile or synchronize their programs correctly to avoid possible complications.

## 四、Java内存模型

#### 概述

从上面知道，缓存使用可能会导致可见性问题、线程切换导致原子性问题、编译优化会带来有序性问题。

那么我们可以通过禁用缓存和编译优化来解决可见性和有序性问题，但这样一来，性能就会降下来。合理的方案是按序禁用缓存以及编译优化，即允许程序员在编写代码的时候指定，实现按需禁用缓存和编译优化。

Java通过Java内存模型来规范JVM按需禁用缓存以及编译优化，其中具体包括了**volatile、synchronized、final关键字以及六项Happens-Before规则**。

#### volatile

volatile并不是Java特有，在C语言中也存在这个关键字，它的最原始的意义就是禁用CPU缓存。

当我们使用volatile关键字修饰一个变量，它隐含告诉编译器，对这个变量的读写不能使用CPU缓存，必须从内存中读取或写入。上述的定义看上去无懈可击，但在实际使用时仍然会存在一些问题。

```java
class Demo01Volatile {
    int x = 0;
    volatile boolean v = false;
    public void writer() {
        x = 42;
        v =true;
    }
    public void reader() {
        if (v=true) {
            // x = ?
        }
    }
}
```

假设使用两个线程分别执行writer() 和reader()，那么x会输出什么呢？

答案是在JDK1.5以前x=0或x=45，在JDK1.5及其之后，x=45。这是因为x没有被volatile修饰，因此在reader读到的可能是CPU缓存中的值。

为了避免上面的问题，在JDK1.5中引入了Happens-Before原则来增强Java内存模型。

#### Happens-Before原则

Happens-Before的通俗的含义是：**前面一个操作的结果对后继操作是可见的**。

Happens-Before约束了编译器的优化行为，即在遵守Happens-Before规则的基础上进行编译器优化。其中包含了6条规则

1. 程序的顺序性原则
2. volatile变量规则
3. 传递性
4. 管程中锁的规则
5. 线程start规则
6. 线程join规则

**规则1-程序的顺序性规则**

<font style="color:red">指一个线程中，按照程序顺序，前面的操作Happens-Before于后继的任意操作。</font>

这个规则映射到Demo01Volatile即`x = 42;`Happens-Before于 `v =true;`，因此

**规则2-volatile变量规则**

<font style="color:red">指对一个volatile变量的写操作，Happens-Before与后继对这个volatile变量的读操作。</font>

这条规则不容易理解，需要结合规则3

**规则3-传递性**

<font style="color:red">如果A Happens-Before B，且B Happens-Before C，那么A Happens-Before C。</font>

针对上述3条规则，再分析Demo01Volatile

|    线程A     |    线程B     |
| :----------: | :----------: |
|     x=42     |              |
| 写变量v=true |              |
|              | 读变量v=true |
|              |   读变量x    |

+ 应用规则1，[对于「x=42」Happens-Before 「写变量v=true」](「x=42对写变量v=true可见，即v=true时，确保x=42可见」)
+ 应用规则2，[「写变量v=true」Happens-Before「读变量v=true」](写变量v=true对读变量v=true可见)
+ 应用规则3，「x=42」Happens-Before「写变量v=true」，「写变量v=true」Happens-Before「读变量v=true」，「读变量v=true」Happens-Before「读变量x」，[则「x=42」Happens-Before「读变量x」](线程A的x=42对线程B的读变量x可见)

**规则4-管程中锁的规则**

<font style="color:red">指对一个锁的解锁Happens-Before与后继对这个锁的加锁。</font>

管程是一种通用的同步原语，在Java中可以认为是synchronized，synchronized是Java中对管程的实现。

```java
synchronized(this) { // 自动加锁
    // int x=10（共享变量）
    if (this.x < 12) {
        this.x = 12;
    }
}// 自动解锁
```

+ 引用规则4，x初始为10，线程A执行完毕x=12，解锁；线程B进入synchronized同步块，加锁，此时比如读到x=12，x=12Happens-Before加锁。

**规则5-线程start规则**

<font style="color:red">指主线程启动子线程，子线程能够给看到主线程在启动该子线程前的操作。</font>

```java
Thread B = new Thread(()->{
  // 主线程调用 B.start() 之前
  // 所有对共享变量的修改，此处皆可见
  // 此例中，var==77
});
// 此处对共享变量 var 修改
var = 77;
// 主线程启动子线程
B.start();
```

**规则6-线程join规则**

<font style="color:red">指主线程等待子线程完成，子线程完成后，主线程能够看到子线程的操作。</font>

```java
Thread B = new Thread(()->{
  // 此处对共享变量 var 修改
  var = 66;
});
// 例如此处对共享变量修改，
// 则这个修改结果对线程 B 可见
// 主线程启动子线程
B.start();
B.join()
// 子线程所有对共享变量的修改
// 在主线程调用 B.join() 之后皆可见
// 此例中，var==66
```

#### final

如果说volatile是用来限制优化，那么final则是建议编译器优化修饰变量。

#### 思考题

有一个共享变量 abc，在一个线程里设置了 abc 的值 `abc=3`，你思考一下，有哪些办法可以让其他线程能够看到abc=3？

回答1：补充一个： 在abc赋值后对一个volatile变量A进行赋值操作，然后在其他线程读取abc之前读取A的值，通过volatile的可见性和happen-before的传递性实现abc修改后对其他线程立即可见【炫技】

#### 评论区

老师，还差两个规则，分别是：

线程中断规则：对线程interrupt()方法的调用先行发生于被中断线程的代码检测到中断事件的发生，可以通过Thread.interrupted()方法检测到是否有中断发生。

对象终结规则：一个对象的初始化完成(构造函数执行结束)先行发生于它的finalize()方法的开始。

所以，个人对于Java内存模型总结起来就是：

1. 为什么定义Java内存模型？现代计算机体系大部是采用的对称多处理器的体系架构。每个处理器均有独立的寄存器组和缓存，多个处理器可同时执行同一进程中的不同线程，这里称为处理器的乱序执行。在Java中，不同的线程可能访问同一个共享或共享变量。如果任由编译器或处理器对这些访问进行优化的话，很有可能出现无法想象的问题，这里称为编译器的重排序。除了处理器的乱序执行、编译器的重排序，还有内存系统的重排序。因此Java语言规范引入了Java内存模型，通过定义多项规则对编译器和处理器进行限制，主要是针对可见性和有序性。
2. 三个基本原则：原子性、可见性、有序性。
3. Java内存模型涉及的几个关键词：锁、volatile字段、final修饰符与对象的安全发布。其中：第一是锁，锁操作是具备happens-before关系的，解锁操作happens-before之后对同一把锁的加锁操作。实际上，在解锁的时候，JVM需要强制刷新缓存，使得当前线程所修改的内存对其他线程可见。第二是volatile字段，volatile字段可以看成是一种不保证原子性的同步但保证可见性的特性，其性能往往是优于锁操作的。但是，频繁地访问 volatile字段也会出现因为不断地强制刷新缓存而影响程序的性能的问题。第三是final修饰符，final修饰的实例字段则是涉及到新建对象的发布问题。当一个对象包含final修饰的实例字段时，其他线程能够看到已经初始化的final实例字段，这是安全的。
4. Happens-Before的7个规则：
   1. 程序次序规则：在一个线程内，按照程序代码顺序，书写在前面的操作先行发生于书写在后面的操作。准确地说，应该是控制流顺序而不是程序代码顺序，因为要考虑分支、循环等结构。
   2. 管程锁定规则：一个unlock操作先行发生于后面对同一个锁的lock操作。这里必须强调的是同一个锁，而"后面"是指时间上的先后顺序。
   3. volatile变量规则：对一个volatile变量的写操作先行发生于后面对这个变量的读操作，这里的"后面"同样是指时间上的先后顺序。
   4. 线程启动规则：Thread对象的start()方法先行发生于此线程的每一个动作。
   5. 线程终止规则：线程中的所有操作都先行发生于对此线程的终止检测，我们可以通过Thread.join（）方法结束、Thread.isAlive（）的返回值等手段检测到线程已经终止执行。
   6. 线程中断规则：对线程interrupt()方法的调用先行发生于被中断线程的代码检测到中断事件的发生，可以通过Thread.interrupted()方法检测到是否有中断发生。
   7. 对象终结规则：一个对象的初始化完成(构造函数执行结束)先行发生于它的finalize()方法的开始。

5. Happens-Before的1个特性：传递性。
6. Java内存模型底层怎么实现的？主要是通过内存屏障(memory barrier)禁止重排序的，即时编译器根据具体的底层体系架构，将这些内存屏障替换成具体的 CPU 指令。对于编译器而言，内存屏障将限制它所能做的重排序优化。而对于处理器而言，内存屏障将会导致缓存的刷新操作。比如，对于volatile，编译器将在volatile字段的读写操作前后各插入一些内存屏障。

## 五、互斥锁

一个或多个操作在CPU执行过程中不被中断的特性称为“原子性”。

#### 如何保证原子性

原子性问题的源头是**线程切换**，如果能够禁用线程切换那不就能解决这个问题了吗？而操作系统做线程切换是依赖 CPU 中断的，所以禁止 CPU 发生中断就能够禁止线程切换。在早期单核 CPU 时代，这个方案的确是可行的，而且也有很多应用案例，但是并不适合多核场景。

因此保证原子性的关键是保证同一时刻内只有一个线程修改共享变量，我们称之为互斥访问。

#### 简易锁模型

锁可能很简单地实现先互斥。

把需要互斥执行的代码称为临界区。线程进入临界区之前首先尝试加锁，如果成功，则进入临界区，这时称这个线程持有锁；如果加锁失败，阻塞等待直到持有锁的线程释放锁。持有锁的线程执行完临界区的代码后，执行解锁操作。

#### 改进后的锁模型

需要明确，锁是什么，锁要保护的是什么？并发编程中，锁和锁要保护的资源时有对应关系的。

因此我们除了需要明确临界区，还需要明确临界区中需要保护的资源，进而创建出与之关联的锁。

#### synchronized

Java中提供synchronized关键字来实现锁。synchronized可以用来修饰方法，代码块。

在使用synchronized不需要显式加锁解锁，在进入synchronized修饰的方法或代码块时会自动加锁，退出这些区域时会自定解锁。

需要注意的是，在synchronized修饰不同方法时，其锁的资源并不相同：

+ 修饰状态方法，锁定当前类的Class对象
+ 修饰非静态方法，锁定当前实例对象this

显然，锁能够保证代码块执行的原子性，锁的可见性通过[Happens-Before](#Happens-Before原则)的规则4保证：对一个锁的解锁Happens-Before与后继对这个锁的加锁。

综合 Happens-Before 的传递性原则，可以得出前一个线程在临界区修改的共享变量（该操作在解锁之前），对后续进入临界区（该操作在加锁之后）的线程是可见的。

#### 锁和受保护资源的关系

**受保护资源和锁之间的关联关系是 N:1 的关系**。多把锁来保护同一个资源，但在并发领域是不行的。

```java
class SafeCalc {
  static long value = 0L;
  synchronized long get() {
    return value;
  }
  synchronized static void addOne() {
    value += 1;
  }
}
```

需要注意上面代码中，代码是用两个锁保护一个资源。这个受保护的资源就是静态变量 value，两个锁分别是 this 和 SafeCalc.class。由于加锁的本质是在锁对象的对象头中写入当前线程id，而此处的SafeCalc.class和this是不同对象，因此两个临界区不形成互斥条件。

#### 锁的粒度

在使用锁对临界资源加锁的时候，需要注意锁的粒度。如：

```java
class Account {
    private String username;
    private Integer account;
	
    // Transfer account from -> to 
    public void transfer(Account from, Account to, Integer count){
		synchronized(Account.class) {
            if (from == null || to == null) {
                return;
            }
            from.account -= count;
            to.account += count;
        }
    }
}
```

显然，如果上述例子中，在方法上加上synchronized或者在synchronized代码块中加上this锁，是无法同时锁定连个账号的。这时需要使用到Acount.class，对于该类的任意一个对象，它们拿到的Account.class对象都是一样。Account.class对象在该类加载阶段时被创建，保证并发安全。

锁的粒度除了影响业务逻辑的正确性，也会影响程序执行的性能。如：

```java
class Person {
    private String username;
    private Intger age;
    
    public void addAge() {
        synchronized(Person.class) {
	        age++;
        }
    }
}
```

显然，在上面的程序中使用Person.class对象作为锁，这肯定是能保证并发操作时能够锁住age的操作。但是，有必要使用拿到Person.class对象这么粗粒度的锁吗？不同的用户对象之间对age的操作是不会造成影响的，因此这种情况下只需要使用对象锁即可。

> 还有一点需要注意的，注意不要使用Acount.username或Account.age作为s锁的对象，因为这些对象可能会发生改变，一旦对他们重新赋值就会变成新的对象，加锁就失效了。**总的来说就是，不能用可变对象做锁。**

## 六、死锁

#### 优化锁粒度

在第[锁的粒度](# 锁的粒度)的第一个案例中，为了能够同时锁住两个账号的交易行为，使用了Account.class对象作为锁。但是这样一来系统中同一时间内只能进行一次操作，这种情况下，交易这一行为毫无并发可言，性能极低。

这时，最容易想到的是：交易时能不能只锁住交易的两个账号

```java
class Account {
    private String username;
    private Integer account;
	
    // Transfer account from -> to 
    public void transfer(Account from, Account to, Integer count){
		synchronized(from) {
            synchronized (to) {
                if (from == null || to == null) {
                    return;
                }
                from.account -= count;
                to.account += count;
            }
        }
    }
}
```

显然，上面程序由于**使用更小粒度的锁**，只会锁住交易的两个对象，并发度大大提高了。

#### 死锁

尽管上面使用了更小粒度的锁，提高到了并发度，但也会引入新的问题，死锁问题。

> 假设用户A和用户B同时向对方转账，用户A拿到了自己的锁产生去获取用户B的锁，而用户B此时页持有了自己的锁去尝试获取用户A的锁。这样双方都用占用了一部分的资源，去尝试占用对方的资源，如果没有外界的干预，就会一直死等下去。

![java_conccurency_dead_lock](https://gitee.com/tobing/imagebed/raw/master/java_conccurency_dead_lock.png)

借助资源分配图，我们可以看到两者双方都是占用了部分资源，去尝试占用剩下的资源，而剩下的资源又被另外一方持有，如此循环等待，一直死等下去。下图展示了3个用户相互请求造成死等的情况。

![resource_allocation_chart](https://gitee.com/tobing/imagebed/raw/master/resource_allocation_chart.png)

在发生死锁之后，两个用户（用户线程）都在等待对方释放资源，一直死等，一直占用系统资源但呈现去无法执行下去。很多时候，只能通过重启应用来解决死锁问题。**因此，解决死锁的最好办法还是规避死锁。**

《[操作系统原理](https://www.cs.uic.edu/~jbell/CourseNotes/OperatingSystems/7_Deadlocks.html)》中提到，死锁的产生有四个必要的条件：

+ 互斥：至少资源必须以不可共享的方式持有，其他任何线程请求此资源，该进程必须等待资源释放。
+ 保持等待：进程必须同时保持至少一个资源等待其他进程保持的资源。
+ 无抢占：一旦一个进程持有一个资源，那么该资源能不能从该进程中被拿走。
+ 循环等待：存在一个进程链，使得每个进程都占有下一个进程所需的至少一种资源。

因此，我们只需要破坏其中之一的条件，就能够保证避免死锁的发生。

+ 对于“保持等待”，可以一次性申请所有的资源，即不会发生保持一部分资源去获取另外一部分的情况
+ 对于“不可抢占”，可以让占用部分资源的线程去申请其他线程的时候，申请不到就主动释放
+ 对于“循环等待”，可以按序来申请资源

##### 破坏保持等待

破坏保持等待的条件，可以通过一次性申请所有资源来解决。

在本场景中可以通过一个中介来一次性所有需要的资源，对于这个中介必须实现同时申请资源，并且同时释放资源。

```java
// 中介【保证单例】
class Allocator {
    
    private List<Object> applicants = new ArrayList<>();
    
    private static final Allocator instance = new Allocator();
    
    private Allocator() {}
    
    public static Allocator getInstance() {
		return instance;
    }

    // 一次申请全部的资源
    synchronized boolean apply(Object from, Object to) {
        if (applicants.contains(from) || applicants.contains(to)) {
            return false;
        }
        sources.add(from);
        sources.add(to);
    }
    
    // 一次性归还所有的资源
    synchronized void free(Object from, Object to) {
        applicants.remove(from);
        applicants.remove(to);
    }
}

// 账号类
class Account {
    private String username;
    private Integer account;
    private Allocator allocator;
	
    // Transfer account from -> to 
    public void transfer(Account from, Account to, Integer count){
        // 一次性申请转出账户和转入账户，直到成功
        while(!allocator.apply(this, target));
        try {
            synchronized(from) {
                synchronized (to) {
                    if (from == null || to == null) {
                        return;
                    }
                    from.account -= count;
                    to.account += count;
                }
            }
        } finally {
            allocator.free(this, target);
        }
    }
}
```

##### 破坏不可抢占条件

synchronized并不能主动释放占有的资源，synchronized在申请资源的时候，如果申请不到会进行阻塞等待。Java提供了java.util.concurrent包中提供了Lock可以解决这个问题。

##### 破坏循环等待条件

破坏这个条件，需要对资源进行排序，按序申请资源。本例中可以通过每个属性id顺序申请。

## 七、等待唤醒机制

在使用破坏保持等待的方式来避免死锁提高并发性能的时候，使用了死循环的方式来等待一次性获取资源。使用这种方式在并发量大的时候，会导致循环的时间太长，严重消耗CPU资源。

Java 中支持一种称为等待-通知的机制（由synchronized结合wait()、notify()、notifyAll()）。使用等待-通知机制时，其执行流程如下：

+ 线程首先会获取互斥锁，当线程要求的条件不满足是，释放互斥锁，进入等待状态。
+ 当要求的条件满足时，通知等待的线程，重新获取互斥锁。

![synchronized_wait_notify1](https://gitee.com/tobing/imagebed/raw/master/synchronized_wait_notify1.png)

在使用等待唤醒机制时，只允许一个线程进入临界区。

当一个线程进入临界区，其他线程只能在临界区之外进行阻塞等待。

当进入临界区的线程因一些条件不满足时，需要进入等待状态，可以调用wait()方法。

调用wait()方法的线程会进入等待队列，同时会释放持有的锁，让其他线程有机会进入临界区。

当一个线程从临界区执行完毕，退出的时候，需要调用notify()或notifyAll()来告知等待队列中的线程来重新请求进入临界区。

> 需要注意的是，上述的等待队列仅有一个，而与互斥锁是一一对应关系。
>
> 还需要注意的是，wait()、notify()、notifyAll()三个方法必须要在获取到相应的互斥锁之后才能调用（对于synchronized就是需要在同步块中才能调用）。否则，如果直接调用，JVM会抛出一个运行时异常：`java.lang.IllegalMonitorStateException`

#### 虚假唤醒

在等待-唤醒模型中，还存在一个问题：[虚假唤醒问题](https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#wait())。

当一个线程以wait()方式来阻塞，后面又被notify唤醒时，其导致wait的条件可能已经发生了变化，这时需要重新去判断条件是否满足。

![wait_notify_spurious_wakeups](https://gitee.com/tobing/imagebed/raw/master/wait_notify_spurious_wakeups.png)

为了解决虚假唤醒的问题，Java API中也提供了一种解决思路：

```java
synchronized (obj) {
    while (<condition does not hold>)
        obj.wait();
    ... // Perform action appropriate to condition
}
```

使用等待-唤醒机制的时候，需要注意notify和notifyAll()的区别。notify是随机通知等待队列中的一个线程，而notifyAll()会通知等待队列中的所有线程。理论而言，notify只唤醒等待队列中的一个，先对要好一点。但实际上，notifyAll()能够保证能够通知所有线程，避免了某些线程永不被通知到。

> 补充1：sleep与wait的区别

1. wait会释放所有锁；sleep不会释放锁资源
2. wait只能在同步方法和同步代码块中使用；sleep可以在任意地方使用
3. wait无手动需捕获异常；sleep需要手动捕获异常
4. wait可以指定或不种指定参数；sleep必须要指定参数
5. 两者的共同点就是，都让渡CPU时间

## 八、并发编程问题

并发编程主要存在三方面问题，安全性、活跃性、性能。

#### 安全性

并发编程的安全性问题主要存在三方面，原子性、有序性和可见性。

