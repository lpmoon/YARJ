package com.lpmoon.agent.command;

import java.nio.channels.SocketChannel;

public class StatisticCommand implements Command {
    private String options;
    private SocketChannel socketChannel;

    private String[] methods;

    public StatisticCommand(String options, SocketChannel socketChannel) {
        this.options = options;
        this.socketChannel = socketChannel;
    }

    @Override
    public String name() {
        return "statistics";
    }

    @Override
    public void init() throws IllegalArgumentException {

    }

    @Override
    public void handle(String options, SocketChannel socketChannel) {

    }
}


