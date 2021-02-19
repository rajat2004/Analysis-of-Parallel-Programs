class A {
    synchronized void methodA(B b) {
        System.out.println("Inside methodA!");
        b.finishB();
    }

    synchronized void finishA() {
        System.out.println("Finishing A");
    }
}

class B {
    synchronized void methodB(A a) {
        System.out.println("Inside methodB!");
        a.finishA();
    }

    synchronized void finishB() {
        System.out.println("Finishing B");
    }
}

public class DeadlockSynchronized {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        A a = new A();
        B b = new B();

        Thread t1 = new Thread(() -> {
            a.methodA(b);
        });
        Thread t2 = new Thread(() -> {
           b.methodB(a);
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
