# 说明
该项目提供了一个简单的机制，用于检测jvm内部的运行情况。
目前支持的命令有
1. help
2. statistics
3. parameter

# 使用

## 打包
```
mvn clean package -PwithToolsJar
```
该项目用到了tools.jar，如果遇到找不到tools.jar的情况，需要在pom.xml中修改如下代码中的systemPath
```
  <profiles>
        <profile>
            <id>withToolsJar</id>
            <activation>
                <property>
                    <name>java.vendor</name>
                    <value>Sun Microsystems Inc.</value>
                </property>
            </activation>
            <dependencies>
                <dependency>
                    <groupId>com.sun</groupId>
                    <artifactId>tools</artifactId>
                    <version>1.8</version>
                    <scope>system</scope>
                    <systemPath>${java.home}/lib/tools.jar</systemPath>
                </dependency>
            </dependencies>
        </profile>
    </profiles>
```
编译好的jar包在target目录下

## 运行
运行源码目录下的Test2.class，然后使用jps查看对应的pid，然后进入到target目录下运行  
windows环境
```
java -cp ./target/DynamicRecordJVM-1.0-SNAPSHOT-jar-with-dependencies.jar;"%JAVA_HOME%/lib/tools.jar";. com.lpmoon.agent.starter.AttachMain xxxx
```
linux环境
```
java -cp ./target/DynamicRecordJVM-1.0-SNAPSHOT-jar-with-dependencies.jar:$JAVA_HOME/lib/tools.jar":. com.lpmoon.agent.starter.AttachMain xxxx
```

其中xxxx对应的是pid。

运行客户端，

使用Help查看对应的命令即可运行。

## TODO

1. 处理各种并发问题

