package com.lpmoon.agent;

import com.lpmoon.agent.starter.Agent;
import com.lpmoon.transformer.RestoreFileTransformer;
import com.lpmoon.transformer.StatisticsClassFileTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liupeng10 on 17/6/28.
 */
public class CommonAgent {
    public static AtomicInteger switcher = new AtomicInteger();

    public static void doAgent(String agentArgs, Instrumentation inst) throws ClassNotFoundException, UnmodifiableClassException,
            InterruptedException {
        System.out.println("enter agent main, parameter is " + agentArgs);

        ClassFileTransformer transformer = null;
        if (switcher.incrementAndGet() % 2 == 1) {
            System.out.println("statistics transformer start");
            transformer = new StatisticsClassFileTransformer();
        } else {
            System.out.println("restore transformer start");
            transformer = new RestoreFileTransformer();
        }

        inst.addTransformer(transformer, true);

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
                    inst.retransformClasses((Class) v.get(i));
                } else {
                    System.out.println(clazz.getName() + " should not be register");
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
