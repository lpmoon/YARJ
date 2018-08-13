package com.lpmoon.agent.command;


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

        try {
            socketChannel.write(ByteBuffer.wrap(help, 0, help.length));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
