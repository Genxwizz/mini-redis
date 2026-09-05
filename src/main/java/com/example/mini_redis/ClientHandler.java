package com.example.mini_redis;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Database database;

    public ClientHandler(
            Socket socket,
            Database database) {

        this.socket = socket;
        this.database = database;
    }

    @Override
    public void run() {

        try (
                socket;
                InputStream input =
                        socket.getInputStream();

                OutputStream output =
                        socket.getOutputStream()
        ) {

            RespReader reader =
                    new RespReader(input);

            RespWriter writer =
                    new RespWriter(output);

            CommandExecutor executor =
                    new CommandExecutor(database);

            while (true) {

                List<String> command =
                        reader.readCommand();

                if (command == null) {
                    break;
                }

                CommandExecutor.Result result =
                        executor.execute(command);

                writeResult(writer, result);
            }

        } catch (Exception e) {

            System.out.println(
                    "Client disconnected: "
                            + e.getMessage()
            );
        }
    }

    private void writeResult(
            RespWriter writer,
            CommandExecutor.Result result)
            throws IOException {

        switch (result.type()) {

            case SIMPLE ->
                    writer.simpleString(
                            result.value()
                    );

            case ERROR ->
                    writer.error(
                            result.value()
                    );

            case BULK ->
                    writer.bulkString(
                            result.value()
                    );

            case INTEGER ->
                    writer.integer(
                            result.integer()
                    );

            case ARRAY ->
                    writer.array(
                            result.array()
                    );
        }
    }
}