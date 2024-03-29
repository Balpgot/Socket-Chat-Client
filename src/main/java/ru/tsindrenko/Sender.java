package ru.tsindrenko;

import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class Sender {

    private static BufferedWriter out = Main.out;
    private static HashSet<String> swearWords = Main.databaseConnector.getBadWords();
    private static HashMap<String,String> tranliteration = Main.databaseConnector.getTranslit();
    private static HashMap<Integer,String> chatrooms = new HashMap<>();
    private static final String avatar = "avatar";
    private static final String fileType = "file";
    private static Gson gson = new Gson();

    //отправка сообщений
    public static void sendMessage(String message){
        try{
            out.write(message+"\n");
            out.flush();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    //проверяет текст на наличие транслита и мата
    public static String modifyText(String message) {
        swearWords.add("сука");
        swearWords.add("блять");
        tranliteration.put("privet","привет");
        tranliteration.put("ilya","илья");
        String [] words = message.split(" ");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<words.length; i++){
            if(swearWords.contains(words[i])){
                sb.append("***");
            }
            else if(tranliteration.containsKey(words[i])){
                sb.append(tranliteration.get(words[i]));
            }
            else
                sb.append(words[i]);
            sb.append(" ");
        }
        System.out.println(sb.toString());
        return sb.toString().trim();
    }

    //метод отправки файла
    public static void sendFile(File file, boolean is_avatar){
        try {
            //определяем размер пакета и открываем файл на чтение
            byte[] byteArray = new byte[8192];
            FileInputStream fis = new FileInputStream(file.getPath());
            //отправляем серверу размер файла
            long size = file.length();
            FileMessage fileMessage;
            if(is_avatar){
                fileMessage = new FileMessage(size,avatar,file.getName(),Main.user.getId(),
                        Main.user.getNickname(),Main.messageReceiver.getCurrentChatID(),
                chatrooms.get(Main.messageReceiver.getCurrentChatID()));
            }
            else
                fileMessage = new FileMessage(size,fileType,file.getName(),Main.user.getId(),
                        Main.user.getNickname(),Main.messageReceiver.getCurrentChatID(),
                        chatrooms.get(Main.messageReceiver.getCurrentChatID()));
            sendMessage(gson.toJson(fileMessage));
            System.out.println("Начинаю оправлять");
            BufferedOutputStream bos = new BufferedOutputStream(Main.serverSocket.getOutputStream());
            while (size>0){
                int i = fis.read(byteArray);
                bos.write(byteArray, 0, i);
                size-= i;
            }
            bos.flush();
            fis.close();
        }
        catch (IOException ex){
            System.out.println("sendFile: " + ex.getMessage());
        }
    }

    public static HashMap<Integer, String> getChatrooms() {
        return chatrooms;
    }

    public static void setChatrooms(HashMap<Integer, String> chatrooms) {
        Sender.chatrooms = chatrooms;
    }
}
