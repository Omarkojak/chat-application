package Client;

import Misc.Message;
import Misc.MessageType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

/**
 * Created by mohamedelzarei on 11/20/16.
 * mohamedelzarei@gmail.com
 */
public class MainLayout implements ClientListener, Serializable {

    private JPanel MainPanel;
    private JList msgs;
    private JButton logoutBtn;
    private JTextArea textArea1;
    private JList membersList;
    private JButton sendBtn;
    private JButton viewAllMembersBtn;
    private JButton viewLocalMemberBtn;
    private JLabel welcomeMsg;
    private JPanel membersListPanel;
    private static DefaultListModel listModel;
    private static MainLayout mainLayout;
    private static JFrame frame;
    private static Client currentClient;
    private static String username;

    public MainLayout() {
        membersListPanel.setBorder(new TitledBorder("Members List"));
        listModel = new DefaultListModel();

        listModel.addElement("TCP CHAT SERVER.");
        msgs.setModel(listModel);

        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentClient.removeUserFromServer(username);
                System.exit(0);
            }
        });
        viewLocalMemberBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentClient.getLocalMembers();
            }
        });
        viewAllMembersBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentClient.getAllMembers(username);
            }
        });
        membersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    textArea1.setText("@" + list.getModel().getElementAt(index) + " ");
                }
            }
        });
        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String words[] = textArea1.getText().split("\\s", 2);

                if (words.length > 1 && words[1] != null) {
                    String to = words[0];
                    String content = words[1];
                    content = content.trim();
                    to = to.substring(1).trim();
                    if (content.length() > 0) {
                        Message msg = new Message(MessageType.CHAT_MESSAGE, username, to, content);
                        listModel.addElement("To " + msg.to + " : " + msg.data);
                        msgs.setModel(listModel);
                        msgs.repaint();
                        msgs.revalidate();
                        textArea1.setText("");

                        currentClient.sendMessage(msg);
                    } else { // No Message
                        listModel.addElement("Please enter a valid non-empty message.");
                        msgs.setModel(listModel);
                        textArea1.setText("");
                        msgs.repaint();
                        msgs.revalidate();
                    }
                } else {
//                    No receiver
                    listModel.addElement("Please start the message with @username msg.");
                    msgs.setModel(listModel);
                    textArea1.setText("");
                    msgs.repaint();
                    msgs.revalidate();
                }

            }
        });
    }

    private static void changeWelcomeMessage(String username) {
        mainLayout.welcomeMsg.setText(" Welcome, " + username + "");
        mainLayout.welcomeMsg.updateUI();
    }

    public static void startClient() {
        init();
        askForServerIP();
        askAndValidateUsername();
    }

    public static void init() {
        frame = new JFrame("MainLayout");
        frame.setTitle("ZH-TCP Client");
        mainLayout = new MainLayout();
        frame.setContentPane(mainLayout.MainPanel);
        frame.setSize(new Dimension(800, 600));
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                currentClient.removeUserFromServer(username);
            }
        });

    }


    private static void askForServerIP() {
        String serverIPPort = (String) JOptionPane.showInputDialog(
                frame,
                "Please Enter the Server IP and Port:\n",
                "Server IP",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "localhost,6000");

        String[] ip_port = serverIPPort.split(",");
        currentClient = new Client(ip_port[0], Integer.parseInt(ip_port[1]));
        currentClient.listener = mainLayout;
        currentClient.connectToServer();
    }


    private static void askAndValidateUsername() {
        String username = "";
        username = (String) JOptionPane.showInputDialog(
                frame,
                "Please Enter Your Username:\n"
                ,
                "Login",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "Untitled");
        currentClient.requestToAddUserToServer(username);
        changeWelcomeMessage(username);
        MainLayout.username = username;
    }

    @Override
    public void sendMessage(Message msg) {
        switch (msg.type) {
            case USER_APPROVED:
                frame.setVisible(true);
                currentClient.getLocalMembers();
                break;
            case ADD_USER:
                break;
            case REMOVE_USER:
                break;
            case USER_EXISTS:
                askAndValidateUsername();
                break;
            case LOCAL_MEMBERS:
                updateMembersList((String[]) msg.data);
                break;
            case ALL_MEMBERS:
                updateMembersList((String[]) msg.data);
                break;
            case CHAT_MESSAGE:
                listModel.addElement("From " + msg.from + " : " + msg.data);
                msgs.setModel(listModel);
                msgs.revalidate();
                msgs.repaint();
                break;
            case ERROR_OCCURED:
                listModel.addElement("Message wasn't sent due to an error with the server.");
                msgs.setModel(listModel);
                msgs.revalidate();
                msgs.repaint();
                break;
            case USER_NOT_FOUND:
                listModel.addElement("Username not found, please enter a valid name or refresh the member list.");
                msgs.setModel(listModel);
                msgs.revalidate();
                msgs.repaint();
                break;
            default:
                break;

        }
    }

    private void updateMembersList(String[] data) {
        DefaultListModel model = new DefaultListModel();
        for (String x : data)
            model.addElement(x);
        membersList.setModel(model);
        msgs.revalidate();
        msgs.repaint();
    }
 // GUI initializer generated by IntelliJ IDEA GUI Designer
 // >>> IMPORTANT!! <<<
 // DO NOT EDIT OR ADD ANY CODE HERE!
    {
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
         MainPanel = new JPanel();
         MainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
         logoutBtn = new JButton();
         logoutBtn.setText("logout");
         MainPanel.add(logoutBtn, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         textArea1 = new JTextArea();
         MainPanel.add(textArea1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
         welcomeMsg = new JLabel();
         welcomeMsg.setText("Label");
         MainPanel.add(welcomeMsg, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         sendBtn = new JButton();
         sendBtn.setText("Send");
         MainPanel.add(sendBtn, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         viewAllMembersBtn = new JButton();
         viewAllMembersBtn.setText("View All Members");
         MainPanel.add(viewAllMembersBtn, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         viewLocalMemberBtn = new JButton();
         viewLocalMemberBtn.setText("View Local Members");
         MainPanel.add(viewLocalMemberBtn, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
         membersListPanel = new JPanel();
         membersListPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
         MainPanel.add(membersListPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
         final JScrollPane scrollPane1 = new JScrollPane();
         membersListPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
         membersList = new JList();
         final DefaultListModel defaultListModel1 = new DefaultListModel();
         defaultListModel1.addElement("NO USERS CONNECTED.");
         membersList.setModel(defaultListModel1);
         scrollPane1.setViewportView(membersList);
         final JScrollPane scrollPane2 = new JScrollPane();
         MainPanel.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
         msgs = new JList();
         msgs.setLayoutOrientation(0);
         scrollPane2.setViewportView(msgs);
     }

     /**
      * @noinspection ALL
      */
     public JComponent $$$getRootComponent$$$() {
         return MainPanel;
     }
}

