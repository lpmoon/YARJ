# 说明
该项目提供了一个简单的机制，用于检测jvm内部的运行情况，打印出各个方法调用总数以及消耗的时长。

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
```
java -cp ./target/DynamicRecordJVM-1.0-SNAPSHOT-jar-with-dependencies.jar;"%JAVA_HOME%/jre/lib/tools.jar";. com.lpmoon.AttachMain xxxx
```
其中xxxx对应的是pid。

如果运行成功则可以在Test2.class对应的控制台中看到对应的输出。停止AttachMain的运行后Test2.class任务将结束，控制打印的输出将停留在AttachMain停止那一刻的数据。
