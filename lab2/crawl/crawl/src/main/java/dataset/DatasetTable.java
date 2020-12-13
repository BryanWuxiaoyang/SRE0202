package dataset;

import java.io.BufferedWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DatasetTable implements Serializable, Iterable<DatasetTable.Tuple> {
    private final AttributeSet attributeSet;

    private final List<Tuple> tupleList;

    public DatasetTable(AttributeSet attributeSet, boolean threadSafe) {
        this.attributeSet = attributeSet;
        this.tupleList = threadSafe ? new CopyOnWriteArrayList<>() : new ArrayList<>();
    }

    public AttributeSet getAttributeSet() {
        return attributeSet;
    }

    public void add(Tuple tuple){
        tupleList.add(tuple);
    }

    public void clear(){
        tupleList.clear();
    }

    public int size(){return tupleList.size();}

    @Override
    public Iterator<Tuple> iterator() {
        return tupleList.iterator();
    }

    public final class Tuple implements Serializable {
        private final String[] values;

        public Tuple(){
            this.values = new String[attributeSet.size()];
        }

        public Tuple(String... values){
            this.values = values.clone();
        }

        public String get(String attrName){
            return values[attributeSet.getByName(attrName).getIndex()];
        }

        public String get(int index){
            return values[index];
        }

        public String get(Attribute attribute){
            return values[attribute.getIndex()];
        }

        public void set(int index, String value){
            values[index] = value;
        }

        public void set(String attrName, String value){
            Attribute attribute = attributeSet.getByName(attrName);
            values[attribute.getIndex()] = value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String value : values) {
                sb.append(value).append("^");
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (Attribute attribute : attributeSet.getAttributes()) {
            sb.append(attribute.getName()).append("^");
        }
        sb.append("\n");

        for (Tuple tuple : tupleList) {
            for (String value : tuple.values) {
                sb.append(value).append("^");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
