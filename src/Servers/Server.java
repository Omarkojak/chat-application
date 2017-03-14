package Servers;

import Client.ClientThread;
import Misc.Message;
import Misc.MessageType;
import Misc.UserMessages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by mohamedelzarei on 11/22/16.
 * mohamedelzarei@gmail.com
 */
public class Server implements Runnable {
    private static Socket socket;
    private static ObjectOutputStream sendToServer;
    private static ObjectInputStream readInputFromServer;
    private static Scanner readInputFromUser;
    private static String mainServerIP;
    private static int localPortForClients;

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static ArrayList<ClientThread> currentClients;
    public HashMap<Integer, Socket> clientsIDS;
    public HashMap<Socket, Integer> IDsSockets;
    private int ctr;

    public Server() {

        try {
            System.out.println(UserMessages.SERVER + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        readInputFromUser = new Scanner(System.in);
        System.out.println("Please Enter the Main Server IP : ");
        mainServerIP = readInputFromUser.nextLine();
        System.out.println("Please Enter your port number : ");
        localPortForClients = readInputFromUser.nextInt();


//        Connect to the main server.
        try {
            socket = new Socket(mainServerIP, 6001);
            clientsIDS = new HashMap<>();
            IDsSockets = new HashMap<>();
            readInputFromServer = new ObjectInputStream(socket.getInputStream());
            sendToServer = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (socket != null && readInputFromServer != null && sendToServer != null) {
            new Thread(this).start();
        }

        try {
            serverSocket = new ServerSocket(localPortForClients);
            currentClients = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                currentClients.add(new ClientThread(this, clientSocket, currentClients));
                currentClients.get(currentClients.size() - 1).start();
                clientsIDS.put(ctr++, clientSocket);
                IDsSockets.put(clientSocket, ctr - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("New user connected to the server.");
        }

    }

    public static void main(String[] args) throws UnknownHostException {
        new Server();
    }


    //    For Responses from the Main Server.
    @Override
    public void run() {
        Object data;
        try {
            while ((data = readInputFromServer.readObject()) != null) {
                Message msg = (Message) data;
                switch (msg.type) {
                    case USER_APPROVED:
                        Message loginMessage = new Message(MessageType.USER_APPROVED, msg.data);
                        sendMessageToClientThroughServer(loginMessage, clientsIDS.get(msg.loginClient));
                        break;
                    case USER_EXISTS:
                        loginMessage = new Message(MessageType.USER_EXISTS, msg.data);
                        sendMessageToClientThroughServer(loginMessage, clientsIDS.get(msg.loginClient));
                        break;
                    case ALL_MEMBERS:
                        sendMessageToClientThroughServer(msg, getUsernameSocket(msg.from));
                        break;
                    case ERROR_OCCURED:
                        sendMessageToClientThroughServer(msg, getUsernameSocket(msg.from));
                        break;
                    case USER_NOT_FOUND:
                        sendMessageToClientThroughServer(msg, getUsernameSocket(msg.from));
                        break;
                    case CHAT_MESSAGE:
                        if (msg.isAlive()) {
                            if (getUsernameSocket(msg.to) != null) {
                                sendMessageToClientThroughServer(msg, getUsernameSocket(msg.to));
                                // FOUND IT;
                                break;
                            }
                        } else {
                            msg.type = MessageType.ERROR_OCCURED;
                            if (getUsernameSocket(msg.from) != null)
                                sendMessageToClientThroughServer(msg, getUsernameSocket(msg.from));
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToClientThroughServer(Message msg, Socket socket) {
        for (ClientThread x : currentClients) {
            if (x.clientSocket == socket) {
                try {
                    x.sendToClient.writeObject(msg);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private Socket getUsernameSocket(String username) {
        for (ClientThread x : currentClients) {
            if (x.clientName.equals(username)) {
                return x.clientSocket;
            }
        }

        return null;
    }

    public void checkUsernameExists(String username, int requestingClient) {
        Message msg = new Message(MessageType.ADD_USER, username);
        msg.loginClient = requestingClient;
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeUsernameFromServer(String username) {
        synchronized (this) {
            for (int i = 0; i < currentClients.size(); i++) {
                if (currentClients.get(i).clientName.equals(username)) {
                    currentClients.remove(i);
                    Message msg = new Message(MessageType.REMOVE_USER, username);
                    try {
                        sendToServer.writeObject(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public void getAllMembers(String username) {
        Message msg = new Message(MessageType.ALL_MEMBERS, null);
        msg.from = username;
        try {
            sendToServer.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToRightClient(Message msg) {
        msg.decreaseTTL();
        if (msg.isAlive()) {
            try {
                sendToServer.writeObject(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            msg.type = MessageType.ERROR_OCCURED;
//            msg.TTL = 4; // RESET TTL TO SEND ERROR MSG
            if (getUsernameSocket(msg.from) != null)
                sendMessageToClientThroughServer(msg, getUsernameSocket(msg.from));
        }
    }
}
