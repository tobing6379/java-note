# Mybatis

## 总体框架设计

Mybatis的总体设计可以分为四层：

+ **接口层**：定义了数据库交互的方式；
+ **数据处理层**：通过传入参数构造动态SQL，将执行SQL得到的结果集封装；
+ **框架支撑层**：包含事务管理、连接池管理、缓存机制、SQL语句的配置方式；
+ **引导层**：配置和启动Mybatis配置信息的方式。

### 接口层

接口层定义了与数据库的交互方式，通常由两种方式：

+ 使用传统Mybatis提供的API；
+ 使用Mapper接口；

**使用传统Mybatis提供的API**

使用传统Mybatis提供的API访问数据库时，需要将`statment id`和查询参数传递给`SqlSession`，使用`SqlSession`对象完成和数据库的交互；Mybatis提供了非常方便和简单的API，供用户是是实现对象数据库的增删改查数据操作，以及对数据库连接信息和Mybatis自身配置信息的维护操作。

**使用Mapper接口**

Mybatis支持将配置文件中的每个`<mapper>`节点抽象为一个Mapper接口，在这个接口中声明的方法和`<mapper>`节点中的`<select|update|delete|insert>`节点项对应，即`<select|update|delete|insert>`节点的id值为Mapper接口中的方法名称，parameterType值表示Mapper方法对应的入参类型，而resultMap对应Mapper接口表示的返回值类型或返回结果集元素类型。

根据Mybatis配置规范配置好之后，同SqlSession.getMapper(XXXMapper.class)方法，Mybatis会根据这个方法的方法名和参数类型，确定 Statement Id，底层还是通过 SqlSession.select("statementId", paramterObject)；SqlSession.update("statementId",parameterObject); 等等来实现对数据库的操作， MyBatis 引用Mapper 接口这种调用方式，纯粹是为了满足面向接口编程的需要。

### 数据处理层

数据处理层需要完成两个功能：

+ 通过传入参数构建动态的SQL语句；
+ SQL语句的执行以及封装查询结果；

**动态SQL生成与参数映射**

Mybatis通过传入参数值，使用OGNL动态构造SQL语句，使得Mybatis有很强的灵活性和扩展性。

参数映射指的是对java数据类型和jdbc数据类型之间的转换，这里包含两个阶段：

+ 查询阶段：将java类型的数据，转换成jdbc类型的数据，通过preparedStatement.setXX设置；
+ 结果返回：对resultSet查询结果集jdbcType数据转换为java数据类型。

**SQL执行与结果集封装**

动态SQL语句生成之后，Mybatis将执行SQL语句，并将可能返回的结果集转换成List列表。Mybatis在对结果集的处理中，支持结果集关系的一对多和多对一的转换，并且有两种支持方式，一种为嵌套查询语句的查询，另外一种是嵌套结果集的查询。

### 框架支撑层

+ **事务管理机制**：事务管理机制是ORM框架不可缺失的一部分，事务管理机制质量是考量一个ORM框架是否优秀的标准。
+ **连接池管理机制**：由于创建数据库连接占用的资源比较大，对于数据吞吐量达和访问量非常大的应用而言，连接池的设计显得非常重要。
+ **缓存机制**：为了提高数据利用率和减少服务器和数据库的杨丽，Mybatis会对一些查询体用会话级别的数据缓存，会将对某一次查询，放到SqlSession中，在允许的时间间隔内，对完全相同的查询，Mybatis会直接将缓存的结果返回个用户，而不用再到数据库中查询。
+ **SQL语句的配置方式**：传统的Mybatis配置SQL语句方式使用XML文件进行配置，但这种方式 不能很好支持面向接口编程的理念，为了支持面向接口编程，Mybatis引入了Mapper接口概念，面向接口引入，对使用注解来配置SQL语句成为可能，用户只需在接口上添加必要的注解即可，不用再去配置XML文件。但目前而言，Mybatis对注解配置SQL只提供了有限的支持，某些高级功能还需要依赖XML配置文件。

### 引导层

引导层是配置和启动Mybatis配置信息的方式。Mybatis体用两种方式来引导Mybatis，基于XML配置文件的方式和基于Java API的方式。

### 主要构件及其相互关系

Mybatis中主要的核心关键有：

+ **SqlSession**：作为Mybatis工作的主要顶层API，表示和数据库交互的会话，完成必要数据库增删改查功能。
+ **Executor**：Mybatis执行器，是Mybatis调度核心，复制SQL语句的生成和查询缓存的维护。
+ **StatementHandler**：封装了JDBC Statement操作，负责对JDBC Statement的操作，如设置参数、来讲Statement结果集转换为List集合。
+ **ParamterHandler**：负责对用户传递的参数转换成JDBC Statement所需的参数。
+ **ResultSetHandler**：负责将JDBC返回的ResultSet结果集对象转换成List类型的集合。
+ **TypeHandler**：负责Java数据类型和jdbc类型数据类型之间的映射和转换。
+ **MappedStatement**：维护了一条`<select|update|delete|insert>`节点的封装。
+ **SqlSource**：负责根据用户传递的parameterObject动态生成SQL语句，将信息封装到BoundSql对象中，并返回。
+ **BoundSql**：表示动态生成的SQL语句以及相应的参数信息
+ **Configuration**：Mybatis所有配置信息都维持在Configuration对象之中。

从Mybatis代码实现角度来看，主体构件的关系如下：

![img](https://gitee.com/tobing/imagebed/raw/master/mybatis-y-arch-4.png)

## 初始化基本过程

Mybatis的初始化可以有两种方式：

+ **基于XML配置文件**：基于XML配置的方式将Mybatis的所有配置信息放到XML文件，Mybatis通过加装并解析XML配置文件，将配置文件信息组装成内部的Configuration对象。
+ **基于Java API**：这种方式不使用XML配置文件，需要Mybatis使用者在Javad代码中，手动创建Configuration对象，然后后将参数设置到Configuration对象中。

### XML配置的初始化

```java
public static void main(String[] args) throws IOException {
    // 读取配置文件
    InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
    // 创建 SqlSessionFactory
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
    // 创建 SqlSession
    SqlSession sqlSession = sqlSessionFactory.openSession();
    // 使用SqlSession获取代理对象
    UserDao mapper = sqlSession.getMapper(UserDao.class);
    // 使用代理对象查询数据库
    UserDTO userDTO = mapper.findOneById(1);
    // 输出结果
    System.out.println(userDTO);
    // 关闭资源
    sqlSession.close();
    in.close();
}
```

上述代码经历了三个阶段：

+ Mybatis初始化

  ```java
  // 读取配置文件
  InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
  // 创建 SqlSessionFactory
  SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
  ```

+ 创建SqlSession

  ```java
  // 创建 SqlSession
  SqlSession sqlSession = sqlSessionFactory.openSession();
  ```

+ 执行SQL语句；

#### Mybatis初始化基本过程

Mybatis初始化需要经历简单的几个步骤：

1. 调用SqlSessionFactoryBuilder对象的build(inputStream)方法；
2. SqlSessionFactoryBuilder会根据输入流inputStream等信息创建XMLConfigBuilder对象;
3. SqlSessionFactoryBuilder调用XMLConfigBuilder对象的parse()方法；
4. XMLConfigBuilder对象返回Configuration对象；
5. SqlSessionFactoryBuilder根据Configuration对象创建一个DefaultSessionFactory对象；
6. SqlSessionFactoryBuilder返回 DefaultSessionFactory对象给Client，供Client使用。

在上面初始化过程中，涉及以下几个对象：

+ **SqlSessionFactoryBuilder** ： SqlSessionFactory的构造器，用于创建SqlSessionFactory，采用了Builder设计模式；
+ **Configuration** ：该对象是SqlMapConfig.xml文件中的所有Mybatis信息；
+ **SqlSessionFactory** ：SqlSession工厂类，以工厂形式创建SqlSession对象，采用了Factory工厂设计模式；
+ **XmlConfigParser** ：负责将SqlMapConfig.xml文件解析为Configuration对象，供SqlSessionFactoryBuilder使用，创建SqlSessionFactory。

#### 创建Configuration对象的过程

在Mybatis初始化基本过程中，具体中XMLConfigBuilder的parse方法将XML文件转换为Configuration对象。

1. XMLConfigBuilder将XML配置文件的信息转换为Document对象；
2. 之后XMLConfigBuilder调用parse方法取出`<configuration>`节点解析得到信息；
3. 将解析得出的值设置到Configuration对象中；
4. 返回Configuration对象。

















