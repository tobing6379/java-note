package top.tobing.synchronized_lock;

/**
 * @author tobing
 * @date 2021/8/20 14:57
 * @description 锁消除-隐式同步操作
 * 锁消除是指虚拟机即时编译器在运行时，对于一些同步代码，如果检查到不可能存在数据竞争，则对其进行锁消除。
 * 锁消除的判定依据来源于逃逸分析的数据支持，如果一行代码中在堆中的所有数据都不会逃逸出其被其他线程访问，
 * 可以把他们当作栈上数据对待，由于栈上数据是线程私有的，因此无需同步。
 */
public class LockEliminationDemo {

    /**
     * 隐式同步操作
     * 使用加号对字符串进行拼接，会在Javac进行编译时被自动优化。
     * JDK5之前被转换为{@link java.lang.StringBuffer#append(String)}，内部是同步操作
     * JDK5及之后转换为{@link java.lang.StringBuilder#append(String)}，内部是非同步操作
     */
    public String concatString(String s1, String s2, String s3) {
        // JDK5中由于转换为StringBuffer#append()方法，内部是同步方法
        // 而在此方法中，不可能存在数据竞争，因此如果没有锁消除
        // 每次执行append方法是都需要执行同步指令，对性能不利
        return s1 + s2 + s3;

        /**
         * 【JDK1.5及之后】
         *  0 new #2 <java/lang/StringBuilder>
         *  3 dup
         *  4 invokespecial #3 <java/lang/StringBuilder.<init> : ()V>
         *  7 aload_1
         *  8 invokevirtual #4 <java/lang/StringBuilder.append : (Ljava/lang/String;)Ljava/lang/StringBuilder;>
         * 11 aload_2
         * 12 invokevirtual #4 <java/lang/StringBuilder.append : (Ljava/lang/String;)Ljava/lang/StringBuilder;>
         * 15 aload_3
         * 16 invokevirtual #4 <java/lang/StringBuilder.append : (Ljava/lang/String;)Ljava/lang/StringBuilder;>
         * 19 invokevirtual #5 <java/lang/StringBuilder.toString : ()Ljava/lang/String;>
         * 22 areturn
         */
    }

    public void testStringBufferAndStringBuilder() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < 1000000; i++) {
            stringBuffer.append(i);
        }
        String res = stringBuffer.toString();
        // System.out.println(res);
    }

    public static void main(String[] args) {
        LockEliminationDemo lockEliminationDemo = new LockEliminationDemo();
        long start = System.currentTimeMillis();
        lockEliminationDemo.testStringBufferAndStringBuilder();
        System.out.println("一共花费总时间（ms）：" + (System.currentTimeMillis() - start));
    }


}
