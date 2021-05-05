package utils;

public class PEGNode {
    public String object_name;
    public PEGNodeType type;
    public String thread_id;

    public String label;
    public int unique_id;

    public PEGNode(String obj, PEGNodeType type, String thread_id, int unique_id) {
        this.object_name = obj;
        this.type = type;
        this.thread_id = thread_id;
        this.unique_id = unique_id;
    }

    public String toString() {
        return "ID: " + unique_id
                + ", Obj: " + object_name
                + ", Type: " + type
                + ", ThreadID: " + thread_id
                + ((label!=null) ? ", Label: " + label : "");
    }
}
