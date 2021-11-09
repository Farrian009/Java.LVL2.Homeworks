package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket client;
    private DataInputStream inServer;
    private DataOutputStream outServer;
    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket client) {
        try {
            this.server = server;
            this.client = client;
            inServer = new DataInputStream(client.getInputStream());
            outServer = new DataOutputStream(client.getOutputStream());

            new Thread(()->{
                try {
                    client.setSoTimeout(120000);
                    while (true) {
                        String message = inServer.readUTF();

                        if (message.equals("/end")) {
                            outServer.writeUTF("/end");
                            throw new RuntimeException("Client decided to interrupted connection");
                        }
                        if (message.startsWith("/auth")) {
                            String[] token = message.split("\\s+", 3);
                            if (token.length < 3)
                                continue;
                            String newNick = server
                                    .getAuthService()
                                    .getNickNameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                login = token[1];
                                if (!server.isLoginAuth(login)) {
                                    nickname = newNick;
                                    sendMsg("/auth_ok " + nickname);
                                    server.subscribe(this, nickname);
                                    System.out.println("Client authificated: " + nickname + " Adress: " + client.getRemoteSocketAddress());
                                    client.setSoTimeout(0);
                                    break;
                                } else
                                    sendMsg("Login used already");
                            } else
                                sendMsg("Login / password is wrong");
                        }
                        if (message.startsWith("/reg")) {
                            String[] token = message.split("\\s+", 4);
                            if (token.length < 4)
                                continue;
                            boolean b = server.getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (b) {
                                sendMsg("/reg_ok");
                            } else {
                                sendMsg("/reg_no");
                            }
                        }
                    }

                    while (true) {
                        String message = inServer.readUTF();
                        if (message.startsWith("/")) {
                            if (message.equals("/end")) {
                                outServer.writeUTF("/end");
                                break;
                            } else if (message.startsWith("/w")) {
                                String[] msgPrivate = message.split("\\s+", 3);
                                server.privateMessage(this, msgPrivate[1], msgPrivate[2]);
                            }
                        } else
                            server.broadcastMessage(this, message);
                    }
                } catch (SocketTimeoutException e) {
                    try {
                        outServer.writeUTF("/end");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnected " + client.getRemoteSocketAddress());
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg (String msg) {
        try {
            outServer.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
