package com.example.mini_redis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MiniRedisServer {

    private static final int PORT = 6379;
    private static final int HTTP_PORT = 8080;
    private final Database database;
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private volatile boolean running = false;

    public MiniRedisServer(Database database) {
        this.database = database;
    }

    public void start() {
        if (running) return;
        running = true;
        executor = Executors.newFixedThreadPool(20);

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("=================================");
            System.out.println("       MINI REDIS TCP SERVER     ");
            System.out.println("       Port: " + PORT);
            System.out.println("=================================");

            while (running && !serverSocket.isClosed()) {
                try {
                    Socket client = serverSocket.accept();
                    executor.submit(new ClientHandler(client, database));
                } catch (SocketException e) {
                    if (!running) {
                        break;
                    }
                    throw e;
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("MiniRedisServer error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        System.out.println("MiniRedisServer stopped.");
    }

    public static void main(String[] args) throws IOException {
        Database database = new Database();
        AofPersistence aof = new AofPersistence("append.aof");
        CommandExecutor executor = new CommandExecutor(database, aof);

        if (aof.exists()) {
            try {
                for (String line : aof.load()) {
                    if (line != null && !line.isBlank()) {
                        executor.execute(java.util.Arrays.asList(line.split(" ")));
                    }
                }
                System.out.println("AOF persistence log replayed successfully.");
            } catch (Exception e) {
                System.err.println("Failed to replay AOF log: " + e.getMessage());
            }
        }

        // Start HTTP Web Dashboard Server
        HttpDashboardServer dashboardServer = new HttpDashboardServer(HTTP_PORT, database, executor);
        dashboardServer.start();

        // Register Shutdown Hook
        MiniRedisServer server = new MiniRedisServer(database);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down Mini Redis application...");
            dashboardServer.stop();
            server.stop();
        }));

        // Start TCP Server
        server.start();
    }
}