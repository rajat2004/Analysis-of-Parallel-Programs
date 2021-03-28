package utils;

import java.util.HashMap;

public class SymbolTable {
    public HashMap<String, String> variables = new HashMap<>();
    public HashMap<String, ClassInfo> classes = new HashMap<>();

    public void add(String var, String type) {
        print("Adding: " + var + " " + type);
        variables.put(var, type);
    }

    public void addClassField(String cname, String field) {
        print("Class: " + cname + ", Adding field: " + field);
        classes.get(cname).fields.add(field);
    }

    public void createClass(String cname) {
        print("Creating class " + cname);
        classes.put(cname, new ClassInfo());
    }

    public void printAll() {
        variables.forEach((var, type) -> Utils.print(var + ": " + type) );
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
