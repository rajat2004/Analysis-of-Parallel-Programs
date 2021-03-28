package utils;

import java.util.HashMap;

public class HeapMap {
    // refs x fields -> values
    // Implemented as [ref][field] = value
    public HashMap<String, HashMap<String, ValuesSet>> map = new HashMap<>();

    public boolean store(String ref, String field, ValuesSet vs) {
        print("Store: " + ref + "." + field);
        setDefault(ref, field);

        return map.get(ref).get(field).union(vs);
    }

    public ValuesSet get(String ref, String field) {
        print("Get: " + ref + "." + field);
        setDefault(ref, field);
        return map.get(ref).get(field);
    }

    public void setDefault(String ref, String field) {
        map.putIfAbsent(ref, new HashMap<>());
        map.get(ref).putIfAbsent(field, new ValuesSet());
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
