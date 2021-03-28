package utils;

import java.util.HashSet;

public class ValuesSet extends HashSet<String> {

    public ValuesSet() {
        super();
    }
    public ValuesSet(ValuesSet strings) {
        super(strings);
    }

    // Adds the contents of l1 to set
    // Returns true if the set has changed (new elements were added)
    public boolean union(ValuesSet s1) {
        int prev_size = this.size();
        this.addAll(s1);

        return prev_size != this.size();
    }

    public boolean union(String value) {
        ValuesSet tmp = new ValuesSet();
        tmp.add(value);
        return this.union(tmp);
    }

    /*
        Finds intersection between 2 sets
        Returns true if non-empty intersection
     */
    public boolean intersection(ValuesSet s1) {
        ValuesSet result_set = new ValuesSet(this);
        result_set.retainAll(s1);

        Utils.print(this.getClass().getSimpleName() + ": Intersection: ");
        Utils.print(this.toString());
        Utils.print(s1.toString());
        Utils.print("Res: " + result_set.toString());

        return result_set.size()!=0;
    }

    private void print(String s) {
        Utils.print(this.getClass().getSimpleName() + ": " + s);
    }
}
