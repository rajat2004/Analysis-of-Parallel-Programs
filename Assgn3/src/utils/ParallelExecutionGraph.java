package utils;

import java.util.HashMap;
import java.util.HashSet;

public class ParallelExecutionGraph {
    public HashMap<String, ThreadInfo> all_threads = new HashMap<>();
    public HashSet<PEGNode> all_nodes = new HashSet<>();

    public ThreadInfo getThreadInfo(String thread_id) {
        return all_threads.get(thread_id);
    }

    public void addThreadObject(String thread_id, String class_name) {
        print("New thread: " + thread_id + ", type: " + class_name);
        all_threads.putIfAbsent(thread_id, new ThreadInfo(thread_id, class_name));
    }

    public void addNodeToThread(String thread_id, PEGNode node) {
        all_threads.get(thread_id).addNode(node);
        all_nodes.add(node);
    }

    public void postCompletion() {
        createInitialEdges();
        removeUnusedThreads();
        verifyPEG();
    }

    private void createInitialEdges() {
        createThreadStartEdges();
    }

    private void createThreadStartEdges() {
        print("Adding thread start -> begin edges");
        all_threads.forEach((thread_id, thread_info) -> {
            HashSet<PEGNode> start_nodes = thread_info.getAllThreadStartNodes();
            for(PEGNode start_node : start_nodes) {
                String thread_to_start = start_node.object_name;
                PEGNode thread_begin_node = all_threads.get(thread_to_start).getThreadBeginNode();

                print("Adding start edge from " + start_node.unique_id +
                        " to Thread: " + thread_to_start + ", begin node: " + thread_begin_node.unique_id);

                start_node.start_successors.add(thread_begin_node);
                thread_begin_node.start_predecessors.add(start_node);
            }
        });
    }

    private void removeUnusedThreads() {
        print("Removing threads which haven't been started");
        all_threads.entrySet().removeIf(entry -> {
            // Main will always be started
            if (entry.getKey().equals("main"))
                return false;

            boolean thread_not_started = entry.getValue().getThreadBeginNode().start_predecessors.isEmpty();
            if (thread_not_started) {
                print("Not started thread: " + entry.getKey());
                // Remove nodes from all_nodes also
                all_nodes.removeAll(entry.getValue().cfg);
            }
            return thread_not_started;
        });
    }

    public void verifyPEG() {
        print("Verifying PEG");
        all_threads.forEach((thread_id, thread_info) -> {
            assert thread_info.name.equals(thread_id);
            thread_info.verifyCFG();
        });
        print("PEG verification done!");
    }

    public void printAll() {
        print("\nPEG:");
        all_threads.forEach((thread_id, thread_info) -> {
            Utils.print("\n\n\nThread: " + thread_id);
            thread_info.printAll();
        });

        print("AllNodes: \n\n\n");
        all_nodes.forEach(node -> Utils.print(node.toString()));
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
