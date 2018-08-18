package com.lpmoon.agent.client;

import com.lpmoon.agent.util.ByteUtil;
import org.apache.commons.cli.*;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws ParseException, IOException {
        Options options = new Options( );
        options.addOption("h", "host", true, "Host ip");
        options.addOption("p", "port", true, "Host port");

        BasicParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, args);
        String ip = null;
        String port = null;
        if (commandLine.hasOption("h")) {
            ip = commandLine.getOptionValue("h");
        }

        if (commandLine.hasOption("p")) {
            port = commandLine.getOptionValue("p");
        }

        if (ip == null || ip.isEmpty() || port == null || port.isEmpty()) {
            throw new RuntimeException();
        }

        Socket socket = new Socket(ip, Integer.parseInt(port));

        Scanner sc = new Scanner(System.in);
        while (true) {
            if (sc != null) {
                System.out.print("cmd>");
                String input = sc.nextLine();
                socket.getOutputStream().write(input.getBytes());
                socket.getOutputStream().flush();


                // 读取返回数据
                byte[] bytes = new byte[1];
                int readCount = 0;
                boolean readHeader = false;

                int offset = 0;

                while (true) {
                    if (!readHeader) {
                        int count = socket.getInputStream().read(bytes, offset, bytes.length - offset);
                        readCount += count;

                        if (readCount <= 0) {
                            continue;
                        }

                        if (bytes[0] == 0x01) {
                            if (readCount < 5) {
                                byte[] newBytes = new byte[5];
                                newBytes[0] = 0x01;
                                bytes = newBytes;
                                offset = readCount;
                                continue;
                            }

                            // header读取完毕
                            int resultCount = ByteUtil.bytesToInt(bytes, 1);
                            byte[] newBytes = new byte[resultCount + 5];
                            bytes = newBytes;
                            System.arraycopy(bytes, 0, newBytes, 0, 5);

                            offset = readCount;
                            readHeader = true;
                        } else if (bytes[0] == 0x02) {
                            break; // 退出
                        }
                    }

                    readCount += socket.getInputStream().read(bytes, offset, bytes.length - offset);
                    offset = readCount;
                    if (offset == bytes.length) {
                        byte[] content = new byte[bytes.length - 5];
                        System.arraycopy(bytes, 5, content, 0, bytes.length - 5);
                        System.out.println(new String(content));
                        readHeader = false;
                        readCount = 0;
                        offset = 0;
                        bytes = new byte[1];
                    }
                }
            }
        }

        // TODO 添加暂停
    }
}
