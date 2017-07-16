package com.eurhythmic.chat.server.core;

/**
 * Created by eurhythmic on 27.06.2017.
 */
public interface SecurityManager {
    void init();
    String getNick(String login, String password);
    void dispose();
}
