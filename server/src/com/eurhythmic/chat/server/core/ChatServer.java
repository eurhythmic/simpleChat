package com.eurhythmic.chat.server.core;

import com.eurhythmic.chat.library.Messages;
import com.eurhythmic.network.ServerSocketThread;
import com.eurhythmic.network.ServerSocketThreadListener;
import com.eurhythmic.network.SocketThread;
import com.eurhythmic.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Created by eurhythmic on 23.06.2017.
 */
public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private static final short TIMEOUT = 2_000;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private final ChatServerListener eventListener;
    private final SecurityManager securityManager;
    private final Vector<SocketThread> clients = new Vector<>();
    private ServerSocketThread serverSocketThread;

    public ChatServer(ChatServerListener eventListener, SecurityManager securityManager) {
        this.eventListener = eventListener;
        this.securityManager = securityManager;
    }

    public void startListening(int port) {
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            putLog("Сервер уже запущен.");
            return;
        }
        serverSocketThread = new ServerSocketThread(this, "ServerSocketThread", port, TIMEOUT);
        securityManager.init();
    }

    public void dropAllClients() {
        putLog("DropAllClients");
    }

    public void stopListening() {

        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            putLog("Сервер не запущен.");
            return;
        }
        serverSocketThread.interrupt();
        securityManager.dispose();
    }

    private synchronized void putLog(String msg) {
        String msgLog = dateFormat.format(System.currentTimeMillis()) + Thread.currentThread().getName() + ": " + msg;
        eventListener.onChatServerLog(this, msgLog);
    }

    // server socket thread methods
    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("started...");
    }

    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("stopped.");
    }

    @Override
    public void onReadyServerSocketThread(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("Server socket is ready...");
    }

    @Override
    public void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket) {
        putLog("Client connected: " + socket);
        String threadName = "Socket thread: " + socket.getInetAddress() + ":" + socket.getPort();
        new ChatSocketThread(this, threadName, socket);
    }

    @Override
    public void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("accept() timeout");
    }

    @Override
    public void onServerSocketThreadException(ServerSocketThread thread, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }


    // socket thread methods
    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread) {
        putLog("started...");
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        clients.remove(socketThread);
        putLog("stopped.");

        ChatSocketThread client = (ChatSocketThread) socketThread;

        if (client.isAuthorized() && !client.isReconnected()) {
            sendToAllAuthorizedClients(Messages.getBroadcast("Server", client.getNick() + " disconnected."));
            sendToAllAuthorizedClients(Messages.getUsersList(getAllUsers()));
        }
    }

    @Override
    public synchronized void onReadySocketThread(SocketThread socketThread, Socket socket) {
        putLog("Socket is ready...");
        clients.add(socketThread);
    }

    @Override
    public synchronized void onReceiveStringSocketThread(SocketThread socketThread, Socket socket, String value) {
        ChatSocketThread client = (ChatSocketThread) socketThread;
        if (client.isAuthorized()) {
            handleAuthorizedClient(client, value);
        } else {
            handleNotAuthorizedClient(client, value);
        }
    }

    private void handleAuthorizedClient(ChatSocketThread client, String msg) {
        sendToAllAuthorizedClients(Messages.getBroadcast(client.getNick(), msg));
    }

    private void handleNotAuthorizedClient(ChatSocketThread newClient, String msg) {
        String[] tokens = msg.split(Messages.DELIMITER);
        if (tokens.length != 3 || !tokens[0].equals(Messages.AUTH_REQUEST)) {
            newClient.messageFormatError(msg);
            return;
        }

        String login = tokens[1];
        String password = tokens[2];
        String nickname = securityManager.getNick(login, password);
        if (nickname == null) {
            newClient.authError();
            return;
        }

        ChatSocketThread client = getClientByNick(nickname);
        newClient.authorizeAccept(nickname);
        if (client == null) {
            sendToAllAuthorizedClients(Messages.getBroadcast("Server", newClient.getNick() + " connected."));
            sendToAllAuthorizedClients(Messages.getUsersList(getAllUsers()));
        } else {
            if(!clients.remove(client)) throw new RuntimeException("Не удалосб удалить клиента.");
            client.reconnect();
            newClient.sendMsg(Messages.getUsersList(getAllUsers()));
        }
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if (client.isAuthorized()) client.sendMsg(msg);
        }
    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }

    private String getAllUsers() {

        StringBuilder sb = new StringBuilder();
        final int cnt = clients.size() - 1;

        for (int i = 0; i <= cnt; i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            sb.append(client.getNick());
            if (i != cnt) sb.append(Messages.DELIMITER);
        }
        return sb.toString();
    }

    public ChatSocketThread getClientByNick(String nickname) {
        final int cnt = clients.size();
        for (int i = 0; i < cnt; i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getNick().equals(nickname)) return client;
        }
        return null;
    }

}


