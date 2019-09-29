package ru.tsindrenko;

import java.io.*;

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
        String userWord;
        File file = new File("D://JavaProjects//ChatClient//src//main//resources//files//2.txt");
        if(file.exists()){
            System.out.println("Файл найден");
        }
        else
            System.out.println("Файл не найден");
        while (true) {
            try {
                userWord = reader.readLine(); // сообщения с консоли
                if(userWord.equals("FILE")){
                    //отправляем флаг файла
                    out.write("FILE\n");
                    out.flush();
                    synchronized (this) {
                        wait(3000);
                    }
                    //начинаем передачу файла
                    if(sendFile(file)){
                        System.out.println("Отправка успешна");
                    }
                }
                else{
                    if (userWord.equals("stop") || !isActive) {
                        out.write("stop\n");
                        break; // выходим из цикла если пришло "stop"
                    } else {
                        out.write(userWord + "\n"); // отправляем на сервер
                    }
                    out.flush(); // чистим
                }
            } catch (IOException e) {
            }
            catch (InterruptedException ex){}
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

    public boolean sendFile(File file){
        try {
            System.out.println("Sending: " + file.getName());
            //определяем размер пакета и открываем файл на чтение
            byte[] byteArray = new byte[8192];
            FileInputStream fis = new FileInputStream(file.getPath());
            synchronized (this) {
                wait(3000); // ждем, чтобы у сервера всё было готово
            }
            //отправляем серверу имя файла
            out.write(file.getName()+"\n");
            out.flush();
            System.out.println("Отправлено имя файла");
            //отправляем серверу размер файла
            long size = file.length();
            out.write(size+"\n");
            out.flush();
            System.out.println("Отправлен размер файла");
            //начинаем отправку данных
            synchronized (this) {
                wait(3000); // ждем, чтобы у сервера всё было готово
            }
            System.out.println("Начинаю оправлять");
            BufferedOutputStream bos = new BufferedOutputStream(Main.serverSocket.getOutputStream());
            while (size>0){
                int i = fis.read(byteArray);
                bos.write(byteArray, 0, i);
                size-= i;
            }
            bos.flush();
            fis.close();
            return true;
        }
        catch (IOException ex){
            System.out.println("sendFile: " + ex.getMessage());
        }
        catch (InterruptedException ex){
            System.out.println("sendFile: " + ex.getMessage());
        }
        return false;
    }

}
