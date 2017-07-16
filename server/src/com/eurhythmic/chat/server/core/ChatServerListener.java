package com.eurhythmic.chat.server.core;

/**
 * Created by eurhythmic on 23.06.2017.
 */
public interface ChatServerListener {
    void onChatServerLog(ChatServer chatServer, String msg);
}
