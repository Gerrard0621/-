package com.ezen.network.chat.client;

public class EzenTalk {

    public static void main(String[] args) {
        ChatFrame chatFrame = new ChatFrame("::: EzenTalk :::");
        chatFrame.initComponents();
        chatFrame.addEventRegister();
        chatFrame.setSize(400, 500);
        chatFrame.setVisible(true);

    }
}
