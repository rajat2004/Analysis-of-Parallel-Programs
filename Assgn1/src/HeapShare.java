class SharedObject {
    private int v = 0;

    void set(int val) {
        v = val;
    }

    int get() {
        return v;
    }
}

class TestThread extends Thread {
    private SharedObject o;
    private int v;

    TestThread(SharedObject o, int v) {
        this.o = o;
        this.v = v;
    }

    public void run() {
        System.out.println(this.getName() + ": Current val: " + o.get());
        o.set(v);
        System.out.println("New val: " + o.get());
    }
}

public class HeapShare {
    public static void main(String[] args) {
        SharedObject o = new SharedObject();
        System.out.println("Current val: " + o.get());

        TestThread t1 = new TestThread(o, 10);
        TestThread t2 = new TestThread(o, 20);

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
