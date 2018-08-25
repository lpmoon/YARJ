package com.lpmoon.agent.util;

import java.util.ArrayList;
import java.util.List;

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

    public static List<String> splitByConstantLength(String src, int length) {
        List<String> splits = new ArrayList<>();
        for (int i = 0; i < src.length(); i+=length) {
            splits.add(src.substring(i, i + length >= src.length() ? src.length() : i + length));
        }

        return splits;
    }
}
