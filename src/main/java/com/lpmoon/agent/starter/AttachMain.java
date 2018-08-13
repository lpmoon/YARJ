package com.lpmoon.agent.starter;

import com.lpmoon.agent.util.JvmUtil;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

/**
 * Created by zblacker on 2017/5/10.
 */
public class AttachMain extends Thread {

    public void startAgent(String jar, String pid, String options) throws Exception {
        attach(jar, pid, options);
    }

    private void attach(String jar, String pid, String options) throws Exception {
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            if (vm == null) {
                throw new Exception("pid " + pid + "not exist");
            }

            vm.loadAgent(jar, options);
            vm.detach();

        } catch (AttachNotSupportedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerStopAgentHook(final String jar, final String pid) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run()
            {
                try {
                    attach(jar, pid, "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new Exception("you must enter a pid");
        }

        String pid = args[0];
        try {
            Long.parseLong(pid);
        } catch (Exception e) {
            throw new Exception("pid must be a number");
        }

        String options = "";
        if (args.length > 1) {
            options = args[1];
        }

        String jar = JvmUtil.getCurrentExecuteJarPath();
        System.out.println("current jar path is " + jar);
        AttachMain attachMain = new AttachMain();
        System.out.println("start attach pid " + pid);
        attachMain.startAgent(jar, pid, options);
        attachMain.registerStopAgentHook(jar, pid);
        System.out.println("end attach pid " + pid);

        while (true) {
            Thread.sleep(5000);
        }
    }
}


