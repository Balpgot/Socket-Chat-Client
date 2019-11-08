package ru.tsindrenko;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MessageReceiver extends Thread {
    private final String disconnectClient = "BREAK_CONNECTION";
    private final String success = "SUCCESSFUL_OPERATION";
    private final String loginInfo = "LOGIN";
    private final String fileInfo = "FILE";
    private final String serviceInfo = "SERVICE";
    private final String messageInfo = "TEXT";
    private final String patchInfo = "PATCH_INFO";
    private final String requestInfo = "REQUEST";
    private final String responseInfo = "RESPONSE";
    private final String userNotFound = "USER_NOT_FOUND";
    private final String wrongPassword = "WRONG_PASSWORD";
    private final String loginIsOccupied = "LOGIN_IS_OCCUPIED";
    private final String userIsLogged = "USER_IS_LOGGED";
    private final String chatroomInfo = "CHATROOM";
    private final String userInfo = "USER";
    private final String getRequest = "GET";
    private final String updateRequest = "UPDATE";
    private final String deleteRequest = "DELETE";
    private final String createRequest = "CREATE";
    private final String blacklistInfo = "BLACKLIST";
    private final String participantsInfo = "PARTICIPANTS";
    private final String moderatorInfo = "MODERATOR";

    private BufferedReader in; // поток чтения из сокета
    private boolean isActive;
    private int currentChatID;
    private GUI gui;
    private Gson gson = new Gson();


    MessageReceiver(BufferedReader in, GUI gui){
        this.in = in;
        this.isActive = true;
        this.gui = gui;
        this.currentChatID = 1;
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
        System.out.println("Пришло сообщение: " + message);
        String header;
        JSONObject json;
        json = new JSONObject(message);
        header = json.get("type").toString();
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
                List<ChatRoom> chatRooms = Main.databaseConnector.getChatrooms();
                List<String> chatroomNames = new ArrayList<>();
                for (ChatRoom chatRoom:chatRooms) {
                    chatroomNames.add(chatRoom.getName());
                }
                gui.getChatrooms().addAll(chatroomNames);
                gui.fillChatroomList();
                break;

            case responseInfo:
                responseHandler(gson.fromJson(message,ResponseMessage.class));
                break;
        }
    }

    private void showMessage(TextMessage message){
        if(Main.databaseConnector.getChatroomName(message.getChatroom_id())==null){
            Main.databaseConnector.addChatroom(message.getChatroom_id(),message.getChatroom_nickname());
            System.out.println("Добавляем чат:" + message.getChatroom_id() + " " + message.getChatroom_nickname());
            gui.getChatrooms().add(message.getChatroom_nickname());
            gui.fillChatroomList();
        }
        if(message.getChatroom_id()==currentChatID){
            Main.databaseConnector.addMessageToHistory(
                    message.getBody(),message.getSender_id(),message.getChatroom_id(),
                    Main.user.getId(),message.getSender_nickname(),true);
            gui.getChatWindow().append(message.getSender_nickname() +
                    ": " + message.getBody() + "\n");
        }
        else{
            Main.databaseConnector.addMessageToHistory(
                    message.getBody(),message.getSender_id(),message.getChatroom_id(),
                    Main.user.getId(),message.getSender_nickname(),false);
            String finalMessage = message.getSender_nickname() + ": " + message.getBody() + "\n";
            if(Main.messageQueue.containsKey(message.getChatroom_id())){
                Main.messageQueue.get(message.getChatroom_id()).add(finalMessage);
            }
            else{
                List<String> queue = new ArrayList<>();
                queue.add(finalMessage);
                Main.messageQueue.put(message.getChatroom_id(),queue);
            }
        }
    }

    private void showFileMessage(FileMessage message, String filePath){
        if(message.getChatroom_id()==currentChatID){
            Main.databaseConnector.addMessageToHistory(
                    filePath,message.getSender_id(),message.getChatroom_id(),
                    Main.user.getId(),message.getSender_nickname(),true);
            gui.getChatWindow().append(message.getSender_nickname() +
                    ": " + filePath + "\n");
        }
        else{
            Main.databaseConnector.addMessageToHistory(
                    filePath,message.getSender_id(),message.getChatroom_id(),
                    Main.user.getId(),message.getSender_nickname(),false);
            String finalMessage = message.getSender_nickname() + ": " + filePath + "\n";
            if(Main.messageQueue.containsKey(message.getChatroom_id())){
                Main.messageQueue.get(message.getChatroom_id()).add(finalMessage);
            }
            else{
                List<String> queue = new ArrayList<>();
                queue.add(finalMessage);
                Main.messageQueue.put(message.getChatroom_id(),queue);
            }
        }
    }

    private void responseHandler(ResponseMessage message){
        if(message.getClassType().equals(userInfo)){
            if(message.getStatus().equals(success) && message.getAction().equals(getRequest) && !gui.getFindTextField().getText().isEmpty()){
                gui.fillSearchList(message.getBody());
                gui.getFindTextField().setText("");
            }
            else if(message.getStatus().equals(success) && message.getAction().equals(getRequest) && !gui.getFindUserTextField().getText().isEmpty()){
                gui.fillSearchList(message.getBody());
                gui.getFindUserTextField().setText("");
            }
            else if(message.getStatus().equals(success) && message.getAction().equals(getRequest)){
                if(gui.getChatParticipants().isEmpty() && message.getParameter().equals(participantsInfo)){
                    gui.getChatParticipants().putAll(message.getBody());
                    gui.getManageParticipantsList().setListData(gui.getChatParticipants().keySet().toArray());
                }
                else if(message.getStatus().equals(success) && message.getParameter().equals(blacklistInfo)) {
                    gui.getBlacklist().putAll(message.getBody());
                    gui.getManageBlacklistList().setListData(gui.getBlacklist().keySet().toArray());
                }
                else if(message.getStatus().equals(success) && message.getParameter().equals(moderatorInfo)) {
                    gui.getModerators().putAll(message.getBody());
                    System.out.println(gui.getModerators());
                    gui.getManageModeratorsList().setListData(gui.getModerators().keySet().toArray());
                    gui.getChatComboBox().setEnabled(true);
                    gui.setCurrentChatAdmin(message.getUser().getNickname());
                    gui.setRights();
                }
            }
            else{
                JOptionPane.showMessageDialog(null, "Произошла ошибка, попробуйте снова.");
            }
        }
        if(message.getClassType().equals(chatroomInfo)){
            if(message.getStatus().equals(success) && message.getAction().equals(createRequest)){
                gui.clearChatroomCreation();
            }
            if(message.getStatus().equals(success) && message.getAction().equals(getRequest)){
                for(String chatName:message.getBody().keySet()){
                    gui.getChatComboBox().addItem(chatName);
                }
            }
            if(message.getStatus().equals(success) && message.getAction().equals(updateRequest)){
                gui.finishEditing();
            }
        }
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
            if(Main.databaseConnector.getChatroomName(fileMessage.getChatroom_id())==null){
                Main.databaseConnector.addChatroom(fileMessage.getChatroom_id(),fileMessage.getSender_nickname());
            }
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
                    append(fileMessage.getFileName()).toString());
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
            showFileMessage(fileMessage,file.getPath());
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

    public int getCurrentChatID() {
        return currentChatID;
    }

    public void setCurrentChatID(int currentChatID) {
        System.out.println("CCI: " + currentChatID);
        this.currentChatID = currentChatID;
    }
}
