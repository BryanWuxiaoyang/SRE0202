package dataproc;

public interface Writer<K, V> {
    void write(K key, V value);
}
