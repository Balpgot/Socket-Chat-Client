package ru.tsindrenko;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginForm extends JFrame{
    private final String userNotFound = "USER_NOT_FOUND";
    private final String wrongPassword = "WRONG_PASSWORD";
    private final String loginIsOccupied = "LOGIN_IS_OCCUPIED";
    private final String userIsLogged = "USER_IS_LOGGED";
    private final String accepted = "SUCCESSFUL_OPERATION";

    private JTextField loginInput;
    private JPasswordField passwordInput;
    private JButton submit;
    private JPanel rootPanel;
    private JLabel loginMessage;
    private JLabel passwordMessage;
    private GUI gui;
    private Gson gson;

    LoginForm(GUI gui){
        this.gui = gui;
        gui.setEnabled(false);
        setContentPane(rootPanel);
        setVisible(true);
        loginMessage.setVisible(false);
        passwordMessage.setVisible(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ButtonEventListener buttonEventListener = new ButtonEventListener();
        submit.addActionListener(buttonEventListener);
        gson = new Gson();
    }


    class ButtonEventListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            loginMessage.setVisible(false);
            passwordMessage.setVisible(false);
            if (!loginInput.getText().isEmpty() && passwordInput.getPassword().length > 0) {
                Sender.sendMessage(gson.toJson(new LoginMessage(loginInput.getText(),passwordToString(passwordInput.getPassword()))));
                loginInput.setEditable(false);
                passwordInput.setEditable(false);
            }
            else {
                if(loginInput.getText().isEmpty()){
                    loginMessage.setVisible(true);
                    loginMessage.setText("Введите логин");
                }
                else
                    loginMessage.setVisible(false);
                if(passwordInput.getPassword().length <=0){
                    passwordMessage.setVisible(true);
                    passwordMessage.setText("Введите пароль");
                }
                else passwordMessage.setVisible(false);
            }
        }
    }

    private String passwordToString(char[] password){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i<password.length; i++){
            sb.append(password[i]);
        }
        return sb.toString();
    }

    public void getInformation(String message){
        switch (message){
            case userNotFound:
                JOptionPane.showMessageDialog(null, "Пользователя с таким логином не найдено");
                loginMessage.setVisible(true);
                loginMessage.setText("Нет пользователя с таким логином");
                loginInput.setEditable(true);
                passwordInput.setEditable(true);
                break;
            case userIsLogged:
                JOptionPane.showMessageDialog(null, "Пользователь уже в сети");
                loginInput.setEditable(true);
                passwordInput.setEditable(true);
                break;
            case wrongPassword:
                JOptionPane.showMessageDialog(null, "Неверный пароль");
                passwordMessage.setVisible(true);
                passwordMessage.setText("Неверный пароль");
                loginInput.setEditable(true);
                passwordInput.setEditable(true);
                break;
            case loginIsOccupied:
                JOptionPane.showMessageDialog(null, "Логин занят");
                loginMessage.setVisible(true);
                loginMessage.setText("Данный логин занят");
                loginInput.setEditable(true);
                passwordInput.setEditable(true);
                break;
            case accepted:
                JOptionPane.showMessageDialog(null, "Вы успешно авторизировались");
                setVisible(false);
                gui.setEnabled(true);
                gui.setVisible(true);
                dispose();
                break;
        }

    }

}
