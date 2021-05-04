package utils;

public class Utils {
    public static boolean debug = true;

    public static void print(String s) {
        if (debug)
            System.out.println(s);
    }
}
