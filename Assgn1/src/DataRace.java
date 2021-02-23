import java.util.concurrent.*;

class Counter {
    private int counter = 0;

    public void increment() {
        counter = counter + 1;
    }

    public int get() {
        return counter;
    }
}

public class DataRace {
    public static void main(String[] args) {
        final int TOTAL_TASKS = 1000;
        ExecutorService service = Executors.newFixedThreadPool(3);
        Counter c = new Counter();

        for(int i=0; i<TOTAL_TASKS; i++) {
            service.submit(() -> {
                c.increment();
            });
        }

        try {
            service.shutdown();
            service.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Counter: " + c.get());
    }
}
