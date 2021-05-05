package utils;

import java.util.ArrayList;

public class ThreadInfo {
    public String name;
    public String class_name;

    ArrayList<PEGNode> cfg = new ArrayList<>();

    ThreadInfo(String name, String class_name) {
        this.name = name;
        this.class_name = class_name;
    }
}
