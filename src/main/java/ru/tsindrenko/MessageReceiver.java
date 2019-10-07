package ru.tsindrenko;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageReceiver extends Thread {
    private BufferedReader in; // поток чтения из сокета
    private boolean isActive;
    private GUI gui;

    MessageReceiver(BufferedReader in, GUI gui){
        this.in = in;
        this.isActive = true;
        this.gui = gui;
        start();
    }

    @Override
    public void run() {
        String str;
        try {
            while (true) {
                str = in.readLine(); // ждем сообщения с сервера
                if(str.startsWith("CHATROOMS:")){
                    gui.fillChatroomList(str);
                }
                if (str.equals("BREAK_CONNECTION") || !isActive) {
                    System.out.println("Соединение завершено");
                    break; // выходим из цикла если пришло "stop"
                }
                //System.out.println(str);
                gui.getChatWindow().append(str + "\n");
            }
        } catch (IOException e) {

        }
        stopReceiver();
    }

    public void stopReceiver(){
        this.isActive = false;
        try {
            in.close();
        }
        catch (IOException ex){
            System.out.println(ex.getMessage());
        }
    }
}
