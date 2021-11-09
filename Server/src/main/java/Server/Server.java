package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static ServerSocket server;
    private static Socket client;


    private static final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;
    static Map<String, SocketAddress> clientsNickName = new HashMap<String, java.net.SocketAddress>();


    public Server () {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");
            while (true) {
                client = server.accept();
                System.out.println(client.getLocalSocketAddress());
                System.out.println("Client connected: " + client.getRemoteSocketAddress());
                new ClientHandler(this, client);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void broadcastMessage (ClientHandler sender, String message) {
        String messageFrom = String.format("%s: %s", sender.getNickname(), message);
        for (ClientHandler c : clients) {
            c.sendMsg(messageFrom);
        }
    }

    public void privateMessage (ClientHandler sender, String receiver, String message) {
        String messagePrivate = String.format("%s for %s: %s", sender.getNickname(), receiver, message);
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(receiver)) {
                c.sendMsg(messagePrivate);
                if (sender.equals(c))
                    return;
                sender.sendMsg(messagePrivate);
                return;
            }

        }
        sender.sendMsg("User " + receiver + " not found.");



    }

    public void subscribe(ClientHandler clientHandler, String nickname) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe (ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuth(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login))
                return  true;
        }
        return false;
    }

    public void broadcastClientList (){
        StringBuilder sb = new StringBuilder("/clientList");
        for (ClientHandler c : clients) {
            sb.append(' ').append(c.getNickname());
        }
        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

}
