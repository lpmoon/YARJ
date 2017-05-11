package com.lpmoon.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by zblacker on 2017/5/12.
 */
public class RestoreFileTransformer implements ClassFileTransformer {
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("before transform " + className + " bytecode length is " + classfileBuffer.length);
        byte[] bytecode = OldClassHolder.getLastRecordClass(className);
        System.out.println("after transform " + className + " bytecode length is " + bytecode.length);
        return bytecode;
    }
}
