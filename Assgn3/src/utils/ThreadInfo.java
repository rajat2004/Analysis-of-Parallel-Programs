package utils;

import java.util.ArrayList;

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
        cfg.add(node);
    }

    public void printAll() {
        Utils.print("Thread: " + name + " , Class: " + class_name);
        for(PEGNode node : cfg) {
            node.printAll();
        }
    }
}
