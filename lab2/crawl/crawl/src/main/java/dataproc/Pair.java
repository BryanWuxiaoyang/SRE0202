package dataproc;

public final class Pair<K, V> {
    K key;
    V value;
    boolean dirty;

    public Pair() {
    }

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Pair(K key, V value, boolean dirty){
        this.key = key;
        this.value = value;
        this.dirty = dirty;
    }
}
