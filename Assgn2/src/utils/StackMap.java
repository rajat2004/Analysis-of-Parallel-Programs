package utils;

import java.util.HashMap;

public class StackMap {
    HashMap<String, ValuesSet> variables = new HashMap<>();

    public void add(String var) {
        add(var, new ValuesSet());
    }

    public void add(String var, ValuesSet values) {
        variables.putIfAbsent(var, values);
    }

    public boolean isVarPresent(String var) {
        return variables.containsKey(var);
    }

    public ValuesSet getValues(String var) {
        return variables.get(var);
    }

    public void printAll() {
        variables.forEach((var, values) -> Utils.print(var + ": " + values.toString()));
    }
}
