package utils;

import java.util.ArrayList;
import java.util.HashMap;

public class FunctionSummary {
    String name = null;
    public ValuesSet this_ref = new ValuesSet();
    HashMap<String, ValuesSet> parameters_mp = new HashMap<>();
    ArrayList<String> formal_params = new ArrayList<>();
    public ValuesSet return_set = new ValuesSet();
    public StackMap stack = new StackMap();

    FunctionSummary(String name) {
        this.name = name;
    }

    public void addLocalVar(String var) {
        print("Adding local var: " + var);
        stack.add(var);
    }

    public void addMethodParameter(String param_name, String type) {
        print("Adding parameter: " + param_name + ", of type " + type);
        formal_params.add(param_name);
        parameters_mp.put(param_name, new ValuesSet());
    }

    // Used to check if var is not of primitive type
    public boolean isVariablePresent(String var) {
        return stack.isVarPresent(var);
    }

    public boolean isFunctionParameter(String var_name) {
        return formal_params.contains(var_name);
    }

    public ValuesSet getFormalParamValues(String var_name) {
        return parameters_mp.get(var_name);
    }

    public String getFormalParamName(int id) {
        return formal_params.get(id);
    }

    public ValuesSet getFormalParamValues(int id) {
        String param_name = getFormalParamName(id);
        return getFormalParamValues(param_name);
    }

    public boolean updateThisRefs(ReferenceObj ref) {
        return this_ref.union(ref);
    }

    public void printAll() {
        Utils.print("Method: " + name);
        Utils.print("This: " + this_ref);
        Utils.print("Formal Params: " + formal_params);
        Utils.print("Returns: " + return_set);
        Utils.print("Stack: ");
        stack.printAll();
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + name + ": " + s);
    }

}
