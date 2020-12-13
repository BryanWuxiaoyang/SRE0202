package dataset;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AttributeSet implements Serializable {
    private final Map<String, Attribute> name2AttrMapper;

    private final List<Attribute> index2AttrMapper;


    public AttributeSet(boolean threadSafe){
        this.name2AttrMapper = threadSafe ? new ConcurrentHashMap<>() : new HashMap<>();
        this.index2AttrMapper = threadSafe ? new CopyOnWriteArrayList<>() : new ArrayList<>();
    }

    public void add(String attrName){
        int index = index2AttrMapper.size();
        Attribute attribute = new Attribute(attrName, index);
        name2AttrMapper.put(attrName, attribute);
        index2AttrMapper.add(attribute);
    }

    public void add(String... attrNames){
        for (String attrName : attrNames) {
            add(attrName);
        }
    }

    public Attribute getByIndex(int index){
        return index2AttrMapper.get(index);
    }

    public Attribute getByName(String name){
        return name2AttrMapper.get(name);
    }

    public int size(){
        return index2AttrMapper.size();
    }

    public List<Attribute> getAttributes(){
        return Collections.unmodifiableList(index2AttrMapper);
    }
}
