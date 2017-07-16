package com.eurhythmic.network;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by eurhythmic on 30.06.2017.
 */
public interface ServerSocketThreadListener {

    void onStartServerSocketThread(ServerSocketThread thread);
    void onStopServerSocketThread(ServerSocketThread thread);

    void onReadyServerSocketThread(ServerSocketThread thread, ServerSocket serverSocket);
    void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket);
    void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket);

    void onServerSocketThreadException(ServerSocketThread thread, Exception e);

}
