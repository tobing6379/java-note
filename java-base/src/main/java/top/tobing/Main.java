package top.tobing;

import java.util.Scanner;

/**
 * @author tobing
 * @date 2021/10/16 12:11
 * @description
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String str = scanner.next();
        switch (str) {
            case "111":
                System.out.println("1111");
                break;
            case "222":
                System.out.println("2222");
                break;
            default:
                System.out.println("3333");
                break;
        }
    }
}
