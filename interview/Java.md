# Java知识

> 对Java平台的理解

+ Java是面向对象语言，有两方面特征「一次编译，到处运行」以及「自动垃圾收集」；
+ 一次编译，到处运行允许Java轻易获得跨平台能力；
+ 自动垃圾收集可以使得程序员不用担心内存的分配和回收；
+ Java体系还包含了两个概念：JRE和JDK；
+ JRE，Java运行时环境，，包含了JVM和Java类库；
+ JDK，Java开发工具，是JRE超级，提供了更多的工具，如编译器，各种诊断工具等；
+ Java是半编译、半解析的语言；
+ 编写的源代码，先通过javac编译成字节码文件，运行时通过解析器将字节码转换为机器码；
+ 除此之外，大部分JVM还支持即时编译器，支持运行时将热点代码编译成机器码，这时属于编译执行。



> Exception与Error的区别

+ Exception与Error都继承了Throwable类，Java中只有Throwable类实例采用被try-catch或throw；
+ Exception和Error是Java平台对不同异常情况的分类；
+ Error是正常情况下，不大可能出现的错误，解决办法Error都会导致程序不可恢复；
+ Exception是程序正常运行中可以预料的意外情况，可能并且应该被捕获进行相应处理；
+ Exception又分为「检查异常」和「不检查异常」；
+ 检查异常必须在源代码中显式进行捕获处理，会在编译期进行检查；
+ 不检查异常指运行时异常，通常是可以通过编码避免的逻辑错误；
+ 常见的检查异常有IOException；
+ 常见的不检查异常有RuntimeException、ArrayIndexOutOfBoundException；
+  在对异常进行捕获时，通常不建议捕获通用的异常，而经历捕获特定的异常；

3、final、finally、finalize

