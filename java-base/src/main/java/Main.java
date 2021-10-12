/**
 * @author tobing
 * @date 2021/9/5 22:00
 * @description
 */
public class Main {
    public static void main(String[] args) {
        String name = "tobing";
        Thread thread = new Thread(() -> {
            System.out.println(name);
        });


        try {
            testThrows();
        } catch (Exception e) {
            System.out.println("捕获到异常");
            e.printStackTrace();
        }

        System.out.println("程序退出");




    }

    public static void testThrows() throws Exception {
        int[] arr = {1, 2, 3};
        System.out.println("testThrow called.");
        System.out.println(arr[3]);
        System.out.println("testThrow return normally");
    }
}
