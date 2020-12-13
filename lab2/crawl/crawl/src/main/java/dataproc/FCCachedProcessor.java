package dataproc;


import com.sun.istack.internal.NotNull;

import java.util.Objects;

public class FCCachedProcessor<K, V> implements Processor<K, V> {
    private final Processor<K, V> processor;

    private final int posMask;

    final Pair<K, V>[] cache;

    public FCCachedProcessor(Processor<K, V> processor, int capacity){
        this.processor = processor;
        int capacity1 = (1 << (int) (Math.ceil(Math.log(capacity))));
        this.posMask = ~((~capacity1) + 1);
        this.cache = new Pair[capacity1];
    }

    private void writeBack(Pair<K, V> pair){
        if(pair == null || !pair.dirty) return;

        processor.write(pair.key, pair.value);
        pair.dirty = false;
    }

    @Override
    public V load(@NotNull K key) {
        int code = key.hashCode();
        int pos = code & posMask;
        V value;
        if(cache[pos] != null && Objects.equals(key, cache[pos].key)){
            value = cache[pos].value;
        }
        else{
            value = processor.load(key);
            if(value != null){
                writeBack(cache[pos]);
                cache[pos].key = key;
                cache[pos].value = value;
                cache[pos].dirty = false;
            }
        }
        return value;
    }

    @Override
    public void write(@NotNull K key, V value) {
        int code = key.hashCode();
        int pos = code & posMask;

        if(cache[pos] != null && Objects.equals(key, cache[pos].key)){
            cache[pos].value = value;
            cache[pos].dirty = true;
        }
        else{
            writeBack(cache[pos]);
            cache[pos].key = key;
            cache[pos].value = value;
            cache[pos].dirty = true;
        }
    }

    public void writeThrough(@NotNull K key, V value){
        int code = key.hashCode();
        int pos = code & posMask;

        write(key, value);
        writeBack(cache[pos]);
    }

    public void flush(){
        for (Pair<K, V> kvPair : cache) {
            writeBack(kvPair);
        }
    }
}
