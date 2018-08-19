package com.lpmoon.agent.util;

public class StringUtils {
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }

        return str.trim().length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
