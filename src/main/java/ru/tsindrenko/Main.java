package ru.tsindrenko;

import java.io.*;
import java.net.Socket;

public class Main {
    static Socket serverSocket; //сокет для общения
    static BufferedReader in; // поток чтения из сокета
    static BufferedWriter out; // поток записи в сокет
    static final int port = 8080;//порт сервера

    public static void main(String[] args) throws IOException {
        serverSocket = new Socket("localhost", port);
        System.out.println("Подключено");
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream())); // читать соообщения с сервера
        out = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream())); // писать на сервер
        //MessageSender messageSender = new MessageSender(out);
        GUI gui = new GUI();
        LoginForm loginForm = new LoginForm(gui);
        MessageReceiver messageReceiver = new MessageReceiver(in, gui);

    }
}
