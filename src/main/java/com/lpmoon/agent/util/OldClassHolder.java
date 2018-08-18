package com.lpmoon.agent.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zblacker on 2017/5/12.
 */
public class OldClassHolder {
    public Map<String, byte[]> oldClasses = new ConcurrentHashMap();

    public void storeClass(String className, byte[] bytecode) {
        oldClasses.put(className, bytecode);
    }

    public byte[] getLastRecordClass(String className) {
        return oldClasses.get(className);
    }
}
