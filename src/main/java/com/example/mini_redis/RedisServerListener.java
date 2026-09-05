package com.example.mini_redis;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class RedisServerListener implements ServletContextListener {

    public static final String DATABASE_ATTRIBUTE = "mini_redis_database";
    private MiniRedisServer redisServer;
    private Thread serverThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Database database = new Database();

        AofPersistence aof = new AofPersistence("append.aof");
        if (aof.exists()) {
            try {
                CommandExecutor executor = new CommandExecutor(database, null);
                for (String line : aof.load()) {
                    if (line != null && !line.isBlank()) {
                        executor.execute(java.util.Arrays.asList(line.split(" ")));
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to replay AOF log: " + e.getMessage());
            }
        }

        context.setAttribute(DATABASE_ATTRIBUTE, database);

        redisServer = new MiniRedisServer(database);
        serverThread = new Thread(() -> {
            System.out.println("Starting MiniRedisServer TCP listener via ServletContextListener...");
            redisServer.start();
        }, "MiniRedisServerThread");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (redisServer != null) {
            System.out.println("Stopping MiniRedisServer TCP listener...");
            redisServer.stop();
        }
    }
}
