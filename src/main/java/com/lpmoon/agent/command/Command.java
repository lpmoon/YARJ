package com.lpmoon.agent.command;


public interface Command {
    String name();
    String help();
    void init() throws IllegalArgumentException;
    void handle();
    void stop();
}
