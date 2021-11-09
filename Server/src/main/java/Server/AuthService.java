package Server;

public interface AuthService {
    String getNickNameByLoginAndPassword (String login, String password);

    boolean registration(String login, String password, String nickname);
}


