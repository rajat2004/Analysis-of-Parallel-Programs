import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class AmdahlsLaw {

    static void work(int n) {
        for(int i=0; i<n; i++) {
            int sum = 0;
            for(int j=1; j<=100000; j++)
                sum += j;
        }

//        System.out.println("Completed!");
    }

    public static void main(String[] args) {
        final int SERIAL = 200;
        final int PARALLEL = 800;
        final int PROCESSORS = 8;

        // To ensure that threads start at the same time as possible
        CyclicBarrier barrier = new CyclicBarrier(PROCESSORS+1);

        Thread[] all_threads = new Thread[PROCESSORS];
        for(int i=0; i<PROCESSORS; i++) {
            all_threads[i] = new Thread(() -> {
                try {
                    barrier.await();
                    work(PARALLEL / PROCESSORS);
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
            all_threads[i].start();
        }

        long start=0, end1=0, end=0;

        try {
            start = System.nanoTime();
            // Perform serial work first
            work(SERIAL);

            end1 = System.nanoTime();

            // All threads have already started and are waiting on barrier, main thread is the last one
            barrier.await();

            for (int i = 0; i < PROCESSORS; i++) {
                all_threads[i].join();
            }
            end = System.nanoTime();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        System.out.println("Serial: " + SERIAL + " Parallel: " + PARALLEL + " Processors: " + PROCESSORS);
        System.out.println("Total Time elapsed: " + (end-start)/1e6);
        System.out.println("Serial Time: "  + (end1-start)/1e6);
        System.out.println("Parallel Time: " + (end-end1)/1e6);
    }
}
