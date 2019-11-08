package ru.tsindrenko;

import java.util.HashSet;

public class ChatRoom {
    private final String type = "CHATROOM";
    private int id;
    private String name;
    private Integer admin_id;
    private boolean is_dialog;
    private HashSet<Integer> participants_id = new HashSet<>();
    private HashSet<Integer> blacklist = new HashSet<>();

    ChatRoom(int id, String name){
        this.id = id;
        this.name = name;
    }

    public ChatRoom(String name, Integer admin_id, boolean is_dialog) {
        this.name = name;
        this.admin_id = admin_id;
        this.is_dialog = is_dialog;
    }

    public ChatRoom(String name, Integer admin_id, boolean is_dialog, HashSet<Integer> participants_id) {
        this.name = name;
        this.admin_id = admin_id;
        this.is_dialog = is_dialog;
        this.participants_id = participants_id;
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

    public Integer getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(Integer admin_id) {
        this.admin_id = admin_id;
    }

    public boolean isIs_dialog() {
        return is_dialog;
    }

    public void setIs_dialog(boolean is_dialog) {
        this.is_dialog = is_dialog;
    }

    public HashSet<Integer> getParticipants_id() {
        return participants_id;
    }

    public void setParticipants_id(HashSet<Integer> participants_id) {
        this.participants_id = participants_id;
    }

    public HashSet<Integer> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(HashSet<Integer> blacklist) {
        this.blacklist = blacklist;
    }
}
