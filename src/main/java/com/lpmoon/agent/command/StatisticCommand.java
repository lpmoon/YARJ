package com.lpmoon.agent.command;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.lpmoon.agent.reporter.CodahaleSummary;
import com.lpmoon.agent.starter.Agent;
import com.lpmoon.agent.transformer.RestoreFileTransformer;
import com.lpmoon.agent.transformer.StatisticsClassFileTransformer;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CommandAnnotation(name="statistics")
public class StatisticCommand implements Command {

    private String options;
    private SocketChannel socketChannel;
    private Instrumentation instrumentation;

    private CodahaleSummary summary = new CodahaleSummary();
    private OldClassHolder oldClassHolder = new OldClassHolder();
    private StatisticsClassFileTransformer transformer = new StatisticsClassFileTransformer(oldClassHolder);
    private RestoreFileTransformer restoreFileTransformer = new RestoreFileTransformer(oldClassHolder);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public StatisticCommand(String options, SocketChannel socketChannel, Instrumentation instrumentation) {
        this.options = options;
        this.instrumentation = instrumentation;
        this.socketChannel = socketChannel;
    }

    @Override
    public String name() {
        return "statistics";
    }

    public static String help() {
        Options options = new Options( );
        options.addOption("c", "class", false, "Classes");
        options.addOption("t", "time", false, "Time");

        HelpFormatter helpFormatter = new HelpFormatter();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(os);

        helpFormatter.printHelp(writer, 80, "statistics", "", options, 1, 1, "");
        writer.flush();

        return os.toString();
    }

    @Override
    public void init() throws IllegalArgumentException {
        summary.start();
    }

    @Override
    public void handle() {

        instrumentation.addTransformer(transformer, true);

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

                if (!clazz.getName().contains("com.lpmoon")) {
                    System.out.println(clazz.getName());
                    instrumentation.retransformClasses((Class) v.get(i));
                } else {
                    System.out.println(clazz.getName() + " should not be register");
                }
            }

            Options op = new Options( );
            op.addOption("c", "class", true, "Classes");
            op.addOption("t", "time", true, "Time");

            BasicParser basicParser = new BasicParser();
            CommandLine commandLine = basicParser.parse(op, this.options.split(" "));

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

    private void innerStop() {
        // 处理
        executorService.shutdown();
        summary.stop();

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

                if (!clazz.getName().contains("com.lpmoon")) {
                    System.out.println(clazz.getName());
                    instrumentation.retransformClasses((Class) v.get(i));
                } else {
                    System.out.println(clazz.getName() + " should not be register");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void writeResult() {
        Map<String, Histogram> histogramMap = summary.getHistograms();

        String data = "";
        Table table = new Table(11);
        try {
            table.addColumn("method", 0, true);
            table.addColumn("size", 0, true);
            table.addColumn("50%", 0, true);
            table.addColumn("75%", 0, true);
            table.addColumn("95%", 0, true);
            table.addColumn("98%", 0, true);
            table.addColumn("99%", 0, true);
            table.addColumn("99.9%", 0, true);
            table.addColumn("max", 0, true);
            table.addColumn("mean", 0, true);
            table.addColumn("min", 0, true);

            List<String> keys = new ArrayList<>(histogramMap.keySet());
            Collections.sort(keys);

            for (String key : keys) {
                Snapshot snapshot = histogramMap.get(key).getSnapshot();
                table.addRow(key, String.valueOf(snapshot.size()), String.valueOf(snapshot.getMedian()), String.valueOf(snapshot.get75thPercentile()),
                             String.valueOf(snapshot.get95thPercentile()), String.valueOf(snapshot.get98thPercentile()), String.valueOf(snapshot.get99thPercentile()),
                             String.valueOf(snapshot.get999thPercentile()), String.valueOf(snapshot.getMax()), String.valueOf(snapshot.getMean()), String.valueOf(snapshot.getMin()));
            }

            data = table.print();
        } catch (Exception e) {
            e.printStackTrace();
        }


        byte[] content = CommonResultBuilder.build(data.getBytes());

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


    public CodahaleSummary getSummary() {
        return summary;
    }

    public void setSummary(CodahaleSummary summary) {
        this.summary = summary;
    }
}


