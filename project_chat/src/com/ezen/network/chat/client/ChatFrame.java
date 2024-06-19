package com.ezen.network.chat.client;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Frame을 이용한 채팅 메인 화면
 */
public class ChatFrame extends Frame {
    TextField nickNameTF, messageTF;
    Button loginButton, sendButton;
    TextArea messageTA;
    List nickNameList;
    Choice nickNameChoice;
    ChatClient chatClient;

    public ChatFrame(String title) {
        super(title);
        nickNameTF = new TextField("대화명을 입력하세요.");
        loginButton = new Button(" 로그인 ");
        messageTA = new TextArea();
        messageTA.setEditable(false);
        nickNameList = new List();
        nickNameList.setPreferredSize(new Dimension(100, 410));
        messageTF = new TextField();
        sendButton = new Button(" 전  송 ");
        nickNameChoice = new Choice();
        nickNameChoice.add("-전체채팅-");
    }

    public void initComponents() {

        Panel topPanel = new Panel();
        topPanel.setLayout(new BorderLayout(5, 5));
        topPanel.add(nickNameTF, BorderLayout.CENTER);
        topPanel.add(loginButton, BorderLayout.EAST);

        Panel bottomPanel = new Panel();
        bottomPanel.setLayout(new BorderLayout(5, 5));
        bottomPanel.add(nickNameChoice, BorderLayout.WEST);
        bottomPanel.add(messageTF, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(messageTA, BorderLayout.CENTER);
        add(nickNameList, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 로그인 처리
     */
    private void login() {
        chatClient = new ChatClient("localhost", 2024, this);
        try {
            chatClient.connect();
            String inputNickName = nickNameTF.getText();
            chatClient.setNickName(inputNickName);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "CONNECT");
            jsonObject.put("nickName", inputNickName);

            chatClient.sendMessage(jsonObject.toString());
            nickNameTF.setEnabled(false);
            loginButton.setEnabled(false);
            chatClient.receive();
        } catch (IOException e) {
            messageTA.append("[채팅서버]에 연결할 수 없습니다.\n");
            messageTA.append("네트워크 상태를 확인하여 주세요.\n");
            messageTA.append(e.getMessage() + "\n");
        }
    }

    /**
     * 메시지 창에 메시지 출력
     */
    public void appendMessage(String message) {
        messageTA.append(message + "\n");
    }

    /**
     * 채팅 메시지 전송
     */
    private void sendMessage() {
        String inputMessage = messageTF.getText();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", "MULTI_CHAT");
        jsonObject.put("nickName", chatClient.getNickName());
        jsonObject.put("message", inputMessage);
        try {
            if (nickNameChoice.getSelectedIndex() != 0) {
                String receiverNickName = nickNameChoice.getSelectedItem();
                jsonObject.put("command", "DM");
                jsonObject.put("receiver", receiverNickName);
            }
            chatClient.sendMessage(jsonObject.toString());

        } catch (Exception e) {  }
        messageTF.setText("");
    }

    /**
     * 사용자리스트 출력
     */
    public void NickNameList(JSONArray jsonArray) {
        nickNameList.removeAll();
        for (Object object : jsonArray) {
            String name = (String) object;
            nickNameList.add(name);
        }
    }

    /**
     * 사용자 선택 출력
     */
    public void ChoiceNickNameList(JSONArray jsonArray) {
        nickNameChoice.removeAll();
        nickNameChoice.add("-전체채팅-");
        for (Object object : jsonArray) {
            String name = (String) object;
            nickNameChoice.add(name);
        }
    }

    /**
     * 서버 종료
     */
    private void exit() {
        if (chatClient != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "DIS_CONNECT");
            jsonObject.put("nickName", chatClient.getNickName());
            try {
                chatClient.sendMessage(jsonObject.toString());
                chatClient.unConnect();
            } catch (IOException e) {
            }
        }
        int confirm = JOptionPane.showConfirmDialog(null,"프로그램을 종료합니다","종료확인",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
        if(confirm == JOptionPane.OK_OPTION){
            dispose();
            setVisible(false);
            System.exit(0);
        }
    }

    public void addEventRegister() {
//        종료 이벤트 처리
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

//       사용자 선택 이벤트 처리
        nickNameTF.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                nickNameTF.setText("");
            }
        });

//        로그인 이벤트 처리
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        nickNameTF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

//        메시지 입력 이벤트 처리
        messageTF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }
}
