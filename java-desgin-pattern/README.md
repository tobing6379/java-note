# 设计模式

## 一、概述

编写高质量代码

![image-20210802153353917](https://gitee.com/tobing/imagebed/raw/master/image-20210802153353917.png)

### 面向对象

目前主流的编程范式有三种，分别是面向过程、面向对象和函数式编程。面向对象是三种之中最为主流的一种，大部分的编程语言都是面向对象编程语言。面向对象编程具有丰富的特新（封装、抽象、继承、多态），可以实现复杂的设计思路，是很多设计原则和设计模式变化实现的基础。

### 设计模式

设计模式是针对软件开发中经常遇到的一些设计问题，总结出来的一套**解决方案**或者**设计思路。**大部分设计模式要解决的都是代码的可扩展性问题。

经典的设计模式有 23 种 ，它们可以分为三大类：创建型、结构型、行为型。  

+ **创建型**
  + 常用：单例模式、工厂模式（工厂方法和抽象工厂）、建造者模式。
  + 不常用：原型模式。
+ **结构型**
  + 常用：代理模式、桥接模式、装饰者模式、适配器模式。
  + 不常用：门面模式、组合模式、享元模式。  
+ **行为型**
  + 常用：观察者模式、模板模式、策略模式、职责链模式、迭代器模式、状态模式。
  + 不常用：访问者模式、备忘录模式、命令模式、解释器模式、中介模式。

通过设计模式可以让我们写出可扩展、可读、可维护的高质量代码。

+ **可维护性(maintainability)**：代码易维护，指的是在不破坏原有代码设计、不引入新的 bug 的情况下，能够快速地修改或者添加代码。 如果代码分层清晰、模块化好、高内聚低耦合、遵从基于接口而非实现编程的设计原则等等，那就可能意味着代码易维护。 
+ **可读性(readability)**：代码是否符合编码规范、命名是否达意、注释是否详尽、函数是否长短合适、模块划分是否清晰、是否符合高内聚低耦合等等  。（可以体现在code review）
+ **可扩展性(extensibility)**：在不修改或少量修改原有代码的情况下，通过扩展的方式添加新的功能代码。（对修改关闭，对扩展开放）
+ **灵活性(flexibility)**：易扩展、易复用或者易用。
+ **简洁性(simplicity)**：代码简单、逻辑清晰。
+ **可复用性(reusability)**：尽量减少重复代码的编写，复用已有的代码。(继承、多态、单一职责、解耦合、高内聚、模块化)
+ **可测试性(testability)**：单元测试。

### 编程规范

编程规范主要解决的是代码的可读性问题。编码规范相对于设计原则、设计模式，更加具
体、更加偏重代码细节。（可以参考《重构》《代码大 全》《代码整洁之道》等）

### 代码重构

重构是软件开发中非常重要的一个环节。持续重构是保持代码质量不下降的有效手段，
能有效避免代码腐化到无可救药的地步。重构的工具就是面向对象设计思想、设计原则、设计模式、编码规范。

### 设计原则


SOLID 原则 -SRP，Single Responsibility Principle 单一职责原则

SOLID 原则 -OCP，Open Closed Principle  开闭原则

SOLID 原则 -LSP，Liskov Substitution Principle 里式替换原则

SOLID 原则 -ISP，Interface Segregation Principle 接口隔离原则

SOLID 原则 -DIP，Dependency Inversion Principle 依赖倒置原则

DRY 原则，Don’t Repeat Yourself  ，不要写重复的代码

KISS 原则，Keep It Simple and Stupid，尽量保持简单

YAGNI 原则，You Ain’t Gonna Need It  ，不要过度设计

LOD 法则，Law of Demeter ，迪米特法则，最小知识原则

## 二、设计原则

#### 单一职责原则

<font style="color:red">**一个类或者模块只负责完成一个职责（或者功能）。  **</font>

一个类只负责完成一个职责或者功能。也就是说，不要设计大而全的类，要设计粒度小、功能单一的类。  如类A负责两个不同职责：职责1，职责2。当职责1需求变更而改变A时，可能造成职责2执行错误，所以需要将类A的粒度分解为 A1，A2

> 单一职责原则注意事项和细节

+ 降低类的复杂度，一个类只负责一项职责。
+ 提高类的可读性，可维护性
+ 降低变更引起的风险
+ 通常情况下，我们应当遵守单一职责原则，只有逻辑足够简单，才可以在代码级违反单一职责原则；只有类中方法数量足够少，可以在方法级别保持单一职责原则

在真正的软件开发中，我们也没必要过于未雨绸缪，过度设计。所以，我们可以先写一个粗粒度的类，满足业务需求。随着业务的发展，如果粗粒度的类越来越庞大，代码越来越多，这个时候，我们就可以将这个粗粒度的类，拆分成几个更细粒度的类。这就是所谓的持续重构。

#### 接口隔离原则

<font style="color:red">**客户端不应该依赖它不需要的接口，即一个类对另一个类的依赖应该建立在最小的接口上。**</font>

类A通过接口Interface1依赖类B，类C通过接口Interface1依赖类D，如果接口 Interface1对于类A和类C来说不是最小接口，那么类B和类D必须去实现他们不需要的方法。

按隔离原则应当这样处理： 将接口Interface1拆分为独立的几个接口， 类A和类C分别与他们需要的接口建立依赖关系。也就是采用接口隔离原则。（接口拆分）

> 接口隔离原则与单一职责原则

接口隔离原则跟单一职责原则有点类似，不过稍微还是有点区别。**单一职责原则针对的是模块、类、接口的设计**。而**接口隔离原则**相对于单一职责原则，一方面它**更侧重于接口的设计**，另一方面它的思考的角度不同 。它提供了一种判断接口是否职责单一的标准：通过调用者如何使用接口来间接地判定。如果调用者只使用部分接口或接口的部分功能，那接口的设计就不够职责单一。  

#### 依赖倒转原则

<font style="color:red">**高层模块不应该依赖低层模块。高层模块和低层模块应该通过抽象来互相依赖。除此之外，抽象不要依赖具体实现细节，具体实现细节依赖抽象。**</font>

依赖倒转(倒置)的中心思想是**面向接口编程**。

依赖倒转原则是基于这样的设计理念：相对于细节的多变性，抽象的东西要稳定的多。以抽象为基础搭建的架构比以细节为基础的架构要稳定的多。在java中，抽象指的是接口或抽象类，细节就是具体的实现类。（抽象比具体稳定）

> 依赖倒转原则的注意事项和细节

+ 低层模块尽量都要有抽象类或接口，或者两者都有，程序稳定性更好
+ 变量的声明类型尽量是抽象类或接口, 这样我们的变量引用和实际对象间，就存在 一个缓冲层，利于程序扩展和优化
+ 继承时遵循**里氏替换原则**

##### 控制反转

控制反转，Inversion Of Control，IOC是一种实现，并不是一种具体的实现方法，一般用于指导**框架层面设计**。此处的**控制**指的是对程序执行流程的控制，而“反转”指的是在没有使用框架之前，程序员自己控制整个程序的执行。在使用框架之后，整个程序的执行流程通过框架来控制。**流程的控制权从程序员“反转”给了框架**。  

##### 依赖注入

依赖注入，Dependency Injection  ，DI和控制反转相反  ，是一种编程技巧。使用依赖注入，我们不通过new的方式在类内部创建依赖类的对象，而是将依赖的类对象在外部创建好之后，通过构造函数、函数参数等方式传递(注入)给类使用。

我们可以通过依赖注入框架提供的扩展点，简单配置一下需要的类，以及类与类之间的依赖关系，这样就可以有框架来自动创建对象、管理对象的生命周期、依赖注入等操作。

#### 里式代换原则

<font style="color:red">**子类对象能够替换程序中父类对象出现的任何地方，并且保证原来程序的逻辑行为不变及正确性不被破坏。 **</font>

子类在设计的时候，要遵守父类的行为约定（或者叫协议）。父类定义了函数的行为约定，那子类可以改变函数的内部实现逻辑，但不能改变函数原有的行为约定。这里的行为约定包括：函数声明要实现的功能；对输入、输出、异常的约定；甚至包括注释中所罗列的任何特殊说明。实际上，定义中父类和子类之间的关系，也可以替换成接口和实现类之间的关系。  

> 违反里氏代换的例子

1. 子类违背父类声明要实现的功能  

   父类中提供的 sortOrdersByAmount() 订单排序函数，是按照金额从小到大来给订单排序的，而子类重写这个 sortOrdersByAmount() 订单排序函数之后，是按照创建日期来给订单排序的。那子类的设计就违背里式替换原则。  

2. 子类违背父类对输入、输出、异常的约定  

   某个函数约定，输入数据可以是任意整数，但子类实现的时候，只允许输入数据是正整数，负数就抛出，也就是说，子类对输入的数据的校验比父类更加严格，那子类的设计就违背了里式替换原则。  

3. 子类违背父类注释中所罗列的任何特殊说明  

   父类中定义的 withdraw() 提现函数的注释是这么写的：“用户的提现金额不得超过账户余额……”，而子类重写 withdraw() 函数之后，针对 VIP 账号实现了透支提现的功能，也就是提现金额可以大于账户余额，那这个子类的设计也是不符合里式替换原则的。  

#### 开闭原则

<font style="color:red">**一个软件实体如类，模块和函数应该对扩展开放(对提供方)，对修改关闭(对使用 方)。用抽象构建框架，用实现扩展细节。**</font>

添加一个新的功能应该是，在已有代码基础上扩展代码（新增模块、类、方法等），而非修改已有代码（修改模块、类、方法等）。  

开闭原则编程中最基础、最重要的设计原则。当软件需要变化时，尽量通过扩展软件实体的行为来实现变化，而不是通过修改已有的代码来实现变化。

#### 迪米特法则

<font style="color:red">**迪米特法则(Demeter Principle)又叫最少知道原则，即一个类对自己依赖的类知道的越少越好。**</font>也就是说，对于被依赖的类不管多么复杂，都尽量将逻辑封装在类的内部。对外除了提供的public 方法，不对外泄露任何信息。

迪米特法则更加简单的定义是：只与直接的朋友通信

直接的朋友：每个对象都会与其他对象有耦合关系，只要两个对象之间有耦合关系， 我们就说这两个对象之间是朋友关系。耦合的方式很多，**依赖，关联，组合，聚合** 等。其中，我们称出现成员变量，方法参数，方法返回值中的类为直接的朋友，而出现在局部变量中的类不是直接的朋友。

也就是说，陌生的类最好不要以局部变量 的形式出现在类的内部。

>迪米特法则注意事项和细节

迪米特法则的核心是**降低类之间的耦合**，并不是要求完全没有依赖关系。

#### 合成复用原则

原则是尽量使用合成/聚合的方式，而不是使用继承。

#### 积分系统设计

在Google中，大部分工程师都具有产品是为，不是完全的技术控。

在设计一个系统时，需要学会借鉴，如设计一个积分系统，可以借鉴淘宝的积分系统。

借鉴的方式可以有两种，一种是直接使用，第二种就是直接百度。通过这两部可以对积分系统的设计有大概的的了解，除此之外还需要结合到产品实际，并适当微创新。

有了这些“粗糙”的设计，还需要进一步明确业务细节。这时候可以通过线框图、用户用例来细化业务流程，挖掘不容易想到的的细节、功能点。

用户用例是用来模拟用户如何使用产品，描述用户在一个特定的场景下的一个完整的业务操作流程。它包含更多细节，容易被人理解。

在明确了具体的业务新设计就需要进行系统设计，主要分为以下步骤：

1. 合理地将功能划分到不同模块
2. 设计模块与模块之间的交互关系，常见的有同步(简单，适用于上下层系统)和异步(解耦，适用于同层系统)
3. 设计模块的接口、数据库、业务模型

进行积分系统的需求分析和系统设计，接下来则是代码实现。在代码实现过程中，主要要进行以下内容：

+ 接口设计：要符合单一职责原则，在职责单一的细粒度接口之上，再封装一层粗粒度的接口给外部使用。  

+ 数据库设计：要求稳定，一般不改动

+ 业务模型设计：系统还比较简单，使用贫血的MVC三层

> MVC分层的好处
>
> + 分层能起到代码复用的作用 
> + 分层能起到隔离变化的作用  
> + 分层能起到隔离关注点的作用  
> + 分层能提高代码的可测试性  
> + 分层能应对系统的复杂性  

#### 设计原则总结

<font style="color:red">**以上七大设计原则的核心思想是，找出应用中可能需要变化的地方，把它们独立出来，不要和那些不需要变化的代码混起来。针对接口编程，而不是针对实现编程。为了交互对象之间的松耦合设计而努力了。**</font>

![image-20210804203318851](https://gitee.com/tobing/imagebed/raw/master/image-20210804203318851.png)





## 三、设计模式

### 简介

设计模式是程序员在面对同类软件工程设计问题所总结出来的有用的经验，模式不是代码，而是某类问题的**通用解决方案**，设计模式（Design pattern）代表了最佳的实践。这些解决方案是众多软件开发人员经过相当长的一段时 间的试验和错误总结出来的。

设计模式的本质提高 软件的维护性，通用性和扩展性，并降低软件的复杂度。

设计模式分为三种类型，共23种。

+ 创建型：**单例**、抽象工厂、原型、建造者、**工厂**
+ 结构型：适配器、桥接、**装饰**、组合、外观、享元、**代理**
+ 行为型：模板方法、命令、访问者、迭代器、**观察者**、中介者、备忘录、解析器、状态、策略、责任链

### 创建型

创建型主要解决对象的创建问题，封装复杂的创建过程，解耦对象的创建代码和使用代码。

+ 单例用于创建全局唯一的对象；
+ 工厂模式用于创建不同但是相关类型的对象（继承同一父类或接口的子类），由给定参数决定创建哪种类型的对象；
+ 建造者模式用于创建复杂对象，可以通过设置不同的可选参数，「定制化」创建不同的对象；
+ 原型模式针对创建成本比较大的对象，利用已有对象进行复制的方式创建，以达到节省创建时间的目的。

#### 单列设计模式

所谓类的单例设计模式，就是采取一定的方法保证在整个的软件系统中，对某个类**只能存在一个对象实例**，并且该类只提供一个取得其对象实例的方法(静态方法)。

> 比如Hibernate的SessionFactory，它充当数据存储源的代理，并负责创建Session对象。SessionFactory并不是轻量级的，一般情况下，一个项目通常只需要一个SessionFactory就够，这是就会使用到单例模式。
>
> 储存之外JDK中的System类也使用了单例设计模式。

单例设计模式在编写时，需要关注的点主要为以下几个点：

+ 构造函数需要是 private 访问权限的，这样才能避免外部通过 new 创建实例；
+ 考虑对象创建时的线程安全问题；
+ 考虑是否支持延迟加载；
+ 考虑 getInstance() 性能是否高（是否加锁）。

单列设计模式一共有七种实现方式：

| 单例模式                                                     | 并发安全   | 是否懒加载 | 备注                                              | 是否推荐 |
| ------------------------------------------------------------ | ---------- | ---------- | ------------------------------------------------- | -------- |
| [饿汉式静态常量](https://github.com/Tobingindex/java-code/blob/master/java-design-pattern/src/main/java/design_pattern/singleton/SingletonDemo01.java) | :+1:       | :no_entry: | JVM保证并发安全，类加载的时候初始化               | 可用     |
| [饿汉式静态代码块](https://github.com/Tobingindex/java-code/blob/master/java-design-pattern/src/main/java/design_pattern/singleton/SingletonDemo02.java) | :+1:       | :no_entry: | JVM保证并发安全，类加载的时候初始化               | 可用     |
| [懒汉式线程不安全](https://github.com/Tobingindex/java-code/blob/master/java-design-pattern/src/main/java/design_pattern/singleton/SingletonDemo03.java) | :no_entry: | :+1:       | 单线程可用，多线程不推荐使用                      | 不推荐   |
| [懒汉式线程安全同步方法](https://github.com/Tobingindex/java-code/blob/master/java-design-pattern/src/main/java/design_pattern/singleton/SingletonDemo04.java) | :no_entry: | :+1:       | 使用synchronized修饰方法，高并发时性能差          | 不可以   |
| [双重检查锁](https://github.com/Tobingindex/java-code/blob/master/java-design-pattern/src/main/java/design_pattern/singleton/SingletonDemo05.java) | :+1:       | :+1:       | 使用synchronized块和双重判断，高并发性能较好      | 推荐     |
| [静态内部类](https://github.com/Tobingindex/java-code/blob/master/java-design-pattern/src/main/java/design_pattern/singleton/SingletonDemo06.java) | :+1:       | :+1:       | 利用JVM内部机制，既实现线程安全，又保证了并发性能 | 推荐     |
| [枚举](https://github.com/Tobingindex/java-code/blob/master/java-design-pattern/src/main/java/design_pattern/singleton/SingletonDemo07.java) | :+1:       | :+1:       | 利用JVM内部机制，既实现线程安全，又保证了并发性能 | 推荐     |

提前初始化的利弊是要根据使用场景而定的，并不是说提前占用了资源就是不好的。

如果初始化耗时长，最好不要等到真正要用它的时候，才去执行这个耗时长的初始化过程，这会影响到系统的性能。采用饿汉式实现方式，将耗时的初始化操作，提前到程序启动的时候完成，这样就能避免在程序运行的时候，再去初始化导致的性能问题。  

如果实例占用资源多，按照 fail-fast 的设计原则（有问题及早暴露），那我们也希望在程序启动时就将这个实例初始化好。如果资源不够，就会在程序启动的时候触发报错（比如Java 中的 PermGen Space OOM），我们可以立即去修复。这样也能避免在程序运行一段时间后，突然因为初始化这个实例占用资源过多，导致系统崩溃，影响系统的可用性。

除此之外，单例模式还会存在以下问题：

+ 单例对 OOP 特性的支持不友好
+ 单例会隐藏类之间的依赖关系
+ 单例对代码的扩展性不友好
+ 单例对代码的可测试性不友好
+ 单例不支持有参数的构造函数

#### 简单工厂模式

<font style="color:red">**定义了一个创建对象的类，由这个类来封装实例化对象的行为(代码)。**</font>

简单工厂模式是属于创建型模式，是工厂模式的一种。简单工厂模式是由一个工厂对象决定创建出哪一种产品类的实例。简单工厂模式是工厂模式家族中最简单实用的模式。

在软件开发中，当我们会用到大量的创建某种、某类或者某批对象时，就会使用到工厂模式。

#### 工厂方法模式

<font style="color:red">**定义了一个创建对象的抽象方法，由子类决定要实例化的类。工厂方法模式将对象的实例化推迟到子类。**</font>

#### 抽象工厂模式

<font style="color:red">**定义了一个interface用于创建相关或有依赖关系的对象簇，而无需指明具体的类。**</font>

抽象工厂模式可以将简单工厂模式和工厂方法模式进行整合。从设计层面看，抽象工厂模式就是对简单工厂模式的改进(或者称为进一步的抽象)。将工厂抽象成两层，AbsFactory(抽象工厂) 和 具体实现的工厂子类。程序员可以根据创建对象类型使用对应的工厂子类。这样将单个的简单工厂类变成了工厂簇，更利于代码的维护和扩展。

**工厂模式在JDK中的应用**

在JDK的Calendar中，使用了简单工厂模式。

**工厂模式总结**

工厂模式将实例化对象的代码提取出来，放到一个类中统一管理和维护，从而达到和主项目依赖关系解耦，提高了项目的扩展和维护性。

三种工厂模式遵循了设计模式的依赖抽象原则：

+ 创建对象实例时，不直接new类，而是把new的操作放到工厂的方法中。
+ 不要让类继承具体的类，而是继承抽象类或是实现interface。
+ 不覆盖基类中已经实现的方法。



#### 建造者模式

日常开发中，创建一个对象最常用的方式是通过new关键字，来调用类的构造函数来完成。

如果一个类有很多属性，为了避免构造函数的参数列表过长，影响代码的可读性和易用性，可以通过各种事情配合和setter方法来解决。

但是如果存在下列情况的任意一种情况，可以考虑使用建造者模式：

+ 把的必填属性放到构造函数中，强制创建对象的时候就设置。如果必填的属性有很多，把这些必填属性都防盗属性的构造器中设置，会出现构造器参数列表很长的问题。如果把必要属性通过setter方法设置，将无法校验这些必填属性是否填写。
+ 如果类的属性之间有一定的依赖关系或约束条件，使用构造器配合set方式，将无法校验这些属性之间的约束关系或依赖关系。
+ 如果希望创建不可变对象，即在对象创建好之后，不能修改内部的属性值；要实现这个功能，不能在类中暴露setter方法。构造函数配合setter方法设置属性值的方式不适用 。

> 工厂模式 VS 建造者模式

建造者模式与工厂模式都可以负责对象的创建工作。

+ 建造者模式通过建造者来创建对象；
+ 工厂模式通过工厂类创建对象；

实际上两者的本质区别是：

+ 工厂模式用于创建不同但是相关类型相同的对象（继承同一个父类或接口的一组子类），由给定的参数来决定创建哪种类型的对象。
+ 建造者模式用于创建一种类型的复杂对象，通过设置不同的可选参数，「定制化」创建不同的对象。

通俗而言：

顾客进去一家餐馆点餐，利用工厂模式，根据用户不同的选择，来制作不同的食物，比如披萨、汉堡、沙拉。对于披萨来说，用户又有各种配料可以定制，比如奶酪、西红柿、起司，通过建造者模式根据用户选择的不同配料来制作披萨。

#### 原型模式

<font style="color:red">**如果对象创建成本比较大，而且同一个类的不同对象之间差别不大（大部分字段相同），可以利用已有的对象进行复制的方式来创建新对象，以达到节省创建时间的目的。这种基于来创建对象的方式叫做原型设计模式设计模式，简称原型模式。**</font>

实际上，对象创建包含申请内存、给成员变量赋值的过程，大部分情况下本身不会花费太多时间，甚至还是可以忽略的。这时如果应用一个复制的模式，只能得到一点点的提升，反而会导致过度设计，得不偿失。

但如果对象的数据需要经过复杂计算才能得到，如排序、计算哈希值，或者需要从RPC、网络、数据库、文件系统等非常慢速的IO中读取。这种情况下，可以利用原型模式，从其他已有对象中直接拷贝得到，而不用每次在创建新对象的时候，都重复执行这些耗时的操作。

Java中，Object类的clone方法可以获得一个对象的拷贝。

但需要注意，拷贝分为深拷贝和浅拷贝。

+ 深拷贝：对于基础数据类型复制值，对于引用数据类型，会复制对象；
+ 浅拷贝：对于基础数据类型复制值，对于引用数据类型只复制引用地址。

而Object的clone方法是使用的浅拷贝。相对地，对于原型模式也分为浅拷贝和深拷贝。

对于一些场景，使用浅拷贝会出现问题，此时必须使用深拷贝。实现深拷贝主要有两种方式：

+ 递归拷贝对象：对于对象中引用的对象、以及对象引用的对象中的对象引用，递归使用clone，直到没有引用对象为止；
+ 将对象序列化：先将对象序列化，再将反序列化成新的对象。

从上面也可以知道，无论采用何种方式，深拷贝始终比浅拷贝要更加占用时间。

### 结构型

结构型模式总结了一些类或对对象组合在一起的经典结构，这些经典结构可以解决忒定应用场景的问题。

#### 代理模式

<font style="color:red">**代理模式在不改变原始类代码的情况下，通过引入代理类来给原始类附加功能。**</font>

如需要收集接口请求的原始数据，如访问时间、处理时长等。如果这将这部分垃圾直接嵌入到业务代码中，会存在两个问题。

1. 性能计数器代码侵入到业务代码中，跟业务代码高度耦合。如果未来需要替换这个框架，替换成本会比较大；
2. 收集接口请求的代码跟业务代码无关，本就不应该放到一个类中。违反了类的单一职责原则。

未来将框架代码和业务代码解耦，可以使用代理模式。

如一个需要统计UserController接口的数据，可以创建一个代理类UserControllerProxy，使得两个类实现相同的接口IUserController。UserController类只负责业务功能，代理类负责在业务代码执行前后执行附加其他逻辑代码，并通过为委托的方式来调用原始类来执行业务代码。

除了实现相同的接口的方式，如果原始类没定义接口，可以采用继承方式。

但是上面的方式存在两个问题。

1. 代理中需要将原始类的所有方法都重新实现一遍，并且每个方法附加相似的代码逻辑；
2. 如果添加的附加功能的类不止一个，需要针对每个类创建一个代理类。

上面的问题可以通过**动态代理**来解决。

使用动态代理时，不事先为每个原始类编写代理类，而是在运行时，动态地创建原始类对应的代理类，然后在系统中用代理类替换掉原始类。

在Java语言，实现动态代理比较简单，Java语言本身提供了动态代理的语法。（实际上动态代理依赖的是Java反射的语法）

对于Spring AOP，其底层实现原理基于动态代理。用户配置好哪些类创建代理，并定义好执行原始类的业务代码前后执行哪些附加功能。Spring为这些类创建动态代理对象，并在JVM中替换原始类对象。原本在代码中执行的原始类的方法，被换为执行代理类的方法，实现了给原始类添加附加功能的目的。

> 应用场景

代理模式的应用场景众多，常见的有：

1. **业务系统的非功能性需求开发；**

   业务系统中开发的非功能性需求，如：监控、统计、鉴权、限流、事务、幂等、日志。将这些附加功能与业务功能解耦，放到代理类中统一处理，让程序员只关注业务方面开发。

2. **代理模式在RPC中的应用；**

   RPC框架可以看做一种代理模式，通过远程代理，将网络通信、数据编解码等细节隐藏。客户端使用RPC访问时就像调用本地函数一样，无需了解跟服务器交互的细节。除此之外PRC访问的开发者字需要开发业务逻辑，就像开发本地使用的函数一样，不需关注跟客户端的交互细节。

3. **代理模式在缓存中的应用；**

   开发一个接口请求的缓存功能，对于某些接口请求，如果入参相同，在设定的时间内，直接返回缓存结构，而不用重新进行逻辑处理。如针对用户个人信息需求，可以开发两个接口，一个支持缓存、一个支持实时查询。

   对于缓存查询接口，可以使用AOP切面完成接口缓存的功能。在应用启动时从配置文件加载需要支持缓存的接口以及缓存策略。请求到来时，在AOP切面拦截请求，如果请求中带支持缓存的字段（如http://.....&cached=true)，可以从缓存中获取数据返回。

#### 桥接模式

桥接模式将抽象和实现解耦，让它们可以独立变化。

一个类存在两个（或多个）独立变化的维度，可以通过组合的方式，让这两个（或多个）维度可以独立机型扩展。

JDBC驱动是桥接模式的经典应用。在典型的JDBC使用时需要经历一下流程：

1. 使用`Class.forName`加载具体的数据库驱动，如`com.mysql.cj.jdbc.Driver`；
2. 在加载`com.mysql.cj.jdbc.Driver`类时，会执行类中static代码块；
3. static代码块中会把具体的驱动Driver注册到DriverManager中；
4. 由于不同的数据库驱动都是实现了JDBC提供的接口；
5. 因此在将具体驱动注册到DriverManager之后，后继所有的JDBC操作都会委派到具体的Driver中执行；

> 代码1：经典JDBC开发流程

```java
// 1、加载具体的Driver实现到DriverManager中
Class.forName("com.mysql.cj.jdbc.Driver");
String url = "jdbc:mysql://localhost:3306/sample_db?user=root&password=root";
///2、使用DriverManager获取Connection对象
// getConnection内部会遍历加载到DriverManager的具体Driver
Connection con = DriverManager.getConnection(url);
Statement stmt = con.createStatement();
String query = "select * from test";
ResultSet rs = stmt.executeQuery(query);
while (rs.next()) {
    rs.getString(1);
    rs.getInt(2);
}
```

#### 装饰器模式

Java IO类库庞大而复杂，有十几个类，负责IO数据的读取和写入。JavaIO按照数据流动的方向以及数据单位可以分为InputStream、OutputStream、Reader、Writer

针对不同读取和写入场景，JavaIO又在这四个父类基础上，扩展出很多子类。

![image-20210908160133011](https://gitee.com/tobing/imagebed/raw/master/image-20210908160133011.png)

Java IO类库通过上面的这些类，可以实现功能丰富的IO操作。从JDK的源码可以看到，是通过使用组合代替继承的方式来避免继承结构太过复杂的情况。

```java
public class FilterInputStream extends InputStream {

    protected volatile InputStream in;

    protected FilterInputStream(InputStream in) {
        this.in = in;
    }
}
```

实际上，Java IO类库使用的是装饰器模式，不仅是简单的「用组合代替继承」，与之相比，装饰器模式主要有两个特点。

+ 装饰器类和原始类继承同样的父类，这样可以对原始类「嵌套」多个装饰器类；
+ 装饰器类是对功能的增强，这是装饰器模式应用场景的重要特点。

同于是基于「组合的关系」，代理模式与装饰器模式的区别是：

+ 代理模式中，代理类附加的是与原始类无关的功能，如日志、统计；
+ 装饰器模式中，装饰器附加的是跟原始类相关的增强功能，如字节流转成字符流；

```java
///// 代理模式代码结构 /////
public interface IA {
    void f();
}
public class A implements IA {
    public void f() { ... };
}
public class AProxy implements IA {
    pirvate IA a;
    public AProxy(IA a) {
        this.a = a;
    }
    public void f() {
        // 新添加的代理逻辑
        a.f();
        // 新添加的代理逻辑
    }
}


///// 装饰器模式代码结构 /////
public interface IA {
    void f();
}
public class A implements IA {
    public void f() { ... };
}
public class ADecorator implements IA {
    private IA a;
    public ADecorator(IA a) {
        this.a = a;
    }
    public void f() {
        // 功能增强代码
        a.f();
		// 功能增强代码
    }
}
```

![image-20210908163056652](https://gitee.com/tobing/imagebed/raw/master/image-20210908163056652.png)

可以看出DataInputStream与BufferedInputStream并非直接继承InputStream，而是继承与FilterInputStream。

InputStream是有个抽象类而非接口，大部分函数都有默认实现。如果需要实现缓存功能（BufferedInputStream），直接继承InputStream则需要所有的抽象方法，在这些方法中有一写时不需要增加缓存功能的。对于这部分代码也必须要重写，这样一来就会导致重复编码问题。

对于DataInputStream同样存在这样的问题，直接继承InputStream需要重写与当前功能流无关的函数。于是人们在BufferInputStream/DataInputStream与InputStream之间添加了一层FilterInputStream，用于实现那部分与功能无关的代码，这样一来BufferInputStream/DataInputStream就可以直接继承FilterInputStream，无需重写与增强功能无关的代码了。

#### 适配器模式

<font style="color:red">**适配器模式用于做适配，可以将不兼容的接口转换为可兼容的接口，让原本由于接口不兼容而不能在一起工作的类可以一起工作。**</font>

如日常生活中我们使用的手机电源适配器，可以将220V家用交流电转换为5V1A的手机用直流电。

**实现方式**

适配器模式主要有两种实现方式

+ 类适配器：使用继承关系实现
+ 对象适配器：使用组合关系实现

> 代码1：类适配器

> 代码2：对象适配器

对于这两种适配器的实现方式，在实际开发中可以根据以下的评判标准来选择：

+ 如果Adaptee方法不多，两种实现方式都可以；
+ 如果Adaptee方法很多，而且Adaptee和ITarget方法定义大部分相同，推荐使用类适配器，这样一来Adaptor可以省掉方法相同的部分，减少代码量；
+ 如果Adaptee方法很多，而且Adaptee和ITarget方法定义大部分不相同，推荐使用对象适配器，因为组合接口相对继承更加灵活。

**应用场景**

适配器模式主要有五种使用场景

+ **封装有缺陷的接口设计**

  如果依赖的外部系统在接口设计上有缺陷，引入之后会影响自身代码的可测试性。为了隔离这个设计上的缺陷，希望对外部接口进行二次封装，抽象出更好的接口设计。

+ **统一对个类的接口设计**

  某个功能的实现依赖多个外部系统或类，通过适配器将他们的接口适配为统一的接口定义，然后可以使用多态的特征来复用代码逻辑。

+ **替换依赖的外部系统**

  把项目中依赖的一个外部系统替换为另一个外部系统时，利用适配器模式，可以减少代码的改动。

+ **兼容老版本接口**

  版本升级时，对于一些废弃的接口，不直接将其删除，而是暂时保留，并且标注为deprecated，并将内部实现逻辑委托为新的接口实现。这样做可以让使用它的项目有个过渡期，不是强制进行代码修改。

  JDK1.0 中包含一个遍历集合容器的类 Enumeration。JDK2.0 对这个类进行了重构，将它改名为 Iterator 类，并且对它的代码实现做了优化。但是考虑到如果将 Enumeration 直接从 JDK2.0 中删除，那使用 JDK1.0 的项目如果切换到 JDK2.0，代码就会编译不通过。为了避免这种情况的发生，我们必须把项目中所有使用到 Enumeration 的地方，都修改为使用 Iterator 才行。

  单独一个项目做 Enumeration 到 Iterator 的替换，勉强还能接受。但是，使用 Java 开发
  的项目太多了，一次 JDK 的升级，导致所有的项目不做代码修改就会编译报错，这显然是
  不合理的。为了做到兼容使用低版本 JDK 的老代码，我们可以暂时保留 Enumeration 类，并将其实现替换为直接调用 Itertor。 

+ **适配不同格式的数据**

  适配器可以用于不同格式的数据之间的适配。如把不同征信系统拉取的不同格式的征信数据，统一为相同的格式，以方便储存和使用。如Java中的Arrays.asList()可以看做是一种数据适配器，将数据类转换为集合容器类型。

> 代码1：封装有缺陷的接口设计

> 代码2：统一多个类的实现

> 代码3：替换依赖的外部系统

**适配器在Java日志系统中的应用**

Java中有很多日志框架，在项目开发中，常常用它们来打印日志信息。如log4j、logcback、JUL、JCL等。

大部分日志都提供了相似的功能，如按照不同级别打印日志等。但是他们并没有实现统一的接口，这主要是引用历史原因，不像JDBC那样，一开始就定义了数据库操作的规范。

如果只开发一个自用的项目，日志框架选择并不是太大的问题。但是如果开发的是一个集成到其他系统的组件、框架、类库等，日志框架选择就没有那么容易。

比如，项目中某个组件使用log4j来打印日志，项目本身却使用的是logback。这样用来将组件引入项目之后。 

> 代理、桥接、装饰器、适配器4中设计模式的区别

代理、桥接、装饰器、适配器，四种是比较常用的结构型设计模式。代码结果非常类似。笼统而言，它们都可以成为Wrapper模式，即通过Wrapper类二叉封装原始类。

尽管代码结构相似，4种设计模式目的完全不同，主要区别如下：

+ **代理模式**：代理模式在不改变原始类接口的条件下，为原始类定义一个代理类，主要目的是控制访问，而非加强功能，这是与装饰器模式最大的不同。
+ **桥接模式**：桥接模式目的是将接口部分和实现部分分离，从而让它们较为容易、也相对独立加以改变。
+ **装饰器模式**：装饰者模式在不改变原始类接口的情况下，对原始类功能进行增强，并且支持多个装饰器的嵌套使用。
+ **适配器模式**：适配器模式是一种事后补救策略。适配器提供根原始类不同的接口，而代理模式、装饰器模式提供的是跟原始类相同的接口。

#### 门面模式

为了保证接口的可用性，需要将接口尽量设计细粒度一点，责任单一一点。但是，如果接口的粒度更小，在接口的使用者开发一个业务功能时，就会导致n多细粒度的接口才能完成，使得接口调用起来非常不方便。

相反，如果接口粒度设计的太大，一个接口返回n多数据，要做n多事情，就会导致接口不够通用、可复用性不好。接口不可复用，需要针对不同的调用的业务需求，就需要开发不同的接口来满足，就会导致系统的接口无限膨胀。

<font style="color:red">**门面模式，也叫外观模式，门面模式为子系统体用一组统一的接口，定义一组高层接口让子系统更容易使用。**</font>

门面模式主要有以下使用场景：

**解决易用性问题**

门面模式可以封装系统的底层实现，隐藏系统的复杂性，提供一组更加简答易用、更高层的接口。比如，Linux 系统调用函数就可以看做一种「门面」，是Linux操作系统暴露给开发者的一组「特殊」的编程接口，封装了底层更基础的 Linux 内核调用。再如 Linux 的 Shell 命令，实际上也可以看作一种门面模式的应用。它封装系统调用，提供更加友好、简单的命令，让我们可以直接通过执行命令来跟操作系统交互。

**解决性能问题**

利用门面模式可以将多个接口调用替换为一个门面接口调用，减少网络通信成本，提高APP客户端的响应速度。

**解决分布式事务问题**

支持两个接口调用在一个事务中执行实现起来比较困难，涉及到分布式事务。虽然可以通过引入分布式事务框架或事后补偿机制来解决，但代码是实现起来比较复杂。比较简单的方法是，利用数据库事务或Spring框架听的事务，一个事务中执行两个调用。

**门面模式 VS 适配器模式**

+ 适配器用于接口转换，解决的是原接口和目标接口不匹配的问题；【注重兼容性】
+ 门面模式左接口整合，解决的是多接口调用带来的问题。【注重易用性】

#### 组合模式

「组合模式」与「组合关系」是两码事。组合模式主要用于处理树形结构数据。

由于组合模式要求数据必须能够表示成树形结构，应用场景特殊，因此这种设计模式实际开发不是很常用。

<font style="color:red">**组合模式将一组对象组织成树形结构，以表示一种「部分-整体」的层次结构。组合让客户端可以统一单一对象和组合对象的处理逻辑。**</font>

组合系统可以运用关于树形结构的场景中。

如在一个OA系统中，公司的组织结构包含部门和员工两种数据类型。其中部门又可以包含子部门和员工。系统希望在内存中构建整个公司的人员架构图，并且提供接口计算部门的薪资成本。

在OA系统中，部门包含子部门和员工，是一种嵌套结构，可以表示成树形结构。计算部门的薪资开支，可以利用树的后序遍历算法。综上所述，这个使用场景可以使用组合模式来设计和实现。

#### 享元模式

享元，即共享的单元。享元模式的意图是复用对象，节省内存，享元的前提是享元对象是不可变对象。

具体而言，当一个系统中存在大量重复对象的时候，如果这些重复对象是不可变对象，可以利用享元模式将对象设计成享元，在内存中只保留一份实例，供多处代码引用。这样可以减少内存中对象的数量，起到节省内存的目的。实际上，不仅相同对象可以设计成享元，对于相似对象，可以将对象中相同的字段提取出来，设计成享元，让这些大量相似对象引用这些享元。

如开发一个棋牌游戏，一个游戏厅有成千上万「房间」，每个房间对应一个棋局。棋局要保存每个棋子的数据，如：棋子类型、棋子颜色、棋子在棋盘中的位置。利用这些信息可以显示一个完整的棋盘给玩家。假如棋子将「棋子类型、棋子颜色、棋子在棋盘中的位置」简单组织在同一各类，这样不同棋盘需要创建不同的棋子对象，如果有成千上万的棋局需要消耗大量内存。使用享元模式，将每一局棋子的共性「棋子类型、棋子颜色」组织在不可变对象中，其他棋局通过聚合这些类来共享这些独享，每个棋盘只需要记录棋子的位置信息即可。

实际使用时，享元模式通常结合工厂模式使用，在工厂类中，通过也Map来缓存已经创建过的享元对象，来达到复用的目的。

**享元模式 VS 单例**

+ 单例模式中，一个类只能一个对象；

+ 享元模式中，一个类不限制创建对象的数量，每个对象被多处代码引用共享。

**享元模式 VS 缓存**

+ 享元模式可以通过工厂类来缓存已经创建好的对象，此处缓存着重点是共享；
+ 平时的缓存主要是为了提高访问效率，而非复用。

**享元模式 VS 对象池**

+ 享元模式和对象池、线程池、连接池都是为了复用；
+ 池化技术的复用可以理解为「重复使用」，目的是节省时间；
+ 享用模式的复用可以理解为「共享使用」，目的是节省空间。

**享元模式在JDK中的运用**

在 Java 中的Integer、String等运用了享用模式。

JDK 1.5开始提供的自动装箱实际上是调用了Integer.valueOf方法，是编译器提供的语法糖。Integer.valueOf的源码如下。

```java
public static Integer valueOf(int i) { 
    if (i >= IntegerCache.low && i <= IntegerCache.high)    
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);    
}
```

从上面源码可以可以知道，当int的范围在[low, high]之间是，自动装箱返回的对象是缓存中的对象。也就是说，在这个范围中的对象是共享的。

除此之外，Java中的包装类，如Long、Short、Byte、Character等都使用了享用模式。

在 JVM 中有一块存储器专门用于储存字符串常量，称为「字符串常量池」。当采用字面量或调用String.intern方法可以将字符串放到字符串常量池中，多个字符串对象共享字符串常量池中的字符串。与Integer等包装类不同的是，包装类的共享对象在JVM启动时就创建，而String的共享对象允许在运行过程中动态创建。

**使用注意**

需要注意，享元模式对JVM的垃圾回收不太友好。因为享元工厂类一直保存了对享元对象的引用，这就导致享元对象在没有任何其他代码引用的情况下，也不会被JVM垃圾回收机制自动回收。因此在对象生命周期较短且不被密集使用你的时候，利用享元模式反而可能会浪费更多的内存。

### 行为型

创建型解决了「对象创建」问题；

结构型解决了「类或对象的组合或组装」问题；

行为型解决了「类或对象之间的交互」问题。

#### 观察者模式

观察者模式，又称发布订阅模式。

<font style="color:red">**在对象之间定义一个一对多的依赖，当一个对象状态改变时，所有依赖的对象会自动收到通知。**</font>一般而言，被依赖的对象称为被观察者，依赖的对象叫做观察者。

实际中，观察者模式比较抽象，可以根据不同场景和需求有完全不同的实现方式。有同步阻塞的实现方式，也有异步非阻塞的实现方式；有进程内的实现方式，也有跨进程的实现方式。

如对于用户组成，需要触发两个行为：注册和发放体验金。如果将它们都放在注册接口中，违反了单一职责原则。这时，如果需求频繁变动，如用户注册成功之后，不再发放体验金，而改为发放优惠卷，除此之外还需要给用户发送一封「欢迎注册成功」的站内信。这种情况下，需要频繁的更改用户注册接口，违反了「开闭原则」。而且用户注册成功之后需要执行的后继操作越来越多，那register函数的垃圾会变得越来越复杂，影响到了代码的可读性和可维护性。

对于以上的情况，可以使用观察者模式进行重构。注册成功之后处理的事件只需要以新的观察者身份添加即可。而注册成功之后，只需要遍历通知所有观察者即可。这样一来，如果需要在注册之后添加新的行为，只需要将其添加到观察者列表中即可，**实现了观察者和被观察者的代码解耦**。

```java
// 观察者接口
public interface RegObserver {
    void handleRegSuccess(long userId);
}
// 促销观察者
public class RegPromotionObserver implement RegObserver {
    @Autowired
    private PromotionService promitionService;
    // 发放体验金
    public void handleRegSuccess(long userId) {
        promitionService.issueNewUserExperienceCash(userId);
    }
}
// 注册通知观察者
public class RegNotificationObserver implement RegObserver {
    @Autowired
    private NotificationService notificationService;
    // 发送欢迎注册成功站内信
    public void handleRegSuccess(long userId) {
        notificationService.sendInboxMessage(userId, "Welcome...");
    }
}

// 被观察者
public class UserController {
    @Autowired
    private UserService userService; 
	private List<RegObserver> regObservers = new ArrayList<>();
    // 设置观察者列表
    public void setRegObservers(List<RegObserver> observers) {
		regObservers.addAll(observers);
	}
    // 注册接口
    public Long register(String telephone, String password) {
		long userId = userService.register(telephone, password);
        // 变量通知观察者
		for (RegObserver observer : regObservers) {
			observer.handleRegSuccess(userId);
		}
        return userId;
	}
}
```

观察者模式应用广泛，小到代码层面解耦合，大到架构层面的系统耦合都有观察者模式的影子。不同的应用场景和需求下，观察者模式有截然不同的实现方式。在上述例子中，观察者和被观察者位于同一个进程且采用的是同步阻塞的方式。在复杂系统中，可以通过创建新的线程或引入消息队列来实现异步非阻塞。

基于消息队列的实现方式，被观察者和观察者解耦更叫彻底，被观察者完全不感知观察者，观察者也完全不感知被观察者。观察者只管发送消息到消息队列，观察者只管从消息队列中读取消息来执行相应逻辑。

**EventBus**

除此之外，观察者模式还也可以通过 Google Guava EventBus 框架实现，它不仅支持异步非阻塞模式，同时也支持同步阻塞模式。使用流程如下：

1. 实例化EventBus（EventBus表示同步阻塞，AsyncEventBus表示异步非阻塞）；
2. 通过EventBus.register注册的观察者；通过EventBus.unregister注销观察者；
3. 通过@Subscribe注解标名类中那个函数可以接受被观察者发送的消息；【特殊之处】
4. 在被观察者中，通过EventBus.pos()向被观察者发送信息。

EventBus的核心数据结构是Observer注册表，它记录了消息类型和可接受消息函数的对应关系。当调用register函数注册观察者时，EventBus通过解析@Subscribe注解，删除Observer注册表；当调用post函数发送消息时，EventBus通过注册表找到可接受消息的函数，然后通过Java反射语法来动态创建对象、执行函数。对于同步阻塞模式，EventBus在一个线程内依次执行相应函数，对于一部非阻塞模式，EventBus通过一个线程池执行相应函数。

**发布订阅 vs 生产消费**

发布-订阅模型是一对多关系，订阅者之间没有竞争关系，可以以同步/异步方式实现；

生产-消费模型是多对多关系，消费者之间存在竞争关系，一般以异方式实现。

两者都可以达到解耦合的作用。

#### 模板模式

模板模式主要用于解决**复用**和**扩展**两个问题。

<font style="color:red">**模板模式，全称模板方法设计模式。模板方法模式在一个方法中定义算法骨架，并将某些步骤推迟到子类中实现。模板方法模式可以让子类在不改变算法结构的情况下，重新定义算法中的某些步骤。**</font>

+ **算法**：可以理解「广义的业务逻辑」，而非数据结构与算法中的算法；
+ **算法骨架**：包含算法骨架的方法是「模板方法」；

**复用**

模板方法模式把一个算法中不变的流程抽象到父类的模板方法中，将可变的部分留给子类来实现。所有子类都可以复用父类中模板方法定义的流程代码。

在Java IO 类库中，有很多类设计用到了模板方法模式，如InputStream、OutputStream、Reader、Writer等。如在InputStream中，read函数是一个模板方法，定义了读取数据的整个流程，并且保留了一个可以由子类定制的抽象方法。

在 Java AbstractList类中，addAll函数同样可以看做模板方法，add()是子类需要重写的方法，默认实现直接抛出了异常，要求子类使用时必须实现。

**扩展**

模板方法模式的扩展性表现在框架的扩展性。利用模板方法模式编写框架，可以让框架使用者在不修改框架源码的基础上，定制化框架的功能。

【Servlet】

在使用 Servlet 时，通常需要定义一个继承了 HttpServlet 的类，并重写其中的 doGet或 doPost方法分别处理 get 和 post 请求。除此之外还需要再配置文件 web.xml进行配置，从而让 Tomcat、Jetty等Servlet容器启动时，可以加载这个文件配置URL与Servlet的映射关系。Servlet容器在接收请求时，根据URL和Servlet的映射关系，找到相应的Servlet，执行它的service方法。HttpServlet中的service方法会调用doGet或doPost方法。

在HttpServlet中，service可以看做是模板方法，而doGet和doPost等是模板中可以由子类定制的部分。实际上，这相当于Servlet框架提供了扩展点，让框架用户在不修改Servlet框架源码的情况下，将业务代码通过扩展点嵌套到框架中执行。

【JUnit】

除此之外，JUnit框架也通过模板模式提供了一些功能扩展点，让框架用户可以在扩展点上扩展功能。

在使用JUnit框架编写测试丹玉时，编写的测试类都要继承框架提供的TestCase类。在TestCase类中，runBare函数是模板方法，定义了执行测试用例的整体流程：先执行 setUp做准备工作，然后执行 runTest运行真正的测试代码，最后执行 tearDown做扫尾工作。

**回调**

回调和模板方法模式类似，同样具有复用和扩展的功能。

实际上，回调不仅可以应用在代码设计上，在更高层次的架构设计也很常见。如通过第三方支付系统实现支付功能，用户在发起支付请求之后，一般不会一直阻塞在支付结果返回，而是注册回调接口给第三方支付系统，第三方支付系统执行完成之后，将结果通过回调接口返回给用户。

回调可以分为同步回调和异步回调。同步回调表示在函数返回之前执行回调函数；异步回调指函数返回之后执行回调。从应用场景而言，同步回调更像模板方法模式，异步回调更像过程中模式。

【JdbcTemplate】

Spring提供了很多 Template 类，如JdbcTemplate、RedisTemplate、RestTemplate，它们基于同步回调实现，由于同步回调从应用场景很像模板模式，因此命名上使用Template作为后缀。

Java 提供了JDBC类库来封装不同类型的数据库操作。但直接使用JDBC来编写操作数据库的代码使用起来比较复杂。在原生JDBC中需要编写很多与业务无关的逻辑，如加载驱动、创建数据库连接、创建statement、关闭连接、关闭statement、处理异常等。针对不同的SQL执行请求，这些流程的代码时相同的、可以服用。

针对上述问题，Spring 提供了 JdbcTemplate，对JDBC进一步封装，来简化数据库编程。使用 JdbcTemplate查询用户信息，只需要编写与业务有关的代码即可，如查询用户SQL语句、查询结果与 实体对象之间映射等。

JdbcTemplate通过回调机制，将不变的流程抽离出来，放到模板方法execute中，将可变部分设计成回调 StatementCallaback，有用户定制。

【setClickListener】

在客户端开发中，经常给控件注册事件监听器。

```java
Button button = (Button)findViewById(R.id.buttion);
button.setOnClickListener(new OnClickListener() {
   @Override
    public void onClick(View v) {
        System.out.println("I am clicked.");
    }
});
```

从代码结构看，事件监听器类似于回调，即传递一个包含回调函数的对象给另一个函数。从应用场景，它很像观察者模式，即事先注册观察者，当用户点击按钮时，发送点击事件个观察者，并执行响应的onClick函数。

【addShutdownHook】

Hook，钩子，与Callaback类似。

Hook 比较经典的应用场景是 Tomcat 和 JVM 的 shutdown hook。JVM提供了 Runtime.addShutdownHook(Thread hook)方法，可以注册一个JVM关闭的Hook。当程序关闭时，JVM会自动调用Hook代码。

**模板模式 vs 回调**

从应用场景看，同步回调和模板方法模式几乎一致。都是在大的算法骨架中，自由替换其中的某个步骤，起到代码复用和扩展的目的。而异步回调跟模板方法有很大的差别，更像是观察者模式。

从代码实现看，回调和模板方法完全不同。回调基于组合关系实现，把一个对象传递给另一个对象，是一种对象关系；模板方法模式基于继承关系实现，子类重写父类的抽象方法，是一种类之间的关系。

组合优于继承，在代码实现上，回调相当于对模板模式更加灵活，主要体现在：

1. Java只支持单继承，基于模板模式编写的子类，已经继承了一个父类，不再具备继承能力；
2. 回调可以使匿名类来创建回调对象，可以不事先按定义类；而模板方法模式针对不同实现都要定义不同子类；
3. 如果某个类定义了多个模板方法，每个方法都有对应的抽象方法，即使只用其中的一个模板方法，子类页必须实现所有的抽象方法。而回调则更加灵活，只需要往用到模板方法中注入回调对象即可。

#### 策略模式

策略模式可以避免冗长的 if-else 或 switch 分支判断，除此之外还可以提供框架的扩展点。

<font style="color:red">**定义一族算法类，将每个算法分别分装起来，让他们可以相互替换。策略模式可以使算法的变化独立于使用它们的客户端。**</font>

工厂模式解耦对象的创建和使用；观察者模式解耦观察者和被观察者；策略模式解耦「策略定义、创建、使用」三部分。

**策略定义**

策略类包含了「一个策略接口」和「一组实现了该接口的策略类」。

由于所有策略类都实现相同的接口，因此客户端代码基于接口而非实现编程，可以灵活替换不同的策略。

**策略创建**

策略模式会包含一组策略，使用时一般会通过type判断创建哪个策略使用。为了封装创建策略，需要对客户端代码屏蔽创建细节。可以根据type创建策略的逻辑抽离到工厂类中。

```java
public class StrategyFactory {
    private static final Map<String, Strategy> strategies = new HashMap<>();
    static {
        strategies.put("A", new StrategyA());
        strategies.put("B", new StrategyB());
    }
    public static Strategy getStrategy(String type) {
        return strategies.get(type);
    }
}
```

一般而言，如果策略类无状态，不包含成员变量，只是纯粹的算法实现，主要的对象可以被共享使用，不行在每次getStrategy的时候重新创建新对象，只需要是在工厂类中事先创建好每个策略对象，缓存其中。如果策略类有状态，根据业务场景需要，可以在工厂类中每次都是创建新的策略对象。

**策略使用**

策略模式包含了一组策略，客户端一般会在运行时确定使用哪种策略。

**消除if-else**

在确定使用的具体策略是，往往需要通过if-else来判断具体使用哪个策略对象。这是可以结合工厂模式，在工厂模式中通过Map来缓存策略，根据type从Map中获取对应的策略，从而避免if-else分组判断逻辑。

#### 责任链模式

模板方法模式、策略模式以及责任链模式都具有**复用**和**扩展**的功能，在框架开发中可以利用它们来提供框架的扩展点，能够让框架使用者在不修改框架源码的情况下，基于扩展点定制化框架的功能。

<font style="color:red">**责任链模式将请求的方和接收解耦，让多个接收对象都有机会处理这个请求。将这些接收对象串成一条链，并沿着这条链传递这个请求，直到链上的某个接收对象能够处理它位置。**</font>

责任链模式中，多个处理器依次处理同一个请求。一个请求先经过A处理器处理，然后把请求递给B处理器，B处理器处理完毕在递给C处理器，以此类推，形成一条链条。链条上的每个处理器各自承担各自的处理职责，因此称为责任链模式。

责任链模式可以有多种实现方式，如用链表储存处理器和用数组储存处理器。

责任链的实现包含了处理器接口（IHandler）或抽象类（Handler），处理器链（HandlerChain）。

责任链模式可以运用于论坛中社区中的UGC的敏感词过滤。我们可以针对不同的过滤规则创建不同的处理器，让UCG依次执行这些处理器的过滤方法，执行不同的过滤逻辑。

另外，责任链模式在框架中运用广泛，可以为框架提供扩展点。具体而言，责任链模式最常用在开发框架的过滤器和拦截器。

**Servlet Filter**

Servlet Filter是Java Servlet规范定义的组件，可以实现对HTTP请求的过滤功能，如鉴权、限流、记录日志、验证参数等。由于是Servlet规范的一部分，因此只要支持Servlet的Web容器都支持过滤器功能。

Servlet Filter使用起来并不困难，执行流程主要有以下几个步骤：

1. 定义实现Filter接口的过滤器类；
2. 将实现的过滤器配置到web.xml；
3. Web容器启动时会读取web.xml配置，创建过滤器对象；
4. 当有请求到达，先经过过滤器，然后才由Servlet处理。

从上面可以知道，添加过滤器非常方便，不需要修改任何代码，符合开闭原则。

Servlet Filter本质上利用了责任链模式，其中Filter接口对应责任链模式中的处理器接口，FilterChain对一个能力责任链中的处理器链。其中Tomcat对FilterChain实现ApplicationFilterChain核心实现如下：

```java
public final Class ApplicaitonFilterChain implements FilterChain {
    private int pos = 0;
    private int n;
    private ApplicationFilterConfig[] filters;
    privaet Servlet servlet;
    
    public void doFilter(ServletRequest request, ServletResponse response) {
        if (pos < n) {
            ApplicationFilter config = filters[pos++];
            Filter filter = config.getFilter();
            filter.doFilter(request, response, this);
        } else {
            servlet.service(request, response);
        }
    }
    
    public void addFilter(ApplicationFilterConfig filterConfig) {
        for (ApplicationFilterConfig filter : filters) {
            if (filter == filterConfig) {
                return;
            }
        }
        if (n == filters.length) {
            // 扩容
            ApplicationFilterConfig[] newFilters =
                new ApplicationFilterConfig[n + INCREMENT];
            System.arraycopy(filters, 0, newFilters, 0, n);
            filters = newFilters;
        }
        filters[n++] = filterConfig;
    }
}
```

doFilter方法中支持双向拦截，即能拦截客户端发送的请求，也能拦截发送给客户端的响应。

**Spring Interceptor**

Spring提供的拦截器也可以用于实现对HTTP请求进行拦截处理。

Spring Interceptor 与 Server Filter不同的是：

+ Servlet Filter是Servlet规范的一部分，实现依赖于Web容器；
+ Spring Interceptor 是 Spring MVC框架的一部分，有Spring MVC框架提供实现；

客户端发送的请求，先经过Servlet Filter，然后再经过 Spring Interceptor，最后到达业务代码中。

Spring Interceptor的底层仍然是通过责任链模式实现，HandlerExecutionChain类是责任链模式中的处理器链。但与Servlet不同的是，Spring Interceptor 将请求和响应的的拦截工作拆分到两个函数中实现。

```java
public class HandlerExecutionChain {
    private final Object handler;
    private HandlerInterceptor[] interceptors;
    
    public void addInterceptor(HandlerInterceptor interceptor) {
        this.initInterceptorList().add(interceptor);
    }
    
    boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HandlerInterceptor[] interceptors = this.getInterceptors();
        if (!ObjectUtils.isEmpty(interceptors)) {
            for(int i = 0; i < interceptors.length; this.interceptorIndex = i++) {
                HandlerInterceptor interceptor = interceptors[i];
                if (!interceptor.preHandle(request, response, this.handler)) {
                    this.triggerAfterCompletion(request, response, (Exception)null);
                    return false;
                }
            }
        }
        return true;
    }

    void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv) throws Exception {
        HandlerInterceptor[] interceptors = this.getInterceptors();
        if (!ObjectUtils.isEmpty(interceptors)) {
            for(int i = interceptors.length - 1; i >= 0; --i) {
                HandlerInterceptor interceptor = interceptors[i];
                interceptor.postHandle(request, response, this.handler, mv);
            }
        }
    }
    
    void triggerAfterCompletion(HttpServletRequest request, 
                                HttpServletResponse response, 
                                @Nullable Exception ex) throws Exception {
        HandlerInterceptor[] interceptors = this.getInterceptors();
        if (!ObjectUtils.isEmpty(interceptors)) {
            for(int i = this.interceptorIndex; i >= 0; --i) {
                HandlerInterceptor interceptor = interceptors[i];
                try {
                    interceptor.afterCompletion(request, response, this.handler, ex);
                } catch (Throwable var8) {
                    logger.error("HandlerInterceptor.afterCompletion threw exception", var8);
                }
            }
        }

    }
}
```

Spring 框架中，DispatchServlet的doDispatch方法来分发请求，他在真正的业务逻辑执行前后，执行HandlerExecutionChain中的applyPreHandle和applyPostHandle函数，用来实现拦截功能。

**Servlet Filter vs Spring Interceptor vs AOP**

三者都可以实现鉴权，但鉴权的粒度有所不同。

+ Filter 可以获取到原始HTTP请求，但无法获取请求控制器以及请求控制器的方法信息；

+ Interceptor 可以获取请求的控制器以及控制器的方法信息，但拿不到请求方法的参数；

+ AOP可以拿到方法参数，但拿不到HTTP请求和响应对象信息；

如果访问控制功能要精确到每个请求，需要使用AOP；AOP可以配置每个Controller的访问权限。而Spring Interceptor和Servlet Filter的粒度相对要粗一点，控制HttpRequest和HttpResponse的访问。除此之外Servlet Filter不能使用Spring容器的资源，只能在Web容器中启动时调用一次，而Spring Interceptor是一个Spring组件，归Spring管理，配置在Spring文件中，因此可以使用Spring的任何资源、对象，如Service对象、数据源、事务管理等，通过IoC注入到Interceptor即可。相比而言，Spring Interceptor更加灵活。

#### 状态模式

状态模式一般用于实现状态机，而状态机常用在游戏、工作流引擎等系统开发。

有限状态机，Finite State Machine，FSM，简称状态机。状态机由状态(State)、事件(Event)、动作(Action)3部分组成。事件也称转移条件，事件触发状态的转移以及动作的执行。

如在「超级玛丽」中，马里奥有多种状态，如Small、Super、Fire、Cape等。不同的游戏情节中，各个形态会相互转化，并响应的增加积分。在这个过程中：

+ 马里奥状态的转变是一个状态机；
+ 游戏是状态机的事件；
+ 加减积分就是状态机中的动作；

状态机的实现有多种方式，如：分支逻辑法、查表法和状态模式。

**分支逻辑法**

最简单直接的实现方式是参照状态转移图，将每一个状态转移直接翻译成代码。转移编写的代码存在大量的if-else或switch-case分支判断，甚至嵌套分支判断。

对于简单的状态机而言，分支逻辑的实现方式是可接受的。但对于复杂的状态机而言，实现方式很容易漏写某个状态转移，其他不利于代码的可读性以及可维护性。

**查表法**

处理状态转移图之外，状态机还可以使用二维表来表示。在二维表中，第一维表示当前状态，第二维表示事件，值表示当前状态经过事件之后，转移到的新状态以及执行的动作。

![image-20211002204634463](https://gitee.com/tobing/imagebed/raw/master/image-20211002204634463.png)相当于分支逻辑的实现方式，查表法的代码实现更加清晰，可读性和可维护性更好。当修改状态机时，只需要修改二维数组即可。

```java
enum Event {
    GOT_MUSHROOM(0),
    GOT_CAPE(1),
    GOT_FIRE(2),
    GOT_MONSTER(3);
    private int value;
    private Event(int value) {
        this.value = value;
    }
    public int getValue() {
        return this.value;
    }
}

class MarioStateMachine {
    private int score;
    private State currentState;
    private static final State[][] transitionTable = {
            {SUPER, CAPE, FIRE, SMALL},
            {SUPER, CAPE, FIRE, SMALL},
            {CAPE, CAPE, CAPE, SMALL},
            {FIRE, FIRE, FIRE, SMALL}
    };
    private static final int[][] actionTable = {
            {+100, +200, +300, +0},
            {+0, +200, +300, -100},
            {+0, +0, +0, -200},
            {+0, +0, +0, -300}
    };
    public MarioStateMachine() {
        this.score = 0;
        this.currentState = State.SMALL;
    }
    public void obtainMushRoom() {        executeEvent(Event.GOT_MUSHROOM);    }
    public void obtainCape() {        executeEvent(Event.GOT_CAPE);    }
    public void obtainFireFlower() {        executeEvent(Event.GOT_FIRE);    }
    public void meetMonster() {         executeEvent(Event.MET_MONSTER);    }
    private void executeEvent(Event event) {
        int stateValue = currentState.getValue();
        int eventValue = event.getValue();
        this.currentState = transitionTable[stateValue][eventValue];
        this.score = actionTable[stateValue][eventValue];
    }
    public int getScore() {        return this.score;    }
    public State getCurrentState() {        return this.currentState;    }
}
```

**状态模式**

查表法的代码实现中，事件触发的动作只是简单的积分加减，因此可以使用int类型的二维数组就能表示，二分数值中的值就表示积分的加减值。但实际的操作往往是一系列复杂操作，这是就没办法通过简单的二维数组来表示。因此查表法具有一定的局限性。

状态模式通过将事件触发的「状态转移」和「动作执行」拆分到不同的状态类中，避免分支判断的逻辑。其中：

+ IMario是状态的接口，定义了所有事件。SmallMario、SuperMario、CapeMario、FireMario是IMario接口的实现类，分别对应状态机中的4个状态。
+ 原来所有的状态转移和动作执行的代码逻辑都集中在MarioStateMachine类中，如今被代码逻辑被分散到4个状态类中。

实际上，像游戏这种比较复杂的状态机，包含的状态很多，优先推荐使用查表法，而状态模式会引入很多的状态类，会导致代码比较难维护。相反，像电商下单、外卖下单这种类型的状态机，它们的状态不多，状态转移页比较简单，但时间触发执行的动作包含的业务逻辑比较复杂，因此更加推荐状态模式实现。

#### 迭代器模式

迭代器模式，也称游标模式。可以用于遍历集合对象，集合对象可以是容器、聚合对象，如数组、链表、树、图、跳表等。迭代器模式将集合对象的遍历操作从集合中拆分出来，放到迭代器中，让两者的职责更加单一。

一个完整的迭代器模式通常包含两部分：容器 和 容器迭代器。但为了实现基于接口编程，容器又包含容器接口、容器实现类，迭代器由包含迭代器接口、迭代器实现类。

Iterator接口有两种定义方式：

+ 第一种：next函数用来将游标后移一位，currentItem函数用来返回当前游标执行的元素；
+ 第二种：返回当前元素与后移一位两个操作，要放到同一个函数next中完成；

迭代器总体的实现思路可以用三句话表示：

1. 迭代器需要定义hashNext、currentItem、next三个基本方法；
2. 待遍历的容器对象通过依赖注入传递到迭代器类中；
3. 容器通过iterator方法创建迭代器。

实际上，Java中的foreach循环只是一个语法糖，底层基于迭代器实现。

利用迭代器模式，封装了对复杂数据结构的遍历。比如，树前中后层序遍历，图的是广度深度优先遍历等。如果有客户端代码来实现这些遍历算法，势必增长开发成本，而且容易写错。如果将这部分遍历的逻辑写到容器类中，也会导致容器类代码的复杂性。

有了容器和迭代器的抽象接口，方便我们开发时，基于接口而非具体的实现编程。当需要切换新遍历算法时，如前序遍历切换到后序遍历，客户端代码只需要将迭代器更换即可，其他代码可以不需要修改。除此之外，添加新的遍历算法，只需要扩展系拿到迭代器类，符合开闭原则。

迭代器模式运用广泛，常见的有如：

+ MySQL ResultSet类提供的first、last、previous等方法可以看做是一种迭代器；
+ JDK 中的 ArrayList、LinkedList等容器类都实现了迭代器；

通过迭代器遍历集合元素时，增加或删除集合中的元素可能导致某个元素重复遍历或遍历不到。但并不是所有情况下遍历都会出错，有时也能正常遍历。因此在遍历的同时增删元素的行为属于**结果不可预期行为**或**未决行为**。

如正在对一个数组进行迭代，这时删除数组中一个元素，将会导致元素整体往前挪动，这将可能导致一些数据不被遍历到；如果往数组中间添加一个元素，将会导致元素整体往后挪动。对于这些未决行为，可以有两种解决方案：

+ ① 遍历的时候不允许增删元素；【实现较为困难】
+ ② 增删元素之后遍历的时候报错；【JDK采用的方式】

第一种实现方式较为困难。

第二种实现方式更为合理，也是Java语言采用的方式。Java中通过在集合容器类中添加modCount成员变量来统计集合被修改的次数，集合没调用一次增加或删除元素的函数，就将modCount加1,。当调用集合元素的iterator函数创建迭代器是，把modCount值传递给迭代器expectedModCount成员变量，之后每次调用迭代器的hasNext、next、currentItem函数，都会检查modCount是否等于expectedModCount，如果两个值不同，表示集合储存的元素已经改变，之前创建的迭代器已经不能正确运行，在继续使用会产生不可预期的结果，因此选择「**fail-fast**」解决方式，抛出运行时异常，结束掉程序。

**遍历时安全删除集合元素**

在Java的迭代器中定义了remove方法，能够在遍历集合的同时安全地删除集合元素。但remove方法比较鸡肋，作用有限。它只能删除游标指向的前一个元素，而且一个next函数只能跟一个remove操作，多次调用remove操作会报错。

 Java中的迭代器通过新增一个lastRet成员变量，用来记录游标指向的前一个元素。同迭代器输出这个元素时，可以更新迭代器中的游标和lastRet值，这样来保证不会因为删除元素而导致某个元素遍历不到。如果通过容器来删除元素，并且希望更新迭代器的游标值来保证遍历不出错。要维护这个容器创建了那些迭代器，每个迭代器是否还在使用等信息，代码实现变得比较复杂。

**支持快照的迭代器**

如果需要一个支持快照的迭代器模式，就要在容器创建迭代器的时候给容器拍一张快照。之后及时增删容器中的元素，快照中的元素并不会做相应的改动。而迭代器遍历的对象是快照而非容器，这样可以避免在使用迭代器遍历的过程中，删除容器中元素导致不可预期的结果或报错。

实现支持快照的迭代器可以有两种实现方式。

【方式一】

最简单的发那个还是就是在迭代器中定义一个成员变量snapshot来储存快照。每当创建迭代器时，拷贝一份容器中元素到快照中，后继的遍历操作基于这个迭代器持有的快照进行。

这种方法虽然简单，但代价高，每次创建迭代器的时候都要拷贝一份数据到快照中，会增加内存的消耗。如果一个容器同时有多个迭代器在遍历元素，就会导致数据重复储存多份。

【方式二】

第二种方式是在容器中为每个元素保存两个时间戳，一个是添加时间戳，一个是删除时间戳。当元素被加入集合时记录添加时间戳，当元素被删除时记录删除时间戳。

每个容器迭代器在创建时记录一个时间戳，当使用迭代器遍历容器时，只有元素的：添加时间戳 < 迭代器创建时间戳 < 删除时间戳 的元素才允许添加到迭代器结果中。

但采用时间戳的方式会导致数组失去随机访问的性质，这时可以采用两个数组来分别记录支持标记删除和不支持标记删除的。

实际上这类似于MySQL的MVCC策略。

#### 访问者模式

<font style="color:red">**访问者模式允许一个或多个操作应用到一组对象上，解耦操作和对象本身。**</font>

一般而言，访问者模式针对一组类型不同的对象（ResuorceFile的子类PDFFile、PPTFile、WORDFile）。在不同的应用场景下，需要对这组对象进行一系列不相关的业务操作，但为了避免不断添加功能导致类的不断膨胀，职责越来越不单一，以避免频繁添加功能导致的频繁代码修改，使用访问者模式可以将对象与操作解耦，将这些业务代码抽离出来，定义在独立细分的访问者类中（Extractor、Compressor）。

假如需要将多种资源文件(pdf/ppt/word)中的内容提取出来放到txt文本中，可以抽象一个ResourceFile类，包含一个extract2txt()方法。PDFFile、PPTFile、WordFile都继承ResourceFile，分别实现不同类型文件提取txt的方法。

如果工具功能不但扩展，不仅要抽取文本内容，还要支持压缩、提取文件原信息、构建索引等一系列功能，继续安装上述的实现思路，会存在几个问题：

+ 违反开闭原则，新加一个功能，所有类的代码都改变了；
+ 虽然功能增多，每个类的代码也在不断膨胀，可达性和可维护性都变差；
+ 把比较上层的业务逻辑都耦合到PdfFile、PPTFile、WordFile类中，导致类的职责不够单一。

针对上述问题，可以进行拆分解耦，将业务操作跟具体的数据结构解耦合，设计成独立的类。

```java
// 抽象资源类
public abstract class ResuorceFile {
    protected String filePath;
}
// PDF资源类
public class PDFFile extends ResuorceFile {}
// PPT资源类
public class PPTFile extends ResuorceFile {}
// WORD资源类
public class WORDFile extends ResuorceFile {}

// 文本提取类
public class Extractor {
    public void extract2Txt(PDFFile pdfFile) {...}
    public void extract2Txt(PPTFile pptFile) {...}
    public void extract2Txt(WORDFile File) {...}
}

public class Application {
    public static void main(String[] args) {
        Extractor extractor = new Extractor();
        List<ResuorceFile> files = listAllResuorceFiles();
        for (ResuorceFile file : files) {
            // 下面语句无法通过
			extractor.extract2Txt(file);
        }
    }
}
```

在上面代码中，把提取文本内容的操作设计为三个重载的函数，但在Application中的代码将会发生编译错误。原因是在Java编译期间无法获取对象的实际类型，会执行静态绑定，根据对象声明的类型来确定函数的重载。由于在上面代码中，三个Extractor.extract2Txt的参数类型都是ResuorceFile，无法确定具体调用哪一个。

而为了能够利用运行时的动态绑定，可以在ResuorceFile抽象类中引入accept方法用于子类重写调用。

```java
// 抽象资源类
public abstract class ResuorceFile {
    protected String filePath;
    abstract public void accept(Extractor extractor);
}
// PDF资源类
public class PDFFile extends ResuorceFile {
    public void accept(Extractor extractor) {
        extractor.extract2Txt(this);
    }
}
// PPT资源类
public class PPTFile extends ResuorceFile {
    public void accept(Extractor extractor) {
        extractor.extract2Txt(this);
    }
}
// WORD资源类
public class WORDFile extends ResuorceFile {
    public void accept(Extractor extractor) {
        extractor.extract2Txt(this);
    }
}

// 文本提取类
public class Extractor {
    public void extract2Txt(PDFFile pdfFile) {...}
    public void extract2Txt(PPTFile pptFile) {...}
    public void extract2Txt(WORDFile File) {...}
}

public class Application {
    public static void main(String[] args) {
        Extractor extractor = new Extractor();
        List<ResuorceFile> files = listAllResuorceFiles();
        for (ResuorceFile file : files) {
			file.accept(file);
        }
    }
}
```

此时，如果继续添加新的功能，如压缩功能，只需要实现类似于Extractor的新类Compressor类，在其中定义三个重载的函数，实现对不同类型文件的压缩。除此之外还要找每个资源文件类中定义新的accept重载函数。

**单分派 vs 双分派**

+ **单分派**：执行哪个对象的方法，根据对象的运行时类型决定；执行对象的那个方法，根据方法的参数**编译时**类型来决定；
+ **双分派**：执行哪个对象的方法，根据对象的运行时类型决定；执行对象的那个方法，根据方法的参数**运行时**类型来决定；

其中的「单」可以理解为执行哪个对象的哪个方法只跟「对象」的运行时类型有关；「双」可以理解为执行哪个对象的哪个方法与「对象」和「方法参数」的运行时类型有关。

单分派和双分派跟多态和函数重载直接相关。目前主流面向对象，如Java、C#、C++都只支持单分派，不支持双分派。

#### 备忘录模式

<font style="color:red">**备忘录模式在不违背封装原则的前提下，捕获一个对象的内部状态，并在该对象之外保存这个状态，以便之后恢复对象为先前的状态。**</font>

备忘录模式主要包含了两部分内容：

+ 储存副本以便后期恢复；
+ 在不违背封装的前提下，进行对对象备份和恢复；

备忘录模式应用场景有限，主要用于放丢失、撤销、恢复等。它与平常提到「备份」很相似。两者主要区别在于，备忘录模式侧重于代码的设计与实现，备份更侧重架构设计或产品设计。

对于大对象的备份来说，备份占用的存储空间较大，备份和恢复花费的时间较长。针对这个问题，不同业务场景有不同的处理方式。如只备份必要的恢复信息，结合最新数据来恢复；如全量备份和增量备份相结合，低频全量备份，高频增量备份。

具体可以参考Redis和MySQL的备份方案。

#### 命令模式

<font style="color:red">**命令模式将请求(命令)封装为一个对象，主要可以使用不同的请求参数化其他对象(将不同请求依赖注入到其他对象)，并且能够支持请求(命令)的排队执行、记录日志、撤销等功能。**</font>

命令模式核心实现手段是「将函数封装成对象」。在C语言中支持函数指针，可以把函数当做变量进行传递。但大部分编程语言，函数无法作为参数传递给其他函数，也无法赋值给变量。借助命令模式，可以将函数封装成对象。具体而言就是将函数封装到类中，实例化一个对象实现函数的传递。从实现角度看，类似于前面提到的回调。

将函数封装为对象之后，对象就可以储存下来，方便控制执行。因此命令模式的主要作用和应用场景是用于控制命令的执行。如异步、延迟、排队执行命令、撤销重做命令、储存命令、给命令记录日志等。

如在游戏端开发中，游戏客户端与服务器的通信非常频繁，为了节省为了连接开销，客户端和服务器之间一般采用长连接方式实现通信。通信的内容一般包含两部分：指令和数据。指令可以看做事件，数据时执行这个指令的数据。

服务器在接收到客户端的请求之后，会解释出指令和数据，并根据指令不同，执行不同的处理逻辑。具体实现可以有两种架构实现：

+ ① 利用多线程，一个线程接收请求，接收到请求之后，启动一个新线程处理。
+ ② 在线程内轮询接收请求和处理请求。尽管无法利用多线程多核处理的优势，但对于IO密集型的业务而言，避免了多线程不断切换对象性能的损耗。

第二种实现方式在手游后端服务器开放中比较常见。手游后端服务器轮询获取客户端发送的请求，获取请求之后借助命令模式，把请求包含的数据和处理逻辑封装为一个对象，储存在内存队列中。然后从队列中取出一定数量的命令执行，执行完毕在重新开始新的一轮轮询。

**命令模式 vs 策略模式**

设计模式的对比可以从两方面入手：

+ 应用场景，即设计模式可以解决哪些问题；
+ 解决方案，即设计模式的设计思路以及具体的代码实现。

策略模式包含策略的定义、创建和使用三部分，从代码结构上看，很像工厂模式。但两者的区别是，策略模式侧重「策略/算法」的特定应用场景，用于解决运行时状态从一组策略中选择不同策略的问题，而工厂模式侧重封装对象的创建过程，这里的对象没有任何业务场景的限定，可以是策略，也可以是其他东西。

在策略模式中，不同策略具有相同的目的、不同的实现、互相之间可以替换。而在命令模式中，不同命令具有不同目的，对应不同的处理逻辑，并且相互之间不可替换。

#### 解释器模式

解释器模式相对小众，只在特定的领域中被用到，如编译器、规则引擎、正则表达式。

<font style="color:red">**解析器为某个语言定义它的语法表示，并定义一个解释器来解释这个语法。**</font>

解释器模式实现的核心思想是将语法解析的工作拆分到各个小类中，以此避免大而全的解析类。一般做法是将语法规则拆分为一些小的独立单元，然后给对每个单元进行解析，最终合并为对整个语法规则的解析。

如在监控系统中，需要支持自定义告警规则，如下面的表达式：

```java
api_error_per_minute > 100 || api_count_per_minute > 10000
```

表示每分钟API总出错数超过100或每分钟API总调用数超过10000就触发告警。

在监控系统中，告警模块只负责根据统计数据和告警规则决定是否触发告警，统计信息可以通过其他模块获取。

```java
Map<String, Long> apiStat = new HashMap<>();
apiStat.put("api_error_per_minute", 103);
apiStat.put("api_count_per_minute", 987);
```

假设自定义告警规则只包含「||、&&、>、<、==」，其中「>、<、==」运算符的优先级高于「||、&&」运算符。在表达式转给你，任意元素之间需要通过空格分隔。

实际上可以将自定义告警规则看做一种特殊的语言的语法规则。需要实现一个解析器，能够根据规则，针对用户输入的数据，判断是否触发告警。利用解析器模式，把解析表达式的逻辑拆分到各个小类中，避免大而复杂的大类出现。

#### 中介模式

<font style="color:red">**中介模式定义了一个单独的对象，来封装一组对象之间的交互，将这组对象之间的交互委派给中介对象交互，来避免对象之间的直接交互。**</font>

中介模式的设计实现很像中间层，通过引入中介这个中间层，将一组对象的交互关系从多对多转换为一对多（网状结构==>星型结构）。原来一个对象要与n个对象交互，现在只需要根一个中介对象交互，从而最小化对象之间的交互关系。降低了代码的复杂度，提供了代码的可读性和可维护性。

如在航空管制中，为了让飞机飞行的时候互不干扰，每架飞机都需要知道其他飞机每时每刻的位置，如果没有中介者，就需要时刻跟其他飞机通信。不同飞机之间形成的通信网络将会非常复杂。如果引入了塔台这个中介者，让每架飞机只跟塔台通信，发送自己的位置给塔台，有塔台统一调度，可以大大简化通信网络。

实际上，微服务中的注册中心可以理解为广义上的中介模式，防止各个服务键错综复杂的调用。

**中介模式 vs 观察者模式**

中介模式和观察者模式都是用于对象关系的解耦。中介模式中对象通过中介者解耦，观察者模式中观察者与被观察者通过消息队列进行解耦。

在观察者模式中，经一个参与者计科院是观察者，也可以是被观察者，但大部分情况下，交换关系是单向的，一个参与者要么是观察者，要么是被观察者，不会兼具两种身份。即观察者模式的应用场景中，参与者的交互关系比较有条理

而中介模式正好相反，只有当参与者之间关系错综复杂，维护成本很高时，才考虑使用中介模式。

### 总结

#### 创建型

创建型设计模式包含了：单例模式、工厂模式、建造者模式、原型模式。它主要用于解决对象的创建问题，封装复杂的创建过程，解耦对象的创建代码和使用代码。

**【单例模式】**

单例模式用来创建全局唯一的对象。一个类只允许创建一个对象，这类就是单例类，这种设计模式成为单例模式。单例有几种经典的实现方式，分别是：懒汉是、饿汉式、双重检查、静态内部类、枚举。

尽管单例很常用，但有些人认为单例实例一种反模式，不推荐使用，主要有原因有：

+ 单例对OOP特性支持不友好；
+ 单例会隐藏类之间的依赖关系；
+ 单例对代码的扩展性不友好；
+ 单例对代码的可测试性不友好；
+ 单例不支持有参的构造函数；

基于上述的问题，可以采用其他方式来实现全局唯一类，如工厂模式、IoC容器等。

**【工厂模式】**

工厂模式包含简单工厂、工厂方法、抽象工厂三种细分模式。其中，简单工厂和工厂方法比较常见，抽象工厂的应用场景比较特殊，因此很少使用。

工厂模式用来创建不同但类型相关的对象（继承同一父类或接口的一组子类），有给定的参数来决定创建哪种类型的对象。实际上，如果创建对象的逻辑不复杂，可以直接通过new方式创建。当创建逻辑比较复杂，可以考虑工程模式封装对象的创建过程，将对象的创建和使用分离。

当每个对象的创建逻辑到比较简单，可以使用简单工厂模式，将多个对象的创建逻辑放到一个类中。当每个对象的创建逻辑比较复制，为了避免设计过于庞大的工厂类，推荐使用工厂方法模式，将创建逻辑拆分更细，每个对象的创建逻辑独立到各自的工厂类中。

工厂模式主要有以下4个作用：

+ **封装变化**：创建逻辑有可能变化，封装成工厂类之后，创建逻辑的变更对使用者透明；
+ **代码复用**：创建代码抽取到独立的工厂类只会可以复用；
+ **隔离复杂性**：封装复杂的创建逻辑，调用者无需了解如何创建对象；
+ **控制复杂度**：将创建代码抽离，让原本的函数或类职责更单一，代码更简洁。

除此之外，工厂模式还被用于依赖注入框架，如Spring IOC、Google Guice，它用于集中创建、组装、管理对象，根具体业务代码解耦，让程序员聚集在业务代码的开发。

**【建造者模式】**

建造者模式用于创建复杂对象，可以通过不同的可选参数，定制化创建不同的对象。建造者模式的原理和实现比价简单。

如果一个类中有很多属性，为了避免构造函数的参数列表太长，影响代码的可读性和易用性，可以通过构造函数配合set方法解决，但如果存在以下情况可以考虑使用建造者模式：

+ 类中存在很多的必填参数，如果把参数直接放到构造函数中设置，构造函数会出现参数很长的问题。如果把必填参数通过set方法设置，校验必填属性逻辑就无法存放；
+ 如果类的属性之间存在一定的依赖关系或约束条件，继续使用构造函数配合set方法的设计思路，那么依赖关系或约束条件的校验逻辑将无处安放；
+ 如果希望创建不可变对象，即对象创建好之后，不能再修改内部的属性值，要实现这个功能，不能在列中暴露set方法。构造函数配合set方法来设置属性值变得不适用；

**【原型模式】**

如果对象创建的成本大，且同一个类中的不同对象之间差别不大，可以利用已有的对象进行复制的方式，来创建新对象，以达到节省创建时间的目的。这种基于原型来创建对象的方式称为原型模式。

原型模式有两种实现方式，深拷贝和浅拷贝。浅拷贝指复制对象中基本数据类型时间和引用对象的地址，不会递归地复制引用对象，以及引用对象的引用对象；而深拷贝得到的是一份完完全全独立的对象。因此深拷贝比浅拷贝而言，更加耗时，更加消耗内存空间。

如果要拷贝的对象是不可变对象，浅拷贝共享不可变对象是没问题的，但对于可变对象而言，浅拷贝得到的对象和元素对象会共享部分数据，就有可能出现数据被修改的风险，也就变得复杂很多。除非 操作非常韩式，比较推荐使用浅拷贝，否则没有充分理由，不要为了一点点性能提升而使用浅拷贝。

#### 结构型

结构型模式主要总结了一些类或对象组合在一起的经典结构，这些经典结构可以解决特定的应用场景问题。结构型模式包括了：代理模式、桥接模式、装饰器模式、适配器模式、门面模式、组合模式、享元模式。

**【代理模式】**

在不改变原始类接口的条件下，为原始类定义一个代理类，主要目的是控制访问，而非加强功能，这是它与装饰器模式最大的不同。一般情况下，让代理类与与原始类实现同样的接口。但是，如果原始类没有定义接口，并且原始代码并不是我们开发维护的。这种情况下，可以通过让代理类继承原始类的方法来实现代理模式。

静态代理需要针对每个类都创建一个代理类，并且每个代理类的代码有点像模板式的重复代码，增加了维护成本和开发成本。因此可以采用动态代理实现代理功能。使用动态代理不用事先为每个原始类编写代理类，而是在运行时动态创建原始类对应的代理类，然后在系统中用代理类替换掉原始类。

代理模式常用于在业务系统中开发一些非功能性需求，如：监控、统计、鉴权、限流、事务、幂等、日志。将这些附加功能与业务功能解耦，放在代理类中统一处理，让程序员只需要关注业务方面的开发。除此之外，代理模式还可以用于RPC、缓存等应用场景。

**【桥接模式】**

桥接模式代码实现简单，但理解不容易，且使用场景有限。一般而言桥接模式使用并不常见。

桥接模式有两种理解方式。第一种理解方式是「将抽象和实现解耦，让它们独立开发」这种理解方式比较特别，且应用场景不多。另外一种理解方式比较简单，等同于「组合优于继承」设计原则，这种理解更加通用，应用场景较多。不管哪种理解方式，代码的结构都是相同的，都是一种类之间的组合关系。

对于第一种理解，需要分清「抽象」和「实现」的概念。其中抽象并非指抽象类或接口、而是被抽象的一套类库，只包含骨架代码，真正的业务逻辑需要委派到定义中的实现来完成。而定义中的实现也并非接口的实现类，而是的一套独立的类库。抽象和实现独立开发，通过对象之间的组合关系组装在一起。

**【装饰器模式】**

装饰器模式主要解决继承关系过于复杂的问题，通过组合来替代继承，给原始类添加增强功能。这也是判断是否使用装饰器的一个重要依据。除此之外，装饰器模式还有一个特点，就是对原始类嵌套使用多个装饰器。为了满足这样的需求，在设计时装饰器类需要跟原始类继承相同的抽象类或接口。

**【适配器模式】**

代理模式、装饰器模式提供的都是源原始类相同的接口，而适配器模式提供跟原始类不同的接口。适配器模式用于做适配，将不兼容的接口转换为可兼容的接口，让原本由于接口不兼容而不能在一起工作的类可以一起工作。适配器模式有两种实现方式：类适配器和对象适配器。其中类适配器使用继承关系实现，对象适配器使用组合关系来实现。

适配器模式是一种事后补救策略，用于补救设计上的缺陷。应用这种模式是「无奈之举」，常用于以下场景：

+ 封装有缺陷的接口设计；
+ 统一多个类的接口设计；
+ 替换依赖的外部系统；
+ 兼容老版本接口；
+ 适配不同格式的数据；

**【门面模式】**

门面模式原理、实现简单，应用场景明确。通过封装细粒度的接口，提供组合各个粒度接口的高层次接口，提高接口的易用性，或解决性能、分布式事务等问题。

**【组合模式】**

组合模式与面向对象中的组合关系是不同的概念。组合模式主要用于处理树形数据结构。因为其应用场景特殊，数据必须能表示成树形结构，这导致了这种设计模式在实际开发中并不是很常见。但是，一旦数据满足树形结构，应用这种模式就能发挥很大的作用，能让代码变得非常简洁。

组合模式的设计思路实际上更像是对业务场景的一种数据结构和算法的抽象。其中数据可以表示成树型结构，将单个对象和组合对象都看作树中的节点，以统一处理逻辑，并且利用属性结构的特点，递归处理每个树，以此简化代码实现。

**【享元模式】**

享元，即被共享的单元。享元模式的意图是复用对象，节省内存，前提是享元是不可变对象。

具体而言，当一个系统中存在重复对象，可以利用享元模式，将对象设计成享元，在内存中只保留一份实例，供多处代码引用，这样可以减少内存中对象的数量，以起到节省内存的目的。实际上，不仅仅相同的对象可以设计成享元的，对于相似的对象，也可以将内部相同的部分提前设计成享元的，让大量相似的对象引用这些享元的。

#### 行为型

行为型主要解决「类或对象之间的交互」问题。包含了：观察者模式、模板模式、策略模式、职责链模式、迭代器模式、状态模式、访问者模式、备忘录模式、命令模式、解释器模式、中介模式。

**【观察者模式】**

观察者模式将观察者与被观察者代码解耦。观察者模式应用场景广泛，小岛代码解耦，大到架构层面的系统解耦，或者缠产品的设计思路，都有观察者模式的影子，如邮件订阅、RSS Feeds，本质上是观察者模式。

不同应用场景和需求下，这个模式有截然不同的实现方式：有同步阻塞方式、有异步非阻塞方式；有进程方式，有跨进程的是实现方式。同步阻塞时最经典的实现方式，主要为了代码解耦；异步非阻塞处理能实现代码解耦之外，还能提高代码的执行效率；进程间的过程中模式更加解耦，异步基于消息队列，用来实现不同进程间的观察者与被观察者之间的交互。

框架的作用有隐藏实现细节，降低开发难度，实现代码复用，解耦业务与非业务代码，让程序员聚焦业务开发。针对异步非阻塞观察者模式，我们也可以将它抽象成 EventBus 框架来达到这样的效果。EventBus 翻译为“事件总线”，它提供了实现观察者模式的骨架代码。我们可以基于此框架非常容易地在自己的业务场景中实现观察者模式，不需要从零开始开发。  

**【模板模式】**

模板方法模式在一个方法中定义一个算法骨架，并将某些步骤推迟到子类中实现。模板方法模式可以让子类在不改边算法整体结构的情况下，重新定义算法的某些步骤。这里的算法可以理解为广义的业务逻辑，并不特指数据结构与算法中的算法。

其中算法的骨架就是模板，包含算法骨架的方法就是模板方法，即模板方法模式名字由来。

模板方法模式有两大作用：复用和扩展。复用表示所有子类可以复用父类的中提供的模板方法的代码。扩展表示，框架可以通过模板模式提供的扩展定，让框架用户可以在不修改框架源码的情况下，基于扩展点定制化框架的功能。

软件开发中回调和模板模式具有相同作用：代码的复用和扩展。在框架、类库、组件等设计中经常被用到，如JdbcTemplate就是用来回调。

相对于普通的函数调用，回调是一种双向的调用关系。A类事件先主从某个函数F到B类，A类在调用B类的P函数时，B了反过来调用A类主从给他的F函数。这里的F函数就会回调函数。

回调可以细分为同步回到和异步回调。从应用场景上看，同步回调更像模板模式，异步回调看起来更像观察者模式。回调跟模板模式的区别更多体现在代码实现，而非应用场景。回调基于组合关系实现，模板模式基于继承关系是实现。回调比模板更加灵活。

**【策略模式】**

策略模式定义一组算法类，将每个算法分别封装起来，让它们可以相互替换。策略模式可以是算法的变化独立于使用它们的客户端。策略模式用于解耦策略的定义、创建、使用。实际上，一个完整的策略模式有这三部分组成。

策略类的定义比较简单，包含了一个策略接口和一组实现这个接口的策略类。策略的创建由工厂完成，封装策略创建的细节。策略模式包含一组策略可选，客户端代码选择使用哪个策略，有两种确定方法：编译时静态确定和运行时动态确定。其中「运行时动态确定」是策略模式最经典的应用场景。

实际项目开发中，策略模式比较常用。最常见的应用场景是，利用它避免冗长的if-else或switch分支判断。不过它的作用远不止如此。它可以先模板模式那样，提供框架的扩展点。实际上，策略模式主要作用还是解耦策略的定义、创建和使用，控制代码的复杂度，让每个部分不至于关于复杂、代码量过多。除此之外，对于复制代码来说，策略模式还能让其满足开闭原则，添加新策略是，最小化、集中化代码改动，减少引入bug的风险。

**【职责链模式】**

在职责链模式中，多个处理器依次处理同一个请求。一个请求先经过A处理器处理，然后把请求传递给B处理器，B处理器处理完之后在传递个C处理器，以此类推，形成一条链条。链条上每个处理器各自承担各自的职责，因此叫做职责链模式。

职责链模式常用于开发框架中，用于实现过滤器、拦截器功能，让框架的使用者不需要修改框架源码的情况下，添加新的过滤、拦截功能。体现了对扩展开放、对修改关闭的设计原则。

**【迭代器模式】**

迭代器模式也称游标模式，用于遍历集合对象。迭代器模式主要作用是解耦容器代码和遍历代码。大部分编程语言都提供了现成的迭代器使用。

遍历集合一般有三种方式：for、foreach、迭代器遍历。后两者属于同一种，都可以看做是迭代器遍历。对于for循环遍历，利用迭代器可以有三个优势：

+ 迭代器模式封装集合内部的复杂数据结构，开发中不需要了解如何遍历，直接使用容器体用的迭代器即可；
+ 迭代器模式将集合对象的遍历操作从集合类拆分，放到迭代器中，让两者的职责更加单一；
+ 迭代器模式让添加新的遍历算法更加容易，更符合开闭原则。除此之外，因为迭代器实现各自相同的接口，在开发中，基于接口而非实现编程，替换迭代器也变得更加容易。

在通过迭代器遍历集合元素的时候，增加或删除集合中的元素，有可能会导致某个元素被重复遍历或遍历不到。针对这个问题，有两种比较干脆的解决方案，来避免出现这种不可预期的运行结果。一种是遍历时不允许增删元素，另一种是增删元素之后让遍历报错。第一种解决方案比较难实现，因为很难确定迭代器使用结束的时间点。第二种解决方案更加合理，Java采用的就是这种方法。删除元素之后，选择fail-fast解决方式，让遍历操作抛出运行时异常。

**【状态模式】**

状态模式一般用于实现状态机，而状态机在游戏、工作流引擎中比较常见。状态机交有限状态机，由三部分组成：状态、事件(转移条件)、动作。事件触发状态的转移以及动作的执行。但是动作不是必须的，也可能只转移状态，不执行任何动作。

针对状态机，有三种实现方式：

1. 分支逻辑法：利用if-else或switch-case分支逻辑，参照状态转移图，将每个状态转移原样直接翻译为代码。对于简单的状态机而言，这种实现方式最简单、最直接、是首选。
2. 查表法：对于状态很多，状态转移比较复杂的状态机而言，查表法比较合适。通过二维数组表示状态转移图，能极大提供代码的可读性和可维护性。
3. 状态模式：对于状态不多、状态转移比较简单，但事件触发执行的动作包含的业务逻辑可能比较复杂的状态机，首选这种实现方式。

**【访问者模式】**

访问者模式允许一个或多个操作应用在一组对象上，设计意图是解耦操作和对象本身，保持类职责单一、满足开闭原则以及应对代码的复杂性。

对于访问者模式，主要难点在于代码实现。而代码是实现比较复杂的原因是，函数重载在大部分面向对象语言中是静态绑定。即调用类的哪个重载函数，是在编译期间，有参数列表决定，而非运行时，根据参数的实际实际类型决定。

由于代码实现难以理解，因此项目中不推荐使用该模式。

**【备忘录模式】**

备忘录模式也称快照模式，具体而言，就是在不违背封装原则的前提下，捕获一个对象的内部状态，并在该对象之外保存这个状态，以便于恢复对象为先去状态。这个模式的定义表达了两部分内容：一部分是，存储副本以便后期恢复；另一部分是，在不违背封装原则的前提下，进行对象的备份和恢复。

备忘录模式应用场景比较明确和有限，主要用于防丢失、撤销、恢复等。跟平时提到的备份很相似。两者的主要区别在于，备忘录模式侧重于代码设计和实现，本分跟侧重于架构设计或产品设计。

对于大对象备份而言，备份占用的空间会比较大，备份和恢复的耗时会比较长，针对这个问题 ，不同的业务场景有不同的处理方式。比如，值备份必要的恢复信息，结合最新的数据来恢复；再比如，全量备份和增量备份相结合，低频全量备份，高频增量备份，两者集合做恢复。

**【命令模式】**

命令模式在平时工作并不常用。

命令模式核心实现是将函数封账成对象。在大部分编程语言中，函数无法作为参数传递给其他函数，也无法赋值给变量。借助命令模式，可以将函数封装成对象，可以实现把函数向对象一样使用。

命令模式的应用场景是用于控制命令的执行，如异步、延迟、排队执行命令、撤销重做命令储存命令、给命令记录日志等。

**【解释器模式】**

解释器模式为某个语言的定义它的语法表示，并定义一个解释器来处理这个语法。

要连接语言表达的信息，必须定义相应的语法规则。这样书写者可以根据语法规则来书写句子，阅读者根据语法规则来阅读句子，这样才能做到信息的正确传递。解释器模式主要用于根据语法规则解读句子的解释器。

解释器模式实现代码比较灵活，没有固定模板。代码实现的核心思想是将语法解析工作拆分到各个小类中，以此来避免大而全 解释类。一般做法是，将语法规则拆分一些小的独立单元，然后对每个单元进行解析，最终合并为对整个语法规则的解析。

**【中介模式】**

中介模式和中间层很像，通过引入中介这个中间层，将一组对象之间的交互关系从多对多转换为一对多。原来一个对象要跟n跟对象交互，现在只需要根一个中介对象交互，从而最小化对象之间的交互关系，降低代码的复杂度，提供代码的可读性和可维护性。

观察者模式和中介模式都是为了实现参与者之间的解耦，简化交互关系。两者不同在于应用场景。观察者模式的应用场景中，参与者之间的交互有条理，一般都是单向的，一个参与者只有一个身份，要么是观察者，要么是被观察者。而中介模式的应用场景中，参与者之间的关系错综复杂，既可以是消息发送者，也可以同时是消息接收者。

## 四、面向对象

### OOA、OOD与OOP

面向对象设计，OOP，Object Oriented Design。  

面向对象编程，OOP， Object Oriented Programming。  

面向对象编程语言，OOPL，Object Oriented Programming Language。  

在OOA和OOD阶段，需要围绕着对象或类来做需求分析和设计的。分析和设计两个阶段最终的产出是类的设计，包括程序被拆解为哪些类，每个类有哪些属性方法，类与类之间如何交互等等。它们比其他的分析和设计更加具体、更加落地、更加贴近编码，更能够顺利地过渡到面向对象编程环节。

简单点讲，面向对象分析就是要搞清楚做什么，面向对象设计就是要搞清楚怎么做，面向对象编程就是将分析和设计的的结果翻译成代码的过程。  

为了能够规范地进行OOA和OOD，可以使用UML来进行建模，但是由于其成本高，不是很推荐使用。

### UML

UML，Unified modeling language，统一建模语言。是一种用于软件系统分析和设计的语言工具，用于帮助软件开发人员进行思考和记录思路的结果。

UML本身是一套符号的规定，这些符号可以用于描述软件模型中的各元素之间的关系，如类、接口、实现、泛华、依赖、组合、聚合等。

UML图主要以下分类：

+ 用例图
+ 静态结果图：类图、对象图、包图、组件图、部署图
+ 动态行为图：交互图、状态图、活动图

其中，类图是描述类与类之间的关系，是UML中的核心。

UML中，类图用于描述系统中的类(对象)本身的组成和类(对象)之间的各种静态关系。类之间的关系包含了：依赖、泛化（继承）、实现、关联、聚合与组合等。

**依赖关系 Dependence**

只要是在类中用到了对方，那么他们之间就存在依赖关系。如果没有对方，连编 绎都通过不了。如

+ 类中用到了对方
+ 如果是类的成员属性
+ 如果是方法的返回类型
+ 是方法接收的参数类型
+ 方法中使用到

**泛化关系 Generalization**

泛化关系实际上就是继承关系，是依赖关系的特例。

**实现关系 Implementation**

实现关系实际上就是A类实现B接口，是依赖关系的特例。

**关联关系 Association**

关联关系实际上就是类与类之间的联系，他是依赖关系的特例。

关联具有导航性：即双向关系或单向关系。

关系具有多重性：如“1”（表示有且仅有一个），“0...”（表示0个或者多个）， “0，1”（表示0个或者一个），“n...m”(表示n到 m个都可以),“m...*”（表示至少m 个）。

**聚合关系 Aggregation**

聚合关系（Aggregation）表示的是整体和部分的关系，整体与部分可以分开。聚 合关系是关联关系的特例，所以他具有关联的导航性与多重性。

**组合关系 Composition**

组合关系也是整体与部分的关系，但是整体与部分不可以分开。

### 面向对象特性

面向对象具有是四大特性：封装、抽象、继承、多态。

#### 封装 Encapsulation

封装也叫作信息隐藏或者数据访问保护。类通过暴露有限的访问接口，授权外部仅能通过类提供的方式（或者叫函数）来访问内部信息或者数据。

封装的实现需要编程语言本身提供一定的语法机制来支持，这个机制就是访问权限控制。如Java中的private等权限修饰符。

通过封装，类通过有限的方法暴露必要的操作，一方面可以隐藏内部数据，保证数据的安全，另一方面可以隐藏类内部的复杂实现，提高类的易用性。

#### 抽象 Abstraction

抽象主要可以隐藏方法的具体实现，让调用者只需要关心提供哪些功能，并不需要知道这些功能的具体实现。

在面向对象中，通常借助编程语言提供的接口类（比如 Java 中的 interface 关键字
语法）或者抽象类（比如 Java 中的 abstract 关键字语法）这两种语法机制，来实现抽象。

抽象作为一种只关注功能点不关注实现的设计思路，可以帮我们的大脑过滤掉许多非必要的信息。  

很多设计原则都体现了抽象的设计思想，比如基于接口而非实现编程、开闭原则（对扩展
开放、对修改关闭）、代码解耦（降低代码的耦合性）。

#### 继承 Inheritance

继承是用来表示类之间的is-a关系。从继承关系上来讲，继承可以分为两种模式，单继承
和多继承。

为了实现继承这个特性，编程语言需要提供特殊的语法机制来支持，如Java使用了extend关注来实现继承，C++使用冒号等。

继承最大的好处就是代码复用。但是除了通过**继承**来实现**代码复用**，也可以通过**组合**关系来实现代码复用。需要注意的是，过度使用继承，继承层次太深态复杂，会导致代码可读性、可维护性变差。

> 思考：为什么有些语言支持多继承，有些语言不支持多继承？
>
> 多继承会带来菱形继承的问题。例如一个类的两个父类，都继承了同一个祖父类，两个父类都 override 了祖父类的方法，这时候孙子类就不知道如何调用了。  

#### 多态 Polymorphism

多态是指，子类可以替换父类，在实际的代码运行过程中，调用子类的方法实现。

多态这种特性也需要编程语言提供特殊的语法机制来实现：

+ 编程语言支持父类引用指向之类对象
+ 编程语言支持继承
+ 编程语言支持子类可以重写父类的方法

除了通过继承+重写的方式来实现，还可以通过实现的方式来实现。

利用多态可以提高代码的可扩展性和复用性。

### 面向过程与面向对象

与面向对象一样，面向过程编程也是一种编程范式或编程风格。它以过程（可以为理解方法、函数、操作）作为组织代码的基本单元，以数据（可以理解为成员变量、属性）与方法相分离为最主要的特点。面向过程风格是一种流程化的编程风格，通过拼接一组顺序执行的方法
来操作数据完成一项功能。  

以看出，面向过程和面向对象最基本的区别就是，代码的组织方式不同。**面向过程风格的代码被组织成了一组方法集合及其数据结构（struct User），方法和数据结构的定义是分开的。面向对象风格的代码被组织成一组类，方法和数据结构被绑定一起，定义在类中。**  

在项目中，尽管我们使用Java作为了开发语言，但是我们会经常写出面向过程的代码。

如直接用 IDE 或者 Lombok 插件自动生成所有属性的 getter、setter 方法，这种方式虽然使用简单，但是实际上它违反了面向对象编程的封装特性，是一种面向过程的编程风格。

另外在Java项目滥用全局变量和全局方法也会导致退化为面向过程编程，但是这些全局变量和全局方法也有一定的存在意义，因此在实际中需要去避免将全部常量定义到一个类中，而应该根据业务、功能将常量拆分到多个类中。

最后，传统的MVC模式中，一般情况下都需要要定义VO、BO、Entity，而这些类中只会定义数据，不会定义方法，所有操作这些数据的业务逻辑都定义在对应的 Controller 类、Service 类、Repository 类中。这也是典型的面向过程的编程风格。  

### 接口与抽象类

#### 抽象类

抽象类不允许被实例化，只能被继承。

抽象类可以包含属性和方法，方法包含了抽象方法和非抽象方法。

子类继承抽象类，必须实现抽象类中的所有抽象方法。 

#### 接口

接口不能包含属性（也就是成员变量）。

接口只能声明方法，方法不能包含代码实现。

类实现接口的时候，必须实现接口中声明的所有方法。  

#### 总结

继承关系是一种 is-a 的关系，抽象类属于类，表示的也是一种 is-a 的关系。

接口表示一种 has-a 的关系，表示具有某种功能。

抽象类更多的是为了代码复用，而接口就更侧重于解耦。接口是对行为的一种抽象，相当于一组协议或者契约。调用者只需要关注抽象的接口，不需要了解具体的实现，具体的实现代码对调用者透明。接口实现了约定和实现相分离，可以降低代码间的耦合性，提高代码的可扩展性。

#### 模拟接口

接口中没有成员变量，只有方法声明，没有方法实现，实现接口的类必须实现接口中的所有方法。只要满足上述定义，从设计角度可以认为是一个接口。

因此通过定义一个没有任何属性，并且方法都定义为abstract，这样一来就可以模拟一个接口；

除此之外还可以通过普通类类模拟一个接口，即让类中的所有方法都抛出异常，进而来限制用户在继承类的同时必须重写这些方法。除此之外通过为这个类的构造函数添加protected访问权限来避免该类实例化。

#### 基于接口而非实现的

基于接口而非实现编程，英文原文是“Program to an interface, not an implementation”。从本质来讲，“接口”就是一组“协议”或者“约定”，是功能提供者提供给使用者的一个“功能列表”  ，而不能简单理解为Java的接口。

通过这条原则，可以将接口和实现相分离，封装不稳定的实现，暴露稳定的接口。上游系统面向接口而非实现编程，不依赖不稳定的实现细节，这样当实现发生变化的时候，上游系统的代码基本上不需要做改动，以此来降低耦合性，提高扩展性。  

“基于接口而非实现编程”可以表示为“基于抽象而非实现编程”。

越抽象、越顶层、越脱离具体某一实现的设计，越能提高代码的灵活性，越能应对未来的需求变化。好的代码设计，不仅能应对当下的需求，而且在将来需求发生变化的时候，仍然能够在不破坏原有代码设计的情况下灵活应对。而抽象就是提高代码扩展性、灵活性、可维护性最有效的手段之一。  

条原则的设计初衷是，将接口和实现相分离，封装不稳定的实现，暴露稳定的接口。上游系统面向接口而非实现编程，不依赖不稳定的实现细节，这样当实现发生变化的时候，上游系统的代码基本上不需要做改动，以此来降低代码间的耦合性，提高代码的扩展性。  

如果在我们的业务场景中，某个功能只有一种实现方式，未来也不可能被其他实现方式替换，那我们就没有必要为其设计接口，也没有必要基于接口编程，直接使用实现类就可以了。  

#### 组合优于继承

继承是面向对象的四大特性之一，用来表示类之间的 is-a 关系，可以解决代码复用的问题。虽然继承有诸多作用，但继承层次过深、过复杂，也会影响到代码的可维护性。