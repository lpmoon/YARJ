package com.lpmoon.agent.starter;


import com.lpmoon.agent.server.AgentServer;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by zblacker on 2017/5/9.
 */
public class Agent {
    public static void agentmain(String agentArgs, Instrumentation inst) throws ClassNotFoundException, UnmodifiableClassException,
            InterruptedException, IOException {

        AgentServer agentServer = new AgentServer(30001, inst);
        agentServer.init();
    }
}
