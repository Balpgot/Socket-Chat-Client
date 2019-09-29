package ru.tsindrenko;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageReceiver extends Thread {
    private BufferedReader in; // поток чтения из сокета
    private boolean isActive;

    MessageReceiver(BufferedReader in){
        this.in = in;
        this.isActive = true;
        start();
    }

    @Override
    public void run() {
        String str;
        try {
            while (true) {
                str = in.readLine(); // ждем сообщения с сервера
                if (str.equals("BREAK_CONNECTION") || !isActive) {
                    System.out.println("Соединение завершено");
                    break; // выходим из цикла если пришло "stop"
                }
                System.out.println(str);
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
