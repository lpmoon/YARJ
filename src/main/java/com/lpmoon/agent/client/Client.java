package com.lpmoon.agent.client;

import com.lpmoon.agent.util.ByteUtil;
import org.apache.commons.cli.*;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws ParseException, IOException {
        Options options = new Options( );
        options.addOption("h", "host", false, "Host ip");
        options.addOption("p", "port", false, "Host port");

        BasicParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, args);
        String ip = commandLine.getOptionValue("h");
        String port = commandLine.getOptionValue("p");

        if (ip == null || ip.isEmpty() || port == null || port.isEmpty()) {
            throw new RuntimeException();
        }

        Socket socket = new Socket(ip, Integer.parseInt(port));

        Console console = System.console();
        console.writer().print("cmd>");

        while (true) {
            if (console != null) {
                String input = console.readLine();
                socket.getOutputStream().write(input.getBytes());


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
                                bytes = newBytes;
                                offset = readCount;
                                continue;
                            }

                            // header读取完毕
                            int resultCount = ByteUtil.bytesToInt(bytes, 1);
                            byte[] newBytes = new byte[resultCount + 5];
                            bytes = newBytes;
                            System.arraycopy(bytes, 0, newBytes, 0, 1000);

                            offset = readCount;
                            readHeader = true;
                        } else if (bytes[0] == 0x02) {
                            break; // 退出
                        }
                    }

                    readCount += socket.getInputStream().read(bytes, offset, bytes.length - offset);
                    offset = readCount;
                    if (offset == readCount) {
                        byte[] content = new byte[bytes.length - 5];
                        System.arraycopy(bytes, 5, console, 0, bytes.length - 5);
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
