package com.lpmoon.agent.command;

import java.nio.channels.SocketChannel;

public interface Command {
    String name();
    void init() throws IllegalArgumentException;
    void handle(String options, SocketChannel socketChannel);
}
