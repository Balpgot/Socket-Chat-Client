package ru.tsindrenko;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class GUI extends JFrame {
    private JPanel rootPanel;
    private JTextArea chatWindow;
    private JList chatroomList;
    private JButton sendButton;
    private JTextField inputMessageField;
    private JButton sendFileButton;
    private JScrollPane chatScrollPane;
    private JScrollPane chatroomScroll;
    private Gson gson;


    GUI(){
        setContentPane(rootPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ButtonEventListener buttonEventListener = new ButtonEventListener();
        sendButton.addActionListener(buttonEventListener);
        sendFileButton.addActionListener(buttonEventListener);
        gson = new Gson();
    }

    class ButtonEventListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            if(e.getSource().equals(sendButton) && !inputMessageField.getText().isEmpty()){
                TextMessage textMessage = new TextMessage(Sender.modifyText(inputMessageField.getText()),0,-1);
                Sender.sendMessage(gson.toJson(textMessage));
                inputMessageField.setText("");
            }
            if(e.getSource().equals(sendFileButton)){
                File file = new File("C://test.txt");
                Sender.sendFile(file);
            }
        }
    }

    public void getChatrooms(){
        Sender.sendMessage("CHATROOMS");
    }

    public void fillChatroomList(String list){
        String chatroomStringList = list.substring(list.indexOf("[")+1,list.indexOf("]"));
        String [] chatrooms = chatroomStringList.split(";");
        chatroomList.setListData(chatrooms);
    }



    public JTextArea getChatWindow(){
        return chatWindow;
    }

}
