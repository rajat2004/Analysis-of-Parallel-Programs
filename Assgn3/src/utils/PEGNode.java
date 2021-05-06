package utils;

import java.util.HashSet;

public class PEGNode {
    public String object_name;
    public PEGNodeType type;
    public String thread_id;

    public String label;
    public int unique_id;

    public HashSet<String> sync_objs = new HashSet<>();

    public HashSet<PEGNode> local_predecessors = new HashSet<>();
    public HashSet<PEGNode> local_successors = new HashSet<>();

    public HashSet<PEGNode> start_predecessors = new HashSet<>();
    public HashSet<PEGNode> start_successors = new HashSet<>();

    public HashSet<PEGNode> notify_predecessors = new HashSet<>();
    public HashSet<PEGNode> notify_successors = new HashSet<>();

    public PEGNode waiting_pred = null;
    public PEGNode waiting_succ = null;

    public HashSet<PEGNode> mhp_nodes = new HashSet<>();
    public HashSet<PEGNode> out_nodes = new HashSet<>();

    public PEGNode(String obj, PEGNodeType type, String thread_id, int unique_id, String label) {
        this.object_name = obj;
        this.type = type;
        this.thread_id = thread_id;
        this.unique_id = unique_id;
        this.label = label;
    }

    public String toString() {
        return "ID: " + unique_id
                + ", Obj: " + object_name
                + ", Type: " + type
                + ", ThreadID: " + thread_id
                + ((label!=null) ? ", Label: " + label : "");
    }

    public boolean isTypeNotify() {
        return type == PEGNodeType.NOTIFY || type == PEGNodeType.NOTIFY_ALL;
    }

    public void verifyNode() {
        assert thread_id != null;

        if (type == PEGNodeType.THREAD_BEGIN) {
            assert local_predecessors.isEmpty();
            for (PEGNode start_node : start_predecessors) {
                assert start_node.type == PEGNodeType.THREAD_START;
                assert start_node.object_name.equals(thread_id);
                assert start_node.start_successors.contains(this);
            }
        }

        if (type == PEGNodeType.THREAD_END) {
            assert local_successors.isEmpty();
        }

        if (type == PEGNodeType.THREAD_START) {
            // Cause I'm not a THREAD_BEGIN
            assert start_predecessors.isEmpty();

            for(PEGNode begin_node : start_successors) {
                assert begin_node.type == PEGNodeType.THREAD_BEGIN;
                assert begin_node.thread_id.equals(object_name);
                assert begin_node.start_predecessors.contains(this);
            }
        }

        if (type != PEGNodeType.WAITING &&
            type != PEGNodeType.NOTIFIED_ENTRY &&
            type != PEGNodeType.NOTIFY &&
            type != PEGNodeType.NOTIFY_ALL) {
            assert waiting_pred == null;
            assert waiting_succ == null;
            assert notify_predecessors.isEmpty();
            assert notify_successors.isEmpty();
        }

        if (type == PEGNodeType.WAIT) {
            assert sync_objs.contains(object_name);
            assert local_successors.size() == 1;
        }

        if (type == PEGNodeType.WAITING) {
            assert waiting_succ != null;
            assert waiting_succ.type == PEGNodeType.NOTIFIED_ENTRY && waiting_succ.object_name.equals(object_name);
            assert !sync_objs.contains(object_name);
        }

        if (type == PEGNodeType.NOTIFIED_ENTRY) {
            assert waiting_pred != null;
            assert waiting_pred.type == PEGNodeType.WAITING && waiting_pred.object_name.equals(object_name);
            assert !sync_objs.contains(object_name);

            for(PEGNode node : notify_predecessors) {
                assert node.object_name.equals(object_name);
                assert node.type == PEGNodeType.NOTIFY || node.type == PEGNodeType.NOTIFY_ALL;
            }
        }

        if (isTypeNotify()) {
            for(PEGNode node : notify_successors) {
                assert node.object_name.equals(object_name);
                assert node.type == PEGNodeType.NOTIFIED_ENTRY;
            }
        }

        if (type == PEGNodeType.SYNC_EXIT) {
            assert sync_objs.contains(object_name);
        }
    }

    public void printAll() {
        Utils.print(toString()
                + ", sync_objs: " + sync_objs);
        Utils.print("Local Predecessors: " + local_predecessors);
        Utils.print("Local Successors: " + local_successors);
        if (!start_predecessors.isEmpty())
            Utils.print("Start predecessors: " + start_predecessors);
        if (!start_successors.isEmpty())
            Utils.print("Start successors: " + start_successors);

        if (waiting_pred != null)
            Utils.print("Waiting Pred: " + waiting_pred);
        if (waiting_succ != null)
            Utils.print("Waiting Succ: " + waiting_succ);
    }
}
