package com.lpmoon.agent.command;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class CommandManager {


    public static void handle(ByteBuffer buf, SocketChannel socketChannel) {
        String data = new String(buf.array());
        String command = data.substring(0, data.indexOf(' '));

        if (command.equals("statics")) {
            Command commandImpl = new StatisticCommand(data.substring(data.indexOf(' ')));

        }
    }
}
