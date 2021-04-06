package utils;

import java.util.HashMap;
import java.util.HashSet;

public class ClassInfo {
    String name;
    String parent_class;
//    HashSet<String> child_classes = new HashSet<>();
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

    public boolean isField(String var_name) {
        return fields.contains(var_name);
    }

    public HashSet<String> getAllFields() {
        // Fields have already been copied from the parent class in SymbolTable::copyfieldsMethods
        return fields;
    }

    public void createMethod(String method_name) {
        print("createMethod: " + method_name);
        methods.put(method_name, new FunctionSummary(method_name));
    }

    public boolean addMethods(HashMap<String, FunctionSummary> parent_methods) {
        int prev_size = methods.size();
        parent_methods.forEach((method_name, summary) -> {
            // Overridden methods shouldn't be replaced
            methods.putIfAbsent(method_name, summary);
        });

        return methods.size()!=prev_size;
    }

    public FunctionSummary getMethod(String method_name) {
        return methods.get(method_name);
    }

    public void printAll() {
        Utils.print("Class " + name);
        Utils.print("Parent: " + parent_class);
//        Utils.print("Child classes: " + child_classes);
        Utils.print("Fields: " + fields);
        Utils.print("Methods: " + methods.keySet());
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
