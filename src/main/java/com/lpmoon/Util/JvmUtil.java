package com.lpmoon.Util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by zblacker on 2017/5/11.
 */
public class JvmUtil {
    public static String getCurrentExecuteJarPath() {
        URL url = JvmUtil.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = null;
        try {
            filePath = URLDecoder.decode(url.getPath(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        File file = new File(filePath);
        filePath = file.getAbsolutePath();
        return filePath;
    }
}
