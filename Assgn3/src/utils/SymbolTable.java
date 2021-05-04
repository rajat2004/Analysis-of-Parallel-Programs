package utils;

import java.util.HashMap;
import java.util.HashSet;

public class SymbolTable {
    public HashMap<String, HashSet<String>> thread_objects = new HashMap<>();

    public void addThreadClass(String class_name) {
        print("New thread class: " + class_name);
        thread_objects.putIfAbsent(class_name, new HashSet<>());
    }

    public void addThreadOfType(String thread_id, String class_name) {
        print("New thread object: " + thread_id + ", type: " + class_name);
        thread_objects.get(class_name).add(thread_id);
    }

    public void printAll() {
        Utils.print("Threads: " + thread_objects.toString());
    }

    public void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
