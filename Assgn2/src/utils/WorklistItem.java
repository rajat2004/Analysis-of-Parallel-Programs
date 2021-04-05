package utils;

public class WorklistItem {
    public String class_name;
    public String method_name;

    public WorklistItem(String cname, String mname) {
        class_name = cname;
        method_name = mname;
    }

    public boolean isSame(String cname, String mname) {
        return class_name.equals(cname) && method_name.equals(mname);
    }

    @Override
    public String toString() {
        return class_name + ":::" + method_name;
    }
}
