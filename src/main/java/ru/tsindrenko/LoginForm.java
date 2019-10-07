package ru.tsindrenko;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginForm extends JFrame{
    private JTextField loginInput;
    private JPasswordField passwordInput;
    private JButton submit;
    private JPanel rootPanel;
    private JLabel loginMessage;
    private JLabel passwordMessage;
    private GUI gui;

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
    }


    class ButtonEventListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!loginInput.getText().isEmpty() && passwordInput.getPassword().length > 0) {
                //Sender.sendMessage(loginFormatter(loginInput.getText(),passwordInput.getPassword().toString()));
                setVisible(false);
                gui.setEnabled(true);
                dispose();
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

    private String loginFormatter(String login, char[] password){
        StringBuffer sb = new StringBuffer("LOGIN_INFO:[");
        sb.append(login).append(",");
        for(int i = 0; i<password.length; i++){
            sb.append(password[i]);
        }
        sb.append("]");
        return sb.toString();
    }

}
