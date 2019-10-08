package ru.tsindrenko;

import java.io.*;
import java.net.Socket;

public class Main {
    static Socket serverSocket; //сокет для общения
    static BufferedReader in; // поток чтения из сокета
    static BufferedWriter out; // поток записи в сокет
    static final int port = 8080;//порт сервера
    final static String file_directory = "D:\\JavaProjects\\ChatClient\\src\\main\\resources\\files";
    static LoginForm loginForm;
    static User user;

    public static void main(String[] args) throws IOException {
        serverSocket = new Socket("localhost", port);
        System.out.println("Подключено");
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream())); // читать соообщения с сервера
        out = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream())); // писать на сервер
        GUI gui = new GUI();
        loginForm = new LoginForm(gui);
        MessageReceiver messageReceiver = new MessageReceiver(in, gui);

    }
}
