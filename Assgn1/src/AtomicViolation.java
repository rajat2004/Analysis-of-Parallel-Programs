import java.util.HashMap;

public class AtomicViolation {
    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        map.put("key", "value");

        Runnable task = () -> {
            boolean present = false;
            synchronized (map) {
                present = map.containsKey("key");
            }

            // To force other thread to acquire lock
            for(int i=0; i<10000; i++);

            if (present) {
                synchronized (map) {
                    System.out.println("Erasing pair, return value: " +
                            map.remove("key"));
                }
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        try {
            t1.start();
            t2.start();

            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
