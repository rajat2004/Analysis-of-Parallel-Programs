package utils;

public class VarScope {
    public boolean is_stack_var = false;
    public boolean is_parameter = false;
    public boolean is_class_field = false;

    public boolean isPresent() {
        return is_stack_var || is_parameter || is_class_field;
    }

    public String toString() {
        return "VarScope: " + is_stack_var + ", " + is_parameter + ", " + is_class_field;
    }
}
