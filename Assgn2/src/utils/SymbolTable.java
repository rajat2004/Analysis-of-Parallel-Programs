package utils;

import java.util.HashMap;

public class SymbolTable {
    public HashMap<String, String> variables = new HashMap<>();

    public void add(String var, String type) {
        Utils.print(this.getClass().getSimpleName() + ": Adding: " + var + " " + type);
        variables.put(var, type);
    }

    public void printAll() {
        variables.forEach((var, type) -> Utils.print(var + ": " + type) );
    }
}
