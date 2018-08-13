package com.lpmoon.agent.command;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CommandManager {
    private Map<String, Command> name2Command = new HashMap<>(2^4);

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
                                    Class cls = Class.forName(className);

                                    boolean match = false;
                                    Class[] interfaces = cls.getInterfaces();
                                    Annotation[] annotations = cls.getAnnotations();
                                    for (Class inter : interfaces) {
                                        if (inter == Command.class) {
                                            for (Annotation annotation : annotations) {
                                                if (annotation instanceof CommandAnnotation) {
                                                    match = true;
                                                    break;
                                                }
                                            }

                                            if (match) {
                                                break;
                                            }
                                        }
                                    }

                                    if (match) {
                                        Constructor constructor = cls.getConstructor();
                                        Object command = constructor.newInstance();
                                        ((Command)command).init();
                                        name2Command.put(((Command) command).name(), (Command) command);
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
        String data = new String(buf.array());
        String command = data.substring(0, data.indexOf(' '));
        String options = data.substring(data.indexOf(' ' + 1));

        name2Command.get(command).handle(options, socketChannel, instrumentation);
    }
}
