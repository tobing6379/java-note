package top;

/**
 * @author tobing
 * @date 2021/10/5 15:56
 * @description
 */
public class Demo01FinallyKeyword {
    public static void main(String[] args) {
        System.out.println(testFinally());
    }

    public static int testFinally() {
        int i = 0;
        try {
            i = 1;
            return i;
        } finally {
            i++;
        }
    }
}
