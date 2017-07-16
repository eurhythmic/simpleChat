package com.eurhythmic.chat.server.core;

import com.eurhythmic.chat.library.Messages;
import com.eurhythmic.network.SocketThread;
import com.eurhythmic.network.SocketThreadListener;

import java.net.Socket;

/**
 * Created by eurhythmic on 04.07.2017.
 */
public class ChatSocketThread extends SocketThread {

    private boolean isAuthorized;
    private boolean isReconnected;
    private String nick;

    public ChatSocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(eventListener, name, socket);
    }

    boolean isAuthorized() {
        return isAuthorized;
    }

    boolean isReconnected(){
        return isReconnected;
    }

    void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    void authorizeAccept(String nick) {
        this.isAuthorized = true;
        this.nick = nick;
        sendMsg(Messages.getAuthAccept(nick));
    }

    void authError() {
        sendMsg(Messages.getAuthError());
        close();
    }

    void reconnect(){
        isReconnected = true;
        sendMsg(Messages.getReconnect());
        close();
    }

    void messageFormatError(String msg) {
        sendMsg(Messages.getMsgFormatError(msg));
        close();
    }

    String getNick() {
        return nick;
    }
}
