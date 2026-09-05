package com.example.mini_redis;

public class RedisValue {

    private final DataType type;
    private final Object value;

    private volatile long expireAt = -1;

    public RedisValue(DataType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public DataType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public boolean isExpired() {

        return expireAt != -1
                && System.currentTimeMillis() >= expireAt;
    }
}