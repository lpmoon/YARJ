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

public class AgentServer {
    private int port;
    private Selector selector;
    private Instrumentation instrumentation;
    private long startTime;

    public AgentServer(int port, Instrumentation inst) {
        this.port = port;
        this.instrumentation = inst;
        this.startTime = System.currentTimeMillis();
    }

    public void init() {

        // TODO 替换为可变的port
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress("localhost", 31112));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    if (key.isAcceptable()) {
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        continue;
                    }

                    if (key.isReadable()) {
                        SocketChannel clntChan = (SocketChannel) key.channel();
                        ByteBuffer buf = (ByteBuffer) key.attachment();
                        long bytesRead = clntChan.read(buf);
                        if (bytesRead == -1) {
                            clntChan.close();
                        } else if (bytesRead > 0) {
                            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                            CommandManager.handle(buf, clntChan);
                        }
                    }

                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
