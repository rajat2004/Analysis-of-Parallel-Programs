package utils;

import java.util.HashMap;

public class SymbolTable {
    public HashMap<String, ClassInfo> classes = new HashMap<>();

    public void createClass(String cname, String parent) {
        print("Creating class " + cname + ", Parent: " + parent);
        classes.put(cname, new ClassInfo(cname, parent));
    }

    public void copyFieldsMethods() {
        // Copy all fields, non-overridden methods from parent to child
        while(true) {
            boolean changed = false;
            for(ClassInfo cinfo : classes.values()) {
                String pname = cinfo.parent_class;
                if (pname != null) {
                    ClassInfo pinfo = classes.get(pname);
                    changed = changed || cinfo.addFields(pinfo.fields);
                    changed = changed || cinfo.addMethods(pinfo.methods);
                }
            }

            if (!changed)
                break;
        }
    }

    public ClassInfo getClassInfo(String cname) {
        return classes.get(cname);
    }

    public void printAll() {
        classes.forEach((cname, info) ->  {
            Utils.print("\n");
            info.printAll();
        });
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
