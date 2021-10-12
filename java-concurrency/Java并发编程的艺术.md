# Java并发编程的艺术

## 并发编程的挑战

并发编程的目的是让程序运行更快，但是想要达到这一目的需要面临很多挑战，如「上下文切换问题」、「死锁问题」、「硬件和软件资源限制问题」等。

### 上下文切换问题

单核处理器也支持多线程执行代码，CPU通过给每个线程分配CPU时间片来实现。CPU通过时间片分配算法来循环执行任务，当一个任务执行一个时间片之后，会切换到下一个任务。但是在任务切换之前，需要保存上一个任务的状态，以便于下次切换回这个任务时，可以再加载这个任务的状态。从任务保存到再加载的过程就是一次上下文切换。

线程上下文切换时，CPU需要保存当前线程的本地数据、程序指针，并加载下一个要执行的线程的本地数据、程序指针等。因此线程上下文的切换成本并不低。

#### 查看上下文切换

上下文切换的消耗主要可以通过两个工具进行度量：

+ 使用Lmbench3可以测量上下文切换的时长；
+ 使用[vmstat](https://www.cnblogs.com/ggjucheng/archive/2012/01/05/2312625.html)可以测量上下文切换的次数；

#### 减少上下文切换

减少上下文切换的方式主要有：无锁并发编程、CAS算法、使用最少线程和使用协程。

+ **无锁并发编程**：多线程竞争锁的时候，会引起上下文切换，因此多线程处理数据时，可以将资源分段，不同线程处理不同段的数据。如根据数据ID按照Hash算法取模分段。
+ **CAS算法**：Java的Atomic包使用CAS算法更新数据，不需要加锁。
+ **使用最少线程**：避免创建不需要的线程，如给很少的任务创建很多的线程来处理，会造成大量线程处于等待状态。
+ **使用协程**：使用单线程实现多任务调度，在单线程内部维持多个任务间的切换。

#### 优化上下文切换

通过减少线上大量的WAITING线程，从而减少上下文切换的次数。主要流程如下：

1. 使用jstack命令dump线程信息；
2. 统计所有线程分别处于什么状态；
3. 打开dump文件查看处于WATING的线程正在做什么；
4. 根据第四步，调整程序优化；
5. 重新dump线程，重新统计信息，查看效果；

WAITING的线程减少，系统上下文切换次数就会变少，因为每次WAITING到RUNNABLE都要进行一次上下文切换。

### 死锁

锁是多线程编程的重要工具，应用场景众多，但使用的过程中稍不注意可能会导致死锁的发生。

线程死锁是指两个或两个以上线程在执行过程中，由于资源竞争或彼此通信而造成阻塞的现象，如果无外力作用，它们都将无法推进下去。

#### 死锁典例

假设用户A和用户B同时向对方转账，用户A拿到了自己的锁产生去获取用户B的锁，而用户B此时持有了自己的锁去尝试获取用户A的锁。这样双方都用占用了一部分的资源，去尝试占用对方的资源，如果没有外界的干预，就会一直死等下去。

![java_conccurency_dead_lock](https://gitee.com/tobing/imagebed/raw/master/java_conccurency_dead_lock.png)

在发生死锁之后，两个线程都在等待对方释放资源，一直死等，一直占用系统资源但是无法执行下去。很多时候只能通过重启应用的方式来解决死锁。因此解决死锁的最好方式是规避死锁。

#### 死锁的规避

《[操作系统原理](https://www.cs.uic.edu/~jbell/CourseNotes/OperatingSystems/7_Deadlocks.html)》中提到，死锁的产生有四个必要的条件：

+ **互斥**：至少一个资源必须以不可共享的方式持有，其他任何线程请求此资源，该进程必须等待资源释放。
+ **保持等待**：进程必须同时保持至少一个资源等待其他进程保持的资源。
+ **无抢占**：一旦一个进程持有一个资源，那么该资源不能从该进程中被拿走。
+ **循环等待**：存在一个进程链，使得每个进程都占有下一个进程所需的至少一种资源。

因此，我们只需要破坏其中之一的条件，就能够保证避免死锁的发生。

+ 对于“保持等待”，可以一次性申请所有的资源，即不会发生保持一部分资源去获取另外一部分的情况。如通过一个中介资源申请者来申请资源。
+ 对于“不可抢占”，可以让占用部分资源的线程去申请其他线程的时候，申请不到就主动释放。Java的synchronized不提供主动释放占有的资源，但可以通过juc包下的Lock解决这个问题。
+ 对于“循环等待”，可以按序来申请资源。如对每个资源进行编号，所有线程按照编号顺序申请。

### 资源挑战

并发编程中，程序的速度受限于计算机硬件资源和软件资源。如服务器带宽只有2Mb/s，某个资源的下载速度是1Mb/s，程序启动10个线程也无法使得下载速度变为10Mb/s。因此并发编程中应该考虑资源的限制。

+ 硬件资源的限制：带宽的上行/下行速度、硬盘读写速度、CPU处理速度；
+ 软件资源的限制：数据库连接数、Socket连接数；

【硬件限制解决方案】

对于硬件资源的限制，可以考虑使用集群并行执行程序。通过哈希算法，让不同数据由不同的机器来处理。

【软件限制解决方案】

对于软件资源的限制，可以考虑使用资源池将资源复用。通过使用连接池将数据库和Socket连接复用。

【并发编程注意点】

除此之外并发编程中，如果将一个需要串行执行的线程并行执行，非但不能提高程序的执行效率，还会引入上下文切换以及资源调度的开销。

## 并发机制底层原理

Java代码在编译之后会编程Java字节码，字节码在被类加载器加载到JVM中，JVM会执行字节码，最终将其转换为汇编指令在CPU上执行。Java使用的并发机制依赖于JVM的实现和CPU的指令。

### CPU理论知识

#### CPU相关术语

| 术语       | 英文单词               | 术语描述                                       |
| ---------- | ---------------------- | ---------------------------------------------- |
| 内存屏障   | memory barriers        | 用于实现对内存操作的顺序限制，一组处理器指令   |
| 缓冲行     | cache line             | 缓存中可以分配的最小储存单位                   |
| 原子操作   | atomic operations      | 不可中断的一系列操作                           |
| 缓存行填充 | cache line fill        | 处理器将认为可缓存的数据按缓存行读取到缓存中   |
| 缓存命中   | cache hit              | 如果访问的数据在缓存中存在，直接访问缓存中数据 |
| 写命中     | write hit              | 如果写入的数据恰好在缓存中，直接写入缓存       |
| 写缺失     | write misses the cache | 有效的缓冲行被写入到不存在的内存区域           |

#### CPU术语定义

| 术语         | 英文                  | 解释                                                         |
| ------------ | --------------------- | ------------------------------------------------------------ |
| 缓存行       | Cache line            | 缓存的最小操作单位                                           |
| 比较并交换   | Compare And Swap      | CAS包含两个值：新值和旧值。操作期间比较旧值有没有发送变化，没有变化才交换新值，变化则不交换 |
| CPU流水线    | CPU pipeline          | CPU中5\~6个不同功能的电路单元组成一条指令处理流水线，然后将一条指令分为合成5\~6步后再由这些电路单元分别执行，这样可以在一个CPU时钟周期完成一条指令，因此提高CPU运算速度。 |
| 内存顺序冲突 | Memory order violaton | 内存顺序冲突一般是false shared导致，当出现这个内存顺序冲突时，CPU必须清空流水线。 |



### volatile

volatile是轻量级的synchronized，在多处理器开发中保证共享变量的「可见性」。可见性即一个线程修改一个共享变量，其他线程能够读取到该共享变量修改后的值。如果volatile使用恰当，会比synchronized的使用和执行成本更低，因为它不会引起线程上下文切换和调度。

#### volatile定义

Java编程语言允许线程访问共享变量，但为了确保共享变量能够被准确和一致地更新，线程应该确保通过排他锁获取这个变量。如果一个字段被声明为volatile，Java线程内存模型确保所有线程看到这个变量的值是一致的。

#### volatile原理

volatile的可见性通过生成插入汇编指令来实现。

volatile变量进行**写操作**前，JVM会先处理器发送一条Lock前缀指令。Lock前缀指令会引发两个行为。

+ **Lock指令会引起处理器缓存写回到内存；**

  将这个变量所在缓存行的数据写回到系统内存。

+ **一个处理器缓存的写回导致其他处理器的缓存无效；**

  每个处理器通过嗅探在总线上传播的数据，会检查自己缓存的值是否已经过期，当处理器发现自己缓存行对应的内存地址被修改，会将当前处理器的缓存设置为无效。当处理器对这个数据进行修改操作操作时，会重新从系统内存中被数据读取到处理器缓存。

#### false-share

JUC包作者 Doug lea 在JDK 7中新增了一个队列集合类LinkedTransferQueue，在使用volatile变量是，通过追加字节码优化队列出队和入队的性能。

LinkedTransferQueue中使用内部类定义队列的头结点head和尾节点tail。Doug lea 通过将LinkedTransferQueue的head和tail追加字节码，使得这两个共享变量的占用64字节。

对现行的大部分CPU而言，其L1、L2、L3缓存的高速缓存行时64Byte的，不支持部分填充缓存行。这就意味着如果队列的头节点head和尾节点tail不足64字节，处理器会将它们读到同一缓存行中，在多处理器下每个处理器都会缓存相同的头、尾节点，当一个处理器试图修改头节点，会将整个缓存行锁定，在缓存一致性的机制作用下，会导致其他处理器不能访问自己高速缓存中的尾节点。而队列中的出队入队操作会不断需改头节点和尾节点，因此在多处理器下会严重影响队列的入队和出队效率。

### synchronized

synchronized是并发编程中元老级角色，一般人们称其为重量级锁。但随着JDK1.6对其进行各种优化，有些情况下它并不是那么重。

在Java中，每个对象都就可以作为锁，具体而言有以下3种表现形式：

+ 对于同步方法而言，锁是当前实例对象；

+ 对于静态同步方法而言，锁是当前Class对象；

+ 对于同步方法块，所示synchronized括号中的对象；

JVM基于进入和退出Monitor对象来实现方法的同步和代码块的同步，但两者实现细节稍有不同。

+ 代码块同步使用monitorenter和monitorexit指令实现；
+ 方法同步使用另外一种方法，细节在JVM规范中没有详细说明，但同样可以使用上述方法；

JVM会在同步代码块开始的位置插入monitorenter指令；在方法结束或异常出插入monitorexit。JVM保证每个monitorenter都会有对应的monitorexit与之配对。

任何一个对象都会有一个monitor与之关联，当且一个monitor被持有之后，它处于锁定状态。线程执行到monitorenter指令，将会超时获取对象对应的monitor所有权，尝试获得对象的锁。

#### Java对象头

synchronized使用的锁位于Java对象头中。如果对象是数组类型，虚拟机使用3个Word储存对象头，如果对象是非数组类型，使用2个Word储存对象头。对象头结构如下：

| 长度     | 内容                   | 说明                         |
| -------- | ---------------------- | ---------------------------- |
| 32/64bit | Mark Word              | 储存对象的hashCode或锁信息等 |
| 32/64bit | Class Metadata Address | 储存到对象类型的数据的指针   |
| 32/32bit | Array Length           | 数组的长度【可选】           |

【Mark Word】

Java对象头的Mark Word 默认储存了对象的HashCode、分代年龄和锁标记。

| 锁状态   | 25bit        | 4bit         | 1bit       | 2bit   |
| -------- | ------------ | ------------ | ---------- | ------ |
| 无锁状态 | 对象hashCode | 对象分代年龄 | 是否偏向锁 | 锁标志 |

运行期间，Mark Word储存的数据会随着锁标志位的变化而变化。Mark Word可能会变化为储存以下4种数据。

|  锁状态  |           25bit+4bit            | 1bit       | 2bit |
| :------: | :-----------------------------: | ---------- | ---- |
| 轻量级锁 |      指向栈中锁记录的指针       | 是否偏向锁 | 00   |
| 重量级锁 |        指向互斥量的指针         |            | 10   |
|  GC标记  |               空                |            | 11   |
|  偏向锁  | \|线程ID\|Epoch\|对象分代年龄\| | 1          | 01   |

#### 锁的升级

Java SE1.6为了减少获得锁和释放锁带来的性能消耗，引入了「偏向锁」、「轻量级锁」，因此在Java SE1.6中，锁一共有4种状态，从低到高分别是**：无锁状态==>偏向锁状态==>轻量级锁状态==>重量级锁状态**，几种状态随着竞争情况逐渐升级。

需要注意，锁可以升级当时不能降级。

**【偏向锁】**

大多数情况下，锁不仅不存在多线程竞争，而且总是由同一个线程多次获得，于是引入了偏向锁。

当一个线程访问同步块并获得锁时，会在对象头和栈帧中记录储存锁偏向的线程ID，以后该线程在进入和退出同步块时不需要进行CAS操作来加锁和解锁，只需要简单测试一下对象头的Mark Word中是否储存指向当前线程的偏向锁。

+ 如果测试成功，表示线程已经获得锁。
+ 如果测试失败，则需要再测试一下Mark Word中偏向锁的表示是否设置为1；
  + 如果没有设置，则使用CAS竞争锁；
  + 如果设置了，则尝试使用CAS将对象头的偏向锁指向当前线程；

偏向锁使用了一种等到竞争出现才释放锁的机制，因此当其他线程尝试竞争偏向锁的时候，持有偏向锁的线程才会释放锁。

偏向锁的撤销需要等待全局安全点。它首先暂停拥有偏向锁的线程，然后检查持有偏向锁的线程是否存活

+ 如果线程不处于活动状态，则将对象设置为无锁状态；
+ 如果线程仍然活着，拥有偏向锁的栈会被执行，遍历偏向对象的锁记录，栈中锁记录和对象头的Mark Work重新偏向其他线程，要么恢复到无锁或标记对象不适合作为偏向锁，最后唤醒暂停的线程。

Java 6和Java 7中偏向锁默认开启，但它在应用程序启动几秒后才会激活。如果必要，可以使用JVM参数关闭延迟，同时可以通过JVM参数关闭偏向锁。

**【轻量级锁】**

线程在执行同步块之前，JVM会先在当前线程栈帧中创建用于储存锁记录的空间，并将对象头中的Mark Word复制到线程的锁记录中（这个过程称为Displaced Mark Word）。然后线程尝试使用CAS将对象头中的Mark Word替换为指向锁记录的指针。

+ 如果成功，当前线程获得锁；
+ 如果失败，表示其他线程竞争锁，当前线程边尝试使用自旋获取锁；

轻量级解锁的时候，会使用原子的CAS操作将Displaced Mark Word替换回到对象头。

+ 如果成功，表示没有竞争发生；
+ 如果失败，表示存在锁竞争，锁膨胀重量级锁；

因为自旋锁会消耗CPU，为了避免无用的自旋，一旦锁升级成重量级锁，就不会在恢复到轻量级锁状态。当锁处于这个状态下，其他线程试图获取锁时，都会被阻塞住，当持有锁的线程释放锁之后会唤醒这些线程，被唤醒的线程就会进行新一轮的夺锁之争。

**【锁的对比】**

| 锁       | 优点                                                         | 缺点                                           | 使用场景                           |
| -------- | ------------------------------------------------------------ | ---------------------------------------------- | ---------------------------------- |
| 偏向锁   | 加锁和解锁不需要额外的消耗，和执行非同步方法相比仅存在纳秒级的差距 | 如果线程间存在锁竞争，会带来额外的锁撤销的消耗 | 适用于仅一个线程访问同步块场景     |
| 轻量级锁 | 竞争的线程不会阻塞，提高程序的响应速度。                     | 如果始终得不到锁竞争的线程，使用自旋会消耗CPU  | 追求响应时间，同步块执行速度非常快 |
| 重量级锁 | 线程竞争不使用自旋，不会消耗CPU                              | 线程阻塞，响应时间缓慢                         | 追求吞吐量同步块执行效率速度较长   |

### 原子操作基本原理

原子操作表示为「不可被中断的一个或一系列操作」。

#### 原子操作的处理器实现

32位IA-32处理器使用基于对**缓存加锁**或**总线加锁**的方式来实现多处理器之间的原子操作。

处理器会自动保证基本的内存操作的原子性。处理器保证从系统内存中读取或写入一个字节是原子的。

比较新的处理器会保证单处理器对同一个缓存行中进行16/32/64位的操作是原子性的，但复杂内存操作不能保证。

但是处理器提供「总线锁」和「缓存锁」两个机制来保证复杂内存操作的原子性。

**【总线锁】**

如多个处理器同时对共享变量机型读改写操作，那么共享变量会被多个处理器同时操作，这些操作不是原子的。原因是多个处理器可能从各自的缓存中读写改变量，而此时缓存中的值有可能已经被其他处理器做出了修改。

为了保证这些操作的原子性，必须保证CPU1读写改共享变量时，CPU2不能操作缓存该共享变量内存地址的缓存。

处理器使用总线锁来解决这个问题。总线锁即使用处理器提供的LOCK #信号，当一个处理器在总线上输出次信号，其他处理器请求会被阻塞，此时该处理器可以独享共享内存。

**【缓存行锁】**

同一时刻，仅需要保证对某个内存地址的操作是原子性即可，采用总线锁会把CPU和内存之间的操作锁住，这使得锁定期间其他处理器不能操作其他内存地址的数据，因此总线锁定的开销比较大，目前处理器在某些场合使用缓存锁定代替总线锁定进行优化。

频繁使用的内存会存在处理器的L1/L2和L3高速缓存中，原子性操作就可以直接在处理器内部缓存执行，而不需要声明总线锁。目前处理器可以使用「缓存锁定」的方式来实现复杂的原子性。所谓「缓存锁定」指内存区域如果被缓存在处理器缓存行，并在Lock操作期间被锁定，那么的它执行锁操作会写到内存时，处理器不在总线上声明LOCK#信号，而是修改内部的内存地址，并允许它的缓存一致性机制保证操作的原子性，因为缓存一致性机制会阻止同时修改有两个以上处理器缓存的内存区域数据，当其他处理器会写也被锁定的缓存行的数据时，会使得缓存行无效。

【无法使用缓存】

有两种情况下处理器不会使用缓存锁定。

+ 当操作数据不能被缓存在处理器内部，或操作的数据跨多个缓存行时，处理器会调用总线锁定；
+ 有些处理器不支持缓存锁定。

#### 原子性操作的Java实现

Java中可以通过**锁**和**CAS**的方式来是实现原子操作。

**【循环CAS实现原子性】**

JVM中CAS操作利用了处理器提供的「CMPXCHG」指令实现，基本思路是循环进行CAS操作直到成功为止。

JDK1.5开始，JDK并发包挺过来一些类来支持原子操作，如AtomicBoolean、AtomicIntger等。这些原子包装类提供了原子性方式将当前值加一或减一。

在Java并发包中有一些并发框架使用了自旋CAS方式是吸纳原子操作。虽然CAS很高效解决了原子操作，但仍然存在三大问题。

+ **ABA问题**：CAS需要在操作值的时候检查值是否发生变化，如果没有则更新。但存在一个值原来是A，变成B，后又变成A，那么使用CAS检查可能无法发现其变化。ABA解决思路是使用版本号，在变量前添加版本号，每次变量更新时把版本号加1。（AtomicStampedReference）
+ **循环时间长开销大**：自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销。
+ **只能保证一个共享变量的原子操作**：要对多个共享变量操作时，循环CAS无法保证操作的原子性，这时可以使用锁。另外一个取巧的方法是将多个变量合并到一个变量。JDK1.5提供了AtomicReference类保证引用对象之间的原子性。

**【锁机制实现原子性】**

锁机制保证只有获得锁的线程才能操作锁定的内存区域。

## Java并发编程基础

Java诞生就选择内置对多线程的支持。

### 线程简介

现代操作系统在运行程序时会为其创建一个进程。现代操作系统中系统调度的最小单位是线程，也叫轻量级进程。一个进程中可以创建多个线程，这些线程拥有各自的计数器、堆栈和局部变量等属性，同时它们可以访问共享的内存变量。

当启动一个Java程序，操作系统会创建一个Java进程。Java程序从main方法开始执行，其中除了存在一个main来执行我们编写的代码，还会存在一些辅助的线程：

+ Signal Dispatcher：分发处理发给JVM信号的进程
+ Reference Handler：清除Reference的线程
+ Finalizer：调用对象finalize方法的线程
+ main：用户程序入口

#### 多线程的优点

使用多线程拥有主动好处，主要原因是：

+ **更多的处理器核心**：随着处理器上的核心越来越多以及超线程技术的广泛运用，大多数计算机都比以往更加擅长并行计算。多线程技术可以更加充分利用多处理器核心，减少程序处理时间。
+ **更快的响应时间**：有是一个业务需要执行多个操作，如果这些操作不具备依赖性，可以利用多线程并发执行，降低接口的响应时间，提高用户体验。
+ **更好的编程模型**：Java为多线程模型提供了良好一致的编程模型，使得开发人员更加专注于问题解决，而非如何考虑将其多线程化。

#### 线程优先级

现代操作系统基于时间片轮转来调度线程，每个线程分配若干时间片，当时间片用完就会发生线程调度，并等待下次分配。线程分配到的时间片决定了线程使用处理器资源的多少，而线程优先级决定了线程需要多或少分配处理器资源的线程属性。

在Java线程中通过整形变量priority来控制优先级，优先级范围是1~10，在创建线程时可以通过setPriority设置，默认为5。优先级高的线程分配时间片要多于优先级低的线程。通常：

+ 偏计算的线程应该设置较低的优先级，确保CPU不会被独占；
+ 频繁阻塞的线程需要设置较高的优先级；如休眠或I/O操作；

在不同JVM以及操作系统，线程规划存在差异，有些操作系统甚至回来对优先级的设定。

#### 线程的状态

Java线程在生命周期中可能会存在以下6种状态，给定一个时刻，只可能存在其中一种状态。

+ **NEW**：初始状态，线程被构建，还没调用start方法；
+ **RUNNABLE**：运行状态，Java线程将操作系统中的「就绪」和「运行」笼统称为RUNNABLE；
+ **BLOCKED**：阻塞状态，表示线程阻塞于锁；
+ **WAITING**：等待状态，表示进程进入等待状态，表示当前线程需要等待其他线程做出一定动作(通知或中断)；
+ **TIME_WAITING**：超时等待状态，不同于WAITING，可以在指定时间自动返回；
+ **TERMINATED**：终止状态，表示当前线程已经执行完毕。

> 可以通过JPS+jstack查看某个线程的状态

线程在自身生命周期中不是固定处于某个状态，而是随着代码会执行在不同线程之间进行切换，Java线程状态变迁如下图所示：

![image-20210925150028540](https://gitee.com/tobing/imagebed/raw/master/image-20210925150028540.png)

1. 线程创建之后，调用start方法开始**运行**；
2. 当线程执行wait()方法之后，线程进入了**等待状态**；
3. 进入**等待状态**的线程需要依赖其他线程的通知才能返回；
4. 进入**超时等待状态**相当于在等待状态的基础添加了超时限制，在超时时间到达时会返回**运行状态**；
5. 当方法调用同步方法是，在没有获取锁的情况下，线程将进入**阻塞状态**；
6. 线程在执行Runnable的run方法之后将会进入**终止状态**。

> 线程阻塞在synchronized关键字修饰的代码块外是阻塞状态；但是阻塞在java.util.concurrent包中Lock接口的线程状态确实等待状态。因为juc包中的Lock接口对于阻塞的实现使用了LockSupport类中的相关方法。

#### Daemon线程

Daemon线程是一种支持性线程，因为它主要被作用程序中后台调度以及支持性工作。当一个Java虚拟机不存在非Daemon线程时，Java虚拟机将会退出。可以在线程启动时，通过Thread.setDaemon将线程设置为Daemon线程。

需要注意的是，在创建Daemon线程时不能依赖finally块中的内容来确保执行关闭或清理资源的逻辑。

### 启动和终止线程

通过调用线程的start方法进行启动，随着run方法的执行完毕线程也随之终止。

#### 构建线程

线程运行前需要完成构建，线程对象在构建是需要提供线程需要的属性，如：线程组、线程优先级、是否Daemon等信息。

一个新构建的线程对象由其parent线程来进行空间分配，child线程继承了parent是否为Daemon、优先级和加载资源的contextClassLoader以及可继承的ThreadLocal，同时还会分配一个唯一的ID来标识这个child线程。

#### 启动线程

线程对象在初始化完成之后，调用start方法可以启动这个线程。调用了start方法，当前线程同步告知Java虚拟机，只要线程规划器空闲，应该立即启动调用start方法的线程。

> 启动一个线程前最后为线程设置一个名称，这样有利于使用jstack分析程序或进行问题排查。

#### 中断的理解

中断可以认为是线程的一个标识位属性，表示一个运行中的线程是否被其他线程进行了中断操作。

其他线程可以通过调用一个线程的interrupt方法将该线程进行中断。

+ 线程通过检查自身是否被中断来进行响应；

+ 线程通过方法isInterrupted来判断是被中断；
+ 线程可以调用静态方法Thread.interrupted对当前线程的中断标识进行复位；
+ 如果线程处于终止状态，即使线程被中断过，调用线程的isInterrupted也仍然返回fasle；

在Java的API中，很多声明抛出了InterruptedException的方法在抛出该异常前，Java虚拟机会先将线程的中断标识位清除；

#### 过时的方法

Thread的suspend、resume和stop分别对应了暂停、恢复和停止一个线程。但是这些API都是过时的，不建议使用。

+ 如对于suspend方法，在调用之后线程不会释放已经占有的资源，而是占有资源进入睡眠，这样容易导致死锁；

+ 同样，stop方法会在终结一个线程是不保证线程的资源正常释放，通常没有给予线程完成资源释放工作的机会。

由于上述方法存在副作用，因此被标识为过时，暂停和恢复操作可以用后面提到的等待/通知机制来替代。

#### 安全终止进程

中断操作是一种简便线程间交互的方式，这种交互方式适合用于取消或停止任务。除了中断，还可以利用一个boolean变量来控制是否需要停止任务并终止该任务。通过标识位或中断操作的方式，能够使得线程在终止时有机会清理资源，更加安全和优雅。

### 线程间通信 

线程开始运行，拥有即的占空间，按照既定的代码执行，直到终止。如果线程是孤立运行，产生的价值很小，如果多个线程相互协作，将会带来巨大价值。

#### volatile和synchronized

Java支持多给线程同时访问一个对象或对象成员变量，由于每个线程可以拥有这个变量的拷贝，因此程序在执行过程中，一个线程看到的变量不定是最新的。

volatile关键字修饰变量时，表示任何对该变量的访问需要从共享内存中获取，而对它的改变必须同步刷新共享内存，它能保证所有线程对变量的访问的可见性。

synchronized关键字可以修饰方法或代码块的形式来进行使用，主要确保多个线程在同一个时刻，只能有一个线程处于方法或同步块中，它保证了线程对变量访问的可见性或排他性。

 对于同步块实现使用了monitorenter和monitorexit指令，同步方法依靠方法在修饰符上的ACC_SYNCHRONIZED来完成。无论采用何种方式，本质上都是对一个对象的监视器进行获取，而这个获取过程是排他的。

任意一个对象拥有自己的监视器，当对这个对象由同步块或者这个对象的同步方法调用时，执行方法的线程必须得到该对象的监视器才能进入到同步块和同步方法，而没有获取到监视器的线程将会在同步块和同步方法的入口处，进入BLOCKED状态。

![image-20210926231951706](https://gitee.com/tobing/imagebed/raw/master/image-20210926231951706.png)

任意对象对Object的访问，首先要获取Object监视器。如果获取失败，线程进入同步队列，线程状态变为BLOCKED。当访问Object的前驱释放了锁，则盖是否操作将唤醒在同步队列中的线程，使其重新尝试对监视器的获取。

#### 等待/通知机制

等待/通知机制是指一个线程A调用了对象O的wait方法进入等待状态，而另一个线程B调用对象O的notify或notifyAll方法，线程A收到通知之后从对象O的wait方法返回，进而执行后继的操作。

上述两个线程通过对象O完成交互，而对象上的wait和notify/notifyAll的关系就行开关信号，用来完成等待方和通知方之间的交互工作。

| 方法名称        | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| notify          | 通知一个在对象上等待的线程，使其从wait()方法返回，而返回的前提是该线程获取到对象的锁 |
| notifyAll       | 通知所有等待在该对象上的线程                                 |
| wait            | 调用该方法的线程进入WAITING状态，只有等待另外线程的通知或被中断才会返回，需要注意，调用wait方法之后，会释放对象的锁 |
| wait(long)      | 超时等待一段时间，这里的参数时间是毫秒，如果没有通知就会超时返回 |
| wait(long, int) | 对超时时间更细粒度的划分，可以到达纳秒                       |

+ 调用wait方法、notify或notifyAll方法时需要先对调用对象加锁；
+ 调用wait方法之后，线程状态有RUNNING变为WAITING，并将当前线程放置到对象的等待队列中；
+ notify或notifyAll方法调用后，等待线程依旧不会从wait返回，需要调用notify或notifyAll的线程释放锁之后，等待线程才有机会从wait返回；
+ notify方法将等待队列中的一个等待线程从等待队列移到同步队列中，而notifyAll将等待队列的所有的线程全部移动到同步队列，被移动的线程状态由WAITING转变为BLOCKED；
+ 从wait方法返回的前提是获得了调用对象的锁。

综上，等待/通知机制依托于同步机制，目的是确保等待线程从wait方法返回时能够感知到通知线程对变量做出的修改。

![image-20210926234238969](https://gitee.com/tobing/imagebed/raw/master/image-20210926234238969.png)

WaitThread首先获取对象的锁，然后调用对象的wait方法，从而放弃锁进入对象的等待度列WaitQueue中，进入等待状态。由于WaitThread释放了对象的锁，NotifyThread随后获取了对象的锁，并调用对象notify方法，将WaitThread从WaitQueue移动到SynchronizedQueue中，此时WaitThread的状态变为阻塞状态。NotifyThread释放了锁之后，WaitThread再次获取到锁并从wait方法返回继续执行。

【等待/通知的经典范式】

在实际实现等待/通知机制的时候，可以遵循固定的范式来进行编写，范式分为两部分，分别针对等待方（消费者）和通知方（生产者）。

**等待方**遵循如下原则：

1. 获取对象的锁；
2. 如果条件不满足，调用对象的wait方法，被通知之后仍要检查条件；
3. 条件满足则执行对应的逻辑。

对应的伪代码如下：

```java
synchronized (对象) {
    while (条件不满足) {
        对象.wait();
    }
	对应的处理逻辑
}
```

通知方准信如下原则：

1. 获取对象锁
2. 改变条件
3. 通知所有等待在对象上的线程

对应的伪代码如下：

```java
synchronized (对象) {
    改变条件
    对象.notifyAll();
}
```

#### 管道输入/输出流

与文件输入/输出或网络输入/输出流不同，管道输入/输出流主要用于线程之间数据传输，传输的媒介为内存。管道流主要包括了4种具体实现。

+ PipedOutputStream、PipedInputStream：面向字节
+ PipedWriter、PipedReader：面向字符

对于Piped类型的流，在使用前必须先要进行绑定，即调用connect方法，如果没有将输入输出流绑定，对于该流将会抛出异常。

#### Thread.join

如果一个线程A执行了thread.join语义，其含义是：线程A等待thread线程终止之后才从thread.join返回。除此之外，Thread还提供了join(long)和join(long, int)两个局部超时返回的方法。这两个超时方法表示，如果线程thread在给定的超时时间内没有返回，那么会从该超时方法返回。

Thread.join的原理是，当线程终止时，会调用线程自身的notifyAll方法，会通知所有等待在该线程对象上的线程。Thread.join的基本流程如下：

```java
public final synchronized void join () throws InterruptedException {
    // 条件不满足，继续等待
    while (isAlive) {
        wait(0);
    }
    // 条件符合，方法返回
}
```

从上面的代码可以看出，Thread.joind实现逻辑和等待/通知经典范式一致，仅加锁、循环和处理逻辑3个步骤。

#### ThreadLocal 的使用

ThreadLocal，即线程变量，是有个以ThreadLocal对象为键，任意对象为值的储存结构。这个结果被附带在线程上，即一个线程可以根据一个ThreadLocal查询到绑定在这个线程上的一个值。

可以通过set(T)方法来设置一个值，在当前线程下再通过get方法获取原来设置的值。

### 线程应用实例

#### 等待超时模式

前面提到的等待/通知的经典范式中，并没有做到超时等待。而超时等待的加入，，只需要对经典范式做出小的改定，改动内容如下：

1. 假设超时时间段是T，可以推断出当前时间now+T之后会超时；
2. 定义变量：等待持续时间（REMAINING=T）和超时时间（FUTURE=now+T）；
3. 使用wait(REMAINING)替换原来的wait()；
4. 如果wait(REMAINING)返回之后执行：REMAINING=FUTURE-now；如果：
   1. REMAINING <= 0，表示已经超时，直接退出；
   2. REMAINING >0，继续执行wait(REMAINING)。

```java
public synchronized Object get(long mills) throws InterruptException {
    long future = System.currentTimeMills() + mills;
    long remaining = mills;
    // 当超时大于0并且result返回值不满足要求
    while ((result == null) && remaing > 0) {
        wait(remaining);
        remaining = futrue - System.currentTimeMills();
    }
    return result;
}
```

#### 简单数据库连接池

可以通过等待超时模型实现一个简单的数据库连接池。

从数据库连接池获取、使用和释放连接的过程，和客户端获取连接的过程被设定为等待超时模式，即1000ms内如果无法获取到可用连接，将会返回给客户端一个null。

## Java中的锁

对Java并发包与锁相关的API和组件的学习可以围绕两方面：使用和实现进行学习。

### Lock接口

Java SE5 之后，并发包增加了Lock接口来实现锁功能，它提供了synchronized关键字类似的同步功能。但是与synchronized不同，在使用时需要显式地获取和释放锁。虽然缺少了隐式获取释放锁的便捷性，但是却拥有了锁获取与释放的可操作性、可中断的获取锁以及超时获取锁等多种synchronized关键字不具备的同步特性。

Lock接口提供synchronized关键字不具备的主要特性如下：

+ **尝试非阻塞地获取锁**：当前线程尝试获取锁，如果这一时刻没有被其他线程获取，则成功返回并持有锁；
+ **能被中断地获取锁**：获得到锁的线程能够响应中断，当获取到锁的线程被中断，中断异常将被抛出，同时锁会被释放；
+ **超时获取锁**：在指定的截止时间之前获取锁，如果截止时间仍旧无法获取锁，则返回。

 Lock中定义了锁获取和释放的基本操作：

| 方法名称                        | 描述                                                         |
| ------------------------------- | ------------------------------------------------------------ |
| void lock()                     | 调用该方法的当前线程将会获取锁，当锁获取后，从该方法返回；   |
| void lockInterruptiably()       | 可中断地获取锁，在缩回去之后可以中断当前线程；               |
| boolean tryLock()               | 尝试非阻塞获取锁，调用该方法立即返回，true表示获成功，false表示失败； |
| boolean tryLock(long, TimeUnit) | 超时获取锁，当线程超时时间内获得锁或超时时间内被中断或超时时间到达都会返回 |
| void unlock()                   | 释放锁                                                       |
| Condition newCondition()        | 获取等待通知组件，组件与锁绑定，当前线程只有获得所，才能调用组件的wait方法，调用后当前线程释放锁 |

Lock的使用方法非常简单，如下所示：

```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    // ...
} finally {
    lock.unlock();
}
```

### 队列同步器AQS

AbstractQueuedSynchronizer，是用来构建锁或其他同步最贱的基础框架，它使用一个int成员变量表示同步状态，通过内置的FIFO队列完成资源获取线程的排队工作。

AQS的主要使用方式是继承，子类通过继承AQS并实现它的抽象方法来管理同步状态，在抽象方法中实现中使用AQS提供的3个方法(getState/setState/compareAndSetState)来对状态进行操作。子类推荐被定义为自定义同步组件的静态内部类，AQS自身没有实现任何接口，仅仅定义了若干同步状态获取和释放的方法来供自定义同步组件使用，AQS计科院支持独占式获取同步状态，也可以支持共享式获取同步状态，这样可以方便实现不同类型的同步组件。

AQS是是实现锁的关键，锁的实现中聚合AQS，利用AQS实现锁的语义。

锁是面向使用者的，定义了使用者与锁交互的接口，隐藏了实现细节；AQS面向的实现者，简化了锁的实现方式，屏蔽了同步状态管理、线程的排队、等待与唤醒等底层操作。锁与AQS很好隔离了使用者与实现者关注的领域。

#### AQS使用

AQS设计基于模板方法模式，使用时需要继承AQS并重写指定的方法，随后将AQS组合在自定义同步组件的实现中。AQS中可重写的方法如下所示：

| 方法名称                      | 描述                                                         |
| ----------------------------- | ------------------------------------------------------------ |
| boolean tryAcquire(int)       | 独占获取同步状态，实现该方法时需要判断当前状态和同步状态是否符合预期，然后对状态进行CAS设置 |
| boolean tryRelease(int)       | 独占释放同步状态，等待获取同步状态的线程将有机会获取同步状态 |
| int tryAcquireShared(int)     | 共享式获取同步状态，返回大于等于0表示成功，反之获取失败      |
| boolean tryReleaseShared(int) | 共享式释放同步状态                                           |
| boolean isHeldExclusively()   | 当前AQS是否在独占模式下被线程占用，一般该方法表示是否被当前线程占用 |

实现自定义同步组件时，将会调用AQS提供的模板方法，如下表所示：

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

#### AQS实现原理

AQS的完成线程同步主要包含：同步队列、独占式同步状态获取和释放、共享时同步状态获取与释放以及超时获取同步状态等AQS的核心数据结构与模板方法。

**【同步队列】**

<font style="color:red"> **AQS依赖内部的FIFO双向同步队列来完成同步状态的管理。当前线程获取同步状态失败时，AQS会将当前线程以及等待状态等信息构造成一个节点并将其加入同步队列，同时会阻塞当前线程，当同步状态释放时，会把首节点中的线程唤醒，使其再次尝试获取同步状态。**</font>

同步队列中的节点用来保存获取同步状态失败的线程引用、等待状态以及前驱和后继节点，节点的属性类型与名称，节点的属性类型与名称以及描述如下所示。

| 属性类型与名称  | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| int waitStatus  | 表示等待节点的状态。                                         |
| Node prev       | 前驱节点，当节点加入同步队列时被设置。                       |
| Node next       | 后继节点。                                                   |
| Node nextWaiter | 等待队列中的后继节点。当节点是共享，子弹是一个SHARED常用，也就是节点类型和等待队列中的后继节点共用同一个字段。 |
| Thread thread   | 获取同步状态的线程。                                         |

AQS的同步队列基本组成结构如下图所示：

![image-20210927205024775](https://gitee.com/tobing/imagebed/raw/master/image-20210927205024775.png)

同步器包含两个特殊节点的引用，一个指向头节点，另一个指向尾节点。

当一个线程成功地获取了同步状态，其他线程将无法获取到同步状态，转而被构造成为节点并加入到同步队列中，而加入同步队列的过程必须保证线程安全。为此AQS提供了过一个基于CAS设置尾节点的方法来保证添加元素是线程安全的。

同步队列遵循FIFO，首节点是获取同步状态成功的节点，首节点的线程在释放同步状态时，将会唤醒后继节点，而后继节点将会在获取同步状态成功时将自己设置为首节点。

设置首节点是通过同步状态成功的线程来完成，由于只有一个线程能够成功获取到同步状态，因此设置头接地的方法不需要使用CAS保证，只需要将首节点设置成为原首节点的后继节点并断开源节点的next引用即可。

**【独占同步状态的获取与释放】**

通过调AQS的acquire方法可以获取同步状态，该方法对中断不敏感。由于线程获取同步状态失败后进入同步队列中，后继对线程进行中断操作时，线程不会从同步队列移出。acquire的执行流程主要有这几个步骤**：完成同步状态的获取、构建节点、加入同步队列以及在同步队列中自旋**。

```java
public final void acquire(int arg) {
    // 调用自定义同步器实现的tryAcquire(int)方法，该方法获取同步状态是线程安全的；
    if (!tryAcquire(arg) &&
        // 如果同步状态获取失败，则构造独占式同步节点，
        // 并通过该addWaiter(Node)方法将节点加入同步队列中，
        // 最后调用acquireQueued方法使得该节点以「死循环」的方式获取同步状态
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) 
        // 如果获取不到阻塞节点中的线程，而被阻塞线程的主要依靠前驱节点出队或阻塞线程被中断来实现
        selfInterrupt();
}
```

> AQS.addWaiter方法

```java
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
```

> AQS.enq方法

```java
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
```

> AQS.acquireQueued方法

```java
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

只允许前驱节点为头节点的节点才能尝试获取同步状态，主要有两个原因：

1. 头节点是成功获取到同步状态的节点，在头节点线程释放了同步状态之后，将会唤醒后继节点，后继节点的线程被唤醒后需要检查自己的前驱释放为头节点；

2. 维护同步队列的FIFO原则。

![image-20210927213027301](https://gitee.com/tobing/imagebed/raw/master/image-20210927213027301.png)

由于首节点线程前驱节点出队或被中断而从等待状态返回，随后检查自己的前驱是否为头结点，如果则尝试获取同步状态。在这个期间，节点与节点之间在循环检查的过程中基本不相互通信，而是简单地判断自己的前驱释放为头节点，这样就使得节点的是否符合FIFO，并且便于对过早通知的处理。

![image-20210927214658747](https://gitee.com/tobing/imagebed/raw/master/image-20210927214658747.png)

当前线程获取同步状态并执行相应逻辑之后，就需要释放同步状态，使得后继节点能够继续获取同步状态。通过调用AQS的release(int)方法可以释放同步状态，该方法在释放了同步状态之后，会唤醒其后继节点。

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {  // 先调用tryRelease操作释放资源
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h); // 然后调用LockSupport.unpark激活AQS队列中的被阻塞的一个线程。
        return true;
    }
    return false;
}
```

方法执行时，会唤醒头节点的后继节点线程，unparkSuccessor方法使用LockSupport来唤醒处于等待状态的线程。

**【共享式同步状态的获取与释放】**

共享式与独占式获取最重要的区别在于同一时刻能否有多个线程同时获取同步状态。

通过AQS的acquireShared(int)方法可以共享获取同步状态，代码如下：

```java
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);           // 成功则返回，失败则将当前线程封装为Node.SHARED的Node节点插入到AQS阻塞队列尾部，并使用LockSupport.park挂起
}
```

> AQS.doAcquireShared

```java
private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);   // 创建Node.SHARED节点，将其放到AQS尾部
    boolean failed = true;
    try {
        boolean interrupted = false;
        // 循环自旋获取同步状态，尝试获取同步状态，如果返回值大于等于0，表示这次获取同步状态成功并从自旋过程中退出
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
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

共享式释放资源通过调用releaseShared进行释放。

```java
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        doReleaseShared();  // 从AQS阻塞队列中挑选一个线程，调用Support.unpark使其唤醒
        return true;
    }
    return false;   // state已经为0，直接返回false
}
```

该方法在释放同步状态之后，将会唤醒处于等待状态的节点。对于能够支持多个线程同时访问的并发组件，与独占式的区别在于tryReleaseShared方法必须保证线程安全，一般通过循环和CAS保证。因为释放同步状态的操作会同时来自多个线程。

#### AQS实战

【使用AQS实现独占锁】

【使用AQS实现TwinsLock】

### 可重入锁

可重入锁，顾名思义就是支持重进入的锁，表示该锁能够支持一个线程对资源的重复加锁。synchronized关键字隐式支持重进入，因此我们可以递归调用synchronized修饰的方法。在方法执行时，执行线程在获取了锁之后仍能连续多次获取锁。

ReentrantLock虽然没有synchronized一样支持隐式的可重入，但是调用lock方法是，已经后的锁的线程能够再次调用lock方法获取锁而不被阻塞。

除此之外ReentrantLock还引入了公平锁和非公平锁的概念。如果在绝对时间内，先对线程进行获取的请一定先被满足，那么整个锁就是公平的；反之是不公平的。公平获取锁即等待时间最长的线程最优先获取锁，即锁获取是顺序的。

事实上，公平的锁机制往往没有非公平的效率高，但是，并不是任何场景都是以TPS作为唯一指标，公平锁能够减少「饥饿」发生的概念。

#### 可重入的实现

可重入值任意线程在获取锁之后能够再次获取该锁而不会被锁阻塞，该特性需要解决以下两个问题：

1. **线程再次获得锁**：锁需要识别获取锁的线程是否为当前占据的线程，虽然是则成功获取。
2. **锁的最终释放**：线程重复n次加锁，随后在第n次释放锁之后，其他线程能够获取该锁。锁的最终释放要求锁对于获取进行技术自增，计数表示当前锁重复获取的次数，而锁被释放时，计数自检，当前计数等于0时表示锁已经成功释放。

ReentrantLock通过内部组合自定义AQS来实现锁的获取与释放。

【非公平锁的实现】

```java
final boolean nonfairTryAcquire(int acquires) {     // 非公平锁尝试获取锁
    final Thread current = Thread.currentThread();  // 获取当前线程
    int c = getState();     // 获取 state
    if (c == 0) {           // 当前lock为空闲状态【state=0】
        if (compareAndSetState(0, acquires)) {  // CAS尝试获取锁【将state从0设置为1】
            setExclusiveOwnerThread(current);           // 获取独占拥有锁的线程为当前线程
            return true;                                // 设置成功返回true
        }
    }
    else if (current == getExclusiveOwnerThread()) {    // 当前lock不空闲，但是持有者是当前线程
        int nextc = c + acquires;                       // 直接更新state的状态
        if (nextc < 0) // overflow                      // 判断可重入次数是否已经溢出
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;               // 否则返回false
}

```

该方法添加了再次获取同步状态的处理逻辑：

+ 通过判断当前线程是否为获取锁的线程来决定获取操作释放成功；
+ 如果是获取锁的线程再次请求，则将同步状态的值增加并返回true，表示获取同步状态成功；

成功获取锁定的线程再次获取锁，只是增加同步状态值，即要求ReentrantLock在释放同步状态时减少同步状态值。

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

【公平获取锁】

公平锁要求锁的获取顺序应该符合请求的绝对时间顺序，即FIFO。

```java
protected final boolean tryAcquire(int acquires) {      // 尝试公平地去获取锁
    final Thread current = Thread.currentThread();      // 获取当前线程
    int c = getState();                                 // 获取AQS中的state状态
    if (c == 0) {                                       // 执行未上锁的逻辑
        if (!hasQueuedPredecessors() &&                     // 查询是否有任何线程在等待获取比当前线程更长的时间。
            compareAndSetState(0, acquires)) {      // 首次获得锁，CAS方式将state状态更新为1
            setExclusiveOwnerThread(current);           // 设置当前拥有独占访问权的线程。
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {    // 如果当前线程与获得锁的线程是同一个线程，则更新state（state表示可重入的次数）
        int nextc = c + acquires;
        if (nextc < 0)                                  // 可重入层级为int上限，超出则抛出异常
            throw new Error("Maximum lock count exceeded");
        setState(nextc);                                // 更新state状态
        return true;
    }
    return false;
}
```

与nonfairTryAcquire比较，唯一不同的位置为判断条件多了hasQueuedPredecessors方法，即加入同步队列中当前节点是否有前驱节点的判断，如果该方法返回true表示有线程比当前线程更早请求获取锁，因此获取等待前驱线程获取并释放锁之后才能继续获取锁。

### 读写锁

前面提到的都是排他锁，同一时刻只允许一个线程进行访问，而读写锁在同一时刻可以允许多个读线程访问，在写线程访问时，所有的读线程和其他写线程均被阻塞。读写锁维护了一对锁，一个读锁和写锁，通过分离读锁和写锁，使得并发性相比一般的排他锁有很大的提升。

除了保证写操作和读操作的可见性以及并发性的提升，读写锁能够简化读写交互场景的编程方式。如对于程序中定义一个共享的用作缓存数据结构，大部分时间提供读服务，而写操作占用很少时间，但是写操作完成需要对后继的读操作可见。

在没有读写锁之前，需要使用synchronized保证更新之后的数据能够被后继的读操作可见。引入了读写锁，仅需要在写操作是获取写锁，读操作是获取读锁即可，相当简单且提供了性能。juc提供了ReentrantReadWriteLock来实现读写锁，主要有以下特性：

| 特性       | 说明                                                         |
| ---------- | ------------------------------------------------------------ |
| 公平性选择 | 支持非公平和公平的锁获取方式，非公平吞吐量由于公平           |
| 重进入     | 支持重进入                                                   |
| 锁降级     | 循环获取写锁、获取读锁再释放写锁的次序，写锁能够降级成为读锁 |

#### 读写锁接口

ReentrantReadWriteLock提供了一些便于外界监控其内部工作状态的方法。

| 方法名称                | 描述                         |
| ----------------------- | ---------------------------- |
| int getReadLockCount()  | 返回当前读锁被获取的次数。   |
| int getReadHoldCount()  | 返回当前线程获取读锁的次数。 |
| boolean isWriteLocked() | 判断写锁是否被获取           |
| int getWriteHoldCount() | 返回当前写锁被获取的次数     |

#### 读写锁的实现

**【读写状态的设计】**

读写锁需要依赖AQS实现同步功能，而读写状态就是AQS的同步状态。

读写锁自定义同步器需要在同步状态上维护读线程和一个写线程的状态，使得该状态的设计成为读写锁实现的关键。如果在一个整形变量上维护多种状态，需要「按位切割使用」这个变量。读写锁将变量切分为两部分：高16位表示读、低16位表示写。

读写锁通过位运算迅速确定读和写各自的状态。假设状态值为S，写状态等于 S&0000FFFF，读状态等于 S>>>16。当写状态增加1，等于S+1，当读状态增加1，等于S+(1<<16)。

**【写锁的获取与释放】**

写锁是有个支持可重入的的排它锁。如果当前线程已经获得写锁，则增加写锁状态，如果其他线程需要获取写锁将进入等待状态。除此了重入条件之外，还增加了一个读锁是否存在的判断，如果存在读锁，则写锁不能被获取。写锁的释放过程与ReentrantLock类似，每次释放均减少写作态，当为0时表示写锁已经释放，从而等待读写线程能够继续访问读写锁，同时前次写线程的修改对后继读写线程可见。

```java
 protected final boolean tryAcquire(int acquires) {
     /*
             * Walkthrough:
             * 1. If read count nonzero or write count nonzero
             *    and owner is a different thread, fail.
             * 2. If count would saturate, fail. (This can only
             *    happen if count is already nonzero.)
             * 3. Otherwise, this thread is eligible for lock if
             *    it is either a reentrant acquire or
             *    queue policy allows it. If so, update state
             *    and set owner.
             */
     Thread current = Thread.currentThread();    // 获取当前线程
     int c = getState();                         // 获取state
     int w = exclusiveCount(c);                  // 获取排他锁的可重入次数
     if (c != 0) {       // 当前锁（写锁）已经被某个线程持有（持有写锁的时候无读锁）
         // (Note: if c != 0 and w == 0 then shared count != 0)
         if (w == 0 || current != getExclusiveOwnerThread()) // 持有锁线程不是当前线程
             return false;
         if (w + exclusiveCount(acquires) > MAX_COUNT)       // 持有锁线程为当前线程，且可重入次数不溢出
             throw new Error("Maximum lock count exceeded");
         // Reentrant acquire
         setState(c + acquires);     // 更新state（可重入次数）
         return true;
     } // 当前锁（写锁）是空闲的，没被占用
     if (writerShouldBlock() || // 判断是否存在读锁【对于非公平，直接返回false，即直接尝试CAS获取锁】【对于公平锁，先判断AQS阻塞队列中是否存在前驱节点，有则放弃获取血锁的权限，直接返回false】
         !compareAndSetState(c, c + acquires))
         return false;           // CAS更新写锁的状态失败，直接返回false
     setExclusiveOwnerThread(current);   // 成功，则直接设置当锁的持有线程
     return true;
 }
```

**【读锁的获取与释放】**

读锁是可重入的共享锁，能够被多个线程同时获取，在没有其他写线程访问时，读锁总会成功地获取，只需要增加读状态。如果线程以及获得了读锁，则增加读状态。如果当前线程获取读锁的时候，写锁以及被其他线程获取，则进入等待状态。

从Java5 到 Java 6 ，获取读锁的实现变得复杂，因为需要维护「当前线程获取读锁的次数」getReadHoldCount()。读状态是所有线程获取读锁次数的综综合，而每个线程各自获取读锁的次数只能选择保存在ThreadLocal中，由线程自身维护，使得读锁的实现变得复杂。

```java
protected final int tryAcquireShared(int ununsed) {
    for (;;) {
        int c = getState();
        int nextc = c + (1<<16);
        if (nextc < c) 
            throw new Error("Maximum lock count exceeded");

        if (exclusiveCount(c) !=0 &&  		// 如果其他线程已经获取了写锁，则当前线程获取读锁失败，进入等待状态
            owner != Thread.currentThread()) 
            return -1;
        if (compareAndSetState(c, nextc))	// 如果当前线程获取了写锁或写锁未被获取，则当前线程增加读状态（CAS）
            return 1;
    }
}
```

**【锁降级】**

锁降级指的是，持有写锁的线程，再获取读锁，随后释放原来的写锁的过程。

### LockSupport

当需要阻塞或唤醒一个线程时，会使用LockSupport工具类完成响应工作。LockSupport是关键同步组件的基础工具，定义了一种公共静态方法，提供了最基本的线程阻塞和唤醒功能。

LockSupport定义了一组以park开头的方法用于阻塞当前线程，以及unpark(Thread)方法来唤醒一个被阻塞的线程。

| 方法名称             | 描述                                                    |
| -------------------- | ------------------------------------------------------- |
| void park()          | 阻塞当前线程，调用unpark方法或当前线程被中断时返回      |
| void parkNanos(long) | 阻塞当前线程，在park方法基础上，添加了nanos纳秒超时返回 |
| void parkUnitl(long) | 阻塞当前线程，直到deadline时间                          |
| void unpark(Thread)  | 唤醒处于阻塞状态的线程                                  |

在Java 6，还添加了park(Object)/parkNanos(Object, long)/parkUntil(Object, long)3个方法，用于实现阻塞当前线程的功能，其中第一个Object参数用于表示当前线程在等待的对象。该对象主要用于问题排除和性能监控。

Java 5之前，当线程阻塞(使用synchronized关键字)在一个对象上时，通过线程dump能够查看该线程的阻塞对象，方便定位方法。而Java 5推出的Lock等并发功能却遗漏了这一点，使得dump无法提供阻塞对象的信息，因此Java6中添加了这个3个方法。

### Condition接口

任意一个Java对象到拥有一组监视器方法，主要包括了wait()/wait(long)/notify()/notifyAll()方法。这些方法与synchronized同步关键字配合，可以实现等待/通知模式。Condition接口也提供了类似于Object监视器方法，与Lock配合可以实现等待/通知模式，但是两者在使用方式和功能特性上有些差别。

| 对比项                                     | Object Monitor Methods    | Condition                                      |
| ------------------------------------------ | ------------------------- | ---------------------------------------------- |
| 前置条件                                   | 获取对象锁                | Lock.lock获取锁，newCondition获取Condition对象 |
| 调用方式                                   | 直接调用，如object.wait() | 直接调用，如condition.await()                  |
| **等待队列个数**                           | 一个                      | 多个                                           |
| 当前线程释放锁并进入等待状态               | 支持                      | 支持                                           |
| **在等待状态不响应中断**                   | 不支持                    | 支持                                           |
| 当前线程释放锁并进入超时等待状态           | 支持                      | 支持                                           |
| **当前线程释放锁进入等待直到将来某个时间** | 不支持                    | 支持                                           |
| 唤醒等待队列中的一个线程                   | 支持                      | 支持                                           |
| 唤醒等待队列中的多个线程                   | 支持                      | 支持                                           |

#### Condition API

Condition定义了等待/通知两种类型的方法，当线程调用这些方法是，需要提前获取Condition对象关联的锁。Condition定义的部分方法如下。

| 方法名称                    | 描述                                                         |
| --------------------------- | ------------------------------------------------------------ |
| void await()                | 当前线程进入等待状态直到被signal或中断                       |
| void awaitUninterruptibly() | 当前线程进入等待状态直到被signal，对中断不敏感               |
| long awaitNanos(long)       | 当前线程进入等待状态直到被signal，中断或超时，返回值表示剩余时间 |
| boolean awaitUntil(Date)    | 当前线程进入等待状态直到被signla，中断或到某个时间，没有到指定时间返回时返回true |
| void signal()               | 唤醒一个等待在Condition上的线程，需要获得Condition相关联的锁 |
| void signalAll()            | 唤醒所有等待在Condition上的线程，需要获得Condition相关联的锁 |

#### Condition实现

每个Condition对象都包含了一个队列（等待队列），该队列是Condition对象实现等待/通知功能的关键。

**【等待队列】**

等待队列是一个FIFO对队列，在队列中每个节点都包含了一个线程引用，该线程就是在Condition对象上等待的线程。如果一个线程调用了Condition.await()方法，则该线程会释放锁、过程节点加入等待队列并进入等待状态。（注意：节点的定义复用了AQS.Node）

Condition拥有等待队列首尾节点的引用，新增节点只需要将原有节点的尾节点nextWait指向它即可。由于调用了await方法的线程必定获取了锁的线程，因此添加节点已经通过锁来保证线程安全。

![image-20210928104422986](https://gitee.com/tobing/imagebed/raw/master/image-20210928104422986.png)

Condition是AQS的内部类，每个Condition实例能够访问AQS提供的方法。

**【等待】**

调用Condition的await()方法，会使得当前线程进入等待队列并释放锁，同时线程状态变为等待状态。当从await()方法返回，当前线程一定获取了Condition相关联的锁。

```java
public final void await() throws InterruptedException { // 调用await方法
    if (Thread.interrupted())
        throw new InterruptedException();
    Node node = addConditionWaiter();       // 创建新的node节点，并插入到添加到【条件队列】末尾
    int savedState = fullyRelease(node);    // 释放当前线程获取的锁
    int interruptMode = 0;
    while (!isOnSyncQueue(node)) {
        LockSupport.park(this);     // 调用park方法阻塞挂起当前线程
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null) // clean up if cancelled
        unlinkCancelledWaiters();
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
}
```

成功获取锁的线程即同步队列中首节点才可以调用该方法，该方法会将当前线程构建成节点并加入等待队列中，然后释放同步状态，唤醒同步队列中后继节点，然后线程会进入等待状态。

当等待队列中的节点别唤醒，则该节点的线程开始尝试获取同步状态。如果不是通过其他线程调用Condition.signal的方式唤醒，而是对等待线程的中断，则抛出InterruptedException。

**【通知】**

调用Condition的signal方法，将会唤醒在等待队列中等待时间最长的节点，在唤醒节点前，会将节点移到同步队列中。

调用了signal方法的前提是当前线程获得了锁，方法内部会对其进行检查。紧接着方法内部将获取等待队列中的首节点，将其移动到同步队列并使用LockSupport唤醒节点中的线程。

通过调用AQS.enq方法，等待队列中的头节点安全地移动到同步队列中。当节点移动到同步队列，当前线程使用LockSupport唤醒该节点的线程。

被唤醒之后的线程，将从await方法中的while循环返回，进而调用同步方法的acquireQueued方法加入到获取同步队列的竞争中。

![condition_signal_await](https://gitee.com/tobing/imagebed/raw/master/condition_signal_await.png)

## Java并发容器和框架

### ConcurrentHashMap

ConcurrentHashMap是线程安全且高效的HashMap。

#### 为什么使用CHM

并发编程中使用HashMap可能导致程序死循环。而使用线程安全的HashTable效率很低，于是ConcurrentHashMap应运而生。

在多线程环境下，使用HashMap的put操作会引起死循环，导致CPU利用率接近100%，因此在并发情况下不能使用HashMap。HashMap在并发执行put操作时会引起死循环，是因为多线程会导致HashMap的Entry链表形成环形数据结构，一旦形成换性数据结构，Entry的next节点将永不为空，就会产生死循环的获取Entry。

HashTable容器使用synchronized保证线程安全，在线程激烈竞争的情况下，HashTable效率低下。当一个线程访问HashTable的同步方法 ，其他线程想要访问同步方法时，将会进入阻塞或轮询状态。

HashTable容器在竞争激烈的并发环境下效率低效的原因是所有访问HashTable的线程都必须竞争同一把锁。ConcurrentHashMap提出，在容器中存放多把锁，每把锁用于锁容器中的一部分数据，多线程访问容器中不同数据，线程间不存在锁竞争，可以有效地提高并发访问的效率，这便是「锁分段技术」。

锁分段技术将数据分成一段一段地储存，然后给每一段数据配一般锁，当一个线程占用锁访问其他一端数据时，其他端的数据也能被其他线程访问。

CHM结构

ConcurrentHashMap主要有Segment数据结构和HashEntry数组组成。

+ Segment是一种可重入锁，在CHM中扮演重要角色；
+ HashEntry用于储存键值对数据。

一个ConcurrentHashMap中包含了一个Segment数组，Segment的结构和HashMap类似，是一种数组和链表结构。一个Segment里包含了一个HashEntry数组，每个HashEntry是一个链表结构元素。

#### CHM初始化

ConcurrentHashMap初始化方法通过initialCapacity、loadFactor和concurrencyLevel来实现初始化segment数组、段偏移量segmentShift、段掩码segmentMask和每个segment中HashEntry数组来实现。

**【初始化segments数组】**

```java
if (concurrencyLevel > MAX_SEGMENTS)
    concurrencyLevel = MAX_SGEMENTS;
int sshift = 0;
int size = 1;
while (sszie < concurrencyLevel) {
    ++sshift;
    ssize <<=1;
}
sgementShift = 32 - sshift;
sgementMask = ssize - 1;
this.sgements = Sgement.newArray(ssize);
```

+ segments数组长度ssize通过cuncurrencyLevel计算得出；
+ segments的长度保证为2的N次方，因此可以通过按位与的散列函数来定位segments数组的索引；
+ segmentShift用于定位参与散列元素的位数，等于32-sshift；
+ segmentMask是散列运算的掩码，等于ssize-1，掩码的各个二进制位都是1；

**【初始化每个segment】**

```java
if (initialCapacity > MAXIMUM_CAPACITY) 
    initialCapacity = MAXIMUM_CAPACITY;
int c = MAXIMUM_CAPACITY / ssize;
if (c * ssize < MAXIMUM_CAPACITY) 
    ++c;
int cap = 1;
while (cap < 1) 
    cap <<=1;
for (int i=0; i < this.segments.length; ++i)
    this.segments[i] = new Segment<K,V>(cap, loadFactor);
```

+ 输入参数initialCapacity是ConcurrentHashMap的初始化容量；
+ loadfactor是每个segment的负载因子；
+ 在构造函数中需要通过上述两个参数初始化数组中的每个Segment；
+ cap即segment中HashEntry数组的长度，等于initialCapacity除以ssize的倍数c；
+ segment的容量 threshold = (int)cap*loadFactor；
+ 默认情况下initialCapacity=16，loadFactor=0.75，cap=1，threshold=0；

#### 初始化Segment

CHM使用分段锁包含不同段数据，在插入数据时通过Wang/Jenkins hash散列算法将元素hashCode再散列，从而定位到Segment。通过再散列，可以减少散列冲突，是元素能够均匀地分布在不同的Segment上，从而提高容器的储存效率。

#### CHM的操作

**【get操作】**

Segment的get操作简单高效。先经过一次再散列，然后使用这个散列值通过散列运算定位到Segment。

get操作的高效之处在于**不用加锁**。除非读取到的值是空才会加锁重读。无需较少的原因在于，get方法将要使用的共享变量定义为volatile类型，可以保证这些值在线程间的可见性，能够被多线程同时读，并且保证不会读到过期的值，但只能是单线程写。当写入的值不依赖于原值，可以被多线程写。

**【put操作】**

由于put方法需要对共享变量进行写入操作，为了保证线程安全，操作共享变量时必须加锁。put方法首先定位Segment，然后在Segment中进行插入操作。插入操作需要经历了两个步骤：

1. 判断是否需要对Segment中的HashEntry数组进行扩容；
2. 定位添加元素的位置，然后将其放在HashEntry数组中。

① 确认是否需要扩容

插入元素之前，先判断Segment的HashEntry数组是否超过阈值，如果超过阈值则对数组进行扩容。

② 如何扩容

扩容时，实现创建一个容量为原来容量两倍的数组，然后将原数组中的元素进行再散列后插入新的数组中。为了高效，CHM不会对整个容器扩容，而只对某个segment进行扩容。

**【size操作】**

统计整个CHM的大小必须要统计每个Segment中元素大小后进行求和。Segment的全局变量count是有个volatile变量。多线程场景下，不能单纯将每个Segment的size元素进行相加，因为在累加时使用的count在累加过程中可以会发生改变，这是统计结果就不准确。

考虑到累加过的count发生变化的概率很小，CHM做法是两次通过不锁住Segment方式统计各个Segment的大小，通过统计过程中容器count发生了变化，则再采用加锁的方式统计segment的大小。

CHM通过该在put、remove和clean方法操作元素前维护modCount的返回，通过比价统计size前后该变量是否发送改变来判断容器大小是否发送变化。

### ConcurrentLinkedQueue

实现安全队列主要有两种方式：一种是使用阻塞算法，另外一种是使用非阻塞算法。

+ 阻塞算法：使用锁来实现；
+ 非阻塞算法：通过使用循环CAS方式来实现；

Java中的ConcurrentLinkedQueue是一个基于链接节点的无界线程安全队列，采用先进先出的规则对节点进行排序。当添加一个元素时，会添加到队尾；当获取一个元素时，会返回队列头部的元素。采用了「wait-free」算法来实现，该算法在「Michael & Scott算法」基础上进行了一些修改。

#### CLQ的结构

CLQ有head和tail节点组成，每个Node有节点元素和指向下一个节点的引用组成，节点与节点之间就是通过next关联起来，从而组成一张链表结构的队列。默认情况下head节点储存的元素为空，tail节点等于head节点。

#### 入队列

**【入队列的过程】**

入队列就是将入队节点添加到队列的尾部。入队主要包含了两个事情：定位尾节点、使用CAS将入队节点设置成尾节点的next节点，如果不成功则重试。

1. 将入队节点设置成当前队列尾节点的下一个节点；
2. 更新tail节点，如果tail节点的next不为空，则将入队节点设置为tail节点；
3. 如果tail节点next节点不为空，则将入队节点设置成tail节点；
4. 如果tail节点next节点为空，则将入队节点试着成tail的next节点；

在多线程同时运行时，必须获取尾节点，然后设置尾节点的下一个节点为入队节点。但此时有可能另外一个线程插队，此时队列的尾节点可能会发生变化，这是当前线程暂停入队操作，然后重新获取尾节点。

```java
public boolean offer (E e) {
	if (e == null) throw new NullPointerException();
    Node<E> n = new Node<E>(e);			// 入队前创建一个入队节点
    retry:				
    for(;;) {							// 死循环，入地不成功反复入队
        Node<E> t= tail;				// 创建一个指向tail节点的引用
        Node<E> p = t;					// p用来表示队列的尾节点，默认情况下等于tail
        for (int hops = 0;;hops++) {	// 获取p节点的下一个节点
            Node<E> next = succ(p);
            if (next != null) {			// 如果next为null，说明p不是尾节点，需要更新p后再将它执行next节点
                if (hops > HOPS && t != tail)	// 循环两次及其以上，并且当前节点还是不等于尾节点
                    continue retry;
                p = next;
            } else if (p.casNext(null, n)) {	// 如果p是尾节点，则设置p节点next节点为入队节点
                if (hops >= HOPS ) casTail(t,n);// 更新tail节点，允许失败
                return true;
            } else {					// p有next节点，表示p的next节点是尾节点，则重新设置p节点
                p = succ(p);
            }
        }
    }
}
```

**【定位尾节点】**

**可以发现tail节点并不总是尾节点，因此每次入队都必须先通过tail节点来找到尾节点。**尾节点可能是tail节点，有可能tail节点next节点。

```java
final Node<E> succ(Node<E> p) {
    Node<E> next = p.getNext();
    return (p == next) ? head : next;
}
```

**【设置入队元素为尾节点】**

p.casNext(null, n)方法用于将入队节点设置为当前队列尾节点的next节点，如果p为null，表示p是当前队列的尾节点，如果不是null，表示有其他线程更新了尾节点，则需要重新获取当前队列的尾节点。

**【HOPS的设计意图】**

使用HOPS可以减少和控制CAS更新tail节点的次数，就能提高入队的效率。因此并不是每次节点入队后都讲tail节点更新为尾节点，而tail节点和尾节点的距离等于等于常量HOPS的值是才更新tail节点，tail和尾节点的距离越长，使用CAS更新tail节点的次数就会越少，但距离越长带来的负面效果就是每次入队时定位尾节点的时间就越长。

#### 出队列

出队列即从队列中返回一个节点元素，并清空该节点对元素的引用。

与入队类似，出队是但并不是每次都更新head节点，当head节点中有元素时，直接弹出head节点的元素而不会更新head节点。只有当head节点里没有元素是，出队操作才会更新head节点。这种通过hops变量来减少使用CAS更新head节点的消耗，从而提高出队效率。

### 阻塞队列

#### 何为阻塞队列

阻塞队列是一种支持两个附加操作的队列。这两个附加操作用于阻塞插入和移除方法。

+ 当队列满时，队列会阻塞插入元素的线程，直到队列有空闲位置；
+ 当队列为空，队列会阻塞获取元素的线程，直到队列中有元素；

阻塞队列常用于生产者和消费者的场景，生产者向队列中添加元素，消费者从队列中获取元素。阻塞队列是用来生产者存放元素、消费者获取元素的容器。

当阻塞队列不可用时，两个附加操作提供了4种处理方式：

| 方法/处理方式 | 抛出异常 | 返回特殊值 | 一直阻塞 | 超时退出 |
| ------------- | -------- | ---------- | -------- | -------- |
| 插入方法      | add      | offer      | put      | offer    |
| 移除方法      | remove   | poll       | take     | poll     |
| 检查方法      | element  | peek       | 不可用   | 不可用   |

+ 抛出异常：当队列满，如果再往队列中插入元素，抛出IllegalStateException；当队列空，从队列获取元素会抛出NoSuchElementException；
+ 返回特殊值：当往队列中插入元素，会返回是否插入成功；如果是移除方法，则从队列中取出一个元素，如果没有则返回null；
+ 一直阻塞：当阻塞队列满，如果生产者线程队列put，队列阻塞生产者线程，直到队列可用或响应中断。当队列空，如果消费者线程会从队列take元素，队列会阻塞消费者线程，直到队列不为空；
+ 超时返回：当阻塞队列满，如果生产者线程往队列中插入元素，队列会阻塞生产者线程一段时间，如果超过指定时间，生产者线程推出；

巧记：put和take方法分别带有t，offer和poll分别带有o。

#### Java中的阻塞队列

JDK 7提供了个阻塞队列，如下：

+ ArrayBlockingQueue：数组构成的有界阻塞队列
+ LinkedBlockingQueue：链表构成的有界组队队列
+ PriorityBlockingQueue：支持优先级排序的无界阻塞队列
+ DelayQueue：支持延时获取元素的无界阻塞队列
+ SynchronousQueue：不储存元素的阻塞队列
+ LikedTransferQueue：链表构成的无界阻塞队列
+ LinkedBlockingDeque：链表过程的无界双向阻塞队列

**【ArrayBlockingQueue】**

默认情况下不保证线程公平访问。当队列可用时，阻塞的线程都可以争夺访问队列的资格，有可能先阻塞的线程最后才访问队列。为了保证公平性，通常会降低吞吐量。

**【LinkedBlockingQueue】**

队列的默认和最大长度是Integer.MAX_VALUE。现在FIFO的原则对元素进行排序。

**【PriorityBlockingQueue】**

默认情况下元素采用自然顺序升序排序。也可以自定义compareTo方法来指定元素排序顺序，或在初始化时指定构造函数的Comparator。

**【DelayQueue】**

使用PriorityQueue实现，队列中的元素必须实现Delayed接口，创建元素时可以指定多久才能从队列中获取当前元素。只有在延迟期满时才能从队列中提取元素。DelayQueue使用场景丰富，常用于：

+ **缓存系统的设计**：用DelayQueue保存元素有效期，使用一个线程循环查询DelayQueue，一旦能从DelayQueue中取出元素，表明缓存有效期到了；
+ **定时任务的调度**：用DelayQueue保存当天将会执行的任务和执行时间，一旦从DelayQueue中获取到任务就开始执行；

**【SynchronousQueue】**

SynchronousQueue不储存元素，每个put操作必须等待一个take操作，否则不能继续添加元素。它支持公平访问度列，默认情况下线程采用非公平策略访问队列。

SynchronousQueue可以看做传球手，复制把生产者线程处理的数据直接传递给消费者线程。队列本身不储存元素，非常适合传递性场景。SynchronousQueue吞吐量高于ArrayBlockingQueue和LinkedBlockingQueue。

**【LikedTransferQueue】**

与其他阻塞队列相比，多了tryTransfer和transfer方法。

+ transfer：如果消费者在等待接收元素，可以把生产者传入的元素立即传输到消费者；如果没有消费者等待，可以把元素存放在队列tail中，并等到该元素被消费才返回。
+ tryTransfer：用于试探生产者传入的元素是否能够直接传给消费者。如果没有消费者等待接收元素，则返回false。
+ tryTransfer方法无论消费者是否接收方法立即返回，而transfer方法必须等待消费者消费才返回。

**【LinkedBlockingDeque】**

LinkedBlockingDeque是一个双向队列，可以从队列的两端插入和移出元素。双向队列以为独立一个操作队列的入口，在多线程同时入队时减少了一般的竞争。

#### 阻塞队列实现原理

队列为空时消费者会一直等待，生产者添加元素时，会通过通知模式来通知消费者进行消费。

```java
private final Condition notFull;
private final COndition notEmpty;

public ArrayBlockingQueue (int capacity, boolean fair) {
    notFull = lock.newCondition();
    notEmpty = locknewCondition();
}

public void put(E e) throws InterruptedException {
    checkNotNull(e);
    final ReetrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == items.length)
            notFull.await();
        insert(e);
    } finally {
        lock.unlock();
    }
}

public E take() throws InterruptedException {
    final ReetrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == 0) 
            notEmpty.await();
        return  extract();
    } finally {
        lock.unlock();
    }
}

private void insert (E x) {
    items[putIndex] = x;
    putIndex = inc[putIndex];
    ++count;
    notEmpty.signal();
}
```

### Fork/Join框架

Fork/Join框架是Java 7提供用于并行执行任务的框架，是把一个大任务分割成若干小任务，最终汇总每个小任务结果后得到大任务结果的框架。

#### 工作窃取算法

工作窃取算法指某个线程从其他队列中窃取任务来执行。可以将一个比较大的任务，分割为若干互不依赖的子任务，为了减少这些线程间的竞争，把这些子任务放到不同队列中，并为每个队列创建单独的线程来执行队列的任务，线程和队列一一对应。

如A线程负责处理A队列中的任务，但有的线程会把自己队的任务干完，而其他线程对应的度列还有任务等待处理。这些干完的线程会去其他线程的队列中窃取一个任务来执行。这是可能存在多个线程访问同一个队列，为了减少窃取任务线程和被窃取任务线程之间的竞争，通常使用双端队列，被窃取任务线程永远从双端队列的头部取任务执行，而窃取任务的线程永远从双端队列的尾部那任务执行。

工作窃取算法的优点是重复利用线程进行并行计算，减少线程间的竞争；

工作窃取算法的缺点是在某些情况下存在竞争，如果双端队列只有一个任务，算法会消耗更多的系统资源，如常见多个线程和多个双端队列。

#### Fork/Join框架的设计

Fork/Join的设计流程主要如下：

1. 分割任务：通过fork类将大任务分割成子任务，有可能分割的子任务仍旧很大，因此需要不但分割，知道分割的子任务足够小；
2. 执行任务并合并结果：分割的子任务分别存放在双端队列，然后启动几个县城分别从双端队列中获取任务执行。子任务执行完结果统一放在一个队列中，然后启动一个线程从队列获取苏杰，然后合并这些数据；

Fork/Join使用两个类完成上述事情：

1. ForkJoinTask：使用Fork/Join框架，必须先创建一个ForkJoin任务。提供在任务中执行fork和join操作的机制。通常继承其子类。
   1. RecursiveAction：用于没有返回结果的任务
   2. RecursiveTask：用于有返回结果的任务
2. ForkJoinPool：ForkJoinTask需要通过ForkJoinPool执行

任务分割的子任务会添加到当前工作线程维护的双端队列，进入队列头部。当一个工作线程的队列暂时没有任务时，会随机从其它工作线程的队列的尾部获取一个任务。

#### Fork/Join框架的异常处理

Fork/Join执行时可能会抛出异常，但没办法在主线程中直接捕获异常，因此ForkJoinTask提供了isCompleteAbnormally方法来检查任务是否已经抛出异常或已经被取消，并且可以通过ForkJoinTask的getException方法获取异常。

#### Fork/Join框架实现原理

ForkJoinPool有ForkJoinTask数组和ForkJoinWorkerThread数组组成，ForkJoinTask数组负责存放程序提交给ForkJoinPool的任务，而ForkJoinWorkerThread负责执行这些任务。

**【ForkJoinTask的fork方法】**

调用ForkJoinTask的fork方法，程序会调用ForkJoinWorkerThread的pushTask方法异步执行任务，然后立即返回结果。push方法把当前任务存放在ForkJoinTask数组队列里。然后调用ForkJoinPool的signalWork()方法唤醒或创建一个工作线程来执行任务。

```java
public final ForkJoinTask<V> fork() {
    ((ForkJoinWorkerThread) Thread.currentThread()).pushTask(this);
    return this;
}
```

**【ForkJoinTask的join方法】**

Join方法主要阻塞当前线程并等待获取结果。首先它调用了doJoin方法，通过doJoin方法得到当前任务的状态来判断返回什么结果，任务的状态有4中：已完成（NORMAL）、被取消（CANCELLED）、信号（SIGNAL）和出现异常（EXCEPTIONAL）。

+ 如果任务状态是已完成，则直接返回任务结果；
+ 如果任务状态是被取消，则直接抛出CancellationException；
+ 如果任务状态是抛出异常，则直接抛出对应的异常；

```java
public final V join() {
    if (doJoin() != NORMAL)
        return reportResult();
    else 
        return getRawResult();
}

private V reportResult() {
    int s;
    Throwable ex;
    if ((s = status) == CANCELLED) 
        throw new CancellationException();
    if (s == EXCEPTIONAL && (ex = getThrowableException()) != null)
        UNSAFE.throwException(ex);
    return getRawResult;
}
```

## 原子操作类

多线程同时更新一个变量，如果操作变量的操作不是原子的，不同线程可能会基于相同的变量版本进行操作，可能会得到一个期望之外的值，这时由于线程不安全的更新操作导致的。通常可以使用synchronized来解决这个问题，会保证多线程不会同时更新变量。

JDK1.5开始提供了java.util.concurrent.atomic包，这个包的原子操作提供了一个简单、性能高效、线程安全地更新一个变量的方式。Atomic包一共提供了13个类，属于4种类型的原子更新方式，分别是原子更新基本类型、原子更新数组、原子更新引用和原子更新属性。Atomic包的类基本使用Unsafe实现的包装类。

### 原子更新基本数据类型

Atomic包提供了3个原子更新基本数据类型的类：

+ AtomicBoolean：原子更新布尔类型
+ AtomicInteger：原子更新整形类型
+ AtomicLong：原子更新长整型

这些了类提供的方法大同小异，以AtomicInteger为例，常用的方法有：

+ int addAndGet(int)：原子方式将输入数据与实例数据相加，并返回结果；
+ boolean compareAndSet(int, int)：如果输入的数值等于预期值，则以原子方式将值设置为输入值；
+ int getAndIncrement()：以原子方式将当前值加1，返回自增前的值；
+ void lazySet(int)：最终设置为新值，使用lazySet设置值后，可能导致其他线程在之后一点时间内还是可以读到旧值；
+ int getAndSet(int)：以原子方式设置newValue的值，并返回旧值。

**【getAndIncrement】**

```java
public final int getAndIncrement() {
    for (;;) {
        int current = get();
        int next = current + 1;
        if (compareAndsSet(current, next))
            return current;
    }
}

public final boolean compareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffetset, expect, update);
}
```

**【Unsafe类】**

Atomic包基本通过Unsafe实现，Unsafe只提供了3种CAS方法：compareAndSwapObject/compareAndSwapInt和compareAndSwapLong。其他诸如Boolean都是先将其转换为整形，在使用compareAndSwapInt进行CAS。

### 原子更新数组

Atomic提供4个类用于原子更新数组的某个元素：

+ AtomicIntegerArray：原子更新数组中的元素
+ AtomicLongArray：原子更新长整型数组的元素
+ AtomicReferenceArray：原子更新引用类型数组元素

### 原子更新引用类型

原子更新基本数据类型AtomicInteger只能更新一个值，如果原子更新多个变量，需要使用原子更新引用类型提供的类。

+ AtomicReference：原子更新引用类型；
+ AtomicReferenceFieldUpdater：原子更新引用类型的字段；
+ AtomicMarkableReference：原子更新带标记位的引用类型。可以原子更新布尔类型的标记为和引用类型。

### 原子更新字段类

Atomic提供3个类来实现原子字段更新：

+ AtomicIntegerFieldUpdater：原子更新整型的字段的更新器。
+ AtomicLongFieldUpdater：原子更新长整型字段的更新器。
+ AtomicStampedFieldUpdater：原子更新带版本号的引用类型。该类将整数值与引用关联起来，用于原子更新数据和数据的版本号，可以解决使用CAS进行原子更新时可能出现的ABA问题。

原子更新字段类需要两步：

1. 因为原子更新子弹类都是抽象类，每次使用时必须使用静态方法newUpdater创建一个更新器，并设置想要更新的类和属性；
2. 更新类的字段必须使用public volatile修饰符。

## Java并发工具类

JDK并发包提供了几个有用的并发工具类。

+ CountDownLatch、CyclicBarrier、Semaphore工具类提供了并发流程控制的手段；
+ Exchanger工具类提供了线程间交换数据的手段。

### CountDownLatch

CountDownLatch允许一个或多个线程等待其他线程完成操作。

传统的方式是通过join方法来实现等待线程。join的实现原理是不但检查join线程是否存活，如果join线程存活则让出当前线程永久等待。直到join线程结束，线程的this.notifyAll()方法会被调用。

JDK1.5 之后的并发包提供的CountDownLatch不仅可以实现join的功能，还可以提供了额外的功能。

CountDownLatch的构造函数用于接收一个int类型的参数作为计数器，如果需要等待n个节点完成，就传入n。

+ 当调用CountDownLatch的countDown方法时，n就会减去1；
+ CountDownLatch的await方法会阻塞当前线程，知道n变为零；
+ 由于countDown方法可以用在任意地方，因此n可以是n个线程，也可以是n个执行步骤；
+ 当用在多个线程的时候只需要把CountDownLatch的引用传递到线程即可；
+ 除此之外，CountDownLatch的await方法还支持超时返回。

### CyclicBarrier

CyclicBarrier是可循环使用的屏障，可以让一组线程到达一个屏障/同步点时被阻塞，知道最后一个线程到达屏障时，屏障才会开门，所有被拦截的屏障才会机型执行。

CyclicBarrier默认构造函数可以传入参数n，参数表示屏障拦截的线程数量，每个线程调用await方法方法高速CyclicBarrier我已经到达了屏障，然后当前线程被阻塞。

除此之外CyclicBarrier还支持更加高级的构造函数CyclicBarrier(int, Runnable)，用于线程到达屏障之后，优先执行一个任务，方便处理更加复杂的业务场景。

CyclicBarrier可以用于多线程计算数据，最后合并计算结果的场景。

#### CyclicBarrier与CountDownLatch的区别

CountDownLatch的计数器只能使用一次，而CyclicBarrier的计数器可以使用reset方法重置。因此CyclicBarrier能够处理更复杂的业务场景。如计算发送错误，可以重置计数器，并让线程重新执行一次。

除此之外，CyclicBarrier还提供了其他有用的方法，如：

+ getNumberWaiting：可以以获取阻塞的线程数量；
+ isBroken：用于了解阻塞的线程是否被中断；

### Semaphore

Semaphore用来控制同时访问特定资源的线程数量，通过协同多个线程，以保证合理地使用公共资源。Semaphore场景的使用场景用于流量限制，如对于数据库连接的限制。

Semaphore构造方法可以接受一个整形数字，表示可用的许可证。线程需要通过Semaphore的acquire方法获取一个许可证，使用完之后调用release方法归还许可证。

Samephore还提供了一些其他的方法，如：

+ int availablePermits()：返回信号量中当前可用的许可数；
+ int getQueueLength()：返回正在等待获取许可证的线程数；

+ boolean hasQueuedThread()：是否有线程正在等待许可证；
+ void reducePermits(int)：减少int个许可证；
+ Collection getQueuedThreads()：返回所有等待获取许可证的线程集合；

### Exchanger

Exchanger是有个用于线程间协作的工具类，用于进行线程间的数据加交换。Exchanger提供了一个同步点，在这个同步点，两个线程可以交换彼此的数据。这两个线程通过exchange方法交换数据，如果第一个线程先执行exchange方法，会一直等待第二个线程执行到exchange方法，当两个线程同时到达同步点，两个线程可以交换数据，将本线程生成的数据传递给对方。

Exchanger可以用于遗传算法，在遗传算中需要选出两个人作为交配对象，这是需要交换两人的数据，并使用交叉规则得出两个交配结果。

Exchanger也可以用于校对工作。

## 线程池

合理使用线程池能够带来3个好处：

+ **降低资源利用率**：通过复用已经创建的线程降低线程创建和消耗造成的消耗；
+ **提供响应速度**：当任务到达，任务可以不需要等待线程创建就能立即执行；
+ **提供线程的可管理性**：线程是稀缺资源，如果无限制创建，不仅会消耗系统的资源，还会降低系统稳定性，使用线程池可以统一分配、调优和监控。

### 线程池原理

当向线程池提交一个任务，线程池会执行如下流程：

1. 线程池判断判断核心线程池的线程是否都在执行任务，如果不是，则获取一个线程处理该任务；
2. 线程池判断工作队列是否已满。如果工作队列没有满，则将提交的任务储存在工作队列中；
3. 线程池判断线程池的线程是否都处于工作状态。如果没有则创建一个新的工作线程来执行任务。如果满了，则交给饱和策略来处理这个任务。

ThreadPoolExecutor执行execute方法分为以下4种情况：

1. 如果当前运行的线程少于corePoolSize，则创建新的线程来执行任务（这一步骤需要获取同步锁）；
2. 如果当前运行的线程等于或多于corePoolSzie，则将任务加入BlockingQueue中；
3. 如果无法将任务加入BlockingQueue，则创建新的线程来处理任务；
4. 如果创建新线程将使得当前线程超出maximumPoolSize，任务将被拒绝，并调用拒绝策略执行相应操作。

由于ThreadPoolExecutor的使用一般是完成预热之后，即当前运行线程数大于等于corePoolSize，线程池才会接受任务的提交，几乎所有的execute方法的调用都是执行步骤2，不需要执行步骤1，获取同步锁。

线程池在创建线程在创建线程时，会将线程封装成工作线程Worker，Worker在执行完任务后，会循环获取工作队列中的任务执行。下面是Worker中的run方法

```java
public void run() {
    try {
        Runnable task = firstTask;
        firstTask = null;
        while (task != null || (task = getTask()) != null) {
            runTask(task);
            task = null;
        }
    } finally {
        workerDone(this);
    }
}
```

线程池中的线程执行任务分为两种情况：

1. 在execute方法创建一个线程时，会让这个线程执行当前任务；
2. 这个线程执行完之后，会反复重BlockingQueue获取任务执行；

### 线程池使用

#### 线程池的创建

可以通过以下构造方法来创建一个线程池：

```java
public ThreadPoolExecutor(int corePoolSize,		// 核心线程数
                          int maximumPoolSize,	// 最大线程数
                          long keepAliveTime,	// 最大线程数中超出核心线程数的线程存活的时间
                          TimeUnit unit,		// 时间的单位
                          BlockingQueue<Runnable> workQueue,	// 超出核心线程数任务保存的阻塞队列
                          ThreadFactory threadFactory,	// 创建线程的工厂
                          RejectedExecutionHandler handler)// 饱和策略
```

+ **corePoolSize**：当提交一个任务时，线程池会创建一个线程用于执行任务，即使其他空闲的基本线程能够执行新任务也会创建线程，等待需要执行的任务数大于线程池基本大小时不再创建。如果调用线程池的prestartAllCoreThreads()方法，线程池会提前创建并启动所有基本线程。

+ **runnableTaskQueue**：用于保存等待执行的任务的阻塞队列，可以选择以下几个阻塞队列：

  + ArrayBlockingQueue：FIFO有界阻塞队列；
  + LinkedBlockingQueue：FIFO无界阻塞队列，吞吐量高于ArrayBlockingQueue；【Executors.newFixedThreadPool()实现原理】
  + SynchronousQueue：不储存元素的阻塞队列。每个插入操作必须等待另一个移除；【Executors.newCachedThreadPool()实现原理】
  + PriorityBlockingQueue：具有优先级的无限阻塞队列。

+ **maximumPoolSize**：线程池运行创建的最大线程数，如果线程池已满，并且创建的线程数小于最大线程数，则线程池会在创建新的线程执行任务。

+ **ThreadFactory**：用于设置创建线程的工厂，可以通过线程工厂给每个创建出来的线程设置有意义的名字，用于问题排除。【可以使用guava提供的ThreadFactoryBuilder快速给线程池设置有意义的名字】

  ```java
  new ThreadFactorBuilder().setNameFormat("XX-task-%d").build();
  ```

+ **RejectedExecutionHandler**：当队列线程池满了，线程池处于饱和，需要采取一种策略来处理新提交的任务。JKD1.5提供一下4中策略：

  + AbortPolicy：直接抛出异常；【默认】
  + CallerRunsPolicy：DiscardOldestPolicy：只用调用者所在线程运行任务；
  + DiscardOldestPolicy：丢弃队列中最近的一个任务，并执行当前任务；
  + DiscardPolicy：不处理，直接丢弃；

  除此之外还可以实现RejectExecutionHandler接口自定义策略。如记录日志或持久化储存不能处理的任务。

+ **keepAliveTime**：线程池工作线程空闲后，保存存活的时间。
+ **TimeUint**：线程活动保存时间的单位。

#### 向线程池提交任务

可以使用两个方法向线程池中提交任务，分别execute()和submit()方法。

+ execute()方法用于提交不需要返回值的任务；【Runnable】
+ submit()方法用于提交需要返回值的任务，返回一个future类型的对象，可以通过该对象来判断任务是否成功执行；【Callable/Runnable】

#### 关闭线程池

可以通过调用shutdown或shutdownNow方法来关闭线程池。它们的原理是变量线程池的工作线程，然后逐个调用线程的interrupt方法来中断线程，因此无法响应中断的任务可能永远无法终止。但是两者存在一定的区别：

+ showdownNow：首先将线程池的状态设置为STOP，然后尝试停止所有正在执行的线程或暂停任务的线程，并返回等待执行任务的列表；
+ showdown：将线程池状态设置成SHUTDOWN状态，然后中断所有没有正在执行任务的线程。

通常调用shutdown方法关闭线程池，如果任务不一定执行完，可以调用shutdownNow方法。

只要调用了两个关闭方法中的任意一个，isShutDown方法会返回true；当所有任务都已经关闭，才表示线程池关闭成功，这时调用isTerminated方法返回true。

#### 合理配置线程池

配置线程池可以根据任务的特性，合理配置线程池参数：

+ 任务性质：CPU密集型任务、IO密集型任务和混合型任务；
+ 任务优先级：高、中和低；
+ 任务的执行时间：长、中和短；
+ 任务的依赖性：是否依赖其他系统资源，如数据库连接；

CPU密集型任务应该配置尽可能小的线程，如配置：Ncpu + 1 个线程的线程池；

IO密集型由于不是一直处理任务，可以配置尽可能多的线程，如 2 * Ncpu；

混合任务如果可以拆分，将其拆分为CPU密集型和IO密集型，只要两个任务的执行时间相差不是太大，分解后执行的吞吐量高于串行执行的吞吐量。如果两个任务执行时间相差太大，则没必要进行分解。可以通过 Runtime.getRuntime().avaliabaleProcessors()。

优先级不同的任务可以使用优先级队列PriorityBlockingQueue来处理，可以让优先级高的任务先执行。

执行时间不同的任务可以交给不同规模的线程池来处理，或者可以使用优先级队列，让执行时间短的任务执行。

依赖数据库连接池的任务，因为线程提交SQL需要等待数据库返回的结果，等待的时间越长，线程数应该设置越大，才能更好利用CPU。

同时建议在配置等待队列为有界队列。有界队列可以增加系统的稳定性和预警能力，可以根据需求设置长度。

#### 线程池监控

如果系统中大量使用线程池，则需要对线程池进行监控，方便在出现问题时，可以根据线程池的使用情况快速定位问题。可以通过线程池提供的参数：

+ taskCount：线程池需要执行的任务数
+ completedTaskCount：线程池在运行中已完成的任务数量，小于或等于taskCount；
+ largestPoolSize：线程池中曾经创建过最大线程数量，通过数据判断线程是否曾经满过。
+ getPoolSize：线程池的线程数量。如果线程池不销毁，线程池的线程不会自动销毁，因此大小自增不减。
+ getActiveCount：获取活动的线程数。



## Executor框架

Java的线程既是工作单元，也会执行机制。JDK 5 开始，把工作单元与执行机制分离开来。工作单元称为Runnable或Callable，而执行机制有Executor框架提供。

### Executor框架简介

#### 两级调度模型

HotSpot VM的线程模型中，Java 线程被一对一映射为本地操作系统线程。Java线程启动时会创建一个本地操作系统线程；当该Java线程终止时，这个操作系统线程也会被回收。操作系统会调度所有线程并将它们分配给可用的CPU。

+ 在上层Java多线程通常会把应用分为若干个任务，然后使用用户级的调度器将这些任务映射为固定数量的线程；【Executor控制】
+ 在底层，操作系统内核将这些线程映射到硬件处理器。【不受应用程序的控制】

![image-20210929204508555](https://gitee.com/tobing/imagebed/raw/master/image-20210929204508555.png)

#### 结构与成员

Executor框架有3大部分组成：

+ 任务：被执行任务需要实现的接口，如Runnable、Callable；
+ 任务的执行：包含任务中心机制核心接口Executor，以及其子接口ExecutorService，和实现类ThreadPoolExecutor和ScheduledThreadPoolExecutor；
+ 异步计算的结果：包括接口Futrue和实现Future接口的FutureTask类。

这些类的关系如下图所示：

![executor_overview](E:/Users/tobing/Desktop/executor_overview.png)

| 类/接口                     | 简介                                            |
| --------------------------- | ----------------------------------------------- |
| Executor                    | Executor框架的基础，将任务提交与任务的执行分离  |
| ThreadPoolExecutor          | 线程池核心实现，用于执行被提交的任务            |
| ScheduledThreadPoolExecutor | 可以在给定的延迟后或定时运行任务，比Timer更强大 |
| Future与FutureTask          | 代表异步计算的结果                              |
| Runnable/Callable实现类     | 可以被TPE或STPE执行                             |

+ 主线程实现创建实现Runnable或Callable接口的任务对象；
+ 工具类Executors可以把一个Runnable对象封装为一个Callable对象；
+ 把Runnable对象直接给ExecutorService执行；
+ 或把Runnable对象或Callable对象提给ExecutorService执行；
+ 如果执行ExecutorService.submit，将返回一个实现Future接口的对象；
+ 最后主线程可以执行FutureTask.get方法等待任务完成，主线程也可以执行FutureTask.cancel方法来取消次任务的执行。

#### Executor框架成员

Executor成员主要有：ThreadPoolExecutor、ScheduledThreadPoolExecutor、Future接口、Runnable接口、Callable接口和Executors。

**【ThreadPoolExecutor】**

ThreadPoolExecutor可以使用工厂类Executors来创建。Executors可以创建3种类型的ThreadPoolExecutor：

| 线程池               | 简介             | 使用场景                                               |
| -------------------- | ---------------- | ------------------------------------------------------ |
| SingleThreadExecutor | 固定线程数线程池 | 适合限制当前线程数量，适用于负载较重的服务器           |
| FixedThreadPool      | 单个线程线程池   | 适用保证顺序执行各个任务                               |
| CachedThreadPool     | 无界线程池       | 适用于执行多短期异步任务的小程序，适用于负载较轻服务器 |

![ftp_ste_ctp](https://gitee.com/tobing/imagebed/raw/master/ftp_ste_ctp.png)

```java
// FixedThreadPool
public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(nThreads, nThreads,		// 核心线程数 = 最大线程数
                                  0L, TimeUnit.MILLISECOND, // 无效
                                  new LinkedBlockingQueue<Runnable>(),// 无界队列【OOM隐患】
                                  threadFactory);
}

// SingleThreadExecutor
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService(
        new ThreadPoolExecutor(1, 1,  						// 核心线程数 = 最大线程数
                               0L, TimeUnit.MILLISECONDS,	// 无效
                               new LinkedBlockingQueue<Runnable>()));// 无界队列【OOM隐患】
}

// CacheThreadPool
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, 					// 核心线程数=0
                                  Integer.MAX_VALUE, 	// 最大线程数=MAX_VALUE【OOM隐患】
                                  60L, TimeUnit.SECONDS,// 60秒生存时间
                                  new SynchronousQueue<Runnable>());// 无容量阻塞队列，用于控制顺序
}
```

**【ScheduledThreadPoolExecutor】**

ScheduleThreadPoolExecutor也可以通过Executors创建。Executors可以创建2种类型的ScheduledThreadPoolExecutor：

| 线程池                           | 简介       | 使用场景                                                     |
| -------------------------------- | ---------- | ------------------------------------------------------------ |
| ScheduleThreadPoolExecutor       | 固定线程数 | 适用多个后台线程执行周期任务，同时限制后台线程的数量         |
| SingleScheduleThreadPoolExecutor | 单个线程数 | 适用于单个后台线程执行周期任务，同时需要保证顺序执行各个任务 |

**【Future】**

Future接口或FutureTask类用于表示异步计算的结果。

**【Runnable接口和Callable接口】**

Runnable接口或Callable接口的实现类可以用于提交给ThreadPoolExecutor或ScheduledThreadPoolExecutor执行。Runnable不会返回结果，Callable可以返回结果。

### FutureTask详解

FutureTask 除了实现Future接口，还实现Runnable接口。FutureTask既可以交给Executor执行，也可以由调度线程直接执行。根据FutureTask.run方法被执行的时间，FutureTask可以处以下面3种状态。

+ **未启动**：创建一个FutureTask，但没执行FutureTask.run方法；
+ **已启动**：FutureTask.run方法被执行过程，FutureTask处于启动状态；
+ **已完成**：FutureTask.run方法执行完正常结束，或被取消或run方法中抛出异常而结束；

| 状态   | FutureTask.get() | FutureTask.cancel(boolean)                        |
| ------ | ---------------- | ------------------------------------------------- |
| 未启动 | 阻塞             | 任务永远不会被执行                                |
| 已启动 | 阻塞             | true以中断试图停止任务；false不会正在执行产生影响 |
| 已完成 | 返回或抛出异常   | 直接返回false                                     |

#### FutureTask实现

FutureTask基于AQS实现。AQS是一个同步框架，提供通用机制来原子性管理同步状态、阻塞和唤醒线程，以及维护被阻塞的线程的队列。每个基于AQS实现的同步器包含两种类型操作：

+ **至少一个acquire**：阻塞调用线程，直到AQS的状态允许这个线程继续执行；【FutureTask.get】
+ **至少一个release**：改变AQS状态，改变后的状态可以允许一个或多个阻塞线程解除阻塞。【FutureTask.run/FutureTask.cancel】

AQS作为「模板方法模式」的基础类，提供给FutureTask的内部子类Sync，内部子类只需要实现状态检查和更新方法即可，这些方法将控制FutureTask获取和释放操作。具体而言，Sync是实现了AQS的tryAcquireShared和tryReleaseShared方法。

FutureTask设计图如下图所示：

![image-20210929215436572](https://gitee.com/tobing/imagebed/raw/master/image-20210929215436572.png)

Sync是FutureTask的内部私有类，继承自AQS。创建FutureTask会创建内部是由的成员对象Sync，FutureTask的所有公有方法委托给内部私有Sync。

**【FutureTask.get】**

FutureTask.get方法会调用AQS.acquireSharedInterruptibly(int)方法，该方法的执行过程如下：

1. 调用AQS.acquireSharedInterruptibly方法，方法首先回调子类Sync中实现的tryAcquireShared方法判断acquire操作释放可以成功；
2. acquire操作可以成功的条件是：state为RAN、CANCELLED，且Runner不为null；
3. 如果成功则get方法立即返回，如果失败则线程到等待队列中等待其他线程执行release操作；
4. 当其他线程执行release操作唤醒当前线程，当前线程再次执行tryAcquireShared将返回1，当前线程将离开等待队列并唤醒它的后继线程；
5. 最后返回计算的结果或抛出异常。

**【FutureTask.run】**

FutureTask.run的执行过程如下：

1. 执行在构造函数中指定的任务；
2. 原子方式更新同步状态。如果这个原子操作成功，设置代表计算结果的变量的值为result的值，然后调用AQS.releaseShared(int)；
3. AQS.releaseShared(int)首先会回调在子类Sync中实现的tryReleaseShared(arg)来执行release操作，然后唤醒线程等待队列中的第一个线程；
4. 调用FutureTask.done()。

当执行FutureTask.get方法时，如果FutureTask不是出于RAN或CANCELLED，当前执行线程将到AQS的线程等待队列中等待。当某个线程执行FutureTask.run或FutureTask.cancel方法时，会唤醒线程等待队列的第一个线程。

## Java并发编程实战

### 生产者消费者模式

并发编程中「生产者消费者」模式可以解决绝大多数并发问题。该模式平衡生产者消除和消费者线程的工作能力来提供程序整体处理数据的速度。

在线程模型中，生产者是生产数据的线程，消费者是消费数据的线程。在多线程开发中，如果生产者处理速度很快，而处理器处理书很慢，生产者需要等待消费者处理完，才能继续生产数据。同样地，如果消费者线程处理能力大于生产者，那么消费者必须等待生产者。为了解决这种生产者消费能力不均衡的问题，便有生产者和消费者模式。

生产者和消费者模式通过一个容器来解决生产者和消费者的强耦合问题。生产者和消费者彼此之间不直接通信，而是通过阻塞队列进行通信，因此生产者生产完数据之后不需要等待消费者处理，直接扔给阻塞队列，消费者不找 生产者要数据，而是直接从阻塞队列中取出来，阻塞队列相当于一个缓冲区，平衡了生产者和消费者的处理能力。

#### 多生产者多消费者场景

多核时代，多线程并发处理速度比单线程处理更快，因此可以使用多个线程来消费数据。

#### 线程池与生产消费者模式

Java线程池是一种生产者和消费者模式的实现方式。

系统中可以使用线程池来实现多生产者和消费者模式。如创建N个不同规模的Java线程池来处理不同性质的任务，如线程池1将数据读取到内存之后，交给线程池2的线程继续处理压缩数据。线程池1主要处理IO密集型任务，线程池2主要处理CPU密集型任务。

### 线上问题定位

有很多问题只有在线上或预发布环境中才能发现，而线上又不能调试代码，所以线上问题定位只能通过**：日志、系统状态和dump线程。**

#### top

top命令可以用于查看每个进程的情况。

在Java应用中，只需要关注COMMAND是Java的性能数据，COMMAND表示启动进程的命令。

使用top的交互命令数字「l」查看每个CPU的性能数据。

使用top的交互命令数字「H」查看每个线程的性能信息。































