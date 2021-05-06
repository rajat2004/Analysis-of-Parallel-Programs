package utils;

import java.util.HashMap;
import java.util.HashSet;

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

    public void createInitialEdges() {
        createThreadStartEdges();
    }

    public void createThreadStartEdges() {
        print("Adding thread start -> begin edges");
        peg.forEach((thread_id, thread_info) -> {
            HashSet<PEGNode> start_nodes = thread_info.getAllThreadStartNodes();
            for(PEGNode start_node : start_nodes) {
                String thread_to_start = start_node.object_name;
                PEGNode thread_begin_node = peg.get(thread_to_start).getThreadBeginNode();

                print("Adding start edge from " + start_node.unique_id +
                        " to Thread: " + thread_to_start + ", begin node: " + thread_begin_node.unique_id);

                start_node.start_successors.add(thread_begin_node);
                thread_begin_node.start_predecessors.add(start_node);
            }
        });
    }

    public void verifyPEG() {
        print("Verifying PEG");
        peg.forEach((thread_id, thread_info) -> {
            assert thread_info.name == thread_id;
            thread_info.verifyCFG();
        });
        print("PEG verification done!");
    }

    public void printAll() {
        print("\nPEG:");
        peg.forEach((thread_id, thread_info) -> {
            Utils.print("\n\n\nThread: " + thread_id);
            thread_info.printAll();
        });

        print("AllNodes: \n\n\n");
        all_nodes.forEach((node_id, node) -> {
            Utils.print(node_id + " : " + node);
        });
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
