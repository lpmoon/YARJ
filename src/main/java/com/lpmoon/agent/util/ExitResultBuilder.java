package com.lpmoon.agent.util;

public class ExitResultBuilder {

    public static byte[] build() {

        byte[] exit = new byte[1];
        exit[0] = 0x02;

        return exit;
    }
}
