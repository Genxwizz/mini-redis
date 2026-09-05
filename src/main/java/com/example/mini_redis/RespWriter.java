package com.example.mini_redis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RespWriter {

    private final BufferedWriter writer;

    public RespWriter(OutputStream outputStream) {

        writer = new BufferedWriter(
                new OutputStreamWriter(
                        outputStream,
                        StandardCharsets.UTF_8
                )
        );
    }

    public void simpleString(String value)
            throws IOException {

        writer.write("+" + value + "\r\n");
        writer.flush();
    }

    public void error(String value)
            throws IOException {

        writer.write("-" + value + "\r\n");
        writer.flush();
    }

    public void integer(long value)
            throws IOException {

        writer.write(":" + value + "\r\n");
        writer.flush();
    }

    public void bulkString(String value)
            throws IOException {

        if (value == null) {

            writer.write("$-1\r\n");

        } else {

            byte[] bytes =
                    value.getBytes(StandardCharsets.UTF_8);

            writer.write("$" + bytes.length + "\r\n");
            writer.write(value);
            writer.write("\r\n");
        }

        writer.flush();
    }

    public void array(List<String> values)
            throws IOException {

        if (values == null) {
            writer.write("*-1\r\n");
            writer.flush();
            return;
        }

        writer.write("*" + values.size() + "\r\n");

        for (String value : values) {

            if (value == null) {
                writer.write("$-1\r\n");
            } else {
                byte[] bytes =
                        value.getBytes(StandardCharsets.UTF_8);

                writer.write("$" + bytes.length + "\r\n");
                writer.write(value);
                writer.write("\r\n");
            }
        }

        writer.flush();
    }
}