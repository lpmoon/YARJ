package com.lpmoon.agent.transformer;

import com.lpmoon.agent.util.OldClassHolder;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zblacker on 2017/5/9.
 */
public class ParameterRecordFileTransformer implements ClassFileTransformer {

    private OldClassHolder oldClassHolder;

    private String method;

    public ParameterRecordFileTransformer(OldClassHolder oldClassHolder) {
        this.oldClassHolder = oldClassHolder;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        oldClassHolder.storeClass(className, classfileBuffer);

        System.out.println("before transform " + className + " bytecode length is " + byteCode.length);
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass cc = cp.get(className);
            System.out.println(className);
            CtMethod[] methods = cc.getDeclaredMethods();
            for (CtMethod m : methods) {
                if (m != null && m.getName().equals(method)) {
                    MethodInfo methodInfo = m.getMethodInfo();
                    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                    LocalVariableAttribute attribute = (LocalVariableAttribute)codeAttribute.getAttribute(LocalVariableAttribute.tag);
                    String[] localVariables = new String[attribute.tableLength()];
                    for (int i = 0; i < attribute.tableLength(); i++) {
                        localVariables[attribute.index(i)] = attribute.variableName(i);
                    }

                    CtClass mapClass = cp.get(Map.class.getName());
                    m.addLocalVariable("_parameters", mapClass);
                    String putParameterCode = "{_parameters = new java.util.HashMap();";
                    int pos = Modifier.isStatic(m.getModifiers()) ? 0 : 1;
                    for (int i = 0; i < m.getParameterTypes().length; i++) {
                        String parameterName = localVariables[i + pos];
                        putParameterCode += "_parameters.put(\"" + parameterName + "\"" + "," + parameterName + ");";
                    }
                    putParameterCode += "((com.lpmoon.agent.command.ParameterCommand)com.lpmoon.agent.command.CommandManager.instance.getCommand(\"parameter\")).putParameter(_parameters);}";

                    System.out.println(putParameterCode);

                    m.insertBefore(putParameterCode);
                    byteCode = cc.toBytecode();
                    cc.detach();
                }
            }
        } catch (Exception ex) {
        }

        System.out.println("after transform " + className + " bytecode length is " + byteCode.length);
        return byteCode;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
