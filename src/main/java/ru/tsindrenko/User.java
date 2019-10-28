package ru.tsindrenko;

public class User {
    private final String type = "USER";
    private int id;
    private String nickname;
    private String login;
    private String password;
    private String avatar;

    User(String login, String password, String nickname){
        this.nickname = nickname;
        this.login = login;
        this.password = password;
    }

    User(int id, String name, String avatar){
        this.id = id;
        this.nickname = name;
        this.avatar = avatar;
    }

    //геттеры и сеттеры

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}