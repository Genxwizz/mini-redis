package com.example.mini_redis;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class HttpDashboardServer {

    private final int port;
    private final Database database;
    private final CommandExecutor commandExecutor;
    private final long startTime;
    private HttpServer server;

public HttpDashboardServer(int port, Database database, CommandExecutor commandExecutor) {
    String renderPort = System.getenv("PORT");

    if (renderPort != null && !renderPort.isBlank()) {
        this.port = Integer.parseInt(renderPort);
    } else {
        this.port = port;
    }

    this.database = database;
    this.commandExecutor = commandExecutor;
    this.startTime = System.currentTimeMillis();
}

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/exec", new ExecHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("=================================");
        System.out.println("   MINI REDIS WEB DASHBOARD     ");
        System.out.println("   HTTP URL: http://localhost:" + port);
        System.out.println("=================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("HttpDashboardServer stopped.");
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equalsIgnoreCase("/index.html")) {
                try (InputStream is = getClass().getResourceAsStream("/static/index.html")) {
                    if (is == null) {
                        sendResponse(exchange, 404, "text/plain", "404 Not Found - index.html missing");
                        return;
                    }
                    byte[] bytes = is.readAllBytes();
                    sendResponseBytes(exchange, 200, "text/html; charset=utf-8", bytes);
                }
            } else {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
            }
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 451, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            long uptimeSec = (System.currentTimeMillis() - startTime) / 1000;
            Set<String> activeKeys = database.keys();
            int totalKeys = database.size();

            StringBuilder json = new StringBuilder("{");
            json.append("\"status\":\"ONLINE\",");
            json.append("\"tcpPort\":6379,");
            json.append("\"httpPort\":").append(port).append(",");
            json.append("\"uptimeSeconds\":").append(uptimeSec).append(",");
            json.append("\"totalKeys\":").append(totalKeys).append(",");
            json.append("\"keys\":[");
            int i = 0;
            for (String k : activeKeys) {
                if (i > 0) json.append(",");
                json.append("\"").append(escapeJson(k)).append("\"");
                i++;
            }
            json.append("]}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    private class ExecHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String rawCommand = parseCommandFromBody(body);

            if (rawCommand == null || rawCommand.isBlank()) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"Empty command\"}");
                return;
            }

            List<String> args = parseCommandLine(rawCommand);
            CommandExecutor.Result result = commandExecutor.execute(args);

            String jsonResult = formatResultToJson(result);
            sendResponse(exchange, 200, "application/json", jsonResult);
        }
    }

    private String parseCommandFromBody(String body) {
        if (body == null) return null;
        body = body.trim();
        if (body.startsWith("{")) {
            // simple JSON key extraction for "command" or "raw"
            int cmdIdx = body.indexOf("\"command\"");
            if (cmdIdx != -1) {
                int colonIdx = body.indexOf(":", cmdIdx);
                if (colonIdx != -1) {
                    int quoteStart = body.indexOf("\"", colonIdx);
                    if (quoteStart != -1) {
                        int quoteEnd = findMatchingQuote(body, quoteStart + 1);
                        if (quoteEnd != -1) {
                            return body.substring(quoteStart + 1, quoteEnd).replace("\\\"", "\"");
                        }
                    }
                }
            }
        }
        return body;
    }

    private int findMatchingQuote(String s, int start) {
        boolean escaped = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return i;
            }
        }
        return -1;
    }

    private List<String> parseCommandLine(String commandLine) {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = ' ';

        for (int i = 0; i < commandLine.length(); i++) {
            char c = commandLine.charAt(i);
            if (inQuotes) {
                if (c == quoteChar) {
                    inQuotes = false;
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"' || c == '\'') {
                    inQuotes = true;
                    quoteChar = c;
                } else if (Character.isWhitespace(c)) {
                    if (!sb.isEmpty()) {
                        list.add(sb.toString());
                        sb.setLength(0);
                    }
                } else {
                    sb.append(c);
                }
            }
        }
        if (!sb.isEmpty()) {
            list.add(sb.toString());
        }
        return list;
    }

    private String formatResultToJson(CommandExecutor.Result result) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"type\":\"").append(result.type().name()).append("\",");

        if (result.type() == CommandExecutor.Result.Type.ERROR) {
            sb.append("\"error\":\"").append(escapeJson(result.value())).append("\"");
        } else if (result.type() == CommandExecutor.Result.Type.SIMPLE || result.type() == CommandExecutor.Result.Type.BULK) {
            if (result.value() == null) {
                sb.append("\"value\":null");
            } else {
                sb.append("\"value\":\"").append(escapeJson(result.value())).append("\"");
            }
        } else if (result.type() == CommandExecutor.Result.Type.INTEGER) {
            sb.append("\"integer\":").append(result.integer());
        } else if (result.type() == CommandExecutor.Result.Type.ARRAY) {
            sb.append("\"array\":[");
            if (result.array() != null) {
                for (int i = 0; i < result.array().size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(escapeJson(result.array().get(i))).append("\"");
                }
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String contentType, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        sendResponseBytes(exchange, statusCode, contentType, bytes);
    }

    private void sendResponseBytes(HttpExchange exchange, int statusCode, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
