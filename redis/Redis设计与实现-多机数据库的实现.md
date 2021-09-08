# Redis设计与实现-多机数据库的实现

## 一、复制

#### 概述

在Redis中可以通过执行SLAVEOF命令或者设置slaveof选项，让一个服务器（从服务器）去复制另一个服务器（主服务器）。

进行复制中的主从服务器双方的数据库将保存相同的数据库，这种现象称为“数据库状态一致”。

#### 旧版复制功能的实现

Redis的复制功能分为同步和命令传播两个操作：

+ 同步操作用于将从服务器的数据库状态更新到主服务器当前删除的数据库状态。
+ 命令传播操作则用于在主服务器的数据库状态被修改，导致主从服务器的数据库状态出现不一致时，让主服务器的数据库重新回到一致状态。

**同步（sync）**

客户端向服务器发送SLAVEOF命令，要求从服务器复制主服务器时，从服务器首先执行同步操作，即将从服务器的数据库状态更新到主服务器当前所处的数据库状态。

从服务器同步操作通过sync命令来完成，具体步骤如下：

1. 从服务器向主服务器发送sync命令
2. 收到sync命令的主服务器执行bgsave命令，在后台生成一个RDB文件，并使用一个缓冲区记录从现在开始执行的所有命令
3. 当主服务器的bgsave命令执行完毕，主服务器会将生成的RDB文件发送从服务器，从服务器接收载入，将自己的数据库状态更新为主服务器执行bgsave命令时的状态
4. 主服务器将记录在缓冲区的所有写命令发送给从服务器，从服务器执行这些写命令，将自身更新到主服务器当前最新状态

![image-20210604092532955](https://gitee.com/tobing/imagebed/raw/master/image-20210604092532955.png)

**命令传播（command propagate）**

同步操作完成之后，主从服务器的数据库达到一致状态，此时如果主服务器接收到一条写命令，从服务器需要保持与主服务器的状态一致性。为了能够让主从服务器保持状态一致，主服务器需要对从服务器执行命令传播操作。即将造称主从不一致的命令发送给从服务器执行，这样就能继续保证主从一致状态。

#### 旧版复制功能的缺陷

在Redis中，主服务器对从服务器复制可以分为以下两种情况：

+ 初次复制：从服务器之前没有复制过任何主服务器，或从服务器当前要复制的主服务器与上一次复制的主服务器不同；
+ 断线后重复制：处于命令传播阶段的主从服务器因为网络原因中断了复制，但从服务器通过自动重连重新连接上了主服务器，并继续复制主服务器。

对于初次复制，旧版复制功能能够很好地完成任务，但是对于断线后重复制，旧版复制功能就显得尤为低效。

在旧版复制功能中，断线后的重复制，从服务器会重新发送SYNC，重新复制一次RDB文件并执行一次缓冲区的命令。

假如某个时间断线，但是在断线时间内主服务器只是得到了少数量的写命令。此时如果采用旧版复制就不得不重新复制一遍RDB，这样无疑是低效的。

每次执行sync命令，主从服务器需要执行以下操作：

1. 主服务器执行BGSAVE命令生成RDB文件，这个生成操作耗费主服务器的大量CPU、内存和IO资源
2. 主服务器需要将生成的RDB文件发送给从服务器，这个发送过程耗费主从服务器的大量为了资源，并对主服务器的命令响应产生影响
3. 接收到RDB文件之后，从服务器需要重新载入RDB文件，这将导致从服务器阻塞，无法处理请求

综上，旧版的断线后复制不仅会造成从服务器的阻塞，还回占用主服务器的大量资源，进而影响整个Redis中间件的性能

#### 新版复制功能实现

为了解决旧版断线后复制的低效，从Redis 2.8 开始使用PSYNC命令代替SYNC执行复制时同步。

PSYNC命令具有完整重同步（full resynchroinzation）和部分重同步（partial resynchroinzation）两种模式：

+ 完整重同步情况：其执行步骤和SYNC命令执行步骤基本一致，通过让主服务器发送RDB文件，向从服务器发送保存在缓冲区里面的写命令实现同步。
+ 部分重同步情况：当主从服务器断线重连时，如果条件允许，主服务器可以将主从端开期间的写命令发送给从服务器，这样从服务器只需要接收写命令，可以实现数据库的更新

PSYNC在主从服务器断线重连时能够提高效率，因为PSYNC对于从服务器已有的部分不行进行RDB发送，一方面可以降低主服务器的性能，另一方面可以减少RDB恢复造成的从服务器阻塞。

![image-20210604095512459](https://gitee.com/tobing/imagebed/raw/master/image-20210604095512459.png)

#### 部分重同步的实现

部分重同步主要以下三部分构成：

+ 主服务器的复制偏移量和从服务器的复制偏移量（replication offset）
+ 主服务器的复制积压缓冲区（replication backlog）
+ 服务器的运行ID（run ID）

**复制偏移量**

执行复制的双方分别维持一个复制偏移量：

+ 主服务器每次向从服务器传播N个字节数据时，将自己的复制偏移值加上N
+ 从服务器每次收到主服务器传播的N个字节数据是，就将自己的偏移量加上N

通过对比主从服务器的复制偏移量可以容易知道主从服务器是否处于一致性：相同一致，不相同不一致。

**复制积压缓冲区**

复制积压缓冲区可以实现在断线重连之后，确定执行完整重同步还是部分重同步？如何执行部分重同步时对从服务器补偿断线阶段的数据？

复制积压缓冲区有主服务器维护一个固定长度先进先出队列，默认大小1MB。

由于该度列的长度固定，因此当入队元素大于队列长度，最先入队的元素将会被弹出，而新元素会被放入队列。

当主服务器进行命令传播时，它不仅会将写命令发送给所有从服务器，还会将写命令入队到复制积压缓冲区里面。如下：

![image-20210604101042879](https://gitee.com/tobing/imagebed/raw/master/image-20210604101042879.png)

![image-20210604101342580](https://gitee.com/tobing/imagebed/raw/master/image-20210604101342580.png)

因此，主服务器的复制积压缓冲区里面会保存一部分最近传播的写命令，并且复制积压缓冲区会为队列中的每个字节记录相应的复制偏移量。

当从服务器重新连上主服务器时，从服务器会通过PSYNC命令将自己的复制偏移量offset发送给主服务器，主服务器会根据这个复制偏移量来决定对从服务器执行何种同步操作：

+ 如果offset偏移量的之后的数据（offset+1）仍然存在复制积压缓冲区，那么主服务器将对从服务器执行部分重同步操作
+ 如果offset偏移量的之后的数据不存在复制积压缓冲区，那么主服务器将对从服务器执行完整重同步操作

需要注意复制积压缓冲区的大小是可配置的，因此我们**可以通过调整复制积压缓冲区的大小**来间接控制服务器在短连之后执行何种同步操作。

复制缓冲区最小大小公式可以根据公式`second * write_size_per_second`来估算：

+ second：断线到重新连接上的平均时间，单位为秒
+ write_size_per_second：主服务器平均每秒产生的写命令数据量

关于复制积压缓冲区大小修改，可以参考配置文件中的repl-backlog-size说明。

**服务运行ID**

服务运行ID实现部分重同步的关键一部分：

+ 每个Redis服务器都有自己运行的ID
+ 运行ID在服务器启动时生成，有40个随机16进制字符组成

主从初次复制时，主服务器将自己运行的ID传送给从服务器，而从服务器会将这个运行ID保存起来。当从服务器断线重连主服务器，从服务器将当前连接的主服务器发送之前保存的运行ID：

+ 如果服务器保存的运行ID和当前连接的主服务器运行ID相同，说明从服务器短信前就是连接当前主服务器（即无发生主从切换），主分区可以尝试执行部分重同步操作。
+ 如果服务器保存的运行ID和当前连接的主服务器运行ID不同，说明已经不是之前的主服务器，这时会执行完整重同步。

#### PSYNC命令实现

PSYNC调用方法有两种：

+ 如果从服务器**从未**复制过任何主服务器，或者之前执行过`SALVEOF no one`命令，那么从服务器在开始一场新的复制时会先主发送`PSYNC ? -1`命令，主动请求主服务器进行完整重同步

+ 如果从服务器**已经**复制过任何主服务器，那么从服务器会开始一次新的复制时将向服务器发送`PSYNC <runid> <offset>`命令

  + runid：上一次复制id主服务器的运行id
  + offset：服务器当前的复制偏移量

  通过上面两个参数，主分区可以判断执行那种同步操作

+ 如果主服务器返回`+FULLRESYNC <runid> <offset>`回复，表示主服务器将与从服务器执行完整重同步

  + runid：这个主服务器的运行id，从服务器会将其保存，用于下次发送PSYNC
  + offset：主服务器当前的复制偏移量，从服务器将会根据这个值作为自己的初始化偏移

+ 如果主服务器返回`+CONTINUE`回复，表示主服务器将与从服务器执行部分重同步操作，从服务器只要等待主服务器将自己缺少的数据发送过来即可

+ 如果主服务器返回`-ERR`回复，表示主服务器版本低于2.8，无法识别PSYNC，此时从服务器将会发送SYNC命令

![image-20210604105236624](https://gitee.com/tobing/imagebed/raw/master/image-20210604105236624.png)

#### 复制实现

通过先从服务器发送SLAVEOF命令，可以让从服务器复制一个主服务器：`SLAVEOF <master_ip> <master_port>`。

Redis2.8或以上版本的复制功能详细步骤如下：

1. 设置服务器地址和端口
2. 建立套接字连接
3. 发送PING命令
4. 身份验证
5. 发送端口信息
6. 同步
7. 命令传播

**设置服务器地址和端口**

发送命令`SALVEOF 127.0.0.1 6379`，从服务器首先将客户端给定IP地址和端口保存到redisServer中的masterhost和masterport属性中。

**建立套接字里阿杰**

在SALVEOF命令执行之后，从服务器将根据命令设置的IP地址和端口，创建连先主服务器的套接字连接。

如果从服务器创建的套接字能够成功连接到主服务器，那么从服务器将为这个套接字关联一个专门用于处理复制工作的文件事件处理器，这个处理器将复制处理后继的复制工作，如接收RDB文件，以及接收主服务器传播的写命令等。

主服务器在接受从服务器的套接字连接之后，为该套接字参加相应的客户端状态，并将从服务器看作是一个连接到主分区的客户端对待，此时从服务器具有server和client两个身份，从服务器可以先主服务器发送命令请求，而主服务器则先从服务器返回命令回复

**发送PING命令**

从服务器成为主服务器的客户端之后，第一件事就是向服务器发送一个PING命令，主要有两个作用：

+ 通过PING命令检查刚刚创建成功的套接字读写状态是否正常
+ 检查主服务器是否可以正常处理命令请求

从服务器发送PING命令之后，可能会遇到以下三种情况：

+ 如果主向从返回一个命令回复，但是从不能再timeout内读取出命令回复的内容，表示当前网络连接不佳，不进行后继操作，而是从断开并重新创建新连接。
+ 如果主向从返回一个错误，表示主服务器暂时无法处理从服务器的命令请求，从断开连接并重新建立新连接。
+ 如果从收到主的PONG，表示主从状态正常，主可以正常处理从发送的命令请求

![image-20210604111247274](https://gitee.com/tobing/imagebed/raw/master/image-20210604111247274.png)

**身份验证**

从服务器收到主服务器的PONG之后，下一步决定是否进行身份验证：

+ 如果从服务器设置了masterauth选项，进行身份验证；
+ 如果从服务器没设置了masterauth选项，不进行身份验证；

身份验证是，从向主发送auth命令，参数为masterauth设置的值。从服务器在身份验证阶段会经历下面三种情况：

+ 主服务器没有设置requirepass选项，且从服务器没设置masterauth，那么主服务器继续执行从服务器发送的命令，复制工作可以继续
+ 主服务器通过auth命令方的密码和主服务器requirepass配置相同，复制工作进行；反之，会返回invalid password错误
+ 主服务器设置了requirepass选项，但从服务器没设置masterauth，主服务器返回一个NOAUTH错误
+ 主服务器没设置requirepass，从设置了masterauth，主服务器返回no password is set错误

所有的错误都会让从服务器中止目前复制工作，并从创建套接字开始重新执行复制，知道身份验证通过，或从服务器放弃执行复制。

![image-20210604113056252](https://gitee.com/tobing/imagebed/raw/master/image-20210604113056252.png)

**发送端口信息**

身份验证之后，从服务器将会执行命令`REPLCONF listening-port <port-number>`向主服务器发送从服务器的监听端口号。

主服务在介绍到这个命令之后，会将端口号记录在从服务器对应的客户端状态的`slave_listening_port`属性。

**同步**

这一步中，从服务器将向主服务器发送PSYNC命令，执行同步操作，并将自己的数据库更新到主服务器数据库当前所处的状态。

需要注意，同步操作之前，只有从服务器是主服务器的客户端，但执行完毕之后，主服务器会成为从服务器当客户端：

+ 如果PSYNC执行的是完整重同步操作，主服务器需要成为从服务器的客户端，才能将保存在缓冲区里面的写命令发送给从服务器执行；
+ 如果PSYNC执行的是部分重同步操作，主服务器需要成为从服务器的客户端，才能向从服务器发送保存在复制积压缓冲区里面的写命令

![image-20210604114029172](https://gitee.com/tobing/imagebed/raw/master/image-20210604114029172.png)

**命令传播**

同步完成之后，主从服务接入命令传播阶段，此时主服务器只要一直将自己执行的写命令发送给从服务器，从服务器只要一直接收并执行主服务器的写命令就可以保证主从服务器一直保持一致。

#### 心跳检测

在命令传播期间，从服务器默认以1秒一次的频率向主服务器发送命令：`REPLCONFIG ACK <replication_offset>`。

其中replication_offset是从服务器当前的复制偏移量。发送REPLCONFIG ACK命令主要作用如下：

+ 检测主从服务器的网络连接状态；
+ 辅助实现min-slaves选项；
+ 检测命令丢失。

**检测主从服务器的网络连接状态**

通过REPLCONFIG ACK命令可以检测网络连接释放正常。如果主服务器超过一秒没有收到从服务器的REPLCONFIG ACK命令，那么主服务器指定从服务器之间的连接出现了问题。

**辅助实现min-slaves选项**

Redis的min-slaves-to-write和min-slaves-max-lag两个选项可以防止主服务器在不安全的情况下执行写命令。

```bash
# 从服务器数量少于3个，或三个从服务器的延迟大于10，主分区将拒绝写命令
min-slaves-to-write 3
min-slaves-max-lag 10
```

**检测命令丢失**

主服务器传播给从服务器的写命令可能会因为网络等因素在半路丢失。主服务器可以通过REPLCONFIG ACK命令的偏移判断是否丢失命令，进而将这些命令重发。

需要注意在2.8之前，即时命令在传播中丢失，主服务器和从服务器将无法注意到，因此尽量使用2.8或以上来保证主从服务器数据一致性。

## 二、Sentinel

#### 概述

Sentinel，哨兵机制Redis高可用的解决方案，由一个或多个Sentinel实例组成的Sentinel System可以见识任意多个主服务器，以及这些主服务器下的所有从服务器，并在被监听的主服务器下线之后，将某个从服务器升级为主服务器，让新的主服务器继续处理命令请求。

当一个主服务器下线时长超过用户设定的下线时长上限时，Sentinel System会对该主服务器执行故障转移操作：

1. Sentinel System会挑选出该主服务器下的一个从服务器，将选中的升级为新的主服务器
2. Sentinel System向该主服务器是所有从服务器发送新的复制命令，让它们连接上新的主服务器，当所有从服务器开始复制新的主服务器时，故障转移操作执行完毕
3. 除此之外，Sentinel System还会监控已经下线的服务器，在它重新上线时，将其设置为新主服务器的从服务器

#### 启动并初始化Sentinel

可以命令`redis-sentinel  sentinel.conf`或`redis-server sentinel.conf --sentinel`来启动一个Sentinel，具体需要执行以下步骤：

1. 初始化服务器
2. 将普通Reids服务器使用的代码替换成Sentinel专用代码
3. 初始化Sentinel状态
4. 根据给定配置文件，初始化Sentinel的监视器主服务器列表
5. 创建连向主服务器的网络连接

**初始化服务器**

Sentinel本质是一个运行在特殊模式下的Redis服务器，启动Sentinel第一步类似于初始化一个普通Redis服务器，但过程并不完全相同。如Sentinel初始化时不会载入RDB文件或AOF文件。其他区别如下：

| 功能                                                     | 使用情况                                                     |
| :------------------------------------------------------- | :----------------------------------------------------------- |
| 数据库和键值对方面的命令， 比如 SET 、 DEL 、 FLUSHDB 。 | 不使用。                                                     |
| 事务命令， 比如 MULTI 和 WATCH 。                        | 不使用。                                                     |
| 脚本命令，比如 EVAL 。                                   | 不使用。                                                     |
| RDB 持久化命令， 比如 SAVE 和 BGSAVE 。                  | 不使用。                                                     |
| AOF 持久化命令， 比如 BGREWRITEAOF 。                    | 不使用。                                                     |
| 复制命令，比如 SLAVEOF 。                                | Sentinel 内部可以使用，但客户端不可以使用。                  |
| 发布与订阅命令， 比如 PUBLISH 和 SUBSCRIBE 。            | SUBSCRIBE 、 PSUBSCRIBE 、 UNSUBSCRIBE PUNSUBSCRIBE 四个命令在 Sentinel 内部和客户端都可以使用， 但 PUBLISH 命令只能在 Sentinel 内部使用。 |
| 文件事件处理器（负责发送命令请求、处理命令回复）。       | Sentinel 内部使用， 但关联的文件事件处理器和普通 Redis 服务器不同。 |
| 时间事件处理器（负责执行 `serverCron` 函数）。           | Sentinel 内部使用， 时间事件的处理器仍然是 `serverCron` 函数， `serverCron` 函数会调用 `sentinel.c/sentinelTimer` 函数， 后者包含了 Sentinel 要执行的所有操作。 |

**将普通Reids服务器使用的代码替换成Sentinel专用代码**

启动Sentinel第二步骤需要将普通Reids服务器使用的代码替换成Sentinel专用代码。如：

+ 普通Redis服务器使用REDIS_SERVERPORT常量作为服务器端口，而Sentinel使用REDIS_SENTINEL_PORT常量值作为服务器端口。

+ 普通Redis服务器使用redisCommand作为服务器命令表，而Sentinel使用sentinelcmds作为服务器命令表。【有命令使用差异的原因】

**初始化Sentinel状态**

应用了Sentinel专用代码之后，服务器会初始化一个sentinelState结构，其中保存了服务器所有与Sentinel功能相关的状态：

```c
struct sentinelState {
    // 当前纪元，用于实现故障转移
    uint64_t current_epoch;
    // 保存了所有被此sentinel监视的主服务器，字典键是主服务器的名字，值是一个指向sentinelRedisInstance结构的指针
    dict *master;
    // 是否进入TILT模式
    int tilt;
    // 目前正在执行的脚本数量
    int running_scripts;
    // 进入TILT模式的时间
    mstime_t tilt_start_time;
    // 最后一次进入TILT模式的时间
    mstime_t previous_time;
    // FIFO队列，包含了所有需要执行的用户脚本
    list *scripts_queue;
} sentinel;
```

**初始化Sentinel状态的master属性**

Sentinel状态的master属性记录了所有被此sentinel监视的主服务器的相关信息，其中：

+ 键是主服务器的名字
+ 值是一个指向sentinelRedisInstance结构的指针

sentinelRedisInstance代表一个被Sentinel监视的Redis服务器实例，可以是主服务器，从服务器或另一个Sentinel。

sentinelRedisInstance的属性众多，部分属性如下：

```c
typedef struct sentinelRedisInstance {
    // 标识值，记录了实例的类型，以及该实例的当前状态
    int flags;
    // 实例的名字
    // 主服务器的名字由用户在配置文件中设置
    // 从服务器以及 Sentinel 的名字由 Sentinel 自动设置
    // 格式为 ip:port ，例如 "127.0.0.1:26379"
    char *name;
    // 实例的运行 ID
    char *runid;
    // 配置纪元，用于实现故障转移
    uint64_t config_epoch;
    // 实例的地址
    sentinelAddr *addr;
    // SENTINEL down-after-milliseconds 选项设定的值
    // 实例无响应多少毫秒之后才会被判断为主观下线（subjectively down）
    mstime_t down_after_period;
    // SENTINEL monitor <master-name> <IP> <port> <quorum> 选项中的 quorum 参数
    // 判断这个实例为客观下线（objectively down）所需的支持投票数量
    int quorum;
    // SENTINEL parallel-syncs <master-name> <number> 选项的值
    // 在执行故障转移操作时，可以同时对新的主服务器进行同步的从服务器数量
    int parallel_syncs;
    // SENTINEL failover-timeout <master-name> <ms> 选项的值
    // 刷新故障迁移状态的最大时限
    mstime_t failover_timeout;
    // ...
} sentinelRedisInstance;
```

+ addr指向一个sentinelAddr结构，其内部保存了实例的IP地址和端口号。

对Sentinel状态的初始化会引发对sentinelRedisInstance（master属性）的初始化，sentinelRedisInstance的初始化会根据载入的Sentinel配置文件进行。

![image-20210604122911701](https://gitee.com/tobing/imagebed/raw/master/image-20210604122911701.png)

**创建连向主服务器的网络连接**

初始化最后一步是创建连向被监视主服务器的网络连接，Sentinel将会成为主服务器的客户端，可以先主服务器发送命令，并从命令回复中获取相关信息。

对于每个被Sentinel监听的服务器，Sentinel会创建两个连向主服务器的异步网络连接：

+ 命令连接，专门用于向主服务器发送命令，并接收命令回复
+ 订阅连接，专门用于定于主服务器的`__sentinel__:hello`频道

目前，Redis的发布订阅功能中，被发送的信息不会保存在Redis服务器中，如果在消息发送时，想要收到信息的客户端不在线或断线，客户端就会丢失这条信息。为了不丢失`__sentinel__:hello`频道的任何用户信息，Sentinel必须专门建立一个订阅连接接收该订阅信息。

![image-20210604123632996](https://gitee.com/tobing/imagebed/raw/master/image-20210604123632996.png)

#### 获取主服务器信息

Sentinel默认以10s一次通过命令连接向被监视的主服务器发送INFO命令来获取主服务器的信息。信息主要包含两方面内容：

+ 关于主服务器本身信息。包括服务器运行ID、服务器角色等，通过这些信息可以来实时更新主服务器的实例结构
+ 主服务器下所有从服务器信息。每个从服务器都由一个“slave”字符串开头的行记录，每行的记录了服务器的IP地址，端口号。通过这些信息Sentinel可以主动发现从服务器。

Sentinel在分析INFO返回信息中包含的从服务器信息时，会检查从服务器对应的实例结构是否已经存在于slaves字典：

+ 存在，Sentinel对其机型更新
+ 不存在，说明是新发现的从服务器，在slaves擦净一个新的实例结构

下面展示了一个主服务器和它的三个从服务器

![image-20210604124417302](https://gitee.com/tobing/imagebed/raw/master/image-20210604124417302.png)

#### 获取从服务器信息

Sentinel通过主服务器发现从服务器。Sentinel除了为这些从服务器创建对应的结构，还会创建了到从服务器的命令连接与订阅连接。

Sentinel与从服务器建立连接之后会以10S一次的频率向从服务器发送INFO命令。从回复中可以提前以下内容：

+ 从服务器的run_id
+ 从服务器的role
+ 主服务器的IP以及端口
+ 主服务器的连接状态
+ 从服务器的优先级
+ 从服务器的复制偏移

通过上述信息，Sentinel对从服务器的实例结构进行更新

#### 先主从服务器发送信息

默认情况下，Sentinel以2S一次的频率，向所有被监视的主服务器和从服务器发送命令：

```bash
PUBLISH __sentinel__:hello "<s_ip>,<s_port>,<s_runid>,<s_epoch>,<m_name>,<m_ip>,<m_port>,<m_epoch>"
```

+ s开头表示Sentinel
+ m开头表示Master

#### 接收来自主从服务器的频道信息

当Sentinel与一个主服务器或从服务器建立订阅连接之后，Sentinel会通过订阅连接向服务器发送以下命令：

```bash
SUBSCRIBE __sentilen__:hello
```

对于每个与Sentinel连接的服务器，Sentinel既通过命令连接向服务器的`__sentilen__:hello`发送消息，又可以通过定于主服务器的`__sentilen__:hello`接收信息。因此，对于监视同一个服务器的多个Sentinel可以接收到各自向频道发送的信息，进而更新自身。

![image-20210604125712424](https://gitee.com/tobing/imagebed/raw/master/image-20210604125712424.png)

多个Sentinel的消息会通过Sentinel的运行ID来区分，对于自己的消息无需处理，对于其他的Sentinel会对对应的主服务器信息进行更新。

**更新sentinels字典**

Sentinel为主服务器创建的实例结构中的sentinels字典保存除了Sentinel本身，所有同样监控这个主服务器的其他Sentinel资料：

+ sentinels字典的键是其中一个Sentinel名字，格式为ip:port
+ sentinels字典的值时间对应的Sentinel的实例结构

当一个Sentinel接收到来自其他Sentinel发来的信息，目标Sentinel会从信息中分析并提取出两方面参数：

+ 与Sentinel有关参数：源Sentinel的IP地址、端口号、运行ID和配置纪元
+ 与主服务器有关参数：源Sentinel监控的主服务名字、IP地址、端口号和配置纪元

根据提取出的主服务器参数，目标Sentinel会在自己的Sentinel状态的masters字典中查找对应的主服务器实例结构，然后根据提取出的Sentinel参数，检查主服务器实例结构的sentinels字典中，源Sentinel的实例结构是否存在：

+ 如果存在，进行更新
+ 不存在，为其添加一个新的实例结构

**创建连接其他Sentinel的命令请求**

当Sentinel从频道信息中发现一个新的Sentinel是，不仅会为新的Sentinel在字典中创建相应实例，还会创建一个连接指向新Sentinel的命令连接，最终监控同一个主服务器的多个Sentinel会形成相互连接的网络。

![image-20210604153234134](https://gitee.com/tobing/imagebed/raw/master/image-20210604153234134.png)

上图展示了三个监视同一主服务器的Sentinel之间的相互连接。

Sentinel之间不会创建订阅连接，只创建命令连接。这是因为Sentinel需要通过接收主服务器或从服务器发来的频道信息来发现未知的新Sentinel，所以才需要建立订阅连接，而相互告知已知的Sentinel只要使用命令连接来进行通信就足够了。

#### 检测主观下线状态

默认情况下，Sentinel会以1s一次的频率先所有它创建了命令连接的实例（包括主服务器、从服务器、其他Sentinel内）发送PING命令，并通过实例返回PING命令回复来判断实例是否在线。

实例对于PING命令的回复可以分为两种情况：

+ 有效回复：实例返回+PONG、-LOADING、-MASTERDOWN三种回复的其中一种
+ 无效回复：实例返回上面三种回复的其中一种或者在指定时间内没有返回任何回复

Sentinel配置文件中的down-after-milliseconds选项指定了Sentinel判断实例进入主观下线所需的时间长度：如果一个实例在donw-after-millisencods毫秒内，连续先Sentinel返回无效回复，那么Sentinel会修改这个实例对应的实例结构。，在flags属性中打开SRI_S_DOWN标识，以此来表示这个实例已经进入了主观下线状态。

需要注意：down-after-milliseconds选项不仅被Sentinel用来判断主服务器主观下线的状态，还会被主服务器来判断从服务器以及所有同样监控这个主服务器的其他Sentinel的主观下线状态。

#### 检查客观下线状态

当Sentinel将一个主服务器判断为主观下线之后，为了确认这个主服务器是否真的下线，它会向同样监控了这个主服务器的其他Sentinel进行询问，看它们是否也认为主服务器已经下线，当Sentinel从其他Sentinel接收到足够多的下线判断，Sentinel就会将服务器判定为客观下线，并对主服务器执行故障转移操作。

**发送is-master-down-by-addr命令**

Sentinel使用`SENTINEL is-master-down-by-addr <ip> <port> <current_epoch> <runid>`命令询问其他Sentinel是否同意主服务器已经下线，参数含义如下：

+ ip，主观下线的主服务器的IP地址
+ port，主观下线的主服务器的端口号
+ current_epoch，Sentinel当前配置的纪元，用于选举Leader
+ runid，可以是`*`或Sentinel的运行ID，`*`代表命令仅仅用于检测主服务器的客观下线状态，而Sentilnel的运行ID用于选举Leader

**接收is-master-down-by-addr命令**

一个Sentinel接收到另一个Sentinel发来的SENTINEL is-master-down-by-addr时，目标Sentinel会分析并取出命令中包含的各个参数，并根据其中的主服务器IP和端口号，检查主服务器是否已经下线，然后向源Sentinel返回Mulit Bulk，其中包含三个参数：

+ down_state，返回目标Sentinel对主服务器的检查结果，1-下线，0-未下线
+ leader_runid，`*`代表命令仅仅用于检测主服务器的下线状态，而局部Leader的运行ID用于选举Leader
+ leader_epoch，目标局部Leader的配置纪元，用于选举Leader

**接收is-master-down-by-addr命令的回复**

根据其他Sentinel发送的`SENTILNEL is-master-down-by-addr`命令回复，Sentinel将会统计其他Sentinel同意主服务器已经下线的数量，当这一数量达到配置指定的判断客观下线所需的数量时，Sentinel会将主服务器实例结构flags属性的SRI_O_DOWN标识打开，表示主服务器已经进入客观下线状态

#### 选举Leader Sentinel

但是过一个主服务器被判断为客观下线，监视这个下线主服务器的各个Sentinel会进行协商，选举出一个Leader，由Leader对下线主服务器执行故障转移操作。

下面是Redis选择Sentinel的Leader过程：

1. 所有在线的Sentinel都有被选为Leader的资格
2. 每次进行Leader选举，无论是否成功，所有的Sentinel的**配置纪元**的值都会自增一次。
3. 在一个配置纪元中，所有的Sentinel都有一次将某个Sentinel设置为局部Leader的机会，并且局部Leader一旦设置这个纪元内无法修改
4. 每个发现主服务器进入客观下线的Sentinel都会要求其他Sentinel将自己设置为局部Leader
5. 当一个Sentinel会另一个Sentinel发送`SENTILNEL is-master-down-by-addr`命令时，并且runid参数不是`*`而是源Sentinel的ID，这是表示源Sentinel要求目标Sentinel选举自己为Leader
6. Sentinel挑选Leader是先到先得，最新先自己发送请求的就会将其设置为局部Leaer，后面收到的都会被拒绝
7. 在Sentinel进行投票返回之后，会放到leader_runid和leader_epoch参数分别记录了目标Sentinel的局部Leader的runId和配置纪元
8. 源Sentinel对收到的回复，会检查leader_epoch是否与自己相同，相同则取出leader_runid，如果属于自己，表示对方选举了自己
9. 如果存在某个Sentinel被半数以上的Sentinel设置为局部Leader，那么整个Sentinel就会成为真实的Sentinel。
10. 半数以上条件会导致最终最多只会出现一个Leader
11. 如果给定时间内没有选举出Leader，那么各个Sentinel会在一段时间之后从新选举

#### 故障转移

在选举出Leader之后，Leader将会对已经下线的主服务器执行故障转移操作，分为以下三个步骤：

1. 在已下线主服务器属下的所有从服务器中，挑选出一个从服务器，并将其转换为主服务器；
2. 让已现主服务器属下的所有从服务器改为复制新的主服务器；
3. 将已经下线的主分区设置为新的从服务器，旧的主服务器重新上线时，就会成为新的主服务器的从服务器

**挑选新的主服务器**

故障转移第一步是在已下线的主服务器属下的从服务器中选出一个状态良好、数据完整的从服务器，然后向这个从服务器发送SALVEOF no one命令，将这个从服务器转换为主服务器。

新的主服务器将从从服务器列表中按照以下条件过滤：

1. 删除列表中所有已经下线或断线状态的从服务器，保证列表中剩余的从服务器是正常在线
2. 删除列表中所有最近5s没有回复过Sentinel Leader的INFO命令的从服务器，确保剩下的从服务器都是从服务器都是最近成功通信过的
3. 删除所有与已下线主服务器连接断开超过down-after-milliseconds*10毫秒的从服务器，保证留下来的从服务器比较新鲜

之后，Leader Sentinel根据从服务器的优先级排序，选出最高优先级的从服务器。如果有多个优先级相同的，按照从服务器的复制偏移量大的选择（即同步的数据最多）。如果存在多个偏移量相同，根据runid大小，选择id最小的服务器。

选出了新主服务器，Leader Sentinel会10S/一次发送SALVE no one，如果回复中角色从salve变为master表示Leader Sentinel已经将选出的服务器顺利升级为主服务器。

**修改从服务器的复制目标**

有了新的主服务器，Leader Sentinel下一步需要把这个新的服务器通过SLAVEOF命令告诉给旧服务器下面所有的从服务器。

![image-20210604170157033](https://gitee.com/tobing/imagebed/raw/master/image-20210604170157033.png)

**将旧的主服务器变为从服务器**

故障转移最后一步是要将已经下线的主服务器设置为新的主服务器的从服务器。

#### 总结

1. Sentinel只是一个运行在特殊模式下的Redis服务器，使用了和普通模式不同的命令表，因此Sentinel模式能够使用的命令与普通Redis服务器并不相同；
2. Sentinel会读入用户配置的信息，为每个要被监控的主服务器创建相应的实例结构，并创建连线主服务器的命令连接和订阅连接，其中命令连接用于向主服务器发送命令请求，而订阅连接则用于接收指定频道的消息；
3. Sentinel通过先主服务器发送INFO命令获取其下的从服务器列表信息，并为这些从服务器创建对应的实例结构，以及连向这些从服务器的命令连接和订阅连接；
4. 一般情况下，Sentinel以10秒一次频率先被监视的主服务器和从服务器发送INFO命令，当主服务器处于下线状态，或Sentinel正对主服务器进行故障转移，频率会变为1秒一次；
5. 当监视同一个主服务器和从服务器的多个Sentinel，会以2秒一次频率，通过监听服务器的频道来发送消息先其他Sentinel宣告自己的存在；
6. 每个Sentinel会从频道中国接受来自其他Sentinel发来的信息，并根据信息创建对应的实例结构，以及命令连接；
7. Sentinel与主从服务器之间会建立命令连接和订阅连接，Sentinel之间则只会创建命令连接；
8. Sentinel会以1秒1次频率向实例（包括主服务器、从服务器、其他Sentinel）发送PING命令，并根据实例对PING命令的回复来判断实例是否在线，当一个实例在指定的市场中连续向Sentinel发送无效回复时，他会向同样监视这个主分区的其他Sentinel询问，看他们是否同意这个主服务器已经进入了主观下线状态。
9. 当Sentinel收集足够多的主观下线投票之后，会将主服务器判断为客观下线，并发起一次针对主服务器的故障转移操作

Sentinel Leader选举使用[Raft算法](http://thesecretlivesofdata.com/raft/)实现。

## 三、集群

#### 概述

Redis集群是Redis提供的分布式数据库的方案，集群通过分片（Sharding）来进行数据共享，并提供复制和故障转移功能。

#### 节点

一个Redis 集群通常包含多个节点，刚开始时，每个节点都是相互独立的，他们处于一个只包含自己的集群当中，当组建一个真正可工作的集群，必须要将各个独立节点连接起来，过程一个包含多个节点的集群。

可以通过`CLUSTER MEET <ip> <port>`命令来连接各个节点。

向一个节点node发送`CLUSTER MEET`命令，可以让node及与ip和port所指定的节点进行握手，当握手成功，node节点会将ip和port指定的节点添加到当前所在集群中。

![image-20210605101940851](https://gitee.com/tobing/imagebed/raw/master/image-20210605101940851.png)

上图展示了三个节点的集群演变的过程

**节点启动**

每个节点就是运行在集群模式下的Redis服务器，Redis服务器启动时会根据cluster-enable配置决定是否开启服务器的集群模式。当指定为一个节点时，会继续使用所有单机模式中使用的服务器组件，比如：

+ 节点会继续使用文件事件处理器来处理命令请求和返回命令回复。
+ 节点会继续使用时间事件处理器来执行serverCron函数，而serverCron函数又会调用集群模式特有的clustercron函数。clusterCron函数负责执行在集群模式下需要执行的常规操作，例如向集群中的其他节点发送Gossip消息，检查节点是否断线，或者检查是否需要对下线节点进行自动故障转移等。
+ 节点会继续使用数据库来保存键值对数据，键值对依然会是各种不同类型的对象。
+ 节点会继续使用RDB持久化模块和AOF持久化模块来执行持久化工作。
+ 节点会继续使用发布与订阅模块来执行PUBLISH、SUBSCRIBE等命令。
+ 节点会继续使用复制模块来进行节点的复制工作。
+ 节点会继续使用Lua脚本环境来执行客户端输入的 Lua脚本。

除此之外，节点会继续使用redisServer结构来保存服务器的状态，使用RedisClient来保存客户端状态。集群模式下用的数据，节点会将其保存到clusterNode、clusterLink以及clusterState结构中。

**集群数据结构**

clusterNode结构保存了一个节点的当前状态，如创建时间、节点的名字、节点当前配置的纪元、节点的IP地址和端口号等。

每个节点都会使用一个clusterNode结构来记录自己状态，并为集群中的所有其他节点（包括主从节点）创建一个相应的clusterNode节点，以此来记录其他节点的状态。

```c
struct clusterNode {
    // 创建节点时间
    mstime_t ctime;
    // 节点名字，由40个16进制字符组成
    char name[REDIS_CLUSTER_NAMELEN];
    // 节点标识
    // 使用各种不同标识值记录节点的角色（是主从节点）、状态（如是否上线）
    int flags;
    // 节点当前的配置纪元
    unit64_t configEpoch;
    // 节点的IP地址
    char ip[REDIS_IP_STR_LEN];
    // 节点的端口号
    int port;
    // 保存连接节点需要的有关信息
    clusterLink *link;
    
    // ...
}
```

其中link属性是有个clusterList结构，保存了连接节点需要的相关信息：

```c
typedef struct clusterLink {
    // 连接创建时间
    mstime_t ctime;
    // TCP套接字描述符
    int fd;
    // 输出缓冲区，保存等待发送个其他节点的消息
    sds sndbuf;
    // 输入缓冲区，保存其他节点接收的消息
    sds rcvbuf;
    // 与这个连接相关联的节点，如无置NULL
    struct clusterNode *node;
} clusterLink;
```

redisClient与clusterNode的异同：redisClient和clusterNode都有自己的套接字描述符和输入、输出缓冲区；但redisClient的套接字和缓冲区用于连接客户端，clusterLink结构中的套接字和缓冲区则是用于连接节点。

下图展示了节点7000创建的clusterState结构

![image-20210605104037969](https://gitee.com/tobing/imagebed/raw/master/image-20210605104037969.png)

+ currentEpoch=0表示集群当前纪元为0
+ size=0表示集群当前没有任何节点在处理槽，对应state属性
+ state=REDIS_CLUSTER_FAIL表示当前集群当前属于下线状态
+ nodes记录了集群目前包含三个节点，分别由是三个clusterNode结构表示
+ myself指向7000的clusterNode节点

类似地，7001与7002也有类似的结构。

**CLUSTER MEET命令实现**

通过`CLUSTER MEET <ip> <port>`命令可以让一个节点A将另一个节点B添加到当前所在集群中。此时节点A将会和节点B进行握手，来确认比起的存在，并未接下来的通信做好基础：

1. 节点A为节点B创建一个clusterNode结构，并将结构添加到clusterState.node字典中
2. 节点A根据CLUSTER MEET命令给定的IP地址端口号向节点B发送一条MEET消息
3. 如果一切顺利，节点B将收到节点A发送的MEET消息，节点B将会为节点A创建一个clusterNode结构并将其添加到自身的clusterState.node中
4. 节点B向节点A返回一条PONG消息
5. 如果一切顺利，节点A收到节点B的PONG消息，通过这条PONG消息可以知道节点B已经成功接收到自己发送的MEET消息
6. 节点A先节点B返回一条PING消息
7. 如果一切顺利，节点B收到节点A返回的PING消息，进而知道节点A成功接收到自己的PONG消息，握手成功。

![image-20210605105413321](https://gitee.com/tobing/imagebed/raw/master/image-20210605105413321.png)

**之后节点A与节点B的信息通过[Gossip协议](# 补充:Gossip算法)传播给集群中的其他节点，让其他节点也与节点B握手，最终，一段时间之后，所有节点都会与节点B认识。**

#### 槽指派

Redis集群通过分片方式来保存数据库中的键值对：集群的整个数据库被分为16384个槽，数据库中每个键都属于一个槽的一个，集群中的每个节点可以处理0个或最多16384个槽。

当数据库中的16384个槽都有节点在处理，集群处于上线状态；想法，如果没有一个槽得到处理处理，那么集群处于下线状态。

可以通过`CLUSTER ADDSLOTS <slot> [slot ...]`命令为当前连接的节点派发若干个槽。

槽的信息由clusterNode结构的slots属性和numslot属性记录。

+ slots是有个二进制位数组，长度为16384/8=2048字节。
+ Redis以0为其实索引，16383为终止索引，对16384个位编号，若干为0表示不处理槽i，为1表示处理槽i。
+ 原因是位数组，因此可以以O(1)时间复杂度访问任意二进制位，来快速检查当前是否负责处理某个槽，同理，指定槽的时候类似。
+ numslot属性记录了及诶单那负责处理的槽的数量。

![image-20210605110618191](https://gitee.com/tobing/imagebed/raw/master/image-20210605110618191.png)

一个节点除了会将自己出来的槽记录在clusterNode结构的slots属性和numslot属性，还会将自己的slots数组同消息发送给集群中的其他节点，告知自己正在处理的槽。

当一个节点A收到另一个节点B的slots数组信息时，节点A会在clusterState.node属性节点B对一个拿到clusterNode中记录到对一个的slots数组中。这样一来，集群中的所有节点都可以知道这16384个槽都分配给了那些节点

但是通过这种方式来记录槽的分配信息，每次在找一个槽的时候，就需要变量集群中的所有clusterNode，依次判断对于的slots数组的指定位置是否为1，这样在集群节点较多时会影响效率。

为此在每个节点的clusterState结构中的slots数组记录了集群中所有16384个槽的指派信息，每个槽指向的是有个clusterNode结构的指针：

+ 如果slots[i]指针为NULL，表示槽i尚为指派给任何节点
+ 如果slots[i]指针为一个clusterNode指针，点槽i分配给了该clusterNode结构代表的节点

![image-20210605111905148](https://gitee.com/tobing/imagebed/raw/master/image-20210605111905148.png)

通过clusterState.slots数组，可以O(1)时间复杂度快速找到对应的clusterNode，进而找到对应对应的节点。

可以看出clusterState.slots与clusterNode.slots是采用空间换时间。一方面clusterState.slots可以实现快速定位槽对应的节点；另一方面clusterNode.slots可以快速给其他节点发送自己的管理的槽信息

**CLUSTER ADDSLOTS命令实现**

CLUSTER ADDSLOTS的伪代码如下：

```python
def CLUSTER_ADDSLOTS(*all_input_slots):
    # 遍历所有输入槽检查是否都是未指派槽
    	# 如果存在一个槽以及被纸盘到某个节点，马上向客户端返回错误，终止执行
        
    # 如果所有输入都是未指派槽，再次遍历所有槽，依次分配给当前节点
    	# 设置clusterState结构slots数组，将slots[i]指向当前节点代表的clusterNode
        # 访问当前节点的clusterNode结构的slot数组，将数组索引[i]的二进制更新为1
```

![image-20210605113150521](https://gitee.com/tobing/imagebed/raw/master/image-20210605113150521.png)

#### 在集群中执行命令

当数据库的16384个槽全部指派之后，集群会进入上线状态，这时客户端可以向集群中的节点发送数据命令。集群中的节点接收到命令之后，先计算键所在的槽，在判断该槽是否属于当前节点。如果属于当前节点，则由当前节点执行命令；如果不属于当前节点，根据clusterState.slots找到所在节点，先客户端返回一个MODED错误，并指引客户端转向正确的节点，再次发送要会执行的命令。

![image-20210605113911017](https://gitee.com/tobing/imagebed/raw/master/image-20210605113911017.png)

**键所在槽的运算**

具体过程分为两大步：

1. 先根据键值对的key，按照CRC16算法计算一个16bit的值；
2. 然后再用这个16bit值对16384取模，得到0~16384范围的模式，每个模数代表一个相应编号的哈希槽。

**判断槽是否由当前节点处理**

通过判断clusterState.slots[i]

+ clusterState.slots[i]==clusterState.myself，当前负责，执行命令
+ clusterState.slots[i]!=clusterState.myself，非当前负责，根据执行的clusterNode获取IP与端口，量客户端返回MOVED错误，指引客户端转向正在处理槽的节点

**MOVED错误**

格式为：`MOVED <slot> <ip>:<port>`

![image-20210605114443317](https://gitee.com/tobing/imagebed/raw/master/image-20210605114443317.png)

一个集群客户端通常会与集群中的多个节点创建套接字连接，所谓的转向仅仅是换一个套接字发送命令。需要注意MOVED指向是对应用户透明的，转向的过程是自定执行的。

**节点数据库实现**

集群节点保存键值对以及键值对过期时间的方式，与单机Redis服务器的方式完全相同。

节点与单机服务器在数据库的区别是，节点只能使用0号数据库，单机Redis服务器则没有这个限制。

需要注意，键值对除了保持在数据库中，节点还会用clusterState.slots_to_keys老保存槽和键的关系。

slots_to_keys是一个跳跃表，每个分值都是一个槽号，每个节点的成员那都是一个数据库键：

+ 当节点要往数据库中添加新的键值对时，节点会将这个键以及键的槽号关联到slots_to_keys中
+ 当节点要删除数据库中的某个键值对时，节点会在slots_to_keys跳跃表接触被删除键与槽号的关联

<font style="color:red">通过跳跃表slots_to_keys记录数据库键所属的槽，可以方便对属于某个或某些槽的所有数据库键进行批量操作。</font>如命令`CLUSTER GETKEYSINSLOT <slot> <count>`命令可以返回最多count个属于槽slot的数据库键，这个命令就是通过遍历slots_to_keys实现。

#### 重新分片

**Reids集群的重新分片操作可以将任意数量已经分配给某个节点的槽改为指派给另一个节点，并且相关槽所属的键值对也会从源节点移动到目标节点。**

重新分片可以在线执行，重新分片过程中，集群不需要下线，而且源节点和目标节点可以继续处理命令请求。

重新分片的目的主要是：键与槽的转换可能会导致某些槽存在的键特别多，而且这些槽集中在某一个节点。为了降低某个节点的压力，合理利用机器性能，因此考虑重写分片。

Redis集群的重新分片有Redis的集群管理软件redis-trib负责执行，Redis提供了进行重新分片的所有命令，而redis-trib则通过向源节点与目标节点发送命令来进行重写分片操作。

redis-trib对集群的单个槽重写分片步骤如下：

1. redis-trib对目标节点发送`CLUSTER SETSLOT <slot> IMPORTING <source_id>`命令，让目标节点准备好从源节点导入（import）属于槽slot的键值对
2. redis-trib对源节点发送`CLUSTER SETSLOT <slot> MIGRATING <target_id>`命令，让源节点准备哈将属于槽slot的键值对迁移（migrate）到目标节点
3. redis-trib对源节点发送`CLUSTER GETKEYSINSLOT<slot> <count>`命令，获取最多count属于槽slot的键值对的键名
4. 对于步骤3获得的每一个键名，redis-trib对源节点发送一个`MIGRATE <traget_ip> <targe_port> <key_name> 0 <timeout>`命令，将选的键原子性地从源迁移到目标节点
5. 重复步骤3和4，直至保存的所有属于槽的键值对都被迁移到目标节点为止
6. redis-trib向集群中的任意节点发送`CLUSTER SETSLOT <slot> NODE <target_id>`命令，将槽slot指派给目标节点，这一信息通过消息发送给整个集群，最终所有节点都知道槽以及分配给目标节点

![image-20210605121257081](https://gitee.com/tobing/imagebed/raw/master/image-20210605121257081.png)

![image-20210605121324822](https://gitee.com/tobing/imagebed/raw/master/image-20210605121324822.png)

#### ASK错误

重新分片期间，源节点向目标节点迁移一个槽的过程中，可能出现这种情况：属于被迁移槽的一部分键值对保存源节点，另一部分保存在目标节点。

当客户端向源节点发送一个源数据库键有关的命令，并且命令要处理的数据库恰好就属于正在迁移的槽：

+ 源节点先在自己数据库查找指定键，找到直接返回
+ 如果没找到，说明键可能已经被迁移到目标节点，源节点先客户端返回一个ASK，指引客户端到正在导入槽的目标节点，再次发送之前的命令

需要注意的时，接到ASK错误之后不会打印错误，而是自动进行转向，对用户是透明的。

**CLUSTER SETSLOTMIGRATING命令实现**

clusterState.migrating_slots_to数组记录了当前节点正在迁移到其他节点的槽。如果migrating_slots_to[i]不是NULL，而是一个clusterNode结构，表示当前节点正将槽i迁移到clusterNode代表的节点。

在对集群进行重新分片时，向源节点发送命令：`CLUSTER SETSLOT <i> MIGRATING <target_id>`可以将源节点clusterState.migrating_slots_to[i]的值设置为target_id代表的clusterNode结构。

**ASK错误**

如果节点收到一个ASK错误命令请求，并且键key所属的槽i正好指派个这个节点，那么节点会尝试在自己的数据库中查找键key，如果找到直接执行客户端发送的命令。

反之，如果在自己数据库没有找到key，会检查自己的clusterState.migrating_slots_to[i]判断可以所属的槽i是否正在迁移，如果迁移会向客户端返回一个错误，引导客户端正确在导入槽i的节点去查找key。

接到ASK错误命令的客户端会先根据错误提供的IP和端口，转向正在导入槽的目标节点，然后会先给目标节点发送一个ASKING命令，之后在重新发送原本要执行的命令。

![image-20210605123036306](https://gitee.com/tobing/imagebed/raw/master/image-20210605123036306.png)

![image-20210605123025858](https://gitee.com/tobing/imagebed/raw/master/image-20210605123025858.png)

**ASKING命令**

ASKING命令的唯一作用是打开发送该命令的客户端的REDIS_ASKING标识。

一般情况下，如果客户端向节点发送一个关于槽i的命令，但是caoi有没有指派给整个节点，那么节点将会先客户端返回一个MOVED错误；但是如果节点在clusterState.migrating_slots_from[i]，表示节点正在导入槽i，并且发送命令的客户端带有REDIS_ASKING标识，那么节点会破例执行关于槽i的命令一次。

![image-20210605123403795](https://gitee.com/tobing/imagebed/raw/master/image-20210605123403795.png)

从上面我们可以看到发送ASKING才能访问正在迁移的槽，同时ASKING命令是一次性的，一但节点执行了一次带有REDIS_ASKING标识的客户端发送命令，客户端的REDIS_ASKING标识就会被移除。

**ASK错误与MOVED错误的区别**

ASK错误和MOVED错误都会导致客户端转向，区别在于：

+ MOVED错误代表槽的负责权已经从一个节点转移到另一个节点：客户端在收到槽i的MOVED错误，以后每次关于槽i的请求都会到MOVED指定的节点执行。
+ ASK错误只是两个节点在迁移槽的过程中使用的一种临时措施：客户端收到槽i的ASK错误之后，客户端会在接下来的一次命令请求中将关于槽i的命令请求发送到ASK错误执行的节点，但这种转向是临时的，不会对今后访问槽i造成影响。客户端在下次请求槽i时仍然会访问目前处理槽i的节点，除非再次返回ASK错误。

#### 复制与故障转移

Ridis集群中的节点分为主节点与从节点，其中主节点用于处理槽，从节点用于复制某个主节点，并在复制的主节点下线时，代替下线主节点继续处理命令请求。

**设置从节点**

先一个从节点发送命令：`CLUSTER REPLICATE <node_id>`，可以让接收命令的节点会成为node_id指定节点的从节点，并开始对主节点进行复制：

+ 收到命令的节点先在自己的clusterState.nodes字典中重大node_id对应的clusterNode结构，并将自己的clusterState.myself.salveof指针执行这个结构，以此来记录这个节点正在复制的主节点
+ 这个节点会修改自己咋clusterState.myself.falgs中的属性，关闭原本REDIS_NODE_MASTER标识，打开REDIS_NODE_SALVE标识，表示这个节点已经有原来的主节点变为从节点。
+ 最后节点会调用复制代码，根据clusterState.myself.salveof指向的clusterNode保存的IP和端口，对主节点进行复制。复制过程与单机模式下类似。因此让从节点发送命令`SALVEOF <master_ip> <master_port>`

一个节点成为了从节点，并开始复制某个主节点这一信息会同消息发送给集群中的所有其他节点，最终集群的所有节点都会知道这一事实。

集群中的所有节点都会在代表主节点的clusterNode结构的salves属性和numsalves属性中记录正在复制这个节点的从节点名单。

**故障检测**

集群中每个节点都会定期向集群转给你其他节点发送PING消息，以此来检测对方是否在线，如果接收PING消息的节点没有在规定的时间内返回PONG消息，那么发送PING消息的节点将会将PING消息的节点标记为疑似下线。

集群的各个节点会通过相互发送消息的方式来交换集群中各个节点的状态信息，例如某个节点是处于在线状态、疑似下线状态，还是已下线的状态。

当一个主节点A通过消息得知主节点B认为节点C进入了疑似下线状态时，主节点A会在自己的clusterState.node字典中找到主节点C对应的clusterNode结构，并将主节点B的下线报告添加到clusterNode结构的fail_reports链表中。

每一个下线报告由一个clusterNodeFailReport结构表示。

```c
struct clusterNodeFailReport{
    // 报告目标节点已经下线的节点
    struct clusterNode *node;
    // 最后一次从node节点收到下线报告的时间，程序使用这个时间戳来检查下线报告是否过期
    mstime_t time;
}
```

![image-20210605153214620](https://gitee.com/tobing/imagebed/raw/master/image-20210605153214620.png)

当一个集群中半数以上的节点将某个节点标记为疑似下线，那么这个个主节点，将会给被标记为已下线，并且向集群广播该节点下线的消息，所有收到这条消息的节点都会立即将节点标记为已下线。

**故障转移**

当一个节点发现你自己正在复制的主节点以及进入下线状态，从节点就开始对下线主节点进行故障转移，步骤如下：

1. 复制下线主节点的所有从节点中，会有一个从节点被选中；
2. 被选中的从节点会执行SLAVEOF no one命令，会成为新的主节点；
3. 新的主节点会撤销所有对已下线主节点的槽指派，并将这些槽全部指派给自己；
4. 新的主节点先集群广播一条PONG消息，这个PONG消息可以让集群中的其他节点立即知道这个节点已经由从节点变成了主节点，并且这个主节点已经接管了原本由已下线节点负责处理的槽；
5. 新的主节点开始接受和处理槽有关的命令请求，故障转移成功。

**选举新的主节点**

集群中的新的主节点是通过选举产生的，流程如下：

1. 集群的配置纪元是有一个自增计数器，初始值为0；
2. 对集群里的某个节点开始一场故障转移操作时，集群配置纪元的值会增加1；
3.  对于每个配置纪元，集群里每个负责处理槽的主节点都有一次投票的机会，第一个向主节点要求投票的从节点将获得主节点的投票；
4. 当从节点发现自己正在复制的主节点已进入已先下线状态时，从节点会向集群广播一条CLUSTERMSG_TYPE_FAILOVER_AUTH_REQUEST消息，安全所有这条消息、并且具有投票权的朱接地那先做个从节点投票；
5. 如果一个主节点具有投票权（负责处理槽），并且这个主节点尚未投票给其他从节点，那么主节点将想要求投票的从节点返回一条CLUSTERMSSG_TYPE_FAILOVER_AUTU_ACK消息，表示这个主节点支持从节点成为新的主节点；
6. 每个参与选举的从节点都会接收到CLUSTERMSSG_TYPE_FAILOVER_AUTU_ACK消息，并根据自己是收到了多少条消息来统计自己获得了多少主节点的支持。
7. 如果集群中有N个具有投票权的朱接地那，那么当一个从节点收集到大于或等于（N/2）+1张支持票时，这个从节点就会当选为新的主节点。
8. 因为在每一个配置纪元里面，每个具有投票权的主节点只有一次投票权，因此N个主节点进行投票，最多只有一个拥有大于等于（N/2）+1张票的从节点，确保了新的从节点只有一个。
9. 如果一个配置纪元中没有从节点能收集到足够多的支持票，那么集群进入一个新的配置纪元，重新进行选举，直到选出新的主节点为止。

上述选举新主节点的方式与先进Sentinel Leader相似，因为两者都是基于Raft算法实现。

#### 消息

汲取中各个节点通过发送和接收消息来进行通信，称发送消息的节点为发送者，接收消息的节点为接收者。

节点发送的消息类型有以下五种：

+ MEET消息：当发送者接收到客户端发送的CLUSTER MEET命令时，发送者将会向接收者发送MEET消息，请求接受者加入到发送者当前在的集群中。
+ PING消息：集群里的每个节点默认每个1S就会从已知的列表随机选出五个节点，然后对这个5个节点中最长时间没有发送给PING消息的节点发送PING消息，一次来检测被选中的节点是否在线。除此之外，如果节点A最后一次收到节点B发送的PONG消息的时间，距离当前时间已经超过节点A的cluster-node-timeout选项设置时长的一半，那么节点A也会向节点B发送PING消息，这可以防止节点A因为长时间没有随机选中节点B作为PING消息的发送对象而导致对节点B的消息更新滞后。
+ PONG消息：当接收者收到发送者发来的MEET消息或者PING消息时，为了向发送者确认这条MEET消息或PING消息已经达到，接收者会先发送者返回一条PONG消息。另外两一个及诶单那也可以通过向集群广播自己的PONG消息来让集群其他的节点垃圾刷新关于这个节点的认识，例如当一次故障转移操作完成，新的主节点会向集群广播一条PONG消息，以此来让集群中的其他节点立即知道这个节点已经变成了主节点，并且接管了已下线节点负责的槽。

+ FAIL消息：当一个主节点A判断另一个主节点B已经进入FAIL状态时，节点A会向集群广播一条关于节点B的FAIL消息，所有收到这条消息的节点都会立即将节点B标记为已下线。
+ PUBLISH消息：当节点接收到一个PUBLISH命令时，节点会执行这个命令，并向集群广播一条PUBLISH消息，所有接收到PUBLISH消息的节点都会执行相同的PUBLISH命令。

**消息头**

节点发送的所有消息都由一个消息头包裹，消息头除了包含消息正文之外，还记录了消息发送者自身的一些消息，因为这些消息也会被消息接收者收到，因此可以认为消息头也是消息的一部分。

消息头的结构如下：

```c
typedef struct {
    // 消息的长度
    uint32_t totlen;
    // 消息的类型
    uint16_t type;
    // 消息正文包含的节点信息数量,只发送MEET、PING、PONG三种Gossip协议消息时使用
    uint16_t count;
    // 发送者所在的配置纪元
    uint16_t currentEpoch;
    // 如果发送者是一个主节点，此处记录的是发送者的配置纪元
    // 如果发送者是一个从节点，此处记录的是发送者正在复制的主节点的配置纪元
    uint64_t configEpoch
    // 发送者名字
    char sender[REDIS_CLUSTER_NAMELEN];
    // 发送者目前的槽指派的消息
    unsigned char myslots[REDIS_CLUSTER_SLOTS/8];
    // 如果发送者是一个主节点，此处记录的是发送者正在复制的主节点的名字
    // 如果发送者是一个从节点，此处记录的是REDIS_NODE_NULL_NAME
    char slaveof[REDIS_CLUSTER_NAMELEN];
    // 发送者端口
    uint16_t port;
    // 发送者的标识
    uint16_t flags;
    // 发送者所处集群的状态
    unsigned char state;
    // 消息的正文（或者说，内容）
    union clusterMsgData data;
    
} clusterMsg;
```

clusterMsgData属性执行联合体，里面是消息的正文：

```c
union clusterMsgData {
	// MEET、PING、PONG消息正文
    struct {
        // 每条MEET、PING、PONG消息包含两个clusterMsgDataGossip结构
        clusterMsgDataGossip gossip[i];
    }ping;
	//FAIL消息正文
    struct {
        clusterMsgDataFail about;
    }fail;
	// Publish消息正文
    struct {
        clusterMsgDataPublish gossip[i];
    }publish;
    // ... 其他消息正文部分
};
```

clusterMsg结构的currentEpoch、sender、myslots等属性记录了发送者自身的节点信息，接收者会根据这些信息，在自己的clusterState.nodes字典里找到发送者对应的clusterNode结构，并对结构进行更新。
举个例子，通过对比接收者为发送者记录的槽指派信息，以及发送者在消息头的myslots属性记录的槽指派信息，接收者可以知道发送者的槽指派信息是否发生了变化。
又或者说，通过对比接收者为发送者记录的标识值，以及发送者在消息头的flags属性记录的标识值，接收者可以知道发送者的状态和角色是否发生了变化，例如节点状态由原来的在线变成了下线,或者由主节点变成了从节点等等。

**MEET/PING/PONG消息的实现**

<font style="color:red">Redis集群中的各个节点通过[Gossip协议](# 补充:Gossip算法)来交换各个节点的状态信息，其中Gossip协议有MEET/PING/PONG三种消息实现，这三种消息的都由两个clusterMsgDataGossip结构组成。</font>MEET/PING/PONG三种消息都使用相同的消息正文，因此节点通过消息头的type属性来判断一条消息具体是哪种。

每次发送MEET/PING/PONG消息时，发送者都从自己的已知节点列表中随机选出两个节点，并将这两个被选中的节点的信息分别保存到两个clusterMsgDataGossip结构中。clusterMsgDataGossip结构记录了被选中节点的名字，发送者与被选中节点最后一次发送和接收PING消息和PONG消息的时间戳，被选中的及诶单那的IP地址和端口号，以及被选中节点的标识值。

当接收者收到MEET/PING/PONG三种消息时，接收者会方哪位消息正文中的两个clusterMsgDataGossip结构，并根据自己是否认识clusterMsgDataGossip结构中被记录的选中节点来选择进行那种操作：

+ 如果选中节点不存在于接收者的已知节点列表，说明接收者是第一次接触到被选中节点，接收者将根据结构中记录的IP地址和端口号，与被选中节点进行握手。
+ 如果选中节点已存在于接收者的已知节点列表，说明接收者之前已经与被选中节点进行过接触，接收者将根据clusterMsgDataGossip结构记录的信息，对被选中节点对应的clusterNode结构进行更新。

**FAIL消息的实现**

当集群中的主节点A将主节点B标记为已下线时，主节点A将向集群广播一条关于主节点B的FAIL消息，所有接收到这条FAIL消息的节点都会讲主节点B标记为已下线。

在集群节点数量较大时，单纯通过Gossip协议来传播节点的已下线信息会给节点的信息更新带来一定的延迟，因为Gossip协议消息通常需要一段时间才能传播到整个集群，而发送FAIL消息可以让集群所有节点立即知道某个主节点已下线，从而尽可能快判断是否需要将集群标记为下线，又或者对线下主节点进行故障转移。

因为集群的节点的名字独一无二，因此FAIL只需要保存下线节点的名字，接收到消息的节点就可以根据这个名字来判断是哪个节点下线了。

**PUBLISH消息实现**

当客户端向集群中的某个节点发送命令：`PUBLISH <channel> <message>`时，接收到PUBLISH命令的节点不仅会先channel频道方消息message，还会向集群广播一条PUBLISH消息，所有接收到消息的节点都会先channel频道发送message消息。

PUBLISH消息正文由clusterMsgDataPublish结构表示：

```c
typedef struct {
    // channel参数长度
    uint32_t channel_len;
    // message参数长度
    uint32_t message_len;
    // 字节数组，保存了客户端通过PUBLISH命令发送给节点的channel参数和message参数
    // 定义为8字节是用于对其其他消息结构
    unsigned char bulk_data[8];
} clusterMsgDataPublish;
```

如执行`PUBLISH "new.it" "hello"`

![image-20210605180020327](https://gitee.com/tobing/imagebed/raw/master/image-20210605180020327.png)

之所以不采用广播方式让所有节点都执行相同的PUBLISH命令，是因为这种做法不符合Redis集群的“各个节点通过发送和接收消息来进行通信”这一规则。

#### 总结

+ 节点通过握手来将其他节点添加到自己所处的集群当中。
+ 集群中的16384个槽可以分别指派给集群中的各个节点，每个节点都会记录哪些槽指派给了自己，哪些槽指派给了其他节点。
+ 节点在接到一个命令请求，会先检查这个命令请求要处理的键所在槽是否有自己负责，如果不是，节点将向客户端返回一个MOVED错误，MOVED错误携带的信息可以指引客户端转向正在负责相关槽的相关节点。
+ 对Redis集群重新分片工作是由redis-trib负责执行，重新分片的关键是将属于某个槽的所有键值对从一个节点转移到另一个节点。
+ 如果节点A正在迁移槽i到节点B，那么当节点A没能在自己的数据库中找到命令指定的数据库键值对时，节点A会向客户端返回一个ASK错误，指引客户端到节点B进行查找指定的数据库键。
+ MOVED错误表示槽的负责人以及从一个节点转移到另一个节点，而ASK错误只是两个节点在迁移槽的过程中使用的一种临时措施。
+ 集群里的从接地那用于复制主节点，并在主节点下线时，代替主节点继续处理命令请求。
+ 集群中的节点通过发送和接收消息进行通信，场景的消息包括MEET/PING/PONG/PUBLISH/FAIL五种。

#### 补充:Gossip算法

A **gossip protocol** is a procedure or process of computer peer-to-peer communication that is based on the way [epidemics](https://en.wikipedia.org/wiki/Epidemic) spread.Some [distributed systems](https://en.wikipedia.org/wiki/Distributed_computing) use peer-to-peer gossip to ensure that data is disseminated to all members of a group.

Gossip是一种基于流行病传播路径的计算机点对点交流的方式的程序。一些分布式系统中使用点对点的gossip 来确保数据最终会被扩散到组内的所有节点。



















































