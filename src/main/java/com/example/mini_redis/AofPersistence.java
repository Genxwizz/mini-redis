package com.example.mini_redis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class AofPersistence {

    private final Path file;

    public AofPersistence(String fileName) {
        this.file = Paths.get(fileName);
    }

   
    public synchronized void append(List<String> command)
            throws IOException {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             file,
                             StandardOpenOption.CREATE,
                             StandardOpenOption.APPEND)) {

            writer.write(String.join(" ", command));
            writer.newLine();
        }
    }

   
    public synchronized List<String> load()
            throws IOException {

        if (!Files.exists(file)) {
            return List.of();
        }

        return Files.readAllLines(file);
    }

 
    public synchronized void clear()
            throws IOException {

        Files.deleteIfExists(file);
    }

   
    public boolean exists() {
        return Files.exists(file);
    }
}