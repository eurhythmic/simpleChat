package com.eurhythmic.network;

import java.net.Socket;

/**
 * Created by eurhythmic on 30.06.2017.
 */
public interface SocketThreadListener {

    void onStartSocketThread(SocketThread socketThread);
    void onStopSocketThread(SocketThread socketThread);

    void onReadySocketThread(SocketThread socketThread, Socket socket);
    void onReceiveStringSocketThread(SocketThread socketThread, Socket socket, String value);

    void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e);
}
