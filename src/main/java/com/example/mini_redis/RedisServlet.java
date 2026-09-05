package com.example.mini_redis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RedisServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Database fallbackDatabase = new Database();

    private Database getDatabase() {
        Object attr = getServletContext().getAttribute(RedisServerListener.DATABASE_ATTRIBUTE);
        if (attr instanceof Database db) {
            return db;
        }
        return fallbackDatabase;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Database database = getDatabase();

        String action = request.getParameter("action");
        String key = request.getParameter("key");
        String value = request.getParameter("value");
        String ttlStr = request.getParameter("ttl");

        String result = null;

        if ("set".equals(action)) {
            if (key == null || key.isBlank() || value == null) {
                result = "Error: Key and value are required";
            } else {
                RedisValue redisValue = new RedisValue(DataType.STRING, value);
                if (ttlStr != null && !ttlStr.isBlank()) {
                    try {
                        long ttlSeconds = Long.parseLong(ttlStr);
                        if (ttlSeconds > 0) {
                            redisValue.setExpireAt(System.currentTimeMillis() + (ttlSeconds * 1000));
                        }
                    } catch (NumberFormatException ignored) {}
                }
                database.put(key, redisValue);
                result = "OK";
            }
        } else if ("get".equals(action)) {
            if (key == null || key.isBlank()) {
                result = "Error: Key is required";
            } else {
                RedisValue redisValue = database.get(key);
                if (redisValue == null) {
                    result = "(nil)";
                } else {
                    result = String.valueOf(redisValue.getValue());
                }
            }
        } else if ("delete".equals(action)) {
            if (key == null || key.isBlank()) {
                result = "Error: Key is required";
            } else {
                boolean deleted = database.delete(key);
                result = deleted ? "Deleted 1 key" : "Key not found (0)";
            }
        } else if ("flush".equals(action)) {
            database.clear();
            result = "OK (Flushed DB)";
        }

        request.setAttribute("result", result);
        request.setAttribute("dbSize", database.size());
        request.setAttribute("keys", database.keys());

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}