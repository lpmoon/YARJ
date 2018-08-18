package com.lpmoon.agent.command;


import com.lpmoon.agent.util.ByteUtil;
import com.lpmoon.agent.util.CommonResultBuilder;
import com.lpmoon.agent.util.ExitResultBuilder;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

@CommandAnnotation(name="help")
public class HelpCommand implements Command {
    private String options;
    private SocketChannel socketChannel;
    private Instrumentation instrumentation;

    public HelpCommand(String options, SocketChannel socketChannel, Instrumentation instrumentation) {
        this.options = options;
        this.socketChannel = socketChannel;
        this.instrumentation = instrumentation;
    }


    @Override
    public String name() {
        return "help";
    }

    public static String help() {
        Map<String, Class> commandMap = CommandManager.instance.getName2CommandClass();

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Class> commandEntry : commandMap.entrySet()) {
            if (!commandEntry.getKey().equals("help")) {
                try {
                    sb.append(commandEntry.getValue().getMethod("help").invoke(null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    @Override
    public void init() throws IllegalArgumentException {

    }

    @Override
    public void handle() {
        byte[] help = help().getBytes();
        byte[] content = CommonResultBuilder.build(help);

        try {
            socketChannel.write(ByteBuffer.wrap(content, 0, content.length));
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] exit = ExitResultBuilder.build();
        try {
            socketChannel.write(ByteBuffer.wrap(exit, 0, exit.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException();
    }
}
