package ru.tsindrenko;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DatabaseConnector {
    private static final String url = "jdbc:mysql://localhost:3306/clientDB?serverTimezone=Europe/Moscow&useSSL=false";
    private static final String login = "root";
    private static final String password = "root";

    private static Connection connection;

    DatabaseConnector() {
        try {
            // opening database connection to MySQL server
            connection = DriverManager.getConnection(url, login, password);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    //методы для работы

    private ResultSet executeQuery(String query) {
        ResultSet resultSet = null;
        Statement statement;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return resultSet;
    }

    public void endConnection() {
        //close connection ,stmt
        try {
            connection.close();
        } catch (SQLException se) { /*can't do anything */ }
    }

    //методы для получения данных

    public HashSet<User> getUsers(){
        ResultSet users = executeQuery("SELECT * FROM Users WHERE is_deleted=0");
        HashSet<User> userList = new HashSet<>();
        User user;
        try{
            while (users.next()) {
                user = new User(
                        users.getInt(1),
                        users.getString(2),
                        users.getString(3));
                userList.add(user);
            }
            users.close();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return userList;
    }

    public List<ChatRoom> getChatrooms(){
        ResultSet resultSet = executeQuery("SELECT * FROM Chatrooms WHERE is_deleted=0 AND user_id ="+Main.user.getId());
        List<ChatRoom> chatRoomList = new ArrayList<>();
        ChatRoom chatRoom;
        try{
            while (resultSet.next()) {
                chatRoom = new ChatRoom(
                        resultSet.getInt(1),
                        resultSet.getString(2));
                chatRoomList.add(chatRoom);
            }
            resultSet.close();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return chatRoomList;
    }

    public User getUser(int id){
        ResultSet userDB = executeQuery("SELECT * FROM Users WHERE id="+id);
        User user = null;
        try {
            if(userDB.next()){
                user = new User(userDB.getInt(1),
                        userDB.getString(2),
                        userDB.getString(3));
                userDB.close();
            }
            else{
                userDB.close();
                return null;
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return user;
    }

    public User getUser(String name){
        ResultSet userDB = executeQuery("SELECT * FROM Users WHERE name='"+name+"'");
        User user = null;
        try {
            if(userDB.next()){
                user = new User(userDB.getInt(1),
                        userDB.getString(2),
                        userDB.getString(3));
                userDB.close();
            }
            else {
                userDB.close();
                return null;
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
            ex.getMessage();
        }
        return user;
    }


    public String getChatroomName(int id){
        ResultSet chatroomDB = executeQuery("SELECT name FROM Chatrooms WHERE id='"+id+"' AND user_id='"+Main.user.getId()+"'");
        String name = "";
        try {
            if(chatroomDB.next()){
                name = chatroomDB.getString(1);
                System.out.println("Имя: " + name);
            }
            else{
                chatroomDB.close();
                return null;
            }

            chatroomDB.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return name;
    }

    public int getChatroomID(String name){
        ResultSet chatroomDB = executeQuery("SELECT id FROM Chatrooms WHERE name='"+name+"'");
        int result = -1;
        try {
            if(chatroomDB.next()){
                result = chatroomDB.getInt(1);
            }
            chatroomDB.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return result;
    }

    public HashSet<String> getBadWords(){
        ResultSet badWordsResult = executeQuery("SELECT * FROM Bad_words");
        HashSet<String> badWords = new HashSet<>();
        try {
            while(badWordsResult.next()){
                badWords.add(badWordsResult.getString(1));
            }
            badWordsResult.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return badWords;
    }

    public HashMap<String,String> getTranslit(){
        ResultSet translitResult = executeQuery("SELECT * FROM Translit");
        HashMap<String,String> translit = new HashMap<>();
        try {
            while(translitResult.next()){
                translit.put(translitResult.getString(1),
                        translitResult.getString(2));
            }
            translitResult.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return translit;
    }

    public void addUser(int id, String name, String avatar){
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO Users (Users.id,Users.name,Users.avatar) VALUES ('" +
                    id + "','" + name + "','" +
                    avatar + "'");
            statement.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void addChatroom(int id, String name){
        System.out.println("ДОБАВЛЯЮ ЧАТКОМНАТУ " + name);
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO Chatrooms (Chatrooms.id,Chatrooms.name,Chatrooms.is_deleted,Chatrooms.user_id) VALUES ('" +
                    id + "','" + name + "'," + "'0','" + Main.user.getId() +"')");
            Sender.getChatrooms().put(id,name);
            statement.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public List<String> getMessages(int chat_id, boolean old){
        String query = "SELECT sender_nickname,body FROM Message_history WHERE chatroom_id='"+chat_id+"' AND is_read=";
        if(old){
            query+="'1'";
        }
        else
            query+="'0'";
        query+=" AND user_id='"+Main.user.getId()+"'";
        ResultSet messagesResult = executeQuery(query);
        List<String> oldMessages = new ArrayList<>();
        try {
            while(messagesResult.next()){
                oldMessages.add(messagesResult.getString(1) +
                        ": " + messagesResult.getString(2) + "\n");
            }
            messagesResult.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return oldMessages;
    }

    public void addMessageToHistory(String body, int sender_id, int chatroom_id, int user_id, String sender_nickname,boolean is_read){
        Statement statement;
        try {
            statement = connection.createStatement();
            String query = "INSERT INTO Message_history (Message_history.body,Message_history.sender_id," +
                    "Message_history.chatroom_id,Message_history.user_id,Message_history.sender_nickname,Message_history.is_read) VALUES ('" +
                    body + "','" + sender_id + "','"+chatroom_id + "','" +user_id +"','"+sender_nickname +"','";
            if(is_read){
                query+="1')";
            }
            else
                query+="0')";
            statement.executeUpdate(query);
            statement.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void setMessageRead(int message_id){
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("UPDATE Message_history SET is_read=1 WHERE id='"+message_id+"'");
            statement.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void setChatMessageRead(int chat_id){
        Statement statement;
        try {
            statement = connection.createStatement();
            String query = "UPDATE Message_history SET is_read=1 WHERE chatroom_id='"+chat_id+"' AND user_id='"+Main.user.getId()+"'";
            statement.executeUpdate(query);
            statement.close();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }


}
