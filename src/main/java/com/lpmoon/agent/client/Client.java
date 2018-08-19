package com.lpmoon.agent.client;

import com.lpmoon.agent.util.ByteUtil;
import com.lpmoon.agent.util.StringUtils;
import org.apache.commons.cli.*;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private String ip;
    private String port;
    private volatile String currentCommand;
    private Socket socket;
    private Scanner sc;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public Client(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public void init() throws IOException {
        sc = new Scanner(System.in);
        socket = new Socket(ip, Integer.parseInt(port));

        registerCtrlCHandler(sc, socket);
        executorService.submit(new ClientTask(sc, socket));
    }

    public class ClientTask implements Runnable {
        private Scanner sc;
        private Socket socket;

        public ClientTask(Scanner sc, Socket socket) {
            this.sc = sc;
            this.socket = socket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (sc != null) {
                        System.out.print("cmd>");
                        String input = sc.nextLine();

                        if (StringUtils.isEmpty(input)) {
                            continue;
                        }

                        int index = input.indexOf(' ');
                        if (index == -1) {
                            currentCommand = input;
                        } else {
                            currentCommand = input.substring(0, index);
                        }

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
                                    currentCommand = "";
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
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

        Client client = new Client(ip, port);
        client.init();
    }

    private void registerCtrlCHandler(Scanner sc, Socket socket) {
        SignalHandler ctrlC = new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                // 如果当前的命令不为空
                if (StringUtils.isNotEmpty(currentCommand)) {
                    String exitCommand = "exit " + currentCommand;

                    try {
                        socket.getOutputStream().write(exitCommand.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("exit " + currentCommand);
                }
            }
        };

        Signal.handle(new Signal("INT"), ctrlC);
    }
}
