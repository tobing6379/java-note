# Java-Virtual-Machine

日常的Java开发中，我们使用JDK提供的javac工具，对编写的源代码进行编译生成class文件，然后使用java工具，执行class文件。

在使用[java](https://docs.oracle.com/en/java/javase/13/docs/specs/man/java.html)工具时，它会启动Java虚拟机来加载指定的类，并执行类的main方法来执行程序。

## 类文件结构

我们常说，Java时一门跨平台的编程语言。在使用Java来编写程序的时候，我们使用编译工具生成的是与平台无关的class字节码文件，它运行在Java虚拟机之上，不通过平台运行着不同的Java虚拟机的示例。正是借助字节码文件，可以实现「一次编译，随处运行」特点。

Java虚拟机在执行字节码文件是，并不会关心字节码文件是否由Java语言编写编译得到的，只要符合《Java虚拟机规范》中定义的字节码文件格式，都可以运行与Java虚拟机之上。

### Class文件结构

《Java虚拟机规范》中定义的Class文件格式主要由以下几部分组成：

+ **magic：魔数**。固定为0xCAFFBABE，确定文件是否为虚拟机结束的Class文件
+ **minor_version：次版本号**。JDK1.2~JDK12，全部固定为0
+ **major_version：主版本号**。编译该类文件的JDK版本，用于向下兼容校验，虚拟机会拒执行超过其版本号的Class文件
+ **constant_pool_count：常量池容量计数值**。从1开始，0用于表示「不引用任何一个常量池项目」
+ **constant_pool：常量池**。主要存放两大类常量：字面量和符号引用
  + 字面量：Java语言层面的常量，如文本字符串、声明为final的常量值等。
  + 符号引用：Class文件不会保存各个方法、字段最终的内存布局。虚拟机做类加载时， 将会从常量池获得对应的符号引用， 再在类创建时或运行时解析、 翻译到具体的内存地址之中。
    + 模块导入或开放的包
    + 类和接口的全限定名
    + 字段名称和标识符
    + 方法名称和描述符
    + 方法句柄和方法类型
    + 动态调用点和动态常量
+ **access_flags：访问标志**。识别类或接口层次的访问信息（是否class/interface/public/abstract/final等）
+ **this_class：类索引**。确定类的全限定类名
+ **super_class：父类索引**。确定该类父类的全限定类名，只要一个
+ **interfaces_count：接口索引计数**。
+ **interfaces：接口索引**。按照implements后顺序从左到右排序
+ **fields_count：字段表集合容量计数**。
+ **fields：字段表集合**。描述接接口或类中声明的变量，包含类和实例变量，不包含方法内局部变量。一个字段可以包含以下信息
  + 作用域：public、private、protected
  + 实例变量/类变量(static修饰)
  + 可变性(final)
  + 并发可见性(volatile)
  + 是否可序列化(transient)
  + 字段类型：基本类型、对象、数组
+ **methods_count：方法表集合容量计数**。
+ **methods：方法表集合**。存储方法的描述，包含访问标志、名称索引、描述符索引、属性表集合。方法内的字节码指令不在方法表集合，而在属性表集合的Code属性。
+ **attributes_count：属性表集合容量计数**。
+ **attributes：属性表集合**。Class文件、 字段表、 方法表都可以携带自己的属性表集合， 以描述某些场景专有的信息。
  + Code：方法表中Java代码编译生成的字节码指令
  + ConstantValue：字段表中final关键字定义的常量值
  + Deprecated：类、方法表、字段表中被声明为deprecated的方法和字段
  + Exceptions：方法抛出的异常列表
  + EnclosingMethod：局部类或匿名类所在的外围方法
  + InnerClasses：内部类列表
  + LineNumberTable：Java源码的行号与字节码指令对应关系
  + LocalVariableTable：方法的局部变量描述
  + StackMapTable：检查和处理目标方法的局部变量和操作数栈所需要类型是否匹配
  + Signature：支持泛型情况下的方法签名。
  + SourceFile：源文件名称。
  + SourceDebugExtension：储存额外调试信息。
  + LocalVariableTypeTable：支持泛型。

> 补充1：简单名称、全限定名、描述符

+ 简单名称：没有制定类型和参数叙事的方法或字段名称
+ 描述符：模式字段数据类型、方法的参数列表(包含数量、类型以及顺序)和返回值
+ 全限定名：类全面中包「.」替换为「/」，用「;」分割多个全限定类名

基本数据类型(byte/char/double/float/int/long/short/boolean)以及无返回值(void)类型使用大写字符表示。数组类型在每个维度前使用前置「[」描述，如`java.lang.String[][]`表示为`[[Ljava/lang/String;`。

> 补充2：字段表集合

字段表集合不会列出从父类或父接口中继承而来的字段，但有可能出现Java代码不存在的字段，编译器会自动在内部类中添加执行外部类实例的字段，来保持外部类的访问性。

Java语言中字段无法重载，遵从名称唯一；对于Class文件格式，遵从描述符唯一。

> 补充2：方法表集合

父类中没有被子类重写的方法不会出现在方法表集合中，但有可能出现编译器自动添加的方法，如类构造器方法`<clinit>`方法实例构造器`cinit`方法。

重载要求方法有相同的方法名和有不同的方法签名：Java代码的方法签名只包含方法名称、参数顺序以及参数类型，注意没有返回值类型。

> 补充3：泛型支持-Signature

JDK 5新增，可选定长属性。在JDK 5里面大幅增强了Java语言的语法， 在此之后， 任何类、 接口、 初始化方法或成员的泛型签名如果包含了类型变量或参数化类型 ，则Signature属性会为它记录泛型签名信息。

之所以要专门使用这样一个属性去记录泛型类型， 是因为Java语言的泛型采用的是擦除法实现的伪泛型，字节码（Code属性）中所有的泛型信息编译（类型变量、参数化类型）在编译之后都通通被擦除掉。

使用擦除法的好处是实现简单（主要修改javac编译器， 虚拟机内部只做了很少的改动)、 非常容易实现向后兼容，运行期也能够节省一些类型所占的内存空间。但坏处是运行期就无法像C#等有真泛型支持的语言那样，将泛型类型与用户定义的普通类型同等对待，例如运行期做反射时无法获得泛型信息。Signature属性就是为了弥补这个缺陷而增设的，现在Java的反射API能够获取的泛型类型，最终的数据来源也是这个属性。 

> 补充4：注解支持-

JDK 5中，Java语法进行了多项增强，其中包含了提供对注解的支持。为了储存源码中的注解信息，Class文件增加了RuntimeVisibleAnnotations、RuntimeInvisibleAnnotations、 RuntimeVisibleParameterAnnotations和RuntimeInvisibleParameterAnnotations四个属性。

### 字节码指令

Java虚拟机的指令长度为一个字节，由操作码与操作数(可选)构成。Java虚拟机是面向操作数栈而不是面向寄存器的，因此大多数指令都不包含操作数。

Java虚拟机解析器在解析操作码时，执行模型如下：

```java
do {
	自动计算寄存器的值加1;
	根据PC寄存器指示的位置，从字节码流中取出操作数码;
	if (字节码存在操作数) 从字节码流中取出操作数;
	执行操作码定义的操作;
} while (字节码流长度 > 0)
```

在Java虚拟机的指令集中，并非每种数据类型和每一种操作都有对应指令。如大部分指令都没有支持`byte`、`char`和`short`，甚至没有支持`boolean`类型。编译器会在运行期将`byte`和`short`类型的数据带符号扩展为`int`，将`boolean`和`char`零位扩展为相应的`int`类型。

字节码按照功能可以分为9类。

+ 加载和存储指令：用于将数据从栈帧中的局部变量表和操作数栈之间进行传输

+ 运算指令：用于对两个操作树栈的值就行特定的运算，并把结果重新存入操作数栈顶

+ 类型转换指令：可以将不同的数值类型相互转换，这些转换一般用于是实现用户代码的显示类型转换

  + 宽化类型转换：int=>long/flaot/double、long=>float/double、float=>double，自动
  + 窄化类型转换：除了上的情况，其他必须显式使用转换指令，可能会存在精度丢失问题

+ 对象创建和访问指令：Java虚拟机把对类实例和数组的创建与操作使用不同的字节码指令

  + 类实例相关：new/getfield/putfield/getstatic/pustatic/instanceof/checkcast
  + 数组相关：newarray/anewarray/multinewarray/\<x\>aload/\<x\>astore/arrarylength

+ 操作数栈管理指令：操作操作数栈和操作普通栈类似，如pop/pop2/dup/dup2等

+ 控制转移指令：可以让Java虚拟机有条件或无条件地从指定位置指令的下一条指令继续执行程序（可以认为控制指令就是在有条件或无条件修改PC寄存器的值）

  + 条件分支：ifeq/iflt/ifle/ifne/ifnull/ifnonnull/if_icmpeq等
  + 符合条件分支：tableswitch/looupswitch
  + 无条件分支：goto/goto_w/jsr/jsr_w/ret

+ 方法调用和返回指令

  + invokevirutal：调用实例方法，根据对象实际类型分派
  + invokeinterface：调用接口方法，运行时搜索实现该接口方法的对象，找出方法调用
  + invokespecial：调用需要特殊处理的实例方法，如实例初始化方法、私有方法和父类方法
  + invokestatic：调用静态方法
  + invokedynamic：用于在运行时动态解析出调用点限定符所引用的方法。（与上面对比）

+ 异常处理指令：Java程序中显式抛出异常操作都由athrow指令完成。

  + 异常表：Java虚拟机中，处理异常不是由字节码指令来完成，而是采用异常表完成

+ 同步指令：用于支持方法级和方法内部一段指令序列的同步，同步结构是使用Monitor实现

  + 方法级同步是隐级的，无需通过字节码指令控制，其实现在方法调用和返回操作之中
  + 虚拟机会从方法常量池中的方法表结构ACC_SYNCHRONIZED判断方法是否同步
  + 在方法调用时，调用指令会检查方法的ACC_SYNCHRONIZED访问标志是否被设置
  + 如果同步方法执行期间抛出异常，而且方法内无处理，同步方法持有的管程会在异常抛出到同步方法外界时自动释放
  + Java虚拟机的指令集中有monitorenter和monitorexit两条指令来支持synchronized关键字的语义

  

  



