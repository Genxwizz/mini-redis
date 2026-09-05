package com.example.mini_redis;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class CommandExecutor {

    private final Database database;
    private final AofPersistence aofPersistence;

    public CommandExecutor(Database database) {
        this(database, new AofPersistence("append.aof"));
    }

    public CommandExecutor(Database database, AofPersistence aofPersistence) {
        this.database = database;
        this.aofPersistence = aofPersistence;
    }

    public Result execute(List<String> args) {

        if (args == null || args.isEmpty()) {
            return Result.error("ERR empty command");
        }

        String command =
                args.get(0).toUpperCase();

        try {

            Result result = switch (command) {

                case "PING" ->
                        ping(args);

                case "SET" ->
                        set(args);

                case "GET" ->
                        get(args);

                case "DEL" ->
                        del(args);

                case "EXISTS" ->
                        exists(args);

                case "EXPIRE" ->
                        expire(args);

                case "TTL" ->
                        ttl(args);

                case "INCR" ->
                        incr(args);

                case "DBSIZE" ->
                        Result.integer(database.size());

                case "KEYS" ->
                        keys(args);

                case "FLUSHDB" ->
                        flushDb(args);

                default ->
                        Result.error(
                                "ERR unknown command '" +
                                args.get(0) + "'"
                        );
            };

            if (aofPersistence != null && result.type() != Result.Type.ERROR) {
                if (List.of("SET", "DEL", "EXPIRE", "INCR").contains(command)) {
                    try {
                        aofPersistence.append(args);
                    } catch (Exception ignored) {}
                } else if ("FLUSHDB".equals(command)) {
                    try {
                        aofPersistence.clear();
                    } catch (Exception ignored) {}
                }
            }

            return result;

        } catch (Exception e) {

            return Result.error(
                    "ERR " + e.getMessage()
            );
        }
    }

    private Result ping(List<String> args) {

        if (args.size() == 1) {
            return Result.simple("PONG");
        }

        if (args.size() == 2) {
            return Result.bulk(args.get(1));
        }

        return Result.error(
                "ERR wrong number of arguments"
        );
    }

    private Result set(List<String> args) {

        if (args.size() < 3) {
            return Result.error(
                    "ERR wrong number of arguments"
            );
        }

        String key = args.get(1);
        String value = args.get(2);

        RedisValue redisValue =
                new RedisValue(
                        DataType.STRING,
                        value
                );

        // SET key value EX seconds
        if (args.size() >= 5 &&
                args.get(3).equalsIgnoreCase("EX")) {

            long seconds =
                    Long.parseLong(args.get(4));

            if (seconds <= 0) {
                return Result.error(
                        "ERR invalid expire time"
                );
            }

            redisValue.setExpireAt(
                    System.currentTimeMillis()
                            + seconds * 1000
            );
        }

        database.put(key, redisValue);

        return Result.simple("OK");
    }

    private Result get(List<String> args) {

        if (args.size() != 2) {
            return Result.error(
                    "ERR wrong number of arguments"
            );
        }

        RedisValue value =
                database.get(args.get(1));

        if (value == null) {
            return Result.bulk(null);
        }

        if (value.getType() != DataType.STRING) {
            return Result.error(
                    "WRONGTYPE Operation against a key"
            );
        }

        return Result.bulk(
                (String) value.getValue()
        );
    }

    private Result del(List<String> args) {

        if (args.size() < 2) {
            return Result.error(
                    "ERR wrong number of arguments"
            );
        }

        long deleted = 0;

        for (int i = 1; i < args.size(); i++) {

            if (database.delete(args.get(i))) {
                deleted++;
            }
        }

        return Result.integer(deleted);
    }

    private Result exists(List<String> args) {

        if (args.size() < 2) {
            return Result.error(
                    "ERR wrong number of arguments"
            );
        }

        long count = 0;

        for (int i = 1; i < args.size(); i++) {

            if (database.exists(args.get(i))) {
                count++;
            }
        }

        return Result.integer(count);
    }

    private Result expire(List<String> args) {

        if (args.size() != 3) {
            return Result.error(
                    "ERR wrong number of arguments"
            );
        }

        RedisValue value =
                database.get(args.get(1));

        if (value == null) {
            return Result.integer(0);
        }

        long seconds =
                Long.parseLong(args.get(2));

        value.setExpireAt(
                System.currentTimeMillis()
                        + seconds * 1000
        );

        return Result.integer(1);
    }

    private Result ttl(List<String> args) {

        if (args.size() != 2) {
            return Result.error(
                    "ERR wrong number of arguments"
            );
        }

        RedisValue value =
                database.get(args.get(1));

        if (value == null) {
            return Result.integer(-2);
        }

        if (value.getExpireAt() == -1) {
            return Result.integer(-1);
        }

        long remaining =
                value.getExpireAt()
                - System.currentTimeMillis();

        if (remaining <= 0) {
            database.delete(args.get(1));
            return Result.integer(-2);
        }

        return Result.integer(
                remaining / 1000
        );
    }

    private Result incr(List<String> args) {

        if (args.size() != 2) {
            return Result.error(
                    "ERR wrong number of arguments"
            );
        }

        String key = args.get(1);

        RedisValue current =
                database.get(key);

        long number = 0;

        if (current != null) {

            if (current.getType() !=
                    DataType.STRING) {

                return Result.error(
                        "WRONGTYPE Operation"
                );
            }

            number = Long.parseLong(
                    (String) current.getValue()
            );
        }

        number++;

        database.put(
                key,
                new RedisValue(
                        DataType.STRING,
                        String.valueOf(number)
                )
        );

        return Result.integer(number);
    }

    private Result keys(List<String> args) {

        if (args.size() != 2) {
            return Result.error(
                    "ERR wrong number of arguments"
            );
        }

        String pattern = args.get(1);

        List<String> result =
                database.keys()
                        .stream()
                        .filter(key ->
                                matches(
                                        key,
                                        pattern
                                ))
                        .sorted()
                        .toList();

        return Result.array(result);
    }

    private boolean matches(
            String key,
            String pattern) {

        if (pattern.equals("*")) {
            return true;
        }

        if (pattern.startsWith("*") &&
                pattern.endsWith("*")) {

            String value =
                    pattern.substring(
                            1,
                            pattern.length() - 1
                    );

            return key.contains(value);
        }

        if (pattern.startsWith("*")) {

            return key.endsWith(
                    pattern.substring(1)
            );
        }

        if (pattern.endsWith("*")) {

            return key.startsWith(
                    pattern.substring(
                            0,
                            pattern.length() - 1
                    )
            );
        }

        return key.equals(pattern);
    }

    private Result flushDb(List<String> args) {

        if (args.size() != 1) {
            return Result.error(
                    "ERR wrong number of arguments"
            );
        }

        database.clear();

        return Result.simple("OK");
    }

    public record Result(
            Type type,
            String value,
            long integer,
            List<String> array
    ) {

        public enum Type {
            SIMPLE,
            ERROR,
            BULK,
            INTEGER,
            ARRAY
        }

        public static Result simple(String value) {
            return new Result(
                    Type.SIMPLE,
                    value,
                    0,
                    null
            );
        }

        public static Result error(String value) {
            return new Result(
                    Type.ERROR,
                    value,
                    0,
                    null
            );
        }

        public static Result bulk(String value) {
            return new Result(
                    Type.BULK,
                    value,
                    0,
                    null
            );
        }

        public static Result integer(long value) {
            return new Result(
                    Type.INTEGER,
                    null,
                    value,
                    null
            );
        }

        public static Result array(
                List<String> value) {

            return new Result(
                    Type.ARRAY,
                    null,
                    0,
                    value
            );
        }
    }
}