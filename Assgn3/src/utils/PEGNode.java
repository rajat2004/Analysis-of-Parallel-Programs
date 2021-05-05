package utils;

import java.util.ArrayList;
import java.util.HashSet;

public class PEGNode {
    public String object_name;
    public PEGNodeType type;
    public String thread_id;

    public String label;
    public int unique_id;

    public boolean is_synchronized = false;
    public String sync_obj = null;

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

    public void printAll() {
        Utils.print(toString()
                + ", is_synchronized: " + is_synchronized
                + ", sync_obj: " + sync_obj);
    }
}
