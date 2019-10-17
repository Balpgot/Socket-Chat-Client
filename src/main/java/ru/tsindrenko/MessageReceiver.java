package ru.tsindrenko;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.List;
import java.util.Random;

public class MessageReceiver extends Thread {
    private final String disconnectClient = "BREAK_CONNECTION";
    private final String accepted = "SUCCESSFUL_OPERATION";
    private final String loginInfo = "LOGIN";
    private final String fileInfo = "FILE";
    private final String serviceInfo = "SERVICE";
    private final String messageInfo = "TEXT";
    private final String patchInfo = "PATCH_INFO";
    private final String requestInfo = "REQUEST_INFO";
    private final String userNotFound = "USER_NOT_FOUND";
    private final String wrongPassword = "WRONG_PASSWORD";
    private final String loginIsOccupied = "LOGIN_IS_OCCUPIED";
    private final String userIsLogged = "USER_IS_LOGGED";
    private final String chatroomInfo = "CHATROOM";
    private final String userInfo = "USER";

    private BufferedReader in; // поток чтения из сокета
    private boolean isActive;
    private GUI gui;
    private Gson gson = new Gson();


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
                System.out.println("Пришло сообщение: "  +str);
                messageHandler(str);
            }
        } catch (IOException e) {

        }
        stopReceiver();
    }

    private void messageHandler(String message){
        String header;
        JSONObject json;
        try {
            json = new JSONObject(message);
            header = json.get("type").toString();
        }
        catch (JSONException ex){
            header = chatroomInfo;
        }
        switch (header){
            case fileInfo:
                receiveFile(gson.fromJson(message,FileMessage.class));
                break;
            case messageInfo:
                showMessage(gson.fromJson(message,TextMessage.class));
                break;
            case serviceInfo:
                serviceHandler(gson.fromJson(message,ServiceMessage.class));
                break;
            case userInfo:
                Main.user = gson.fromJson(message,User.class);
                break;
            case chatroomInfo:
                gui.fillChatroomList(gson.fromJson(message,new TypeToken<List<ChatRoom>>() {}.getType()));
        }
    }

    private void showMessage(TextMessage message){
        gui.getChatWindow().append(message.getSender_nickname() +
                ": " + message.getBody() + "\n");
    }

    private void serviceHandler(ServiceMessage message){
        switch (message.getHeader()){
            case loginInfo:
                Main.loginForm.getLoginInformation(message.getStatus());
                break;
            default:
                System.out.println(message.getHeader() + " " + message.getStatus());
        }
    }

    public void receiveFile(FileMessage fileMessage){
        try {
            //получаем размер файла
            long size = fileMessage.getSize();
            System.out.println("Размер файла: " + size);
            //объявляем размер пакета
            byte [] bytes = new byte[8192];
            //устанавливаем файл на запись
            File file = new File(new StringBuffer().
                    append(Main.file_directory).
                    append(fileMessage.getFileType()).
                    append("//").
                    append(new Random().nextInt()).toString());
            file.createNewFile();
            if(file.exists()){
                System.out.println("Файл существует");
            }
            else
                System.out.println("Файл не существует");
            //запускаем поток записи в файл
            FileOutputStream fileWriter = new FileOutputStream(file);
            //объявляем поток откуда пойдут данные
            BufferedInputStream bis = new BufferedInputStream(Main.serverSocket.getInputStream());
            int i;
            //считываем данные пока они не закончатся
            while (size>0){
                i = bis.read(bytes);
                fileWriter.write(bytes,0,i);
                size-=i;
            }
            fileWriter.close();
        }
        catch (IOException ex) {
            System.out.println("receive file: " + ex.getMessage());
        }
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
