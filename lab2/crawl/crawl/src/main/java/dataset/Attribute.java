package dataset;

import java.io.Serializable;

public class Attribute implements Serializable {
    private final String name;

    private final int index;

    Attribute(String name, int index){
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        return index == ((Attribute) obj).index;
    }
};
