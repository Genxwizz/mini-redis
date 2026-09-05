package com.example.mini_redis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespReader {

    private final BufferedReader reader;

    public RespReader(InputStream inputStream) {

        reader = new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                )
        );
    }

    public List<String> readCommand() throws IOException {

        String firstLine = reader.readLine();

        if (firstLine == null) {
            return null;
        }

        // RESP Array
        if (firstLine.startsWith("*")) {

            int count = Integer.parseInt(
                    firstLine.substring(1)
            );

            List<String> command = new ArrayList<>();

            for (int i = 0; i < count; i++) {

                String lengthLine = reader.readLine();

                if (lengthLine == null ||
                        !lengthLine.startsWith("$")) {

                    throw new IOException("Invalid RESP");
                }

                int length = Integer.parseInt(
                        lengthLine.substring(1)
                );

                if (length == -1) {
                    command.add(null);
                    continue;
                }

                if (length < 0) {
                    throw new IOException("Invalid bulk string length: " + length);
                }

                char[] buffer = new char[length];

                int read = 0;

                while (read < length) {

                    int n = reader.read(
                            buffer,
                            read,
                            length - read
                    );

                    if (n == -1) {
                        throw new IOException(
                                "Unexpected connection close"
                        );
                    }

                    read += n;
                }

                // consume CRLF
                reader.readLine();

                command.add(new String(buffer));
            }

            return command;
        }

        // Simple inline command.
        String trimmed = firstLine.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        String[] parts = trimmed.split("\\s+");

        return List.of(parts);
    }
}