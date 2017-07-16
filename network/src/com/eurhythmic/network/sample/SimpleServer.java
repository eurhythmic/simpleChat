package com.eurhythmic.network.sample;

import com.eurhythmic.network.ServerSocketThread;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by eurhythmic on 30.06.2017.
 */
public class SimpleServer {

    public static void main(String[] args) {
        try(
                ServerSocket serverSocket = new ServerSocket(8189);
                Socket socket = serverSocket.accept();
                ) {

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (true){
                out.writeUTF("echo " + in.readUTF());
            }

        } catch (IOException e){
                    e.printStackTrace();
        }
    }

}
