package top.tobing;

/**
 * @author tobing
 * @date 2021/9/6 22:39
 * @description
 */
public class Demo01ExceptionFinally {
    public static void main(String[] args) {
        int i = testFinally();
//        int j = testFinally1();
//        System.out.println(testFinally()); // 返回3
//        System.out.println(testFinally1()); // 返回3
        System.out.println(i); // 返回3
//        System.out.println(j); // 返回3

    }

    private static int testFinally() {
        int i = 1;
        try {
            i = 2;
            return i;
        } finally {
            i = 3;
        }
    }

    /**
     * 0 iconst_1
     * 1 istore_0
     * 2 iconst_2
     * 3 istore_0
     * 4 iload_0
     * 5 istore_1
     * 6 iconst_3
     * 7 istore_0
     * 8 iload_1
     * 9 ireturn
     * 10 astore_2
     * 11 iconst_3
     * 12 istore_0
     * 13 aload_2
     * 14 athrow
     */

    private static int testFinally1() {
        try {
            return 3;
        } finally {
            return 2;
        }
    }

    /**
     * 0 iconst_3   // 将常量3压栈
     * 1 istore_0   // 将栈顶元素放到变量槽0
     * 2 iconst_2   // 将常量2压栈
     * 3 ireturn    // 将栈顶元素返回2
     * 4 astore_1
     * 5 iconst_2
     * 6 ireturn
     */

    /**
     * 【ireturn】
     * 「Description」
     * The current method must have return type boolean, byte, short,
     * char, or int. The value must be of type int. If the current method
     * is a synchronized method, the monitor entered or reentered on
     * invocation of the method is updated and possibly exited as if by
     * execution of a monitorexit instruction (§monitorexit) in the current
     * thread. If no exception is thrown, value is popped from the operand
     * stack of the current frame (§2.6) and pushed onto the operand stack
     * of the frame of the invoker. Any other values on the operand stack
     * of the current method are discarded.
     * The interpreter then returns control to the invoker of the method,
     * reinstating the frame of the invoker.
     *「Run-time Exceptions」
     * If the Java Virtual Machine implementation does not enforce
     * the rules on structured locking described in §2.11.10, then if the
     * current method is a synchronized method and the current thread is
     * not the owner of the monitor entered or reentered on invocation of
     * the method, ireturn throws an IllegalMonitorStateException.
     * This can happen, for example, if a synchronized method contains
     * a monitorexit instruction, but no monitorenter instruction, on the
     * object on which the method is synchronized.
     * Otherwise, if the Java Virtual Machine implementation enforces
     * the rules on structured locking described in §2.11.10 and if the first
     * of those rules is violated during invocation of the current method,
     * then ireturn throws an IllegalMonitorStateException.
     */
}
