package com.lpmoon.agent.command;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CommandManager {


    private Map<String, Class> name2CommandClass = new HashMap<>();

    private Map<String, Command> name2Command = new HashMap<>();

    public static CommandManager instance;

    private static Object locker = new Object();

    @SuppressWarnings("unchecked")
    public void init() {
        String basePack = "com.lpmoon.agent.command";
        try {
            Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(basePack.replace(".", "/"));

            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                String protocol = url.getProtocol();
                if ("jar".equalsIgnoreCase(protocol)) {
                    //转换为JarURLConnection
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    if (connection != null) {
                        JarFile jarFile = connection.getJarFile();
                        if (jarFile != null) {
                            //得到该jar文件下面的类实体
                            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                            while (jarEntryEnumeration.hasMoreElements()) {
                                JarEntry entry = jarEntryEnumeration.nextElement();
                                String jarEntryName = entry.getName();
                                //这里我们需要过滤不是class文件和不在basePack包名下的类
                                if (jarEntryName.contains(".class") && jarEntryName.replaceAll("/", ".").startsWith(basePack)) {
                                    String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                                    Class<?> cls = Class.forName(className);

                                    boolean match = false;
                                    Class[] interfaces = cls.getInterfaces();
                                    CommandAnnotation annotation = cls.getAnnotation(CommandAnnotation.class);
                                    for (Class inter : interfaces) {
                                        if (inter == Command.class) {
                                            System.out.println(className);
                                            if (annotation != null) {
                                                match = true;
                                                break;
                                            }
                                        }
                                    }

                                    if (match) {
                                        System.out.println(annotation.name());
                                        name2CommandClass.put(annotation.name(), cls);

//                                        Constructor constructor = cls.getConstructor();
//                                        Object command = constructor.newInstance();
//                                        ((Command)command).init();
//                                        name2Command.put(((Command) command).name(), (Command) command);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle(ByteBuffer buf, SocketChannel socketChannel, Instrumentation instrumentation) {
        buf.flip();
        List<Byte> dataBytes = new ArrayList<Byte>();
        while (buf.hasRemaining()) {
            dataBytes.add(buf.get());
        }

        byte[] bytes = new byte[dataBytes.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = dataBytes.get(i);
        }

        String data = new String(bytes);

        int idx = data.indexOf(' ');
        String commandStr = data.substring(0, idx < 0 ? data.length() : idx);
        String options = "";
        if (idx > 0) {
            options = data.substring(idx + 1);
        }

        // 如果是退出命令
        if (commandStr.equals("exit")) {
            Command command = name2Command.get(options);
            if (command == null) {
                return;
            }

            command.stop();
            name2Command.remove(options);
            return;
        }

        Command command = null;
        synchronized (locker) {
            Class<?> commandClass = name2CommandClass.get(commandStr);

            try {
                Constructor constructor = commandClass.getConstructor(String.class, SocketChannel.class, Instrumentation.class);
                command = (Command) constructor.newInstance(options, socketChannel, instrumentation);
                Method initMethod = commandClass.getMethod("init");
                initMethod.invoke(command);
            } catch (Exception e) {
                e.printStackTrace();
                // 异常
                return;
            }
            name2Command.put(commandStr, command);
        }

        command.handle();
    }

    public Map<String, Command> getName2Command() {
        return name2Command;
    }

    public Map<String, Class> getName2CommandClass() {
        return name2CommandClass;
    }

    public void setName2CommandClass(Map<String, Class> name2CommandClass) {
        this.name2CommandClass = name2CommandClass;
    }

    public void setName2Command(Map<String, Command> name2Command) {
        this.name2Command = name2Command;
    }

    public Command getCommand(String name) {
        synchronized (locker) {
            return name2Command.get(name);
        }
    }
}
