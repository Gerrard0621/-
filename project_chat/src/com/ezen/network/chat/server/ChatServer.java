package com.ezen.network.chat.server;

import com.ezen.network.chat.client.ChatClient;
import org.json.JSONArray;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP/IP 기반의 멀티 채팅 서버 구현
 * 멀티스레드 기반 - 클라이언트 다중 요청 처리
 * ThreadPool 적용
 */
public class ChatServer {

    private int port;
    private ServerSocket serverSocket;
//    스레드 풀 생성
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    boolean stop;

    private HashMap<String, SocketClient> socketClients;

    public ChatServer() {
       this(2024);
    }

    public ChatServer(int port) {
        this.port = port;
        socketClients = new HashMap<>();
    }

    public int getPort() {
        return port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public ExecutorService getThreadPool() { return threadPool; }

    /**
     * 채팅 서버 구동
     */
    public void start(){
        new Thread() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    System.out.println("########### [채팅서버("+port+")] 실행됨 ###########");
                    while (!stop) {
                        System.out.println("[채팅서버] 채팅 클라이언트 연결 요청을 기다림.");
                        Socket socket = serverSocket.accept();
                        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
                        String clientIp = isa.getAddress().getHostAddress();
                        System.out.println("[채팅서버] 채팅 클라이언트(" + clientIp + ") 연결을 해옴...");
                        SocketClient socketClient = new SocketClient(socket, ChatServer.this);
                    }
                } catch (IOException e) {
                    System.err.println("[서버] " + e.getMessage());
                }
            }
        }.start();
    }

    /**
     * 채팅 서버에 클라이언트(SocketClient) 연결 시 Map 에 클라이언트 추가
     * @param socketClient
     */
    public void addSocketClient(SocketClient socketClient){
        String key = socketClient.getNickName();
        socketClients.put(key, socketClient);
        System.out.println("▣ 클라이언트 입장 : " + key);
        System.out.println("▣ 현재 채팅서버에 연결된 클라이언트 수 : " + socketClients.size());
    }

    /**
     * 연결된 클라이언트 목록 반환
     */
    public List<String> getSocketClients() {
        List<String> list = new ArrayList<>();
        Collection<SocketClient> connectionList = socketClients.values();
        for (SocketClient socketClient : connectionList) {
            String nickname = socketClient.getNickName();
            list.add(nickname);
        }
        return list;
    }

    /**
     * 채팅 서버에 연결된 모든 클라이언트(SocketClient)들에게 메시지 전송
     * @param jsonMessage
     */
    public void sendAllMessage(String jsonMessage) throws IOException {
        Collection<SocketClient> connectionList = socketClients.values();
        for (SocketClient socketClient : connectionList) {
            socketClient.sendMessage(jsonMessage);
        }
    }

    /**
     * 채팅 서버에 연결된 모든 클라이언트(SocketClient) 검색
     */
    public SocketClient findByNickName(String nickName){
        return socketClients.get(nickName);
    }


    /**
     * 클라이언트(SocketClient) 연결 종료 시 Map 에서 클라이언트 제거
     * @param socketClient
     */
    public void removeSocketClient(SocketClient socketClient){
        String key = socketClient.getNickName();
        socketClients.remove(key);
        System.out.println("▣ 클라이언트  퇴장 : " + key);
        System.out.println("▣ 현재 채팅서버에 연결된 클라이언트 수 : " + socketClients.size());
    }

    /**
     * 채팅 서버 종료
     */
    public void stop(){
        try {
            serverSocket.close();
            for (SocketClient sc : socketClients.values()) {
                if(sc != null) { sc.close(); }
            }
            threadPool.shutdownNow();
            System.out.println("[채팅서버] 종료됨.");
        } catch (IOException e) { }
    }
}
