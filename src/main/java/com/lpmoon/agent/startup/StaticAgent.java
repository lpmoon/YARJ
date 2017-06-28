package com.lpmoon.agent.startup;

import com.lpmoon.agent.CommonAgent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by liupeng10 on 17/6/28.
 */
public class StaticAgent {
    public static void premain(String agentOps, Instrumentation inst) throws InterruptedException, UnmodifiableClassException, ClassNotFoundException {
        CommonAgent.doAgent(agentOps, inst);
    }
}
