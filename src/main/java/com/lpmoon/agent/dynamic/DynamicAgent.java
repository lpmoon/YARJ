package com.lpmoon.agent.dynamic;

import com.lpmoon.agent.CommonAgent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by zblacker on 2017/5/9.
 */
public class DynamicAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) throws ClassNotFoundException, UnmodifiableClassException,
            InterruptedException {
        CommonAgent.doAgent(agentArgs, inst);
    }
}
