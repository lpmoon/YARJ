package com.lpmoon.agent.util;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CommonResultBuilder {
    public static byte[] build(byte[] content) {

        byte[] result = new byte[content.length + 5];
        result[0] = 0x01;
        byte[] length = ByteUtil.toByteArray(content.length);
        result[1] = length[0];
        result[2] = length[1];
        result[3] = length[2];
        result[4] = length[3];
        System.arraycopy(content, 0, result, 5, content.length);

        return result;
    }
}
