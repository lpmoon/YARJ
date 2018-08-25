package com.lpmoon.agent.command;

import com.lpmoon.agent.starter.Agent;
import com.lpmoon.agent.transformer.ParameterRecordFileTransformer;
import com.lpmoon.agent.transformer.RestoreFileTransformer;
import com.lpmoon.agent.util.CommonResultBuilder;
import com.lpmoon.agent.util.ExitResultBuilder;
import com.lpmoon.agent.util.OldClassHolder;
import com.lpmoon.agent.util.Table;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CommandAnnotation(name="parameter")
public class ParameterCommand implements Command {

    private String options;
    private SocketChannel socketChannel;
    private Instrumentation instrumentation;

    private OldClassHolder oldClassHolder = new OldClassHolder();
    private ParameterRecordFileTransformer transformer = new ParameterRecordFileTransformer(oldClassHolder);
    private RestoreFileTransformer restoreFileTransformer = new RestoreFileTransformer(oldClassHolder);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private List<Map<String, Object>> paramsHolder = new LinkedList<>();

    private String destClass;
    private String destMethod;

    public ParameterCommand(String options, SocketChannel socketChannel, Instrumentation instrumentation) {
        this.options = options;
        this.instrumentation = instrumentation;
        this.socketChannel = socketChannel;
    }

    @Override
    public String name() {
        return "parameter";
    }

    public static String help() {
        Options options = new Options( );
        options.addOption("c", "class", true, "Class");
        options.addOption("m", "method", true, "Method");
        options.addOption("t", "time", true, "Time");

        HelpFormatter helpFormatter = new HelpFormatter();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(os);

        helpFormatter.printHelp(writer, 80, "parameter", "", options, 1, 1, "");
        writer.flush();

        return os.toString();
    }

    @Override
    public void init() throws IllegalArgumentException {
    }

    @Override
    public void handle() {
        try {

            Options op = new Options();
            op.addOption("c", "class", true, "Class");
            op.addOption("m", "method", true, "Method");
            op.addOption("t", "time", true, "Time");

            BasicParser basicParser = new BasicParser();
            CommandLine commandLine = basicParser.parse(op, this.options.split(" "));

            if (!(commandLine.hasOption("c") && commandLine.hasOption("m"))) {
                executorService.shutdown();
                writeExit();
            }

            destClass = commandLine.getOptionValue("c");
            destMethod = commandLine.getOptionValue("m");
            transformer.setMethod(destMethod);

            instrumentation.addTransformer(transformer, true);

            ClassLoader classLoader = Agent.class.getClassLoader();
            Class classLoaderClazz = classLoader.getClass();
            while (classLoaderClazz != ClassLoader.class) {
                classLoaderClazz = classLoaderClazz.getSuperclass();
            }

            System.out.println(classLoaderClazz);
            Field field = classLoaderClazz.getDeclaredField("classes");
            field.setAccessible(true);
            Vector v = (Vector) field.get(classLoader);
            for (int i = 0; i < v.size(); i++) {
                Class clazz = (Class) v.get(i);
                if (clazz == null) {
                    continue;
                }

                if (!clazz.getName().contains("com.lpmoon") && clazz.getName().equals(destClass)) {
                    System.out.println(clazz.getName());
                    instrumentation.retransformClasses((Class) v.get(i));
                } else {
                    System.out.println(clazz.getName() + " should not be register");
                }
            }

            if (commandLine.hasOption("t")) {
                String time = commandLine.getOptionValue("t");
                executorService.schedule(new ReportTask(), Integer.parseInt(time), TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        writeResult();
        innerStop();
    }

    public void putParameter(Map parameters) {
        System.out.println(parameters);
        System.out.println("fuck a fuck");
        this.paramsHolder.add(parameters);
    }

    /**
     * report task
     */
    private class ReportTask implements Runnable {

        @Override
        public void run() {
            writeResult();
            innerStop();
        }
    }

    private void innerStop() {
        // 处理
        executorService.shutdown();

        instrumentation.addTransformer(restoreFileTransformer, true);

        try {
            ClassLoader classLoader = Agent.class.getClassLoader();
            Class classLoaderClazz = classLoader.getClass();
            while (classLoaderClazz != ClassLoader.class) {
                classLoaderClazz = classLoaderClazz.getSuperclass();
            }

            System.out.println(classLoaderClazz);
            Field field = classLoaderClazz.getDeclaredField("classes");
            field.setAccessible(true);
            Vector v = (Vector) field.get(classLoader);
            for (int i = 0; i < v.size(); i++) {
                Class clazz = (Class) v.get(i);
                if (clazz == null) {
                    continue;
                }

                if (!clazz.getName().contains("com.lpmoon") && clazz.getName().equals(destClass)) {
                    instrumentation.retransformClasses((Class) v.get(i));
                } else {
                    System.out.println(clazz.getName() + " should not be register");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeResult() {

        String data = "";
        if (!paramsHolder.isEmpty()) {
            Map firstParameters = paramsHolder.get(0);
            List<Object> parameterNames = new ArrayList<>(firstParameters.keySet());
            Table table = new Table(parameterNames.size());
            try {
                for (Object key : parameterNames) {
                    table.addColumn(key.toString(), 0, true);
                }

                for (Map parameters : paramsHolder) {
                    List<String> values = new ArrayList<>();
                    for (Object parameterName : parameterNames) {
                        values.add(parameters.get(parameterName).toString());
                    }

                    table.addRow(values.toArray(new String[parameterNames.size()]));
                }

                data = table.print();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        byte[] content = CommonResultBuilder.build(data.getBytes());

        try {
            socketChannel.write(ByteBuffer.wrap(content, 0, content.length));
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeExit();
    }

    private void writeExit() {
        byte[] exit = ExitResultBuilder.build();
        try {
            socketChannel.write(ByteBuffer.wrap(exit, 0, exit.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


