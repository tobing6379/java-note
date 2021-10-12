# Redis-数据库实现与持久化

## 一、数据库

### 服务器中的数据库

Redis服务器所有数据库保存在服务器状态redis.h/redisServer结构的db数组中，数组中的每个redisDb结构代表一个数据库。

默认情况下，Redis的目标数据库为0号数据库，客户端可以通过SELECT命令来切换目标数据库。 

![image-20210530171257179](https://gitee.com/tobing/imagebed/raw/master/image-20210530171257179.png)

Redis是一个键值对数据库服务器，里面的每个数据库都由redis.h/redisDb结构表示