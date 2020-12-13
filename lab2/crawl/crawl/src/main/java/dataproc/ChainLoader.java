package dataproc;

public class ChainLoader<K, V> implements Loader<K, V> {
    private final Loader<K, V> loader;

    private Loader<K, V> parentLoader;

    public ChainLoader(Loader<K, V> loader){
        this.loader = loader;
    }

    public ChainLoader(Loader<K, V> loader, Loader<K, V> parentLoader){
        this.loader = loader;
        this.parentLoader = parentLoader;
    }

    @Override
    public V load(K key) {
        V v = parentLoader == null ? null : parentLoader.load(key);
        return v == null ? loader.load(key) : v;
    }

    public void setParentLoader(Loader<K, V> parentLoader){
        this.parentLoader = parentLoader;
    }

    public Loader<K, V> getParentLoader(){
        return parentLoader;
    }
}
