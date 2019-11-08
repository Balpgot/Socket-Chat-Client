package ru.tsindrenko;

import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GUI extends JFrame {
    //основа
    private JPanel rootPanel;
    private JTabbedPane tabbedPanel;
    private Gson gson;
    //чат
    private JButton sendButton;
    private JTextField inputMessageField;
    private JButton sendFileButton;
    private JTextArea chatWindow;
    private JList chatroomList;
    private JPanel chatPanel;
    private HashSet<String> chatrooms;
    //аккаунт
    private JPanel accountPanel;
    private JScrollPane chatScroll;
    private JScrollPane chatListScroll;
    private JTextField nameTextField;
    private JButton createButton;
    private JCheckBox checkBoxDialog;
    private JList participantsList;
    private JButton makeParticipantButton;
    //контакты-найти пользователя
    private JList findList;
    private JTextField findTextField;
    private JButton findButton;
    private JPanel adminPanel;
    private JTabbedPane administratedTabbedPanel;
    private JPanel contactsPanel;
    private JScrollPane searchScroll;
    private JLabel chatroomName;
    private JScrollPane participantsScroll;
    private JLabel participantsLabel;
    private JLabel findLabel;
    private HashMap<Integer, String> participants;
    private HashMap<String, Integer> searchResults;
    //управление чат-комнатой
    private JList manageParticipantsList;
    private JPanel chatManagePanel;
    private JList manageBlacklistList;
    private JList manageModeratorsList;
    private JComboBox chatComboBox;
    private JButton blacklistButton;
    private JButton moderatorButton;
    private JButton saveButton;
    private JList findUsersList;
    private JButton participantButton;
    private JButton findDialogButton;
    private JTextField findUserTextField;
    private JLabel participantsManageLabel;
    private JLabel findManageLabel;
    private JLabel blacklistLabel;
    private JLabel moderatorLabel;
    private HashMap<String, Integer> blacklist;
    private HashMap<String, Integer> moderators;
    private HashMap<String, Integer> chatParticipants;
    //cлушатели
    ButtonEventListener buttonEventListener;
    ListEventListener listSelectionListener;
    KeyboardListener keyboardListener;
    //константы
    private final String userInfo = "USER";
    private final String chatroomInfo = "CHATROOM";
    private final String getRequest = "GET";
    private final String updateRequest = "UPDATE";
    private final String deleteRequest = "DELETE";
    private final String createRequest = "CREATE";
    private final String adminInfo = "ADMIN";

    GUI() {
        //основные настройки
        setContentPane(rootPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocation(700, 350);
        setSize(600, 400);
        tabbedPanel.addChangeListener(new TabGUIChangeListener());
        administratedTabbedPanel.addChangeListener(new TabGUIChangeListener());
        gson = new Gson();
        chatPanel.setEnabled(true);
        accountPanel.setEnabled(false);
        adminPanel.setEnabled(false);
        //слушатели событий
        buttonEventListener = new ButtonEventListener();
        listSelectionListener = new ListEventListener();
        keyboardListener = new KeyboardListener();
        //настройки чата
        sendButton.addActionListener(buttonEventListener);
        sendFileButton.addActionListener(buttonEventListener);
        chatroomList.addListSelectionListener(new ListEventListener());
        inputMessageField.setFocusable(true);
        inputMessageField.addKeyListener(keyboardListener);
        //настройки создания чата
        findList.addListSelectionListener(new ListEventListener());
        participantsList.addListSelectionListener(new ListEventListener());
        makeParticipantButton.addActionListener(buttonEventListener);
        findButton.addActionListener(buttonEventListener);
        createButton.addActionListener(buttonEventListener);
        participants = new HashMap<>();
        searchResults = new HashMap<>();
        chatrooms = new HashSet<>();
        //настройки администратора
        chatComboBox.addActionListener(new AdministratorComboBoxListener());
        findUsersList.addListSelectionListener(new AdministratorListsEventListener());
        manageParticipantsList.addListSelectionListener(new AdministratorListsEventListener());
        manageModeratorsList.addListSelectionListener(new AdministratorListsEventListener());
        manageBlacklistList.addListSelectionListener(new AdministratorListsEventListener());
        findDialogButton.addActionListener(new AdministratorButtonsEventListener());
        blacklistButton.addActionListener(new AdministratorButtonsEventListener());
        moderatorButton.addActionListener(new AdministratorButtonsEventListener());
        participantButton.addActionListener(new AdministratorButtonsEventListener());
        blacklist = new HashMap<>();
        moderators = new HashMap<>();
        chatParticipants = new HashMap<>();
    }

    /*
     * ОБЩИЕ МЕТОДЫ
     */

    //отправляет сообщение обработчику
    public void sendMessageToSender() {
        int currentId = Main.messageReceiver.getCurrentChatID();
        TextMessage textMessage = new TextMessage(Sender.modifyText(inputMessageField.getText()), Main.user.getId(), Main.user.getNickname(), currentId, Main.databaseConnector.getChatroomName(currentId));
        Sender.sendMessage(gson.toJson(textMessage, TextMessage.class));
        inputMessageField.setText("");
    }

    //отправляет файл обработчику
    public void sendFileToSender() {
        JFileChooser fileopen = new JFileChooser();
        int ret = fileopen.showDialog(null, "Выбрать файл");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileopen.getSelectedFile();
            Sender.sendFile(file, false);
        }
    }

    //добавляет результат респонса по пользователям
    public void fillSearchList(HashMap<String, Integer> users) {
        if (!findTextField.getText().isEmpty()) {
            findList.setListData(users.keySet().toArray());
            searchResults.putAll(users);
        } else if (!findUserTextField.getText().isEmpty()) {
            findUsersList.setListData(users.keySet().toArray());
            searchResults.putAll(users);
        }
    }

    //запрос по пользователям
    public void findUsers() {
        if (!findTextField.getText().isEmpty()) {
            Sender.sendMessage(gson.toJson(new RequestMessage(userInfo, getRequest, findTextField.getText())));
        } else if (!findUserTextField.getText().isEmpty()) {
            Sender.sendMessage(gson.toJson(new RequestMessage(userInfo, getRequest, findUserTextField.getText())));
        }
    }

    //заполняет список чатов пользователя
    public void fillChatroomList() {
        if (!chatrooms.isEmpty()) {
            chatroomList.setListData(chatrooms.toArray());
        }
    }

    //заполняет окно чата
    public void showAllMessages() {
        int chatID = Main.databaseConnector.getChatroomID(chatroomList.getSelectedValue().toString());
        System.out.println("CID: " + chatID);
        Main.messageReceiver.setCurrentChatID(chatID);
        chatWindow.setText("");
        List<String> oldMessages = Main.databaseConnector.getMessages(chatID, true);
        for (String message : oldMessages) {
            chatWindow.append(message);
        }
        if (Main.messageQueue.containsKey(chatID)) {
            chatWindow.append("*-----Новые сообщения-----*\n");
            List<String> queuedMessages = Main.messageQueue.get(chatID);
            for (String message : queuedMessages) {
                chatWindow.append(message);
            }
            Main.messageQueue.remove(chatID);
            Main.databaseConnector.setChatMessageRead(chatID);
        }
    }

    /*
     * СОЗДАНИЕ ЧАТА
     */

    //добавляет пользователя в список участников
    public void updateParticipants() {
        JList currentList = null;
        //обозначить текущий список
        if (findList.getSelectedValue() != null) {
            currentList = findList;
        } else if (participantsList.getSelectedValue() != null) {
            currentList = participantsList;
        } else {
            JOptionPane.showMessageDialog(null, "Выберите пользователя из списка");
        }

        //если пользователь выбран
        if (currentList != null) {
            //удалить из списка участников
            if (currentList.equals(participantsList)) {
                if (currentList.getSelectedValue().toString().equals(Main.user.getNickname())) {
                    JOptionPane.showMessageDialog(null, "Нельзя удалить себя из участников");
                } else {
                    participants.remove(searchResults.get(currentList.getSelectedValue().toString()));
                    participantsList.setListData(participants.values().toArray());
                    if (participants.size() <= 2) {
                        checkBoxDialog.setEnabled(true);
                    }
                }
            }
            //проверить, нет ли пользователя в списках
            else if (participants.values().contains(currentList.getSelectedValue().toString())) {
                JOptionPane.showMessageDialog(null, "Пользователь уже добавлен в участники");
            }

            //добавить пользователя
            else {
                participants.put(searchResults.get(currentList.getSelectedValue().toString()), currentList.getSelectedValue().toString());
                participantsList.setListData(participants.values().toArray());
                if (participants.size() > 2) {
                    checkBoxDialog.setEnabled(false);
                    checkBoxDialog.setSelected(false);
                }
            }
        }
    }

    //создает чат-комнату
    public void createChatroom() {
        if (participants.size() < 2) {
            JOptionPane.showMessageDialog(null, "Нельзя создать комнату без собеседников");
        } else if (nameTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Название чата не должно быть пустым");
        } else {
            ChatRoom chatRoom = new ChatRoom(nameTextField.getText(), Main.user.getId(), checkBoxDialog.isSelected());
            chatRoom.getParticipants_id().addAll(participants.keySet());
            Sender.sendMessage(gson.toJson(new RequestMessage(chatroomInfo, createRequest, chatRoom)));
            createButton.setEnabled(false);
        }
    }

    //завершает создание чат-комнаты
    public void clearChatroomCreation() {
        //сохраняем данные
        chatrooms.add(nameTextField.getText());
        Main.messageReceiver.setCurrentChatID(Main.databaseConnector.getChatroomID(nameTextField.getText()));
        System.out.println(Main.messageReceiver.getCurrentChatID());
        //восстанавливаем страницу
        createButton.setEnabled(true);
        checkBoxDialog.setSelected(false);
        participants.clear();
        participantsList.setListData(participants.values().toArray());
        findTextField.setText("");
        searchResults.clear();
        findList.setListData(searchResults.keySet().toArray());
        //работаем с основной страницей
        tabbedPanel.setSelectedComponent(chatPanel);
        chatroomList.removeListSelectionListener(listSelectionListener);
        chatroomList.setListData(chatrooms.toArray());
        chatroomList.addListSelectionListener(listSelectionListener);
        chatroomList.setSelectedValue(nameTextField.getText(), true);
        nameTextField.setText("");
    }

    /*
     * ЗАПОЛНЕНИЕ СТРАНИЦЫ АДМИНИСТРИРОВАНИЯ
     */

    //хз зачем
    public void fillChatroomList(List<Integer> chatrooms) {
        List<String> chatroomNames = new ArrayList<>();
        for (Integer chatroom : chatrooms) {
            chatroomNames.add(Main.databaseConnector.getChatroomName(chatroom));
        }
        this.chatrooms.addAll(chatroomNames);
        chatroomList.setListData(chatroomNames.toArray());
    }

    public void setRights() {
        if (moderators.containsKey(Main.user.getNickname())) {
            System.out.println("Пользователь - модератор");
            participantButton.setEnabled(false);
            moderatorButton.setEnabled(false);
            findUserTextField.setEnabled(false);
            findDialogButton.setEnabled(false);
        } else {
            participantButton.setEnabled(true);
            moderatorButton.setEnabled(true);
            findUserTextField.setEnabled(true);
            findDialogButton.setEnabled(true);
        }
    }

    //слушатели

    public class AdministratorComboBoxListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println(chatComboBox.getSelectedItem());
            if (administratedTabbedPanel.getSelectedIndex() == 0) {
                if (chatComboBox.getSelectedItem().toString() != null) {
                    blacklist.clear();
                    moderators.clear();
                    chatParticipants.clear();
                    Sender.sendMessage(gson.toJson(new RequestMessage(chatroomInfo, getRequest, new ChatRoom(Main.databaseConnector.getChatroomID(chatComboBox.getSelectedItem().toString()), chatComboBox.getSelectedItem().toString()))));
                    chatComboBox.setEnabled(false);
                }
                if (moderators.containsKey(Main.user.getNickname())) {
                    System.out.println("Пользователь - модератор");
                    participantButton.setEnabled(false);
                    moderatorButton.setEnabled(false);
                    findUserTextField.setEnabled(false);
                    findDialogButton.setEnabled(false);
                } else {
                    participantButton.setEnabled(true);
                    moderatorButton.setEnabled(true);
                    findUserTextField.setEnabled(true);
                    findDialogButton.setEnabled(true);
                }
            }
        }
    }

    class AdministratorListsEventListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            //очищаем лишние списки
            if (e.getSource().equals(findUsersList)) {
                manageBlacklistList.clearSelection();
                manageParticipantsList.clearSelection();
                manageModeratorsList.clearSelection();
            } else if (e.getSource().equals(manageBlacklistList)) {
                manageParticipantsList.clearSelection();
                manageModeratorsList.clearSelection();
                findUsersList.clearSelection();
            } else if (e.getSource().equals(manageParticipantsList)) {
                manageModeratorsList.clearSelection();
                findUsersList.clearSelection();
                manageBlacklistList.clearSelection();
            } else {
                manageParticipantsList.clearSelection();
                findUsersList.clearSelection();
                manageBlacklistList.clearSelection();
            }
        }
    }

    class ListEventListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                //заполняем чат сообщениями
                if (e.getSource().equals(chatroomList)) {
                    showAllMessages();
                }
                //очишаем списки создания чата
                if (e.getSource().equals(findList)) {
                    participantsList.clearSelection();
                } else if (e.getSource().equals(participantsList)) {
                    findList.clearSelection();
                }


            }
        }
    }

    class KeyboardListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && !inputMessageField.getText().isEmpty()) {
                sendMessageToSender();
            }
        }
    }

    class AdministratorButtonsEventListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(findDialogButton)) {
                findUsers();
            }
            if (e.getSource().equals(blacklistButton)) {

            }
        }
    }

    class ButtonEventListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(sendButton) && !inputMessageField.getText().isEmpty()) {
                sendMessageToSender();
            } else if (e.getSource().equals(sendFileButton)) {
                sendFileToSender();
            } else if (e.getSource().equals(makeParticipantButton)) {
                updateParticipants();
            } else if (e.getSource().equals(createButton)) {
                createChatroom();
            } else if (e.getSource().equals(findButton)) {
                findUsers();
            }
        }
    }

    class TabGUIChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            System.out.println("TP: " + tabbedPanel.getSelectedIndex());
            System.out.println("ATP: " + administratedTabbedPanel.getSelectedIndex());
            if (tabbedPanel.getSelectedIndex() == 0) {
                chatPanel.setEnabled(true);
                accountPanel.setEnabled(false);
                adminPanel.setEnabled(false);
            } else if (tabbedPanel.getSelectedIndex() == 1) {
                chatPanel.setEnabled(false);
                accountPanel.setEnabled(true);
                adminPanel.setEnabled(false);
            } else if (tabbedPanel.getSelectedIndex() == 2) {
                chatPanel.setEnabled(false);
                accountPanel.setEnabled(false);
                adminPanel.setEnabled(true);
            }
            if (tabbedPanel.getSelectedIndex() == 2) {
                if (administratedTabbedPanel.getSelectedIndex() == 1) {
                    if (participants.isEmpty()) {
                        participants.put(Main.user.getId(), Main.user.getNickname());
                    }
                    participantsList.setListData(participants.values().toArray());
                }
                if (administratedTabbedPanel.getSelectedIndex() == 0) {
                    if (chatComboBox.getModel().getSize() == 0) {
                        Sender.sendMessage(gson.toJson(new RequestMessage(chatroomInfo, getRequest, adminInfo)));
                    }
                }
            }
        }
    }

    //геттеры и сеттеры

    public HashSet<String> getChatrooms() {
        return chatrooms;
    }

    public JTextArea getChatWindow() {
        return chatWindow;
    }

    public JTextField getFindTextField() {
        return findTextField;
    }

    public JTextField getFindUserTextField() {
        return findUserTextField;
    }

    public JComboBox getChatComboBox() {
        return chatComboBox;
    }

    public HashMap<String, Integer> getBlacklist() {
        return blacklist;
    }

    public HashMap<String, Integer> getModerators() {
        return moderators;
    }

    public HashMap<String, Integer> getChatParticipants() {
        return chatParticipants;
    }

    public JList getManageParticipantsList() {
        return manageParticipantsList;
    }

    public JList getManageBlacklistList() {
        return manageBlacklistList;
    }

    public JList getManageModeratorsList() {
        return manageModeratorsList;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel = new JTabbedPane();
        rootPanel.add(tabbedPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        chatPanel = new JPanel();
        chatPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("Чат", chatPanel);
        inputMessageField = new JTextField();
        inputMessageField.setText("");
        chatPanel.add(inputMessageField, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(302, 30), null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Отправить");
        chatPanel.add(sendButton, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(302, 30), null, 0, false));
        sendFileButton = new JButton();
        sendFileButton.setText("Отправить файл");
        chatPanel.add(sendFileButton, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chatScroll = new JScrollPane();
        chatPanel.add(chatScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        chatWindow.setText("");
        chatScroll.setViewportView(chatWindow);
        chatListScroll = new JScrollPane();
        chatListScroll.putClientProperty("html.disable", Boolean.FALSE);
        chatPanel.add(chatListScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatroomList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        chatroomList.setModel(defaultListModel1);
        chatListScroll.setViewportView(chatroomList);
        accountPanel = new JPanel();
        accountPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("Аккаунт", accountPanel);
        adminPanel = new JPanel();
        adminPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPanel.addTab("Администрирование", adminPanel);
        administratedTabbedPanel = new JTabbedPane();
        adminPanel.add(administratedTabbedPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        chatManagePanel = new JPanel();
        chatManagePanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        administratedTabbedPanel.addTab("Управление чатами", chatManagePanel);
        chatComboBox = new JComboBox();
        chatManagePanel.add(chatComboBox, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(165, 30), new Dimension(165, 30), new Dimension(165, 30), 0, false));
        blacklistButton = new JButton();
        blacklistButton.setText("<>");
        chatManagePanel.add(blacklistButton, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, 60), new Dimension(60, 60), new Dimension(60, 60), 0, false));
        moderatorButton = new JButton();
        moderatorButton.setText("<>");
        chatManagePanel.add(moderatorButton, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, 60), new Dimension(60, 60), new Dimension(60, 60), 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        chatManagePanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(4, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        manageBlacklistList = new JList();
        scrollPane1.setViewportView(manageBlacklistList);
        final JScrollPane scrollPane2 = new JScrollPane();
        chatManagePanel.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 5, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        manageParticipantsList = new JList();
        scrollPane2.setViewportView(manageParticipantsList);
        final JScrollPane scrollPane3 = new JScrollPane();
        chatManagePanel.add(scrollPane3, new com.intellij.uiDesigner.core.GridConstraints(6, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        manageModeratorsList = new JList();
        scrollPane3.setViewportView(manageModeratorsList);
        final JScrollPane scrollPane4 = new JScrollPane();
        chatManagePanel.add(scrollPane4, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        findUsersList = new JList();
        scrollPane4.setViewportView(findUsersList);
        participantButton = new JButton();
        participantButton.setText("<>");
        chatManagePanel.add(participantButton, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, 60), new Dimension(60, 60), new Dimension(60, 60), 0, false));
        findDialogButton = new JButton();
        findDialogButton.setText("Искать");
        chatManagePanel.add(findDialogButton, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Сохранить");
        chatManagePanel.add(saveButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        findUserTextField = new JTextField();
        chatManagePanel.add(findUserTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        participantsManageLabel = new JLabel();
        participantsManageLabel.setText("Участники");
        chatManagePanel.add(participantsManageLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        findManageLabel = new JLabel();
        findManageLabel.setText("Найденные пользователи");
        chatManagePanel.add(findManageLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        blacklistLabel = new JLabel();
        blacklistLabel.setText("Заблокированные пользователи");
        chatManagePanel.add(blacklistLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        moderatorLabel = new JLabel();
        moderatorLabel.setText("Модераторы");
        chatManagePanel.add(moderatorLabel, new com.intellij.uiDesigner.core.GridConstraints(5, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        contactsPanel = new JPanel();
        contactsPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        administratedTabbedPanel.addTab("Создать чат", contactsPanel);
        searchScroll = new JScrollPane();
        contactsPanel.add(searchScroll, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 2, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        findList = new JList();
        final DefaultListModel defaultListModel2 = new DefaultListModel();
        findList.setModel(defaultListModel2);
        searchScroll.setViewportView(findList);
        chatroomName = new JLabel();
        chatroomName.setText("Название чата:");
        contactsPanel.add(chatroomName, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        findTextField = new JTextField();
        contactsPanel.add(findTextField, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        participantsScroll = new JScrollPane();
        contactsPanel.add(participantsScroll, new com.intellij.uiDesigner.core.GridConstraints(3, 3, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        participantsList = new JList();
        participantsScroll.setViewportView(participantsList);
        participantsLabel = new JLabel();
        participantsLabel.setText("Список участников:");
        contactsPanel.add(participantsLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        findButton = new JButton();
        findButton.setText("Найти");
        contactsPanel.add(findButton, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createButton = new JButton();
        createButton.setText("Создать");
        contactsPanel.add(createButton, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxDialog = new JCheckBox();
        checkBoxDialog.setText("Диалог");
        contactsPanel.add(checkBoxDialog, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        contactsPanel.add(nameTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        findLabel = new JLabel();
        findLabel.setText("Поиск");
        contactsPanel.add(findLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        makeParticipantButton = new JButton();
        makeParticipantButton.setEnabled(true);
        makeParticipantButton.setText("<>");
        contactsPanel.add(makeParticipantButton, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, 60), new Dimension(60, 60), new Dimension(60, 60), 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
