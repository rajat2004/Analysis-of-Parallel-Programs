package utils;

import java.util.ArrayList;
import java.util.HashSet;

public class ThreadInfo {
    public String name;
    public String class_name;

    ArrayList<PEGNode> cfg = new ArrayList<>();

    public ThreadInfo(String name, String class_name) {
        this.name = name;
        this.class_name = class_name;
    }

    public void addNode(PEGNode node) {
        Utils.print("Thread: " + name + " - Adding node: " + node);
        updatePredecessorNodes(node);
        updateSuccessorNodesOfPrevNodes(node);
        cfg.add(node);
    }

    public void updatePredecessorNodes(PEGNode new_node) {
        if (new_node.type == PEGNodeType.THREAD_BEGIN) {
            print("Node: " + new_node.type + ", no predecessor to be added");
            return;
        }

        PEGNode prev_node = cfg.get(cfg.size() - 1);

        if (new_node.type == PEGNodeType.NOTIFIED_ENTRY) {
            print("Type: " + new_node.type + ", setting wait pred to " + prev_node.unique_id);
            new_node.waiting_pred = prev_node;
            return;
        }

        new_node.local_predecessors.add(prev_node);
    }

    public void updateSuccessorNodesOfPrevNodes(PEGNode new_node) {
        if (new_node.type == PEGNodeType.NOTIFIED_ENTRY) {
            PEGNode waiting_pred_node = new_node.waiting_pred;
            print("New node type: " + new_node.type + ", setting wait succ of " + waiting_pred_node.unique_id);
            waiting_pred_node.waiting_succ = new_node;
            return;
        }

        for(PEGNode local_pred : new_node.local_predecessors) {
            local_pred.local_successors.add(new_node);
        }
    }

    public HashSet<PEGNode> getAllThreadStartNodes() {
        HashSet<PEGNode> start_nodes = new HashSet<>();
        for(PEGNode node : cfg) {
            if (node.type == PEGNodeType.THREAD_START)
                start_nodes.add(node);
        }

        return start_nodes;
    }

    public HashSet<PEGNode> getAllThreadJoinNodes() {
        HashSet<PEGNode> join_nodes = new HashSet<>();
        for(PEGNode node : cfg) {
            if (node.type == PEGNodeType.THREAD_JOIN)
                join_nodes.add(node);
        }

        return join_nodes;
    }

    public PEGNode getThreadBeginNode() {
        return cfg.get(0);
    }

    public PEGNode getThreadEndNode() {
        return cfg.get(cfg.size()-1);
    }

    public void verifyCFG() {
        print("Verifying Thread: " + name);
        assert getThreadBeginNode().type == PEGNodeType.THREAD_BEGIN;
        assert getThreadEndNode().type == PEGNodeType.THREAD_END;

        for(PEGNode node : cfg) {
            assert node.thread_id == name;
            node.verifyNode();
        }

        print("Verification done!");
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }

    public void printAll() {
        Utils.print("Thread: " + name + " , Class: " + class_name);
        for(PEGNode node : cfg) {
            Utils.print("\n");
            node.printAll();
        }
    }
}
