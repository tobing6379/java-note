# Java 集合框架

## 简介

容器，就是可以容纳其他Java对象的对象。Java Collection Framework 为Java开发中提供了通用容器，其始于JDK1.2，优点是：

+ 降低编程难度
+ 提高程序性能
+ 提高API间互操作性
+ 降低学习难度
+ 降低设计和实现相关API的难度
+ 增加程序重用性

Java 容器只能存放对象，对于基本数据类型（int，long，float，double等）需要将其包装为对象类型才能放到容器中。很多时候自动装箱拆箱能够自定完成。这虽然会导致额外的性能和空间开销，但简化了设计和编程。

### Collection

容器主要包括 Collection 和 Map 两种，Collection 储存对象的集合，而 Map 储存键值对的映射表。

Collection 中主要包含了 Set、List 和 Queue三个子接口，主要的实现类有：

+ **TreeSet**：基于红黑树实现，支持有序操作，查找元素的效率为 O(logN) ，不容HashSet；

+ **HashSet**：基于哈希表实现，支持快速查找，但不支持有序操作。失去了元素的插入顺序信息，因此通过 Iterator 遍历 HashSet 得到的结果是不确定的；

+ **LinkedHashSet**：具有 HashSet 的查找效率，且内部使用双向链表维护元素的插入顺序；

+ **ArrayList**：基于动态数组实现，支持随机访问；

+ **Vector**：和ArrayList类似，但它是线程安全的；

+ **LinkedList**：基于双向链表实现，只能顺序访问，可以快速的从中间插入和删除元素，可以用于实现栈和队列；

+ **PriorityQueue**：基于堆结构实现，用于实现优先队列；

Map 中的实现类主要有：

+ **TreeMap**：基于红黑树实现；
+ **HashMap**：基于哈希表实现；
+ **HashTable**：和HashMap类似，但是它是线程安全的。尽管是线程安全，但是效率低，不使用它，而是使用ConcurrentHashMap来支持线程安全；
+ **LinkedHashMap**：使用双向链表来维护元素顺序，顺序为插入顺序或最近最少使用顺序LRU。

## ArrayList

### 概述

ArrayList实现了List接口，是顺序容器，即元素存放的数据与放进去的顺序一致，允许放入null值，底层通过数组实现。该类除了没有使用同步，实现大致与Vector相同。每个ArrayList都有一个容量，表示底层数组的实际大小，容器内存储存的元素不能超过当前容量。当向容器中添加元素时，如果容量不足，容器会自动增大底层数组的大小。

ArrayList 的 size()，isEmpty()，get()，size() 方法均可以在常数时间复杂度内完成，add() 方法的时间开销与插入的位置有关，addAll() 方法的时间开销跟插入元素的个数成正比。

为了追求效率，ArrayList没有实现同步，因此如果需要多个线程并发访问，用户可以手动同步，或者使用Vector替代。

### ArrayList的实现

 #### 底层数据结构

```java
```

#### 构造函数

#### 自动扩容

每次向数组添加元素时，都要去检查添加元素的个数是否