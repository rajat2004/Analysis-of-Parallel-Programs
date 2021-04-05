package utils;

public class Utils {
    // For printing debug statements
    // Use System.out.println() for actual output
    public static boolean debug = true;

    public static void print(String s) {
        if (debug)
            System.out.println(s);
    }
}
