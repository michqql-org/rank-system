package me.michqql.ranksystem.ranks.collection;

import java.util.HashMap;
import java.util.function.Function;

public class ListenerHashMap<K,V> extends HashMap<K,V> {

    private Runnable changeRunnable;

    public void setChangeRunnable(Runnable changeRunnable) {
        this.changeRunnable = changeRunnable;
    }

    private void run() {
        if(changeRunnable != null)
            changeRunnable.run();
    }

    @Override
    public V put(K key, V value) {
        V old = super.put(key, value);
        run();
        return old;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V old = super.computeIfAbsent(key, mappingFunction);
        run();
        return old;
    }
}
