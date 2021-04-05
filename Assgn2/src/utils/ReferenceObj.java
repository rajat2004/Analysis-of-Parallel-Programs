package utils;

import java.util.Objects;

public class ReferenceObj {
    public String type;
    public int id;

    public ReferenceObj(String type, int id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public String toString() {
        return "R" + "_" + type + "_" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferenceObj that = (ReferenceObj) o;
        return id == that.id && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }
}
