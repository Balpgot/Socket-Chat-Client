package ru.tsindrenko;

import java.io.*;
import java.net.Socket;

public class Main {
    private static Socket clientSocket; //сокет для общения
    private static BufferedReader in; // поток чтения из сокета
    private static BufferedWriter out; // поток записи в сокет
    private static final int port = 8080;

    public static void main(String[] args) throws IOException {
        clientSocket = new Socket("localhost", port);
        System.out.println("Подключено");
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // читать соообщения с сервера
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())); // писать на сервер
        MessageSender messageSender = new MessageSender(out);
        MessageReceiver messageReceiver = new MessageReceiver(in);
    }
}
