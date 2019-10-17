package ru.tsindrenko;

public class ChatRoom {
    private final String type = "CHATROOM";
    private int id;
    private String name;

    ChatRoom(int id, String name){
        this.id = id;
        this.name = name;
    }

    //геттеры и сеттеры

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
