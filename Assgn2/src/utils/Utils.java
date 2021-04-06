package utils;

public class Utils {
    // For printing debug statements
    // Use System.out.println() for actual output
    public static boolean debug = true;

    public static void print(String s) {
        if (debug)
            System.out.println(s);
    }

    public static String ARRAY_CLASS = "DUMMY-ARRAY-CLASS";
    public static String MAIN_METHOD = "main";
}
