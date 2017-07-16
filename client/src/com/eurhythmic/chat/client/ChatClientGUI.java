package com.eurhythmic.chat.client;

import com.eurhythmic.chat.library.Messages;
import com.eurhythmic.network.SocketThread;
import com.eurhythmic.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * Created by eurhythmic on 23.06.2017.
 */
public class ChatClientGUI extends JFrame implements Thread.UncaughtExceptionHandler, SocketThreadListener {

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final String DEFAULT_PORT = "8189";
    private static final String TITLE = "Chat client";
    private static final String BTN_SEND_NAME = "Send";
    private static final String BTN_CONNECT_NAME = "Login";
    private static final String BTN_DISCONNECT_NAME = "Disconnect";
    private static final String CHK_ALWAYS_ON_TOP = "Always on top";
    private static final double SCREEN_RATIO = 0.5;
    private static final int WIDTH;
    private static final int HEIGHT;
    private static final int USER_LIST_WIDTH = 150;

    static {
        Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int) (SCREEN_SIZE.getWidth() * SCREEN_RATIO);
        HEIGHT = (int) (SCREEN_SIZE.getHeight() * SCREEN_RATIO);
    }

    final JTextField fieldIPAddr = new JTextField(DEFAULT_IP);
    final JTextField fieldPort = new JTextField(DEFAULT_PORT);
    final JCheckBox chkAlwaysOnTop = new JCheckBox(CHK_ALWAYS_ON_TOP);
    final JTextField fieldLogin = new JTextField("eurhythmic");
    final JPasswordField fieldPass = new JPasswordField("canadian");
    final JButton btnLogin = new JButton(BTN_CONNECT_NAME);
    final JButton btnDisconnect = new JButton(BTN_DISCONNECT_NAME);
    final JTextField fieldInput = new JTextField();
    final JButton btnSend = new JButton(BTN_SEND_NAME);
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final JPanel upperPanel = new JPanel(new GridLayout(2, 3));
    private final JTextArea log = new JTextArea();
    private final JList<String> userList = new JList<>();
    private final JPanel bottomPanel = new JPanel(new BorderLayout());
    private final ClientGUIListener clientGUIListener = new ClientGUIListener(this);
    private SocketThread socketThread;

    private ChatClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);
        setLocationRelativeTo(null); // центр экрана

        configureUpperPanel();
        configureLog();
        configureUserList();
        configureBottomPanel();
        configureListeners();

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClientGUI();
            }
        });
    }

    private void configureUpperPanel() {
        upperPanel.add(fieldIPAddr);
        upperPanel.add(fieldPort);
        upperPanel.add(chkAlwaysOnTop);
        upperPanel.add(fieldLogin);
        upperPanel.add(fieldPass);
        upperPanel.add(btnLogin);
        add(upperPanel, BorderLayout.NORTH);
    }

    private void configureLog() {
        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);
    }

    private void configureUserList() {
        JScrollPane scrollUsers = new JScrollPane(userList);
        scrollUsers.setPreferredSize(new Dimension(USER_LIST_WIDTH, 0));
        add(scrollUsers, BorderLayout.EAST);
    }

    private void configureBottomPanel() {
        bottomPanel.add(btnDisconnect, BorderLayout.WEST);
        bottomPanel.add(fieldInput, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);
        bottomPanel.setVisible(false);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void configureListeners() {
        btnSend.addActionListener(clientGUIListener);
        btnDisconnect.addActionListener(clientGUIListener);
        btnLogin.addActionListener(clientGUIListener);
        chkAlwaysOnTop.addActionListener(clientGUIListener);

        fieldLogin.addKeyListener(clientGUIListener);
        fieldPass.addKeyListener(clientGUIListener);
        fieldIPAddr.addKeyListener(clientGUIListener);
        fieldPort.addKeyListener(clientGUIListener);
        fieldInput.addKeyListener(clientGUIListener);
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

    // commands
    public void connect() {
        try {
            Socket socket = new Socket(fieldIPAddr.getText(), Integer.parseInt(fieldPort.getText()));
            socketThread = new SocketThread(this, "SocketThread", socket);
        } catch (IOException e) {
            e.printStackTrace();
            log.append("Exception :" + e.getMessage() + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        }
    }

    public void disconnect() {
        socketThread.close();
    }

    void sendMsg() {
        String msg = fieldInput.getText();
        if (msg.equals("")) return;
        fieldInput.setText(null);
        socketThread.sendMsg(msg);
    }

    public void onAlwaysOnTopChange() {
        setAlwaysOnTop(chkAlwaysOnTop.isSelected());
    }

    @Override
    public void onStartSocketThread(SocketThread socketThread) {
        System.out.println("Socket thread started");
    }

    private final String[] EMPTY = new String[0];

    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(dateFormat.format(System.currentTimeMillis()) + ": " + "Соединение разорвано.\n");
                log.setCaretPosition(log.getDocument().getLength());
                upperPanel.setVisible(true);
                bottomPanel.setVisible(false);
                userList.setListData(EMPTY);
                setTitle(TITLE);
            }
        });
    }

    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(dateFormat.format(System.currentTimeMillis()) + ": " + "Соединение установлено.\n");
                log.setCaretPosition(log.getDocument().getLength());
                upperPanel.setVisible(false);
                bottomPanel.setVisible(true);

                String login = fieldLogin.getText();
                String password = new String(fieldPass.getPassword());
                socketThread.sendMsg(Messages.getAuthRequest(login, password));
            }
        });
    }

    @Override
    public void onReceiveStringSocketThread(SocketThread socketThread, Socket socket, String value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                handleMessage(value);
            }
        });
    }

    private void handleMessage(String msg) {
        String[] tokens = msg.split(Messages.DELIMITER);
        String type = tokens[0];
        switch (type) {
            case Messages.AUTH_ACCEPT:
                setTitle(TITLE + " (" + tokens[1] + ")");
                break;
            case Messages.AUTH_ERROR:
                log.append("Неверное имя или пароль\n");
                log.setCaretPosition(log.getDocument().getLength());
                break;
            case Messages.BROADCAST:
                log.append(dateFormat.format(Long.parseLong(tokens[1])) + " - " + tokens[2] + ": " + tokens[3] + "\n");
                log.setCaretPosition(log.getDocument().getLength());
                break;
            case Messages.USERS_LIST:
                String allUsersMsg = msg.substring(Messages.USERS_LIST.length() + Messages.DELIMITER.length());
                String[] users = allUsersMsg.split(Messages.DELIMITER);
                Arrays.sort(users);
                userList.setListData(users);
                break;
            case Messages.RECONNECT:
                log.append("Переподкючен с другого клиента\n");
                log.setCaretPosition(log.getDocument().getLength());
                break;
            case Messages.MSG_FORMAT_ERROR:
                log.append(dateFormat.format(System.currentTimeMillis()) + ": " + "Неверный формат сообщения: '" + msg + "'\n");
                log.setCaretPosition(log.getDocument().getLength());
                break;
            default:
                throw new RuntimeException("Unknown message type: " + type);
        }
    }

    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                e.printStackTrace();
            }
        });
    }
}

