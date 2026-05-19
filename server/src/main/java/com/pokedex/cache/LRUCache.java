package com.pokedex.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-safe LRU cache with TTL-based expiry.
 * Direct port of the JavaScript LRUCache from cache.js.
 */
public class LRUCache<V> {

    private final int maxSize;
    private final long ttlMs;
    private final LinkedHashMap<String, CacheEntry<V>> map;

    private record CacheEntry<V>(V value, long timestamp) {}

    public LRUCache(int maxSize, long ttlMs) {
        this.maxSize = maxSize;
        this.ttlMs = ttlMs;
        // accessOrder=true makes it behave as LRU on get()
        this.map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry<V>> eldest) {
                return size() > LRUCache.this.maxSize;
            }
        };
    }

    public synchronized V get(String key) {
        CacheEntry<V> entry = map.get(key);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() - entry.timestamp() > ttlMs) {
            map.remove(key);
            return null;
        }
        return entry.value();
    }

    public synchronized void set(String key, V value) {
        map.remove(key);
        map.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
    }

    public synchronized int size() {
        return map.size();
    }

    public synchronized void clear() {
        map.clear();
    }
}
