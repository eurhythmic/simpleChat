package com.eurhythmic.chat.server.gui;

import com.eurhythmic.chat.server.core.ChatServer;
import com.eurhythmic.chat.server.core.ChatServerListener;
import com.eurhythmic.chat.server.core.SQLSecurityManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by eurhythmic on 23.06.2017.
 */
public class ChatServerGUI extends JFrame implements ActionListener, ChatServerListener, Thread.UncaughtExceptionHandler {

    private static final double SCREEN_RATIO = 0.4;
    private static final int WIDTH;
    private static final int HEIGHT;
    private static final String TITLE = "Chat server";
    private static final String START_LISTENING = "Start listening";
    private static final String DROP_ALL_CLIENTS = "Drop all clients";
    private static final String STOP_LISTENING = "Stop listening";

    static {
        Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int) (SCREEN_SIZE.getWidth() * SCREEN_RATIO);
        HEIGHT = (int) (SCREEN_SIZE.getHeight() * SCREEN_RATIO);
    }

    private final JButton btnStartListening = new JButton(START_LISTENING);
    private final JButton btnStopListening = new JButton(STOP_LISTENING);
    private final JButton btnDropAllClients = new JButton(DROP_ALL_CLIENTS);
    private final ChatServer chatServer = new ChatServer(this, new SQLSecurityManager());
    private final JTextArea log = new JTextArea();

    private ChatServerGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);

        btnStartListening.addActionListener(this);
        btnDropAllClients.addActionListener(this);
        btnStopListening.addActionListener(this);

        JPanel upperPanel = new JPanel(new GridLayout(1, 3));
        upperPanel.add(btnStartListening);
        upperPanel.add(btnDropAllClients);
        upperPanel.add(btnStopListening);

        add(upperPanel, BorderLayout.NORTH);

        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatServerGUI();
            }
        });
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String msg;
        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        if (stackTraceElements.length == 0) {
            msg = "Пустой stackTraceElements";
        } else {
            msg = e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + stackTraceElements[0];
        }
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == btnStartListening) {
            chatServer.startListening(8189);
        } else if (src == btnDropAllClients) {
            chatServer.dropAllClients();
        } else if (src == btnStopListening) {
            chatServer.stopListening();
        } else {
            throw new RuntimeException("Unknown src = " + src);
        }
    }

    @Override
    public void onChatServerLog(ChatServer chatServer, String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength()); // чтобы скролл корректно работал (автоскролл криво работает периодически)
            }
        });
    }
}
