package Client;

import Misc.Message;
import Misc.MessageType;
import Servers.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;



/**
 * Created by mohamedelzarei on 11/22/16.
 * mohamedelzarei@gmail.com
 */
public class ClientThread extends Thread {
    public String clientName;
    private ArrayList<ClientThread> currentClients;
    public ObjectOutputStream sendToClient;
    public ObjectInputStream readFromClient;
    private Server server;
    public Socket clientSocket;

    public ClientThread(Server server, Socket clientSocket, ArrayList<ClientThread> currentClients) {
        this.clientSocket = clientSocket;
        this.currentClients = currentClients;
        this.server = server;
    }

    public String[] getMemberList() {
        String localMembers[] = new String[currentClients.size()];
        for (int i = 0; i < currentClients.size(); i++) {
            if (currentClients.get(i) != null && currentClients.get(i).clientName != null)
                localMembers[i] = currentClients.get(i).clientName;
        }
        return localMembers;
    }

    public boolean checkIfClientInSameServer(Message msg) {
        if (msg == null) return false;
        String usrname = msg.to;
        for (String x : getMemberList()) {
            if (x.equals(usrname))
                return true;
        }
        return false;
    }

    //    Data from clients;
    @Override
    public void run() {
        try {
            sendToClient = new ObjectOutputStream(clientSocket.getOutputStream());
            readFromClient = new ObjectInputStream(clientSocket.getInputStream());
            Object data;
            while (((data = readFromClient.readObject()) != null)) {
                Message msg = (Message) data;
            	if (msg == null || !msg.isAlive()) {
            		continue;
				}
                switch (msg.type) {
                    case ADD_USER:
                        String username = (String) msg.data;
                        server.checkUsernameExists(username, server.IDsSockets.get(clientSocket));
                        break;
                    case REMOVE_USER:
                        username = (String) msg.data;
                        server.removeUsernameFromServer(username);
                        readFromClient.close();
                        sendToClient.close();
                        break;
                    case USER_APPROVED:
                        username = (String) msg.data;
                        this.clientName = username;
                        break;
                    case LOCAL_MEMBERS:
                        sendToClient.writeObject(new Message(MessageType.LOCAL_MEMBERS, getMemberList()));
                        break;
                    case ALL_MEMBERS:
                        server.getAllMembers(msg.from);
                        break;
                    case CHAT_MESSAGE:
                        if (msg.isAlive()) {
                            if (!checkIfClientInSameServer(msg)) {
                                server.sendMessageToRightClient(msg);
                            } else {
                                sendMessageToLocalClient(msg);
                            }
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

    private void sendMessageToLocalClient(Message msg) {
        String usrname = msg.to;
        synchronized (this){
            for (ClientThread x : currentClients) {
                if (x.clientName.equals(usrname)) {
                    try {
                        x.sendToClient.writeObject(msg);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
