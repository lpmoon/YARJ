package com.lpmoon.agent.command;


public interface Command {
    String name();
    void init() throws IllegalArgumentException;
    void handle();
    void stop();
}
