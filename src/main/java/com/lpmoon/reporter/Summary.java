package com.lpmoon.reporter;

/**
 * Created by zblacker on 2017/6/29.
 */
public interface Summary {

    /**
     * 获取统计器的名称
     * @return
     */
    String getName();

    /**
     * 启动
     */
    void start();

    /**
     * 统计数据
     * @param clazz 类名
     * @param method 方法名
     * @param cost 耗时
     */
    void report(String clazz, String method, long cost);

    /**
     * 停止
     */
    void stop();

    /**
     * 是否已经启动
     * @return
     */
    boolean isStarted();

    /**
     * 是否已经关闭
     * @return
     */
    boolean isStopped();
}
