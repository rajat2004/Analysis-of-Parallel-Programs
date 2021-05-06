package utils;

import java.util.HashMap;
import java.util.HashSet;

public class MHPAnalyzer {
    ParallelExecutionGraph peg;
    boolean global_updated = true;

    HashMap<String, HashSet<PEGNode>> monitors = new HashMap<>();
    HashMap<String, HashSet<PEGNode>> notify_nodes = new HashMap<>();
    HashMap<String, HashSet<PEGNode>> waiting_nodes = new HashMap<>();

    public MHPAnalyzer(ParallelExecutionGraph peg) {
        this.peg = peg;
    }

    public void run() {
        print("Starting MHP analysis!!!");
        initialSetup();

        global_updated = true;
        int iteration = 0;

        while(global_updated) {
            global_updated = false;
            iteration++;

            Utils.print("\n\n\nStarting Iteration: " + iteration);
            runAnalysis();

            peg.verifyPEG();
        }
    }

    private void runAnalysis() {
        // First run on main thread
        ThreadInfo main_thread_info = peg.all_threads.get("main");
        print("Start Main thread analysis");
        for(PEGNode node : main_thread_info.cfg) {
            runMHPAnalysis(node);
        }

        peg.all_threads.forEach((thread_id, thread_info) -> {
            if (!thread_id.equals("main")) {
                Utils.print("\n\n");
                print("Starting analysis of thread: " + thread_id);
                for(PEGNode node : thread_info.cfg)
                    runMHPAnalysis(node);
            }
        });

        print("Completed one iteration of analysis!!");
    }

    private void updateInfoChanged(boolean updated) {
        global_updated |= updated;
    }

    private void runMHPAnalysis(PEGNode node) {
        Utils.print("\n");

        updateNotifySucc(node);
        updateMHPInfo(node);
        updateOUTInfo(node);
        ensureSymmetryOfMHPInfo(node);
    }

    private void updateNotifySucc(PEGNode node) {
        if (node.isTypeNotify()) {
            print("updateNotifySucc: " + node);
            HashSet<PEGNode> new_notify_succ = getAllMatchingNodes(node.object_name,
                    PEGNodeType.NOTIFIED_ENTRY);

            new_notify_succ.removeIf(m -> !node.mhp_nodes.contains(m.waiting_pred));

            boolean notify_succ_updated = node.notify_successors.addAll(new_notify_succ);
            updateInfoChanged(notify_succ_updated);

            if (notify_succ_updated)
                print("New notify successors: " + node.notify_successors);
        }
    }

    private HashSet<PEGNode> getAllMatchingNodes(String obj_name, PEGNodeType type) {
        HashSet<PEGNode> matching_nodes = new HashSet<>();
        peg.all_nodes.forEach((node_id, node) -> {
            if (node.object_name.equals(obj_name) && node.type == type)
                matching_nodes.add(node);
        });

        return matching_nodes;
    }

    private void ensureSymmetryOfMHPInfo(PEGNode node) {
        print("Ensuring symmetry of MHP info for: " + node);
        for(PEGNode mhp_node : node.mhp_nodes) {
            boolean sym_updated = mhp_node.mhp_nodes.add(node);
            if (sym_updated)
                print("Symmetry updated in " + mhp_node);
        }
    }

    private HashSet<PEGNode> GenNotifyAll(PEGNode node) {
        assert node.type == PEGNodeType.NOTIFIED_ENTRY;

        HashSet<PEGNode> obj_notified_entry = getAllMatchingNodes(node.object_name, PEGNodeType.NOTIFIED_ENTRY);
        // Note that condition is prefixed by "!" below
        obj_notified_entry.removeIf(m ->
           !m.waiting_pred.mhp_nodes.contains(node.waiting_pred)
        );

        HashSet<PEGNode> obj_notify_all = getAllMatchingNodes(node.object_name, PEGNodeType.NOTIFY_ALL);

        HashSet<PEGNode> gen_notify_all = new HashSet<>();
        for(PEGNode m : obj_notified_entry) {
            for(PEGNode r : obj_notify_all) {
                if (node.waiting_pred.mhp_nodes.contains(r)
                    && m.waiting_pred.mhp_nodes.contains(r)) {
                    gen_notify_all.add(m);
                    break;
                }
            }
        }

        print("GenNotifyAll: " + gen_notify_all);
        return gen_notify_all;
    }

    private void updateMHPInfo(PEGNode node) {
        print("updateMHPInfo: " + node + "\t\t Current MHP set: ");
        node.mhp_nodes.forEach(m_node -> Utils.print("\t" + m_node));

        HashSet<PEGNode> new_nodes;
        if (node.type == PEGNodeType.THREAD_BEGIN) {
            new_nodes = getUnionOfOUT(node.start_predecessors);
            new_nodes.removeAll(peg.getThreadInfo(node.thread_id).cfg);
        }
        else if (node.type == PEGNodeType.NOTIFIED_ENTRY) {
            new_nodes = getUnionOfOUT(node.notify_predecessors);
            new_nodes.retainAll(node.waiting_pred.out_nodes);
            new_nodes.addAll(GenNotifyAll(node));
        }
        else {
            new_nodes = getUnionOfOUT(node.local_predecessors);
        }

        boolean mhp_nodes_updated = node.mhp_nodes.addAll(new_nodes);
        updateInfoChanged(mhp_nodes_updated);

        if (mhp_nodes_updated)
            print("updateMHPInfo completed: " + node + "\n\t\t New MHP Set: " + node.mhp_nodes);
        else
            print("updateMHPInfo: No change");
    }

    private void updateOUTInfo(PEGNode node) {
        print("updateOUTInfo: " + node + "\t\t Current OUT set: " + node.out_nodes);
        // TODO: Check if Out info is union or direct update, assuming union right now

        HashSet<PEGNode> gen_set = getGENSet(node);
        HashSet<PEGNode> kill_set = getKILLSet(node);

        HashSet<PEGNode> new_out_set = new HashSet<>();

        new_out_set.addAll(node.mhp_nodes);
        new_out_set.addAll(gen_set);
        new_out_set.removeAll(kill_set);

        boolean out_updated = node.out_nodes.addAll(new_out_set);
        updateInfoChanged(out_updated);

        if (out_updated)
            print("New OUT set: " + node.out_nodes);
        else
            print("OUT set unchanged");
    }

    private HashSet<PEGNode> getGENSet(PEGNode node) {
        print("Getting GEN of " + node);
        HashSet<PEGNode> gen_set = new HashSet<>();
        if (node.type == PEGNodeType.THREAD_START) {
            String thread_id = node.object_name;
            gen_set.add(peg.getThreadInfo(thread_id).getThreadBeginNode());
        }
        else if (node.isTypeNotify()) {
            gen_set.addAll(node.notify_successors);
        }
        else {
            // Nothing
        }

        print("Gen set: " + gen_set);
        return gen_set;
    }

    private HashSet<PEGNode> getKILLSet(PEGNode node) {
        print("Getting KILL of " + node);
        HashSet<PEGNode> kill_set = new HashSet<>();

        if (node.type == PEGNodeType.THREAD_JOIN) {
            String thread_to_kill = node.object_name;
            kill_set.addAll(peg.getThreadInfo(thread_to_kill).cfg);
        }
        else if (node.type == PEGNodeType.SYNC_ENTRY || node.type == PEGNodeType.NOTIFIED_ENTRY) {
            kill_set.addAll(monitors.get(node.object_name));
        }
        else if (node.type == PEGNodeType.NOTIFY &&
                waiting_nodes.get(node.object_name).size() == 1) {
            kill_set.addAll(waiting_nodes.get(node.object_name));
        }
        else if (node.type == PEGNodeType.NOTIFY_ALL) {
            kill_set.addAll(waiting_nodes.get(node.object_name));
        }
        else {
            // Nothing
        }

        print("Kill set: " + kill_set);
        return kill_set;
    }

    private HashSet<PEGNode> getUnionOfOUT(HashSet<PEGNode> nodes) {
        print("Getting union of OUT of: " + nodes);
        HashSet<PEGNode> union_out = new HashSet<>();
        for(PEGNode node : nodes)
            union_out.addAll(node.out_nodes);

        return union_out;
    }

    private void initialSetup() {
        generateMonitors();
        generateNotifyNodes();
        generateWaitingNodes();
    }

    private void generateMonitors() {
        print("\t\tGenerating monitors!");
        peg.all_nodes.forEach((node_id, node) -> {
            if (!node.sync_objs.isEmpty()) {
                for(String obj : node.sync_objs) {
                    monitors.putIfAbsent(obj, new HashSet<>());
                    monitors.get(obj).add(node);
                }
            }
        });

        print("Monitors:");
        monitors.forEach((obj_name, nodes) -> {
            Utils.print("\nObj: " + obj_name);
            for(PEGNode node : nodes)
                Utils.print("\t" + node.toString() + ", " + node.sync_objs);
        });
    }

    private void generateNotifyNodes() {
        print("\t\tGenerating notify nodes!");
        peg.all_nodes.forEach((node_id, node) -> {
            if (node.isTypeNotify()) {
                notify_nodes.putIfAbsent(node.object_name, new HashSet<>());
                notify_nodes.get(node.object_name).add(node);
            }
        });

        Utils.print("\n\nNotifyNodes:");
        notify_nodes.forEach((obj_name, nodes) -> {
            Utils.print("\nObj: " + obj_name);
            Utils.print(nodes.toString());
        });
    }

    private void generateWaitingNodes() {
        print("\t\tGenerating Waiting nodes");
        peg.all_nodes.forEach((node_id, node) -> {
            if (node.type == PEGNodeType.WAITING) {
                waiting_nodes.putIfAbsent(node.object_name, new HashSet<>());
                waiting_nodes.get(node.object_name).add(node);
            }
        });

        Utils.print("\n\nWaiting nodes:");
        waiting_nodes.forEach((obj_name, nodes) -> {
            Utils.print("\nObj: " + obj_name);
            Utils.print(nodes.toString());
        });
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
