package utils;

import java.util.HashMap;

public class ParallelExecutionGraph {
    HashMap<String, ThreadInfo> peg = new HashMap<>();
    HashMap<Integer, PEGNode> all_nodes = new HashMap<>();

    public void addThreadObject(String thread_id, String class_name) {
        print("New thread: " + thread_id + ", type: " + class_name);
        peg.putIfAbsent(thread_id, new ThreadInfo(thread_id, class_name));
    }

    public void addNodeToThread(String thread_id, PEGNode node) {
        peg.get(thread_id).addNode(node);
        all_nodes.put(node.unique_id, node);
    }

    public void printAll() {
        print("\nPEG:");
        peg.forEach((thread_id, thread_info) -> {
            print("\nThread: " + thread_id);
            thread_info.printAll();
        });

        print("\nAllNodes:");
        all_nodes.forEach((node_id, node) -> {
            Utils.print(node_id + " : " + node);
        });
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
