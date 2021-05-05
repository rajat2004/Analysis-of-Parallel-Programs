package utils;

import java.util.HashMap;

public class ParallelExecutionGraph {
    HashMap<String, ThreadInfo> peg = new HashMap<>();

    public void addThreadObject(String thread_id, String class_name) {
        print("New thread: " + thread_id + ", type: " + class_name);
        peg.putIfAbsent(thread_id, new ThreadInfo(thread_id, class_name));
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
