package com.ezen.network.chat.client;

import com.ezen.network.chat.server.ChatServer;
import com.ezen.network.chat.server.SocketClient;
import com.ezen.network.chat.server.ChatServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TCP/IP 기반의 클라이언트 구현
 */
public class ChatClient {
    private String serverIp;
    private int port;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String nickName;
    private ChatFrame chatFrame;
    private List<String> nickNameList;

    public ChatClient(ChatFrame chatFrame) {
        this("localhost", 2024, chatFrame);
    }

    public ChatClient(String serverIp, int port, ChatFrame chatFrame) {
        this.serverIp = serverIp;
        this.port = port;
        this.chatFrame = chatFrame;
        this.nickNameList = new ArrayList<>();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * 채팅서버 연결
     * @throws IOException
     */
    public void connect() throws IOException {
        socket = new Socket(serverIp, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    /**
     * 채팅서버에 메시지 전송
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
        out.flush();
    }

    /**
     * 채팅서버로부터 메시지 수신
     * @throws IOException
     */
    public void receive() throws IOException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String jsonMessage = in.readUTF();
                        JSONObject jsonObject = new JSONObject(jsonMessage);
                        String command = jsonObject.getString("command");
                        String nickName = jsonObject.getString("nickName");
                        switch (command) {
                            case "CONNECT":
                                chatFrame.appendMessage("----------------------------");
                                chatFrame.appendMessage("♡♡♡[" + nickName + "]님이 입장하였습니다.♡♡♡");
                                chatFrame.appendMessage("----------------------------");
                                break;
                            case "CONNECTION_LIST":
                                JSONArray jsonArray = jsonObject.getJSONArray("list");  // SocketClient 참고
                                chatFrame.NickNameList(jsonArray);
                                chatFrame.ChoiceNickNameList(jsonArray);
                                break;
                            case "MULTI_CHAT":
                                String chatMessage = jsonObject.getString("message");
                                chatFrame.appendMessage("[" + nickName + "]" + chatMessage);
                                break;
                            case "DM":
                                String dmMessage = jsonObject.getString("message");
                                String senderName = jsonObject.getString("nickName");
                                String receiverName = jsonObject.getString("receiver");
                                chatFrame.appendMessage("["+senderName+"] →→ ["+receiverName +"] :" + dmMessage);
                                break;
                            case "DIS_CONNECT":
                                chatFrame.appendMessage("----------------------------");
                                chatFrame.appendMessage("♥♥♥[" + nickName + "]님이 퇴장하였습니다.♥♥♥");
                                chatFrame.appendMessage("----------------------------");
                                chatFrame.nickNameList.remove(nickName);
                                chatFrame.nickNameChoice.remove(nickName);
                                break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("[클라이언트] 채팅서버와 연결 해제");
                }
            }
        };
        thread.start();
    }
    /**
     * 채팅서버 연결 종료
     */
    public void unConnect() throws IOException {
        socket.close();
    }
}
