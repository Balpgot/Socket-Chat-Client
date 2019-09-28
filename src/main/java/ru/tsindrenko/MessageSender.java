package ru.tsindrenko;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class MessageSender extends Thread {
    private BufferedReader reader; // ридер читающий с консоли
    private BufferedWriter out; // поток записи в сокет
    private boolean isActive;

    MessageSender(BufferedWriter out){
        this.out = out;
        this.isActive = true;
        reader = new BufferedReader(new InputStreamReader(System.in));
        start();
    }

    @Override
    public void run() {
        while (true) {
            String userWord;
            try {
                userWord = reader.readLine(); // сообщения с консоли
                if (userWord.equals("stop") || !isActive) {
                    out.write("stop" + "\n");
                    break; // выходим из цикла если пришло "stop"
                } else {
                    out.write(userWord + "\n"); // отправляем на сервер
                }
                out.flush(); // чистим
            } catch (IOException e) {

            }
            finally {
                try {
                    out.write("stop");
                    out.flush();
                    System.out.println("Отправлено");
                }
                catch (IOException ex){}
            }
        }
        stopSender();

    }

    //Завершает работу потоков
    public void stopSender(){
        try {
            out.close();
            reader.close();
            this.isActive = false;
        }
        catch (IOException ex){
            System.out.println(ex.getMessage());
        }
    }

}
