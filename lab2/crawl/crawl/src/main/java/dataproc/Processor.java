package dataproc;

public interface Processor<K, V> extends Loader<K, V>, Writer<K, V>{
}
