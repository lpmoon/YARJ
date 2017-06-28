package com.lpmoon.reporter;

/**
 * Created by zblacker on 2017/6/29.
 */
public abstract class AbstractSummary implements Summary {

    protected boolean started;
    protected volatile boolean stopped;

    @Override
    public void start() {
        if (!started) {
            synchronized (AbstractSummary.class) {
                if (!started) {
                    started = true;
                    doInnerStart();
                }
            }
        }

        System.out.println("Summary " + getName() + " started ......");
    }

    abstract void doInnerStart();

    abstract void doInnerStop();

    @Override
    public void stop() {
        if (!stopped) {
            synchronized (AbstractSummary.class) {
                if (!stopped) {
                    stopped = true;
                    doInnerStop();
                }
            }
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }
}
