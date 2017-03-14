package Client;

import Misc.Message;
import Misc.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by mohamedelzarei on 11/22/16.
 * mohamedelzarei@gmail.com
 */
public class Client implements Runnable {
    public static Socket socket;
    private static ObjectOutputStream sendToServer;
    private static ObjectInputStream readInputFromServer;
    private String serverIP;
    private int port;
    public ClientListener listener;

    public Client(String serverIP, int port) {
        this.serverIP = serverIP;
        this.port = port;
    }

    public void connectToServer() {
        try {
            socket = new Socket(serverIP, port);
            sendToServer = new ObjectOutputStream(socket.getOutputStream());
            readInputFromServer = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (socket != null && readInputFromServer != null && sendToServer != null) {
            new Thread(this).start();
        }
    }

    void requestToAddUserToServer(String username) {
        Message msg = new Message(MessageType.ADD_USER, username);
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    Response From Server
    @Override
    public void run() {
        Object data;
        try {
            while (((data = readInputFromServer.readObject()) != null)) {
                Message msg = (Message) data;
                switch (msg.type) {
                    case USER_APPROVED:
                        String username = (String) (msg.data);
                        listener.sendMessage(new Message(MessageType.USER_APPROVED, username));
                        sendToServer.writeObject(new Message(MessageType.USER_APPROVED, username));
                        break;
                    case USER_EXISTS:
                        listener.sendMessage(new Message(MessageType.USER_EXISTS, msg.data));
                        break;
                    case LOCAL_MEMBERS:
                        listener.sendMessage(new Message(MessageType.LOCAL_MEMBERS, msg.data));
                        break;
                    case ALL_MEMBERS:
                        listener.sendMessage(new Message(MessageType.ALL_MEMBERS, msg.data));
                        break;
                    case CHAT_MESSAGE:
                        listener.sendMessage(msg);
                        break;
                    case ERROR_OCCURED:
                        listener.sendMessage(msg);
                        break;
                    case USER_NOT_FOUND:
                        listener.sendMessage(msg);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeUserFromServer(String username) {
        Message msg = new Message(MessageType.REMOVE_USER, username);
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getLocalMembers() {
        Message msg = new Message(MessageType.LOCAL_MEMBERS, null);
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getAllMembers(String username) {
        Message msg = new Message(MessageType.ALL_MEMBERS, null);
        msg.from = username;
        try {
        	sendToServer.flush();
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message msg) {
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
