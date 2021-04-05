package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ClassInfo {
    String name;
    String parent_class;
    HashSet<String> child_classes = new HashSet<>();
    public HashSet<String> fields = new HashSet<>();
    HashMap<String, FunctionSummary> methods = new HashMap<>();

    ClassInfo(String name_val, String parent) {
        name = name_val;
        parent_class = parent;
    }

    public void addField(String var_name) {
        print("addField: " + var_name);
        fields.add(var_name);
    }

    public boolean addFields(HashSet<String> parent_fields) {
        int prev_size = fields.size();
        fields.addAll(parent_fields);

        return fields.size()!=prev_size;
    }

    public void addMethod(String method_name) {
        print("addMethod: " + method_name);
        methods.put(method_name, new FunctionSummary(method_name));
    }

    public FunctionSummary getMethod(String method_name) {
        return methods.get(method_name);
    }

    // TODO: Either copy all fields from parent at the end of SymbolTable, or recurse above
    public HashSet<String> getAllFields() {
        return fields;
    }

    public void printAll() {
        Utils.print("Class " + name);
        Utils.print("Parent: " + parent_class);
        Utils.print("Child classes: " + child_classes);
        Utils.print("Fields: " + fields);
//        fields.forEach((f, vs) -> Utils.print("\t" + f + ": " + vs));
        Utils.print("Methods: ");
        methods.forEach((m, summary) -> {
            Utils.print("\t" + m + ": ");
            summary.printAll();
            Utils.print("\n");
        });
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + name + ": " + s);
    }
}
