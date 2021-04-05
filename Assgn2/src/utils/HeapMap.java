package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HeapMap {
    // refs x fields -> values
    // Implemented as [ref][field] = value
    public HashMap<String, HashMap<String, ValuesSet>> map = new HashMap<>();

    public boolean store(String ref, String field, ValuesSet vs) {
        print("Store: " + ref + "." + field + " ---> " + vs.toString());
//        setDefault(ref, field);

        return get(ref, field).union(vs);
    }

    public ValuesSet get(String ref, String field) {
        print("Get: " + ref + "." + field);
//        setDefault(ref, field);
        return map.get(ref).get(field);
    }

    public void setDefault(String ref, String field) {
        map.get(ref).putIfAbsent(field, new ValuesSet());
    }

    public void createEntries(String ref, HashSet<String> all_fields) {
        map.putIfAbsent(ref, new HashMap<>());
        for(String field : all_fields) {
            setDefault(ref, field);
        }
    }

    public void printAll() {
        for(String ref : map.keySet()) {
            map.get(ref).forEach((field, vs) ->
                    print(ref + "." + field + ": " + vs.toString())
            );
        }
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
