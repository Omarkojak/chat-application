package Servers;

import Misc.Message;
import Misc.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by mohamedelzarei on 11/22/16. mohamedelzarei@gmail.com
 */
public class ServerThread extends Thread {
	private ArrayList<ServerThread> currentServers;
	private ObjectOutputStream sendToConnectedServer;
	private ObjectInputStream readFromConnectedServer;
	private static HashSet<String> currentUsers;
	private Socket connectedServerSocket;
	private MainServer server;

	public ServerThread(MainServer server, Socket connectedServerSocket, ArrayList<ServerThread> currentServers,
			HashSet<String> currentUsers) {
		this.connectedServerSocket = connectedServerSocket;
		this.currentServers = currentServers;
		this.server = server;
		this.currentUsers = currentUsers;

	}

	private boolean addNewClient(String username) {
		for (String x : currentUsers) {
			if (x.equals(username))
				return false;
		}
		currentUsers.add(username);
		return true;
	}

	@Override
	public void run() {
		try {
			// Try to get in/out streams for connected servers.
			sendToConnectedServer = new ObjectOutputStream(connectedServerSocket.getOutputStream());
			readFromConnectedServer = new ObjectInputStream(connectedServerSocket.getInputStream());

			Object data;
			while (((data = readFromConnectedServer.readObject()) != null)) {
				Message msg = (Message) data;
				if (msg == null || !msg.isAlive()) {
                    sendToConnectedServer.writeObject(new Message(MessageType.ERROR_OCCURED, null));
				}
				switch (msg.type) {
				case ADD_USER:
					String username = (String) msg.data;
					Message loginMessage;
					if (addNewClient(username)) {
						loginMessage = new Message(MessageType.USER_APPROVED, username);
					} else {
						loginMessage = new Message(MessageType.USER_EXISTS, username);
					}
					loginMessage.loginClient = msg.loginClient;
					sendToConnectedServer.writeObject(loginMessage);
					break;
				case REMOVE_USER:
					username = (String) msg.data;
					removeClient(username);
					break;
				case ALL_MEMBERS:
					String[] allMembers = getAllMembers();
					Message allMembersMsg = new Message(MessageType.ALL_MEMBERS, allMembers);
					allMembersMsg.from = msg.from;
					sendToConnectedServer.writeObject(allMembersMsg);
					break;
				case CHAT_MESSAGE:
					if (checkThatUserExists(msg)) {
						sendMessageToUsername(msg);
					} else {
						msg.type = MessageType.USER_NOT_FOUND;
						sendToConnectedServer.writeObject(msg);
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

	private boolean checkThatUserExists(Message msg) {
		for (String x : currentUsers) {
			if (msg.to.equals(x))
				return true;
		}
		return false;
	}

	private void sendMessageToUsername(Message msg) {
		synchronized (this) {
			for (ServerThread s : currentServers) {
				try {
					Message msg2 = new Message(msg.type, msg.from, msg.to, msg.data);
					msg2.TTL = msg.TTL;
					msg2.decreaseTTL();

					s.sendToConnectedServer.writeObject(msg2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String[] getAllMembers() {
		String[] allMembers = new String[currentUsers.size()];
		int i = 0;
		for (String x : currentUsers) {
			allMembers[i++] = x;
		}
		return allMembers;
	}

	private void removeClient(String username) {
		currentUsers.remove(username);
	}

}
