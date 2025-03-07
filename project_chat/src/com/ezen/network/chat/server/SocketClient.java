package com.ezen.network.chat.server;

import com.ezen.network.chat.client.ChatFrame;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 채팅서버에 연결한 클라이언트와 소켓을 이용한 1:1 메시지 송수신
 * 채팅 서버 관점에서 SocketClient 는 접속한 클라이언트를 의미한다.
 */
public class SocketClient {

    private ChatServer chatServer;
    ChatFrame chatFrame;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean stop;

    private String clientIp; // 접속 클라이언트 아이피
    private String nickName; // 접속 클라이언트 대화명

    public SocketClient(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        clientIp = isa.getAddress().getHostAddress();
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            receiveNsendMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getNickName() {
        return nickName;
    }

    /**
     * 채팅 클라이언트가 전송한 다양한 메시지 수신 및 전송
     */
    private void receiveNsendMessage() {
        ExecutorService threadPool = chatServer.getThreadPool();
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!stop) {
                        String jsonMessage = in.readUTF();
                        System.out.println("[채팅서버] ChatClient -> ChatServer : " + jsonMessage);
                        JSONObject jsonObject = new JSONObject(jsonMessage);
                        String command = jsonObject.getString("command");
                        nickName = jsonObject.getString("nickName");
                        switch (command) {
                            case "CONNECT":
                                chatServer.addSocketClient(SocketClient.this);
                                List<String> clients = chatServer.getSocketClients();
                                JSONArray jsonArray = new JSONArray();
                                for (Object nickName : clients) {
                                    jsonArray.put(nickName);
                                }
                                jsonObject.put("command","CONNECTION_LIST");
                                jsonObject.put("nickName", "SERVER");
                                jsonObject.put("list", jsonArray);
                                chatServer.sendAllMessage(jsonMessage);
                                chatServer.sendAllMessage(jsonObject.toString());
                                break;
                            case "MULTI_CHAT":
                                chatServer.sendAllMessage(jsonMessage);
                                break;
                            case "DM":
                                String receiverName = jsonObject.getString("receiver");
                                SocketClient receiverClient = chatServer.findByNickName(receiverName);
                                SocketClient myClient = chatServer.findByNickName(nickName);
                                if(receiverClient != null) {
                                    receiverClient.sendMessage(jsonMessage);
                                    myClient.sendMessage(jsonMessage);
                                }
                                break;
                            case "DIS_CONNECT":
                                chatServer.sendAllMessage(jsonMessage);
                                chatServer.removeSocketClient(SocketClient.this);
                                return;
                        }
                    }
                } catch (IOException e) {

                } finally {
                    close();
                }
            }
        });
    }
    /**
     * 현재 연결한 클라이언트에게 메시지 전송 (1:1)
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
        }

    }
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
        }
    }

}
