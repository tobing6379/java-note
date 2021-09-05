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

![image-20210904150428233](https://gitee.com/tobing/imagebed/raw/master/image-20210904150428233.png)

#### 三次握手详细过程

**服务端**

TCP三次握手中，服务器端需要在客户端连接之前，创建套接字、绑定地址信息、开始监听、接收客户端的连接。

> 函数原型详解

【参加套接字】

```c
// domain：何种套接字，如PF_INET、PF_INET6等
// type：套接字类型，如SOCK_STREAM(TCP)、SOCK_DGRAM(UDP)、SOCK_RAW(原始套接字)
// protocol：原本指定通信协议，现在基本废弃，一般写成0
int socket(int domain, int type, int protocol);
```

【绑定地址信息】

```c
// fd：
// addr：接收通用地址格式，实际困难是IPv4、IPv6或本地套接字，根据len判断addr怎么解析
// len：传入的地址长度，可变值
bind(int fd, sockaddr * addr, socklen_t len);

// 对于使用者，使用时按照如下使用
struct sockaddr_in name;
bind(sock, (struct sockaddr *)&name, sizeof (name);
// 对于实现者，实现时根据地质结构前2个字节判断是那种地址，使用len处理长度可变的结构
// 执行bind时，对地址和端口可以有多种处理方式。
// 如把地址设置为本机IP地址，相当于告诉操作系统内核，仅对目标IP是本机IP地址的IP包处理。如果不清楚应用程序部署在那台计算机，可以使用通配地址。
// 对于 IPv4 的地址来说，使用 INADDR_ANY 来完成通配地址的设置；
// 对于 IPv6 的地址来说，使用 IN6ADDR_ANY 来完成通配地址的设置。
struct sockaddr_in name;
name.sin_addr.s_addr = htonl (INADDR_ANY); 
// 处理地址还需要设置端口。
// 如果把端口设置为0，相当于把端口选择权交给操作系统内核，根据算法选择一个空闲端口，完成套接字绑定
// 对于服务端，通常会将端口指定为众所周知的端口，以便于客户端可以顺利连接到服务器
```

【开始监听】

执行完bind函数，创建好的套接字与地址实现了关联。接下来便执行listen函数让服务器真正处于可接听状态。前面初始化 的套接字，可以认为是有个「主动」的套接字，目的是之后主动发起请求；通过listen可以将「主动」的套接字转换为「被动」的套接字，告诉系统内核，**这个套接字是用于等待用户用户请求的**。

```c
// socketfd：套接字描述符
// backlog：未完成连接队列的大小，其大小决定了可以接收的并发数目
int listen (int socketfd, int backlog);
```

【接收连接】

当客户端的连接请求到达时，服务器端应答成功，连接建立，这个时候操作系统内核需要把这个事件通知到应用程序，并让应用程序感知到这个连接。  

accept 函数的作用就是连接建立之后，操作系统内核和应用程序之间的桥梁。

```c
// listensockfd：listen的套接字
// cliaddr：客户端地址信息
// addrlen：地址大小
int accept(int listensockfd, struct sockaddr *cliaddr, socklen_t *addrlen);
```

需要注意，到目前为止存在两个套接字描述字：

+ 监听套接字描述字：一直存在，为多个客户端服务，直到关闭；
+ 已连接套接字描述字：一旦客户端和服务器连接成功，操作系统内核为这个客户端生成一个**已连接套接字**，应用服务器使用这个**已连接套接字**和客户进行通信处理。当连接处理完成，TCP连接释放，已连接套接字也会随之关闭。

>创建套接字的详细过程

```c
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
// 创建套接字
int make_socket (uint16_t port) {
    int sock;
    struct sockaddr_in name;
    // 创建字节流类型的 IPV4 socket
    sock = socket (PF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        perror ("socket");
        exit (EXIT_FAILURE);
    }
    // 绑定到 port 和 ip
    name.sin_family = AF_INET;                  // IPV4
    name.sin_port = htons (port);               // 指定端口
    name.sin_addr.s_addr = htonl (INADDR_ANY);  // 通配地址
    // 把 IPV4 地址转换成通用地址格式，同时传递长度
    if (bind (sock, (struct sockaddr *) &name, sizeof (name)) < 0) {
        perror ("bind");
        exit (EXIT_FAILURE);
    }
    // 返回创建好的socket
    return sock
}
```



**客户端**

客户端连接到服务器需要经过：创建套接字、向服务器发起请求等过程。

【创建套接字】

与服务器端一样。

【先服务器端发送请求】

客户端与服务器端的连接建立通过connect函数完成。

```c
// sockfd：连接套接字
// servaddr：套接字地址结构
// addrlen：结构的大小
int connect(int sockfd, const struct sockaddr *servaddr, socklen_t addrlen);
```

客户端调用connect是可以不调用bind，因此操作系统内核可以确定源IP地址，并安装一定算法选择一个临时端口作为源端口。

如果连接套接字时TCP套接字，会调用connect激发TCP的三次握手过程，只有在连接建立成功或出错才会返回。出错返回主要包含以下情况：

1. 三次握手无法建立，客户端发送的SYN包没有任何响应，返回TIMEOUT。（通常是IP地址写错）
2. 客户端收到RST回答，客户端会立即返回CONNECTION REFUSED错误。（目的地址的目的端口SYN到达，但该端口没有在监听的服务器；TCP想取消一个已有连接；TCP接收到一个根本不存在的连接上的节点）
3. 客户端发出的SYN包在网络引起了「destination unreachable」。（通常是客户端与服务器端路由不通）



**三握手流程**

目前使用的网络编程模式是阻塞式，即调用发起后不会直接返回，由操作系统内核处理之后才会返回。

![image-20210903163935475](https://gitee.com/tobing/imagebed/raw/master/image-20210903163935475.png)

连接刚开始，**服务器端**通过socket、bind和listen完成被动套接字准备工作，然后调用accept阻塞等待客户端连接；**客户端**通过调用socket和connect函数之后，会阻塞等待操作系统内核完成接下来任务，具体如下：

1. 客户端协议栈先服务器发送SYN包，序列号为j；【客户端-SYN_SENT】
2. 服务器的协议栈收到这个包，向客户端进行ACK应答，应答值为j+1，表示对SYN包j的确认，同时服务器也发送一个SYN包，告诉客户端自己的发送序列号为k；【服务器-SYN_RCVD】
3. 客户端协议栈收到ACK，使得应用程序从connect调用返回，表示客户端到服务器端的单向连接建立完成，同时客户端协议栈也会对服务器的SYN包进行应答；【客户端-ESTABLISHED】
4. 应答包达到服务器端后，服务器端协议栈使得accept阻塞调用返回，这时服务器端到客户端的单向连接也建立完成。【服务端-ESTABLISHED】

#### Socket读写

当套接字创建以及TCP连接建立之后，接下来可以使用创建的套接字收发数据。

> 收发数据函数原型

【发送数据】

发送数据常用有三个函数：write、send和sendmsg

```c
// 常见的文件写函数，把socketfd换为文件描述符就是普通文件写入。
ssize_t write (int socketfd, const void *buffer, size_t size)
// 上一个的扩展，发送带外数据（基于TCP协议的紧急数据，用于特定场景的紧急处理）
ssize_t send (int socketfd, const void *buffer, size_t size, int flags)
// 可以指定多重缓冲区传输数据，以msghdr结构体方式发送数据
ssize_t sendmsg(int sockfd, const struct msghdr *msg, int flags)
```

尽管普通文件描述符与套接字描述符表现形式一致，但内在区别很不一样。

+ 对于普通文件描述符，一个文件描述符代表了打开的一个文件句柄，通过调用write，操作系统内核可以不断往文件系统写入字节流。（写入的字节流大小与输入的参数size相同，否则出错）
+ 对于套接字描述符，代表了一个双向连接，在套接字描述符调用write写入的字节有可能比请求的数量少，这是与普通文件描述符不同的。（这是因为操作系统内核为读取和发送数据做了额外的工作）

【发送缓冲区】

TCP三次握手成功，TCP连接建立成功，操作系统内核会为每个连接创建配套的基础设施，其中包含了发送缓冲区。

发送缓冲区的大小可以通过套接字选项来改变，当我们的应用程序调用 write 函数时，实际
所做的事情是把数据从应用程序中拷贝到操作系统内核的发送缓冲区中，并不一定是把数据
通过套接字写出去。  这是会存在以下几种情况：

+ 发送缓冲区足够大，可以会直接容纳数据，这时皆大欢喜，从write调用退出，返回写入的字节大小就是应用程序的数据大小。
+ 发送缓冲区足够大但有数据没法送完成；缓冲区大小不能容纳应用程序数据。这时应用程序会在write函数被阻塞，不直接返回。对于大部分UNIX系统，会一直等到应用程序数据完全放到发送缓冲区，再从系统调用返回。

【读取数据】

UNIX中万物皆文件，可以将套接字描述符传递到处理本地文件设计的函数，包括了read和write交换数据的函数。

```c
// 内核从套接字描述字socketfd读取最多size字节，并将结果存储到buffer中。
// 
ssize_t read (int socketfd, void *buffer, size_t size);
```

+ 返回值是实际读取的字节数目，也有一些特殊情况：
  + 0表示EOF，在网络中表示对方发送FIN包，要处理断连的情况；
  + -1表示出错。如果是非阻塞 I/O，情况会略有不同，在后面的提高篇中我们会重点讲述非阻塞 I/O 的特点

> 案例1：循环读取数据

```c
/* 从 socketfd 描述字中读取 "size" 个字节. */
ssize_t readn(int fd, void *vptr, size_t size) {
	size_t nleft;
	ssize_t nread;
    char *ptr;
    
	ptr = vptr;
	nleft = size;
    while (nleft > 0) {
        if ( (nread = read(fd, ptr, nleft)) < 0) {
            if (errno == EINTR)  {
                // 再次调用read
                nread = 0; 
            } else {
                return(-1);
            }
        } else if (nread == 0) {
            // EOF表示套接字关闭
            break; 
        }
            nleft -= nread;
            ptr += nread;
    }
    // 返回的是实际读取的字节数
    return(n - nleft); 
}
```

> 案例2：服务器端读取数据的程序  

```c
int main(int argc, char **argv) {
    int 				listenfd,connfd;
    socklen_t 			clilen;
    struct sockadd_in 	cliaddr, servaddr;
    // 创建套接字
    listendfd = socket(AF_INET, SOCK_STREAM, 0);
    
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sin_family 	= AF_INET;	
    servaddr.sin_add.s_add	 = hlonl(INADDR_ANY);
    servadd.sin_port		= htons(12345);
    
    // bind到本地地址，端口为12345
    bind(listendfd, (SA*)&servaddr, sizeof(servaddr));
    // listen的backlog为1024
    listen(listenfd, 1024);
    
    // 循环处理用户请求，通过accept获取实际连接，开始读取数据
    for(;;;) {
        clilen = sizeof(cliaddr);
        connfd = accpet(listenfd, (SA*)&cliaddr, &clilen);
        read_data(connfd);	// 读取数据
        close(connfd);		// 关闭连接套接字（监听套接字仍然保持）
    }
}

void read_data(int sockfd) {
    ssize_t n;
    char buf[1024];
    
    int time=0;
    for(;;;) {
        fprintf(stdout, "block in read\n");
        if ((n=Readn(sockfd, buf, 1024)) == 0) {
            return;
        }
        time++;
        fprintf(stdout, "1K read forr %d \n", time);
        usleep(1000);
    }
}
```

> 客户端发送数据

```c
int main(int argc, char **argv) {
    int sockfd;
    struct sockaddr_in servaddr;
    
    if (argc!=2) {
        err_quit("usage:tcpclient <IPaddress>");
    }
    // 创建socket套接字
    sockfd = socket()(AF_INET, SOCK_STREAM, 0);
    // 调用connect向对应服务器发起连接七牛
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_port=htons(SERV_PORT);
    inet_pton(AF_INET, argv[1], &servaddr.sin_addr);
    connect(sockfd, (SA*)&servaddr, sizeof(servaddr));
    // 连接建立成功后，调用send_data发送数据
    send_data(stdin, sockfd);
    exit(0);
}

# define MESSAGE_SIZE 10240000
void send_data(FILE *fp, int sockfd) {
    // 初始化长度为MESSAGE_SIZE的字符串流
    char *query;
    query = malloc(MESSAGE_SIZE+1);
    for (int i=0; i<MESSAGE_SIZE; i++) {
        query[i] = 'a';
    }
    query[MESSAGE_SIZE] = '\0';
    // 调用send函数将MESSAGE_SZIE长度的字符串发送出去
    const char *cp;
    cp = query;
    remaining = strlen(query);
    while (remaining) {
        n_written = send(sockfd, cp, remaining, 0);
        fprintfp(stdout, "send into buffer %ld \n", n_writeen);
        if (n_written<=0) {
            perror("send");
            return;
        }
        remaining -= n_written;
        cp += n_written;
    }
    return;
}
```

总而言之，对于send函数，返回成功仅代表数据写到发送缓冲区成功，并不代表对方已经成功收到；对于read函数，需要循环读取数据，并考虑EOF等异常条件。



#### UDP通信过程

上述主要是TCP的通信过程，对于UDP其通信过程如下：

![image-20210903232251465](https://gitee.com/tobing/imagebed/raw/master/image-20210903232251465.png)

+ 服务器创建UDP套接字之后，绑定本地端口，调用recvfrom函数等待客户端的报文发送；
+ 客户端创建套接字之后，调用sendto函数往目标地址和端口发送UDP报文；
+ 然后客户端和服务器进入互相应答过程。

```c
#include <sys/socket.h>

// 【接收报文】
// sockfd：本地创建的套接字描述符
// buff：执行本地的缓存
// nbytes：最大接收数据字节
// flags：I/O相关参数
// from：返回对方发送方的地址和端口等信息，与TCP不同，TCP通过accept函数拿到没设置信息决定对端的信息，UDP报文每次接收都会获取对端的信息，即报文与报文之间没有上下文
// addrlen：
ssize_t recvfrom(int sockfd, void *buff, size_t nbytes, int flags,
                struct sockaddr *from, socklen_t *addrlen);

// 【接收报文】
// sockfd：本地创建的套接字描述符
// buff：发送的缓存
// nbytes：发送字节数
// falg：设置为0
ssize_t sendto(int sockfd, const void *buff, size_t nbytes, int flag,
              const struct sockaddr *to , socklen_t *addrlen);
```

> UDP服务器端

```c
#include  "lib/common.h"
static int count;
static void recvfrom_int(int signo) {
    printf("\nreceived %d datagrams\n", count);
}

int main(int argc, char ** argv) {
    int socket_fd;
    socket_fd = socket(AF_INET, SOCK_DATAGRAM, 0);
    
    struct sockaddr_in aserver_addr;
    bzeor(&server_addr, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	server_addr.sin_port = htons(SERV_PORT);
    
    bind(socket_fd, (struct sockaddr *) &server_addr, sizeof(server_addr));

    socklen_t client_len;
    char message[MAXLINE];
    count = 0;
    // 为该服务器创建一个信号处理函数，便于响应Ctrl+C退出，打印出收到的总报文树
    signal(SIGINT, recvfrom_int);
    
    struct sockaddr_in client_addr;
	client_len = sizeof(client_addr);
    // 通过调用 recvfrom 函数获取客户端发送的报文
    for(;;) {
        int n = recvfrom(socket_fd, message, MAXLINE, 0, (struct sockaddr *) &client_addr, &client_len);
        message[n] = 0;
        printf("received %d bytes: %s\n", n, message);
 
        char send_line[MAXLINE];
        sprintf(send_line, "Hi, %s", message);
 
        sendto(socket_fd, send_line, strlen(send_line), 0, (struct sockaddr *) &client_addr, client_len);
 
        count++;
    }
    
}
```

> UDP客户端

```c
#include "lib/common.h"
 
# define    MAXLINE     4096
 
int main(int argc, char **argv) {
    if (argc != 2) {
        error(1, 0, "usage: udpclient <IPaddress>");
    }
    
    int socket_fd;
    socket_fd = socket(AF_INET, SOCK_DGRAM, 0);
 
    struct sockaddr_in server_addr;
    bzero(&server_addr, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(SERV_PORT);
    inet_pton(AF_INET, argv[1], &server_addr.sin_addr);
 
    socklen_t server_len = sizeof(server_addr);
 
    struct sockaddr *reply_addr;
    reply_addr = malloc(server_len);
 
    char send_line[MAXLINE], recv_line[MAXLINE + 1];
    socklen_t len;
    int n;
 
    // 从标准输入中读取的字符进行处理后，调用 sendto 函数发送给目标服务器端
    // 然后再次调用 recvfrom 函数接收目标服务器发送过来的新报文，并将其打印到标准输出上。
    while (fgets(send_line, MAXLINE, stdin) != NULL) {
        int i = strlen(send_line);
        if (send_line[i - 1] == '\n') {
            send_line[i - 1] = 0;
        }
 
        printf("now sending %s\n", send_line);
        size_t rt = sendto(socket_fd, send_line, strlen(send_line), 0, (struct sockaddr *) &server_addr, server_len);
        if (rt < 0) {
            error(1, errno, "send failed ");
        }
        printf("send bytes: %zu \n", rt);
 
        len = 0;
        n = recvfrom(socket_fd, recv_line, MAXLINE, 0, reply_addr, &len);
        if (n < 0)
            error(1, errno, "recvfrom failed");
        recv_line[n] = 0;
        fputs(recv_line, stdout);
        fputs("\n", stdout);
    }
 
    exit(0);
}
```

#### TCP与UDP

+ 对于UDP，只运行客户端，程序会阻塞在recvfrom上；

+ 对于TCP，只运行客户端，TCP客户端的connect函数会直接返回「Connection refused」信息。
+ 对于UDP，一个服务器端可以依次处理多个服务器端的报文，精髓服务器端重启仍能机型接收客户端报文；（UDP是无连接的）
+ 对于TCP，服务器端重启之后，必须要重新连接才能继续发送报文信息。

#### 本地套接字

本地套接字，也叫UNIX域套接字，是IPC的一种实现方式。

本地套接字是一种特殊类型的套接字，与TCP/IP套接字不同。TCP/UDP套接字在本地地址通信也需要做系统网络协议栈；而本地套接字，提供了以这个单机跨进程间调用的手段，减少协议栈实现的复杂度，效率比TCP/UDP套接字高许多。

> 服务端：本地字节流套接字

```c
#include "lib/common.h"

int main(int argc, char **argv) {
    if (argc != 2) {
        error(1,0,"usage:unixstreamserver <local_path>");
    }
    
    int listenfd, connfd;
    socklen_t clilen;
    struct sockaddr_un cliaddr, serveraddr;
    
    listenfd = socket(AF_LOCAL, SOCK_STREAM, 0);
    if (listenfd < 0) {
        error(1, errno, "socket create failed");
    }
    // 创建AF_LOCAL套接字
    char *local_path = argv[1];
    // unlink可以把存在的文件上传，保证幂等性
    unlink(local_path);
    bzhero(&servaddr, sizeof(servaddr));
    servaddr.sun_fimaly = AF_LOCAL;
    stccpy(servaddr.sun_path, local_path);
    // 绑定本地地址（本地文件路径，必须是绝对路径，必须是一个文件，bind操作会自动创建文件）
    // Linux下，任何文件操作都要有对应的权限，应用程序启动时有对应的属主，对应的属主必须要有对于本地文件的权限
    // 如果启动程序对用户没有监听文件的权限，在启动时会提示绑定失败。
    if (bind(listenfd, (struct socketaddr *)&servaddr, sizeof(servaddr) < 0)) {
        error(1, errno, "bind failed");
    }
    // 监听操作
    if (listen(listenfd, LISTENQ) < 0) {
        error(1, errno, "listen failed");
    }
    
    clilen = sizeof(cliaddr);
    if (connfd=accept(listenfd, (struct *)&cliaddr, &clilen) < 0) {
        if (errno == EINTR) {
            error(1, errno, "accept failed");
        } else {
            error(1, errno, "accept failed");
        }
    }
    
    char buf[BUFFER_SIZE];
    
    while(1) {
        bzero(buf, sizeof(buf));
        if (read(connfd, buf, BUFFER_SIZE) == 0) {
            printf("client quit");
            break;
        }
        printf("Receive: %s", buf);
        
        char send_line=[MAXLINE];
        sprintf(send_line, "Hi, %s", buf);
        
        in nbytes = sizeof(send_line);
        
        if (write(connfd, send_line, nbytes) != nbytes) {
            error(1, errno, "write error");
        }
    }
    close(listenfd);
    close(connfd);
    exit(0);
}
```

> 客户端：本地字节流套接字

```c
#include "lib/common.h"
 
int main(int argc, char **argv) {
    if (argc != 2) {
        error(1, 0, "usage: unixstreamclient <local_path>");
    }
 
    int sockfd;
    struct sockaddr_un servaddr;
 
    sockfd = socket(AF_LOCAL, SOCK_STREAM, 0);
    if (sockfd < 0) {
        error(1, errno, "create socket failed");
    }
 
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sun_family = AF_LOCAL;
    strcpy(servaddr.sun_path, argv[1]);
 
    if (connect(sockfd, (struct sockaddr *) &servaddr, sizeof(servaddr)) < 0) {
        error(1, errno, "connect failed");
    }
 
    char send_line[MAXLINE];
    bzero(send_line, MAXLINE);
    char recv_line[MAXLINE];
 
    while (fgets(send_line, MAXLINE, stdin) != NULL) {
 
        int nbytes = sizeof(send_line);
        if (write(sockfd, send_line, nbytes) != nbytes)
            error(1, errno, "write error");
 
        if (read(sockfd, recv_line, MAXLINE) == 0)
            error(1, errno, "server terminated prematurely");
 
        fputs(recv_line, stdout);
    }
 
    exit(0);
}
```

> 服务端：本地数据包套接字

```c
#include  "lib/common.h"
 
int main(int argc, char **argv) {
    if (argc != 2) {
        error(1, 0, "usage: unixdataserver <local_path>");
    }
 
    int socket_fd;
    socket_fd = socket(AF_LOCAL, SOCK_DGRAM, 0);
    if (socket_fd < 0) {
        error(1, errno, "socket create failed");
    }
 	// 创建套接字，AF_LOCAL
    struct sockaddr_un servaddr;
    char *local_path = argv[1];
    unlink(local_path);
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sun_family = AF_LOCAL;
    strcpy(servaddr.sun_path, local_path);
 	// 绑定本地地址
    if (bind(socket_fd, (struct sockaddr *) &servaddr, sizeof(servaddr)) < 0) {
        error(1, errno, "bind failed");
    }
 	// 使用recvfrom和sendto进行数据包收发
    char buf[BUFFER_SIZE];
    struct sockaddr_un client_addr;
    socklen_t client_len = sizeof(client_addr);
    while (1) {
        bzero(buf, sizeof(buf));
        if (recvfrom(socket_fd, buf, BUFFER_SIZE, 0, (struct sockadd *) &client_addr, &client_len) == 0) {
            printf("client quit");
            break;
        }
        printf("Receive: %s \n", buf);
 
        char send_line[MAXLINE];
        bzero(send_line, MAXLINE);
        sprintf(send_line, "Hi, %s", buf);
 
        size_t nbytes = strlen(send_line);
        printf("now sending: %s \n", send_line);
 
        if (sendto(socket_fd, send_line, nbytes, 0, (struct sockadd *) &client_addr, client_len) != nbytes)
            error(1, errno, "sendto error");
    }
 
    close(socket_fd);
 
    exit(0);
}
```

> 服务端：本地数据包套接字

```c
#include "lib/common.h"
 
int main(int argc, char **argv) {
    if (argc != 2) {
        error(1, 0, "usage: unixdataclient <local_path>");
    }
 
    int sockfd;
    struct sockaddr_un client_addr, server_addr;
 
    sockfd = socket(AF_LOCAL, SOCK_DGRAM, 0);
    if (sockfd < 0) {
        error(1, errno, "create socket failed");
    }
 
    bzero(&client_addr, sizeof(client_addr));        /* bind an address for us */
    client_addr.sun_family = AF_LOCAL;
    strcpy(client_addr.sun_path, tmpnam(NULL));
 
    if (bind(sockfd, (struct sockaddr *) &client_addr, sizeof(client_addr)) < 0) {
        error(1, errno, "bind failed");
    }
 
    bzero(&server_addr, sizeof(server_addr));
    server_addr.sun_family = AF_LOCAL;
    strcpy(server_addr.sun_path, argv[1]);
 
    char send_line[MAXLINE];
    bzero(send_line, MAXLINE);
    char recv_line[MAXLINE];
 
    while (fgets(send_line, MAXLINE, stdin) != NULL) {
        int i = strlen(send_line);
        if (send_line[i - 1] == '\n') {
            send_line[i - 1] = 0;
        }
        size_t nbytes = strlen(send_line);
        printf("now sending %s \n", send_line);
 
        if (sendto(sockfd, send_line, nbytes, 0, (struct sockaddr *) &server_addr, sizeof(server_addr)) != nbytes)
            error(1, errno, "sendto error");
 
        int n = recvfrom(sockfd, recv_line, MAXLINE, 0, NULL, NULL);
        recv_line[n] = 0;
 
        fputs(recv_line, stdout);
        fputs("\n", stdout);
    }
 
    exit(0);
}
```

### 工具

#### ping

ping命名来自于声呐探测，在网络上用来完成对网络连通性的探测。

ping 是基于一种叫做ICMP的协议开发的，ICMP又是一种基于IP协议的控制协议，翻译为网际控制协议。格式如

```
----------------------------------
|              IP首部             |  
----------------------------------
|              IP首部             |
----------------------------------
|              IP首部             |
----------------------------------
|              IP首部             |
----------------------------------
|              IP首部             |
----------------------------------
|   类型  |  代码  |  校验和  |     |
----------------------------------
|           标识符 |     序列号     |
----------------------------------
|              可选数据            |
-----------------------------------
```

+ 类型：即 ICMP 的类型, 其中 ping 的请求类型为 0，应答为 8
+ 代码：进一步划分 ICMP 的类型, 用来查找产生错误的原因。
+ 校验和：用于检查错误的数据。
+ 标识符：通过标识符来确认是谁发送的控制协议，可以是进程 ID。
+ 序列号：唯一确定的一个报文，前面 ping 名字执行后显示的 icmp_seq 就是这个值。

发起ping命令时，ping程序实际会组装成上述格式的IP报文，报文的目标地址为ping的目标地址，源地址是发送ping的主机地址，同时按照ICMP报文格式填上数据，在可选数据上可以填上发送时时间戳。

IP报文通过ARP协议，原地址和目标地址被翻译为MAC地址，经数据链路层，报文被传输出去。报文到达目标地址后，目的地址所在主机对数据按照ICMP协议进行解析进行应答。

#### ifconfig

```bash
[root@ize80rg3vgac04z ~]# ifconfig
docker0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 172.18.0.1  netmask 255.255.0.0  broadcast 172.18.255.255
        ether 02:42:b0:06:3d:ca  txqueuelen 0  (Ethernet)
        RX packets 367030  bytes 106192753 (101.2 MiB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 368996  bytes 43654954 (41.6 MiB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0

eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 172.17.17.40  netmask 255.255.192.0  broadcast 172.17.63.255
        ether 00:16:3e:17:32:76  txqueuelen 1000  (Ethernet)
        RX packets 45791566  bytes 12404830827 (11.5 GiB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 32686573  bytes 13707508062 (12.7 GiB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0

lo: flags=73<UP,LOOPBACK,RUNNING>  mtu 65536
        inet 127.0.0.1  netmask 255.0.0.0
        loop  txqueuelen 1  (Local Loopback)
        RX packets 14968997  bytes 926913295 (883.9 MiB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 14968997  bytes 926913295 (883.9 MiB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0

vethbb277f8: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        ether 2a:11:e7:fe:2f:7a  txqueuelen 0  (Ethernet)
        RX packets 338333  bytes 107025332 (102.0 MiB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 340035  bytes 34581442 (32.9 MiB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
```

#### netstat与lsof

netstat用于了解当前的网络连接状况；

![image-20210904205521082](https://gitee.com/tobing/imagebed/raw/master/image-20210904205521082.png)

lsof可以找出指定IP地址或端口打开套接字的进程。

#### tcpdump

tcpdump是强大的抓包工具。

![image-20210904205909827](https://gitee.com/tobing/imagebed/raw/master/image-20210904205909827.png)[S]：SYN，表示开始连接

[.]：没有标记，一般是确认

[P]：PSH，表示数据推送

[F]：FIN，表示结束连接

[R] ：RST，表示重启连接

seq：包序号，就是 TCP 的确认分组

cksum：校验码

win：滑动窗口大小

length：承载的数据（payload）长度 length，如果没有数据则为 0

### TCP四挥手

TCP连接终止时，会经过四次挥手阶段：

![image-20210905163909851](https://gitee.com/tobing/imagebed/raw/master/image-20210905163909851.png)

1. 主机1先发送FIN报文，主机2收到之后进入CLOSE_WAIT状态，并发送ACK应答；
2. 同时主机2通过read调用获取EOF，并将结果通知应用程序进行主动关闭操作，发送FIN报文；
3. 主机1收到主机2的ACK确认之后，进入TIME_WAIT状态（通常规定停留2MSL，Linux固定为60秒）；
4. 经历了TIME_WAIT之后，主机1进入COLSED状态。

只有发起连接一方才会进入TIME_WAIT状态。

#### TIME_WAIT

在TCP四挥手阶段，发起连接方会有一个阶段处于TIME_WAIT状态。

**TIME_WAIT的作用**

TIME_WAIT状态可以帮助对方顺利地关闭掉连接。

在TCP四挥手中存在这种情况：主机1 对于主机2的FIN包确认可能会丢失（看上图）。对于主机2，如果一直没有收到主机1的确认，会中重试时间之后对FIN进行重发。

此时，如果主机1之前采用的是直接进入CLOSE，将无法正确处理FIN包，作用将会导致主机2无法正常关闭此链接；

此时，如果主机1之前采用的是TIME_WAIT，主机2重发的FIN报文将能够被主机1正确处理。

<font style="color:red">**综上所述，TIME_WAIT存在的目的就是为了让对方能够顺利地把连接关闭掉。**</font>

TIME_WAIT的等待时间-2MSL是考虑到让两个方向上的分组都被丢弃，使得原来连接的分组在网络上自然消失，这样就能保证出现的分组一定是新的连接产生的。

**TIME_WAIT的危害**

经过TIME_WAIT可以让对方能够顺利地把连接关闭，但是TIME_WAIT也会存在一些问题，主要有两种。

+ 内存资源占用：这个级别基本可以忽略；
+ 端口资源占用：端口资源占用，一个TCP连接会消耗一个本地端口，系统中端口资源有限（32768~61000），如果TIME_WAIT过多将会导致新连接无法创建。

**TIME_WAIT**

为了避免TIME_WAIT导致的端口不足，对系统的使用产生影响，可以对TIME_WAIT进行优化，主要有以下几种优化手段。

+ net.ipv4.tcp_max_tw_bukets，系统中处于TIME_WAIT的连接一旦超过该值，会将所有TIME_WAIT重置，这种方式太暴力且治标不治本。
+ 调低TCP_TIMEWAIT_LEN，重新编译系统，方法补不错，但对人员要求较高，需要重新编译内核。
+ 配置SO_LINGER参数，
  + 设置l_onoff=0，关闭此项功能，l_linger会忽略
  + 设置l_onoff!=0，l_linger=0，表示调用CLOSE之后，会发立刻发送一个RST标志给对方，该TCP连接将跳过四次挥手，因此TIME_WAIT状态也会跳过（强行关闭）。这时，排队数据不会被发送，被动关闭方也不知道对方已经彻底断开，只有被动关闭方正阻塞在recv调用时，收到RST时，会立即得到一个「connect reset by peer」的异常。（非常危险，不推荐）

+ net.ipv4.tcp_tw_reuse，从协议安全可控的角度，可以复用处于 TIME_WAIT 的套接字为新的连接所用。

> 协议安全可控
>
> 1. 适用于连接发起方；
> 2. 对应的TIME_WAIT状态的连接创建时间超过1秒才可以复用

#### 连接的关闭

TCP连接是前双工通信，因此每个方向必须单独进行关闭，当一方完成任务就能发送一个FIN来终止这个方向的连接。当一端收到一个FIN，会通知应用层另一端终止了那个方向的数据传送。



> close函数

```c
// 对已连接的套接字执行close操作，成功返回0，出错返回-1
// 该函数会对套接字引用计数减去1，一旦发现套接字引用计数为0，会对套接字彻底释放，关闭TCP两个方向的数据流
// close执行关闭时，对于输入，系统内核将该套接字设置为不可读，任何读操作都会返回异常；对于输出，系统尝试发送缓冲区的数据发送给对方，并最好先对方发送一个FIN报文，接下来如果对该套接字执行写操作会返回异常
int close(int sockfd)
```

> shutdown函数

```c
// 关闭一个方向的连接
// howto用于函数设置
// SHUT_RD(0)：关闭连接的读方向，到套节字的操作直接返回EOF
// SHUT_WR(1)：关闭连接的写方向，通常被称为半关闭的连接
// SHOT_DRWR(2)：相当于上述操作各执行一遍
int shutdown(int sockfd, int howto);
```

> close与shutdown函数的区别

+ close只是把套接字引用计数减去1，未必会立即关闭这个连接；
+ close函数如果在套接字引用计数到达0时，立即终止读和写两个方向的数据传送；
+ 如果期望关闭连接职工的其中一个时，应该使用shutdown函数。

### 连接的保持

很多情况下，连接的一端需要一直感知连接的状态，如果连接无效了，应用程序可能需要报错，或者重新发起连接等。

在TCP编程中，如果没有数据的读写，将没有办法发现TCP连接是否有效，如客户端突然崩溃，服务器端可能会长期维护一个无用的TCP连接。

TCP提供了Keep-Alive机制来保持连接的活跃，其运行原理为：在一个时间段内，如果没有任何连接活动，TCP活动机制会每隔一段时间发送一个探测报文，探测报文包含的数据很少，如果连续几个报文没有得到响应，会认为当前TCP连接已经死亡，系统内核将成为信息通知给上传应用程序。











