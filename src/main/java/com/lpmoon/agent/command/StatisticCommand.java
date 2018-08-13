package com.lpmoon.agent.command;

import com.lpmoon.agent.starter.Agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;
import java.util.Vector;

@CommandAnnotation
public class StatisticCommand implements Command {
    public StatisticCommand() {

    }

    @Override
    public String name() {
        return "statistics";
    }

    @Override
    public void init() throws IllegalArgumentException {

    }

    @Override
    public void handle(String options, SocketChannel socketChannel, Instrumentation instrumentation) {

        ClassFileTransformer transformer = null;

        instrumentation.addTransformer(transformer, true);

        try {
            ClassLoader classLoader = Agent.class.getClassLoader();
            Class classLoaderClazz = classLoader.getClass();
            while (classLoaderClazz != ClassLoader.class)
                classLoaderClazz = classLoaderClazz.getSuperclass();

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
}


