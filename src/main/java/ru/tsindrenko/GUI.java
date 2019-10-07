package ru.tsindrenko;

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

    GUI(){
        setContentPane(rootPanel);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ButtonEventListener buttonEventListener = new ButtonEventListener();
        sendButton.addActionListener(buttonEventListener);
        sendFileButton.addActionListener(buttonEventListener);
    }

    class ButtonEventListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            if(e.getSource().equals(sendButton) && !inputMessageField.getText().isEmpty()){
                Sender.sendMessage(inputMessageField.getText());
                inputMessageField.setText("");
            }
            if(e.getSource().equals(sendFileButton)){
                File file = new File("C://time.txt");
                Sender.sendFile(file);
            }
        }
    }

    public void getChatrooms(){
        Sender.sendMessage("CHATROOMS");
    }

    public void fillChatroomList(String list){
        String chatroomStringList = list.substring(list.indexOf("[")+1,list.indexOf("]"));
        String [] chatrooms = chatroomStringList.split(",");
        chatroomList.setListData(chatrooms);
    }



    public JTextArea getChatWindow(){
        return chatWindow;
    }

}
