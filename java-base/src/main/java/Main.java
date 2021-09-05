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

    }
}
