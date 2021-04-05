package utils;

import java.util.ArrayList;

public class FunctionSummary {
    String name = null;
    ValuesSet this_ref = new ValuesSet();
//    HashMap<String, ValuesSet> parameters_mp;
    ArrayList<String> formal_params = new ArrayList<>();
    ValuesSet return_set = new ValuesSet();
    public StackMap stack = new StackMap();

    FunctionSummary(String name) {
        this.name = name;
    }

    public void addLocalVar(String var) {
        stack.add(var);
    }

    // Used to check if var is not of primitive type
    public boolean isVariablePresent(String var) {
        return stack.variables.containsKey(var);
    }

    public void printAll() {
        Utils.print("Method: " + name);
        Utils.print("This: " + this_ref);
        Utils.print("Formal Params: " + formal_params);
        Utils.print("Returns: " + return_set);
        Utils.print("Stack: ");
        stack.printAll();
    }
}
