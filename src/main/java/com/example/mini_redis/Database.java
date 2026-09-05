package com.example.mini_redis;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Database {

    private final ConcurrentHashMap<String, RedisValue> data =
            new ConcurrentHashMap<>();

    public RedisValue get(String key) {

        RedisValue value = data.get(key);

        if (value == null) {
            return null;
        }

        if (value.isExpired()) {
            data.remove(key, value);
            return null;
        }

        return value;
    }

    public void put(String key, RedisValue value) {
        data.put(key, value);
    }

    public boolean delete(String key) {
        return data.remove(key) != null;
    }

    public boolean exists(String key) {
        return get(key) != null;
    }

    public int size() {
        cleanupExpired();
        return data.size();
    }

    public Set<String> keys() {

        cleanupExpired();

        return data.keySet();
    }

    public void clear() {
        data.clear();
    }

    private void cleanupExpired() {

        data.forEach((key, value) -> {

            if (value.isExpired()) {
                data.remove(key, value);
            }
        });
    }
}