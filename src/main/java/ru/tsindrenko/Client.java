package ru.tsindrenko;

import java.io.*;
import java.net.Socket;

public class Client {
    private static Socket clientSocket; //сокет для общения
    private static BufferedReader reader; // нам нужен ридер читающий с консоли, иначе как
    // мы узнаем что хочет сказать клиент?
    private static BufferedReader in; // поток чтения из сокета
    private static BufferedWriter out; // поток записи в сокет
    private static final int port = 8080;

    public static void main(String[] args) throws IOException{
        try {
            // адрес - локальный хост, порт - 8080, такой же как у сервера
            clientSocket = new Socket("localhost", port);
            System.out.println("Подключено");// этой строкой мы запрашиваем
            //  у сервера доступ на соединение
            reader = new BufferedReader(new InputStreamReader(System.in));
            // читать соообщения с сервера
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // писать туда же
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String message;
            String serverResponse;
            while (true) {
                //System.out.println("Вы что-то хотели сказать? Введите это здесь:");
                // если соединение произошло и потоки успешно созданы - мы можем
                //  работать дальше и предложить клиенту что то ввести
                // если нет - вылетит исключение
                message = reader.readLine(); // ждём пока клиент что-нибудь
                // не напишет в консоль
                out.write(message + "\n"); // отправляем сообщение на сервер
                out.flush();
                serverResponse = in.readLine(); // ждём, что скажет сервер
                if(serverResponse.equals("break_connection")){
                    System.out.println("Вы отключены от сервера");
                    break;
                }
                System.out.println(serverResponse); // получив - выводим на экран
            }
        }
        finally {
            System.out.println("Клиент был закрыт...");
            in.close();
            out.close();
            clientSocket.close();
        }
    }
}
