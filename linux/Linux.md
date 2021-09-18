# Linux

## Shell

### 监测程序

![image-20210918235337991](https://gitee.com/tobing/imagebed/raw/master/image-20210918235337991.png)

![image-20210918235410947](https://gitee.com/tobing/imagebed/raw/master/image-20210918235410947.png)

![image-20210918235626796](https://gitee.com/tobing/imagebed/raw/master/image-20210918235626796.png)

![image-20210918235631519](https://gitee.com/tobing/imagebed/raw/master/image-20210918235631519.png)

#### ps-探查进程

ps命令可以输出运行在系统上的所有程序信息，常见的参数以下

![ps-params](https://gitee.com/tobing/imagebed/raw/master/ps-params.png)

通常会使用`-ef`的参数组合来查看系统中运行的所有进程，使用该命令是会输出以下有用信息。

+ UID：启动进程的用户
+ PID：进程的进程ID
+ PPID：父击进程的进程号
+ C：进程生命周期中的CPU利用率
+ STIME：进程启动是的系统时间
+ TTY：进程启动时的中断设备
+ TIME：运行进程需要的累积CPU时间
+ CMD：启动的程序名称



#### top-实时监测

top命令可以实时显示进程信息。top命令输出的信息主要有：

+ 第一行(系统概述)：当前时间、运行时间、登录用户数、系统平均负载(1min/5min/15min)
  + 近15min平均负载高说明系统可能有问题；
+ 第二行(进程概述)：处于运行进程数、处于休眠进程数、处于僵化进程数
+ 第三行(CPU概述)：根据进程属主(用户/系统)和进程状态(运行/空闲/等待)输出CPU利用率
+ 第四行(内存状态)：物理内存总量、使用量、空闲量
+ 第五行(swap状态)：交换空间总量、使用量、空闲量
+ 其他行(进程详情)：包含了以下内容：

![top-details](https://gitee.com/tobing/imagebed/raw/master/top-details.png)

默认，top启动会按照CPU利用率排序。