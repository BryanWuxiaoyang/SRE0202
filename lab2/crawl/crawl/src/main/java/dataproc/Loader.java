package dataproc;

public interface Loader<K, V> {
    V load(K key);
}
