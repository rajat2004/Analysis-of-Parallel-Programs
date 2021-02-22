import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

class BarrierThread extends Thread {
    private CyclicBarrier B1, B2;

    public BarrierThread(CyclicBarrier b1, CyclicBarrier b2) {
        this.B1 = b1;
        this.B2 = b2;
    }

    public void run() {
        System.out.println("Starting " + this.getName());
        try {
            B1.await();
            System.out.println("Thread " + this.getName() + ": Barrier 1 completed");
            B2.await();
            System.out.println("Finishing thread " + this.getName());
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}

public class DeadlockCyclicBarrier {
    public static void main(String[] args) {
        CyclicBarrier b1 = new CyclicBarrier(2);
        CyclicBarrier b2 = new CyclicBarrier(2);

        BarrierThread t1 = new BarrierThread(b1, b2);
        BarrierThread t2 = new BarrierThread(b2, b1);

        t1.start();
        t2.start();
    }
}
