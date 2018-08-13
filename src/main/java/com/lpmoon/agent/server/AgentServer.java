package com.lpmoon.agent.server;

import com.lpmoon.agent.command.CommandManager;

import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;

public class AgentServer {
    private int port;
    private Selector selector;
    private Instrumentation instrumentation;
    private long startTime;

    private CommandManager commandManager = new CommandManager();

    public static AgentServer instance;

    public AgentServer(int port, Instrumentation inst) {
        this.port = port;
        this.instrumentation = inst;
        this.startTime = System.currentTimeMillis();
    }

    public void init() {
        CommandManager.instance = commandManager;
        commandManager.init();

        System.out.println("server started");

        // TODO 替换为可变的port
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress("localhost", port));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                try {
                    selector.select();
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectedKeys.iterator();

                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        try {
                            if (key.isAcceptable()) {
                                System.out.println("accept");
                                SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                                channel.configureBlocking(false);
                                channel.register(selector, SelectionKey.OP_READ);
                                System.out.println(channel.getRemoteAddress());
                            } else if (key.isReadable()) {
                                System.out.println("read");
                                SocketChannel clntChan = (SocketChannel) key.channel();
                                ByteBuffer buf = ByteBuffer.allocate(1000);
                                long bytesRead = -1;
                                try {
                                    bytesRead = clntChan.read(buf);
                                } catch (Exception e) {
                                    key.cancel(); // 取消注册，否则会一直readable的情况
                                }

                                if (bytesRead == -1) {
                                    try {
                                        clntChan.close();
                                    } catch (Exception e) {
                                        key.cancel(); // 取消注册
                                    }
                                } else if (bytesRead > 0) {
                                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                                    commandManager.handle(buf, clntChan, instrumentation);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            iter.remove();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
