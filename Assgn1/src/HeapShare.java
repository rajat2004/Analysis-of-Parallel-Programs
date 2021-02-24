import java.util.HashMap;

public class HeapShare {
    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        String K="SomeKey", V="SomeVal";

        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println("Key " + K + ", Val " + map.get(K));
            System.out.println("Adding " + K + "," + V + " pair to map");
            map.put(K, V);
        });

        Thread t2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println("Key " + K + ", Val " + map.get(K));
            System.out.println("Erasing pair: " + K + " " + map.remove(K));
        });

        try {
            t1.start();
            t1.join();

            t2.start();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
