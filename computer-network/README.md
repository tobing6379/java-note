# 计算机网络

不闻不若闻之，闻之不若见之，见之不若知之，知之不若行之。--荀子

## 网络编程

高性能网络编程，第一就是理解网络协议，并在这基础上和内核配合，感知各种网络I/O事件；第二就是学会使用线程处理并发。

### 数据报与字节流

TCP，字节流套接字（Stream Socket），一般使用「SOCK_STREAM」表示。Stream Socket是可靠的，双向连接的通信串流，通过诸如连接管理、拥塞控制、数据流与窗口管理、超时重传等一系列设计提供高质量的端到端的通信方式。

UDP，数据报套接字（Datagram Socket），一般使用「SOCK_DGRAM」表示。UDP的优点就是速度快，使用于广播或多播场景。UDP也可以通过应用程序来保证可靠性，如对报文进行编号，设计Request-Ack机制，再加上重传等。

### 客户端-服务器模型

目前主流的网络请求基本是基于客户端-服务器模型开展。在客户端-服务器端网络编程中，客户端和服务器工作的逻辑如下。

![image-20210903000127608](https://gitee.com/tobing/imagebed/raw/master/image-20210903000127608.png)

**服务器**

服务器端需要在客户端发起连接请求前完成初始化。服务器端初始化过程如下：

1. 首先初始化Socket；
2. 执行bind函数将服务绑定到一个众所周知的地址和端口上；
3. 服务器执行listen函数，将原先的Socket转换为服务端的Socket；
4. 最后服务器阻塞在accept上等待客户端连接；

**客户端**

在服务端准备就绪，客户端可以连接到服务器。服务端连接过程如下：

1. 客户端初始化Socket；
2. 执行connect向服务器的地址和端口发送连接请求（服务器的地址和端口是众所周知的）；

**连接完成**

上述过程是TCP三次握手，一旦三次握手完成，客户端和服务器建立连接，可以进入数据传输过程。数据传输的流程如下：（一旦连接完成，数据的传输是双向的）

1. 客户端进程会向操作系统内核发起write字节流写操作；
2. 内核协议栈将字节流通过网络设备传输到服务器端；
3. 服务器从内核得到信息，将字节流从内核读到进程中；
4. 服务器开始业务逻辑处理，处理完成将结果以同样的方式写回客户端；
5. 客户端完成与服务器端交互，会执行socket函数与服务器端断开连接；
6. 操作系统内核通过原先的连接链路先服务器端发送一个FIN包；
7. 服务器端收到客户端发送来的FIN，执行被动关闭；【此时整个链路处于半关闭】
8. 之后服务器端页执行close函数，整个链路真正关闭。

关闭状态下，发起close请求的一方**在没收到对方FIN包之前都认为连接是正常**的；在全关闭状态下，双方都感知连接已经关闭。

在上述过程中，Socket是用于建立连接，传输数据的唯一途径。

### Socket

**Socket，套接字，表示可以通过插口接入的方式，快速完成网络连接和数据收发。**通过Socket概念，可以屏蔽底层协议栈的差别。

#### Socket地址格式

使用套接字时，需要解决通信双方寻址的问题。只有通过地址才能找到对应的主机进行连接的建立。套接字的地址主要有三种格式。

**通用套接字地址格式**

```c
// POSIX.1g 规范规定了地址族为2字节的值
typedef unsigned short int sa_family_t;
// 描述通用套接字地址
struct sockaddr{
    // 地址族（16bit）：表示用什么样的方式保存地址和解释地址
    // AF_LOCAL：本地地址，一般用于本地Socket通信
    // AF_INET：因特网，使用IPv4地址
    // AF_INET6：因特网，使用IPv6地址
    sa_family_t sa_family;
    // 具体地址值（112bit）
    char sa_data[14];
};
```

> AF表示Address Family；PF表示Protocol Family，协议族。

**IPv4套接字格式地址**

```c
// IPV4 套接字地址（32bit）
typedef uint32_t in_addr_t;
struct in_addr {
    in_addr_t s_addr;
};

// 描述IPV4的套接字地址格式
struct sockaddr_in {
    // 固定AF_INET(16bit)
    sa_family_t sin_family;
    // 端口（16bit），范围是0~65535
    // 0~1024，用于特殊进程
    // >5000，用于非特殊进程
    in_port_t sin_port;
    // Internet address（32bit）
    struct in_addr sin_addr;
    // 占位，不做实际用处
    unsigned char sin_zero[8];
};
```

**IPv6套接字格式地址**

```c
truct sockaddr_in6 {
    // 固定AF_INET6(16bit)
    sa_family_t sin6_family;
    // 传输端口（同IPV4）
    in_port_t sin6_port;
    // IPv6流控信息（32bit）
    uint32_t sin6_flowinfo;
    // IPv6地址（128bit）
    struct in6_addr sin6_addr;
    // * IPv6域ID（32bit）
    uint32_t sin6_scope_id;
};
```

**AF_LOCAL**

上述的IPv4和IPv6都是因特网套接字，除此之外还有一种本地套接字格式，用于作为本地进程间的通信。

```c
struct sockaddr_un {
    // 固定为AF_LOCAL
    unsigned short sun_family;
    // 路径名
    char sun_path[108];
};
```

> 由于本地Socket实际上是基于文件操作，因此只需要根据文件路径便可以区分，无需端口信息。

#### 三次握手详细过程











