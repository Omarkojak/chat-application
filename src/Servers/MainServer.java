package Servers;

import Misc.UserMessages;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by mohamedelzarei on 11/22/16.
 * mohamedelzarei@gmail.com
 */
public class MainServer {

    public static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static ArrayList<ServerThread> currentServers;
    private static HashSet<String> currentUsers;

    public MainServer() {
        try {
            serverSocket = new ServerSocket(6001);
            System.out.println(UserMessages.SERVER + InetAddress.getLocalHost().getHostAddress());
            currentServers = new ArrayList<>();
            currentUsers = new HashSet<>();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                currentServers.add(new ServerThread(this, clientSocket, currentServers,currentUsers));
                currentServers.get(currentServers.size() - 1).start();
                System.out.println("New server connected.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new MainServer();
    }
}
