package top.tobing.clazz_struct;

import java.io.Serializable;

/**
 * @author tobing
 * @date 2021/8/12 18:43
 * @description
 */
public class ClazzStructDemo implements Serializable {
    private String name;
    private int age;

    public int getAge(int userId) {
        return age;
    }
}

/**
 * |    执行「javap -verbose ClazzStructDemo.class」结果
 * |    Classfile /E:/Users/Tobing/Desktop/hand/hzero/hzero-sample-parent-develop/java-note/java-virtual-machine/target/classes/top/tobing/clazz_struct/ClazzStructDemo.class
 * |      Last modified 2021-8-12; size 506 bytes
 * |      MD5 checksum 0b7e748cdce9aa9beda6c66129e97b40
 * |      Compiled from "ClazzStructDemo.java"
 * |    public class top.tobing.clazz_struct.ClazzStructDemo implements java.io.Serializable
 * |      minor version: 0
 * |      major version: 52
 * |      flags: ACC_PUBLIC, ACC_SUPER
 * |    Constant pool:
 * |       #1 = Methodref          #4.#22         // java/lang/Object."<init>":()V
 * |       #2 = Fieldref           #3.#23         // top/tobing/clazz_struct/ClazzStructDemo.age:I
 * |       #3 = Class              #24            // top/tobing/clazz_struct/ClazzStructDemo
 * |       #4 = Class              #25            // java/lang/Object
 * |       #5 = Class              #26            // java/io/Serializable
 * |       #6 = Utf8               name
 * |       #7 = Utf8               Ljava/lang/String;
 * |       #8 = Utf8               age
 * |       #9 = Utf8               I
 * |      #10 = Utf8               <init>
 * |      #11 = Utf8               ()V
 * |      #12 = Utf8               Code
 * |      #13 = Utf8               LineNumberTable
 * |      #14 = Utf8               LocalVariableTable
 * |      #15 = Utf8               this
 * |      #16 = Utf8               Ltop/tobing/clazz_struct/ClazzStructDemo;
 * |      #17 = Utf8               getAge
 * |      #18 = Utf8               (I)I
 * |      #19 = Utf8               userId
 * |      #20 = Utf8               SourceFile
 * |      #21 = Utf8               ClazzStructDemo.java
 * |      #22 = NameAndType        #10:#11        // "<init>":()V
 * |      #23 = NameAndType        #8:#9          // age:I
 * |      #24 = Utf8               top/tobing/clazz_struct/ClazzStructDemo
 * |      #25 = Utf8               java/lang/Object
 * |      #26 = Utf8               java/io/Serializable
 * |    {
 * |      public top.tobing.clazz_struct.ClazzStructDemo();
 * |        descriptor: ()V
 * |        flags: ACC_PUBLIC
 * |        Code:
 * |          stack=1, locals=1, args_size=1
 * |             0: aload_0
 * |             1: invokespecial #1                  // Method java/lang/Object."<init>":()V
 * |             4: return
 * |          LineNumberTable:
 * |            line 10: 0
 * |          LocalVariableTable:
 * |            Start  Length  Slot  Name   Signature
 * |                0       5     0  this   Ltop/tobing/clazz_struct/ClazzStructDemo;
 * |
 * |      public int getAge(int);
 * |        descriptor: (I)I
 * |        flags: ACC_PUBLIC
 * |        Code:
 * |          stack=1, locals=2, args_size=2
 * |             0: aload_0
 * |             1: getfield      #2                  // Field age:I
 * |             4: ireturn
 * |          LineNumberTable:
 * |            line 15: 0
 * |          LocalVariableTable:
 * |            Start  Length  Slot  Name   Signature
 * |                0       5     0  this   Ltop/tobing/clazz_struct/ClazzStructDemo;
 * |                0       5     1 userId   I
 * |    }
 * |    SourceFile: "ClazzStructDemo.java"
 */
