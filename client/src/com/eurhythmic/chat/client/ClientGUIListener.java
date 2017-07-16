package com.eurhythmic.chat.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by eurhythmic on 23.06.2017.
 */
 class ClientGUIListener implements ActionListener, KeyListener {

    private ChatClientGUI chatClientGUI;
    private Object actionSource;
    private int keyCode;

    ClientGUIListener(ChatClientGUI chatClientGUI) {
        this.chatClientGUI = chatClientGUI;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        actionSource = e.getSource();

        if (actionSource.equals(chatClientGUI.btnSend)) {
            chatClientGUI.sendMsg();
        } else if (actionSource.equals(chatClientGUI.btnLogin) ||
                actionSource.equals(chatClientGUI.fieldIPAddr) ||
                        actionSource.equals(chatClientGUI.fieldPort) ||
                        actionSource.equals(chatClientGUI.fieldLogin) ||
                        actionSource.equals(chatClientGUI.fieldPass)
        ) {
            chatClientGUI.connect();
        } else if (actionSource.equals(chatClientGUI.btnDisconnect)) {
            chatClientGUI.disconnect();
        } else if (actionSource.equals(chatClientGUI.chkAlwaysOnTop)) {
            chatClientGUI.onAlwaysOnTopChange();
        } else {
            throw new RuntimeException("Unknown command: " + e.getActionCommand());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER){
            actionSource = e.getSource();

            if (    actionSource.equals(chatClientGUI.fieldIPAddr) ||
                    actionSource.equals(chatClientGUI.fieldPort) ||
                    actionSource.equals(chatClientGUI.fieldLogin) ||
                    actionSource.equals(chatClientGUI.fieldPass)){
                chatClientGUI.connect();
            } else if (actionSource.equals(chatClientGUI.fieldInput)){
                chatClientGUI.sendMsg();
            }
        }
    }




}
