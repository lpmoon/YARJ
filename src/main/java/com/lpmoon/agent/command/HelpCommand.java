package com.lpmoon.agent.command;


import com.lpmoon.agent.util.ByteUtil;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

@CommandAnnotation
public class HelpCommand implements Command {
    @Override
    public String name() {
        return "help";
    }

    @Override
    public String help() {
        Map<String, Command> commandMap = CommandManager.instance.getName2Command();

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Command> commandEntry : commandMap.entrySet()) {
            if (!commandEntry.getKey().equals(name())) {
                sb.append(commandEntry.getValue().help());
            }
        }

        return sb.toString();
    }

    @Override
    public void init() throws IllegalArgumentException {

    }

    @Override
    public void handle(String options, SocketChannel socketChannel, Instrumentation instrumentation) {
        byte[] help = help().getBytes();
        byte[] content = new byte[help.length + 5];
        content[0] = 0x01;
        byte[] length = ByteUtil.toByteArray(help.length);
        content[1] = length[0];
        content[2] = length[1];
        content[3] = length[2];
        content[4] = length[3];
        System.arraycopy(help, 0, content, 5, help.length);

        try {
            socketChannel.write(ByteBuffer.wrap(content, 0, content.length));
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] exit = new byte[1];
        exit[0] = 0x02;
        try {
            socketChannel.write(ByteBuffer.wrap(exit, 0, exit.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
